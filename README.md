# Game of Life — Concurrent Cell Simulation

A multi-threaded simulation where **every cell is its own Java thread**, competing for a finite,
shared food supply. Cells eat, reproduce and starve independently; a local LLM reads the resulting
game records and writes an analysis of which reproduction strategy survived and why.

The cellular automaton is the excuse. The project is really about **contention** — what happens when
N independent threads want the same scarce resource, and how you keep that fair, deadlock-free and
observable.

**Stack:** Java 21 · Spring Boot 3.2 · PostgreSQL 15 · React 19 (Vite) · Tailwind 4 · LM Studio

---

## The model

Two cell types compete in the same resource pool:

| | Reproduction | Cost |
|---|---|---|
| **AsexualCell** | Divides alone after 2 meals — produces **two** children | No partner needed, so it never blocks |
| **SexualCell** | Must find a living, willing partner within 5 attempts — produces **one** child | Spends time searching; may starve while looking |

Each cell loops: eat or starve → get hungry again → reproduce once fed → repeat. Food is finite.
A cell that dies drops 1–5 food back into the pool, so death feeds the survivors.

The interesting question the simulation answers: **under scarcity, does the cheap strategy or the
careful one win?** That's what the LLM is asked to explain from the recorded runs.

---

## Concurrency design

This is the part worth reading.

### Thread per cell
Every `Cell` implements `Runnable` and gets a named thread (`Cell-7`) started by `CellManager`.
Cells are never scheduled centrally — they act on their own clocks, which is what makes the
contention real rather than simulated.

### Food is a fair semaphore
```java
this.foodSemaphore = new Semaphore(initialFood, true);  // fair
this.availableFood = new AtomicInteger(initialFood);
```

**Fairness is deliberate.** With an unfair semaphore, a small set of lucky threads barges the queue
repeatedly and the rest starve — and in this domain starvation isn't a metaphor, the cell dies and
leaves the simulation. Fair mode costs throughput to guarantee FIFO handoff. Here correctness of the
*model* beats raw speed.

### Eating is a timed acquire, not a blocking one
```java
boolean acquired = foodSemaphore.tryAcquire(timeoutMs, MILLISECONDS);
```

A hungry cell waits a bounded time for a permit. If none arrives it dies of starvation. Two things
fall out of this: there is no indefinite blocking anywhere in the system (so no deadlock on the food
path), and death becomes a natural consequence of scarcity rather than something the engine has to
decide.

### Cell collections are copy-on-write
```java
this.cells   = new CopyOnWriteArrayList<>();
this.threads = new CopyOnWriteArrayList<>();
```

`findMatingPartner()` iterates the whole list on every mating attempt, while births and deaths
mutate it. Reads massively outnumber writes, so copy-on-write avoids both `ConcurrentModification`
and the lock contention a synchronised list would add to the hot path.

### Mating is guarded and double-checked
```java
synchronized (manager) {
    if (partner.isWantingToReproduce() && partner.isAlive()) {
        manager.reproduce(this, partner);
        ...
    }
}
```

Between finding a partner and mating with it, that partner may have starved or paired off with
someone else. The lock makes the check-and-reproduce atomic, and the condition is re-tested *inside*
it — a plain check before the lock would be a TOCTOU race.

### Pause is a guarded block, not a spin
```java
public void checkPause() {
    synchronized (pauseLock) {
        while (isPaused) pauseLock.wait();
    }
}
```

Cells park on a dedicated monitor rather than polling a flag. `while` rather than `if`, to survive
spurious wakeups. `setPaused(false)` calls `notifyAll()`.

### Everything counted is atomic
`AtomicInteger` for the food count, the cell-ID sequence and the division/reproduction tallies —
all written from many threads and read by the status endpoint. `AtomicReference<Double>` holds the
speed multiplier so it can be changed mid-run without a lock.

### Shutdown is cooperative
`stopAll()` flips each cell's alive flag, then interrupts every thread. Cells check
`Thread.currentThread().isInterrupted()` in their loop and treat `InterruptedException` as death,
so nothing is killed mid-mutation.

