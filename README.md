# Game of Life Simulation

A sophisticated cellular simulation project that models asexual and sexual reproduction strategies, complete with AI-powered analysis using local LLM integration.

## Project Overview

This project simulates a "Game of Life" where cells compete for resources using different reproduction strategies:
- **Asexual Cells**: Reproduce through cell division
- **Sexual Cells**: Reproduce through mating with partners

The simulation tracks various metrics and uses a local AI model (via LM Studio) to analyze patterns and survival strategies across multiple game runs.

## Architecture

```
game-of-life/
├── backend/              # Spring Boot REST API
├── frontend/             # React/Vue frontend
└── docker/              # Docker configuration
    └── docker-compose.yml
```

### Tech Stack

**Backend:**
- Java 21
- Spring Boot 3.2.0
- PostgreSQL 15
- Hibernate/JPA
- WebClient for AI integration

**Frontend:**
- React/Vue.js
- Axios for API calls
- Modern UI components

**Infrastructure:**
- Docker & Docker Compose
- LM Studio (local LLM)

## Start

### Prerequisites

- Docker and Docker Compose installed
- LM Studio installed and running
- Java 21 (for local development)
- Node.js 18+ (for frontend development)
- Git


2. **Set up environment variables**

Create a `.env` file in the `docker/` directory:

```env
DB_NAME=gameoflife
DB_USER=postgres
DB_PASSWORD=your_secure_password
LM_STUDIO_URL=http://localhost:1234
```


3. **Start LM Studio**

- Open LM Studio
- Load a model (recommended: qwen3-v1-8b or similar)
- Go to **Server Settings**
- Enable **"Serve on Local Network"**
- Set port to `1234`
- Start the server

4. **Run with Docker Compose**


cd docker
docker compose up --build


The services will be available at:
- Backend API: http://localhost:8080
- Frontend: http://localhost:5173
- PostgreSQL: localhost:5454

### For Linux

If you're on Linux, use the host network mode for easier connectivity:

Update `docker-compose.yml`:
```yaml
backend:
  network_mode: host
```

And update the environment variable:
```env
LMSTUDIO_API_URL=http://localhost:1234
```

## 📖 Usage

### Starting a Simulation

```bash
# Start simulation with default food (15 units)
POST http://localhost:8080/api/simulation/start

# Start with custom food amount
POST http://localhost:8080/api/simulation/start?initialFood=20
```

### Monitoring Status

```bash
GET http://localhost:8080/api/simulation/status
```

### Adding Resources

```bash
# Add more food
POST http://localhost:8080/api/simulation/food/add?amount=10

# Add new cells
POST http://localhost:8080/api/simulation/cells/add?type=sexual
POST http://localhost:8080/api/simulation/cells/add?type=asexual
```

### Stopping and Analyzing

```bash
# Stop simulation and save results
POST http://localhost:8080/api/simulation/stop

# Get AI analysis of last 3 games
GET http://localhost:8080/api/simulation/ai/summary?lastNgames=3

# View game history
GET http://localhost:8080/api/simulation/history
```

## Testing AI Connection

Test if the backend can reach LM Studio:


GET http://localhost:8080/api/simulation/ai/test

{
  "status": "success",
  "message": "Connection successful! ..."
}