---

## Tradeoffs

- **Thread-per-cell instead of a pool.** Maps one-to-one onto the domain and makes contention
  genuine, but it caps the simulation at low thousands of cells — each cell costs a platform thread.
  A pool would scale further and destroy the model's clarity. Virtual threads (`Thread.ofVirtual()`)
  would give both.
- **Fair semaphore over throughput.** Explained above. The wrong call in a server, the right one here.
- **One coarse lock on `CellManager` for mating.** Simple and provably correct. It does serialise all
  reproduction, which becomes the bottleneck at high cell counts. The alternative — per-pair lock
  ordering by cell ID to avoid deadlock — is more code for a bottleneck that doesn't bite at this
  scale.
- **Local LLM rather than a hosted API.** LM Studio runs offline, costs nothing per run and lets the
  simulation be analysed hundreds of times while iterating. Output quality is lower than a frontier
  model's, which is acceptable when the input is a small structured summary rather than prose.

---

## Running it

### Prerequisites
- Docker and Docker Compose
- LM Studio with a model loaded and its server running
- Java 21 and Node 18+ for local development

### 1. Clone
```bash
git clone https://github.com/NoMercy17/Game-of-Life.git
cd Game-of-Life
```

### 2. Environment
Create `docker/.env`:
```env
DB_NAME=gameoflife
DB_USER=postgres
DB_PASSWORD=a_secure_password
LM_STUDIO_URL=http://host.docker.internal:1234
```

### 3. Start LM Studio
Load a model (default expected: `qwen/qwen3-8b`), open **Server Settings**, enable **Serve on Local
Network**, set the port to `1234` and start the server.

### 4. Run
```bash
cd docker
docker compose up --build
```

| Service | URL |
|---|---|
| Backend API | http://localhost:8080 |
| Frontend | http://localhost:5173 |
| PostgreSQL | localhost:5454 |

<details>
<summary>On Linux</summary>

`host.docker.internal` isn't available by default. Either set `network_mode: host` on the backend
service and use `LM_STUDIO_URL=http://localhost:1234`, or add
`extra_hosts: ["host.docker.internal:host-gateway"]` to keep the compose file portable.

</details>

---

## API

Base path `/api/simulation`.

| Method | Path | Description |
|---|---|---|
| `POST` | `/start?initialFood=20` | Start a run with a given food supply (default 20) |
| `POST` | `/addCell?type=asexual\|sexual` | Introduce a new cell mid-run |
| `POST` | `/addFood` | Release more permits into the pool |
| `POST` | `/speed?action=fast\|slow\|normal` | Scale the simulation clock |
| `POST` | `/togglePause` | Pause / resume all cell threads |
| `POST` | `/killAll` | Stop every cell, keep the record |
| `POST` | `/reset` | Clear the pool and cell registry |
| `GET` | `/status` | Live counts, food remaining, division and reproduction tallies |
| `GET` | `/ai/summary?lastNgames=3` | LLM analysis across the last N recorded runs |

Each finished run is persisted as a `GameRecord`: duration, total and alive counts per cell type,
divisions, reproductions, and the generated summary.

---

## Frontend

React 19 + Vite 7 + Tailwind 4, with `lucide-react` icons. Real-time dashboard showing live cell
counts, remaining food and the AI-generated narrative for completed runs.

`[Add a screenshot here — the sci-fi dashboard is the best-looking thing in this repo and nobody
can see it from the README:]`
```markdown
![Simulation dashboard](docs/simulation-dashboard.png)
```

---

## Known limitations

- **`spring.jpa.hibernate.ddl-auto=create-drop`** — the schema is rebuilt on every start, so game
  history does not survive a restart. Fine for development, wrong for the LLM analysis feature,
  which is more interesting the more runs it has. Switching to `validate` plus Flyway migrations is
  the fix.
- **No automated tests yet.** The concurrency logic is exactly the kind of code that deserves them —
  a deterministic starvation test and a mating-race test are the two that matter.
- Cell count is bounded by platform threads, as described above.
