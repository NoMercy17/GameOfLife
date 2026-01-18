package com.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.model.AsexualCell;
import com.model.Cell;
import com.model.SexualCell;

public class CellManager {
    private final List<Cell> cells;
    private final List<Thread> threads;
    private final AtomicInteger nextCellId;
    private final ResourcePool resourcePool;
    
    // STATS TRACKERS
    private final AtomicInteger totalReproductions = new AtomicInteger(0); // Sexual
    private final AtomicInteger totalDivisions = new AtomicInteger(0);     // Asexual (NEW)
    
    private final AtomicReference<Double> timeScale = new AtomicReference<>(1.0);
    private boolean isPaused = false;
    private final Object pauseLock = new Object();

    public CellManager(ResourcePool resourcePool) {
        this.cells = new CopyOnWriteArrayList<>();
        this.threads = new CopyOnWriteArrayList<>();
        this.nextCellId = new AtomicInteger(1);
        this.resourcePool = resourcePool;
    }

    public void setSpeed(String speed) {
        if (speed.equals("fast")) timeScale.set(0.2);
        else if (speed.equals("slow")) timeScale.set(2.0);
        else timeScale.set(1.0);
    }

    public void sleepFor(long milliseconds) throws InterruptedException {
        long scaledDuration = (long) (milliseconds * timeScale.get());
        if (scaledDuration < 10) scaledDuration = 10;
        Thread.sleep(scaledDuration);
    }

    public void setPaused(boolean paused) {
        synchronized (pauseLock) {
            this.isPaused = paused;
            if (!paused) pauseLock.notifyAll();
        }
    }

    public void checkPause() {
        synchronized (pauseLock) {
            while (isPaused) {
                try { pauseLock.wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
    }

    public void addCell(Cell cell) {
        cells.add(cell);
        Thread thread = new Thread(cell, "Cell-" + cell.id);
        threads.add(thread);
        thread.start();
    }

    public SexualCell findMatingPartner(SexualCell requester) {
        for (Cell cell : cells) {
            if (cell instanceof SexualCell && cell != requester && cell.isAlive() && cell.isWantingToReproduce()) {
                return (SexualCell) cell;
            }
        }
        return null;
    }

    // --- FIX: SEPARATE COUNTERS ---
    public void reproduce(Cell parent1, Cell parent2) {
        if (parent1 instanceof AsexualCell) {
            // Asexual Division
            totalDivisions.incrementAndGet(); // TRACK DIVISION
            
            addCell(new AsexualCell(nextCellId.getAndIncrement(), this, resourcePool));
            addCell(new AsexualCell(nextCellId.getAndIncrement(), this, resourcePool));
        } else if (parent1 instanceof SexualCell && parent2 instanceof SexualCell) {
            // Sexual Reproduction
            totalReproductions.incrementAndGet(); // TRACK REPRODUCTION
            
            addCell(new SexualCell(nextCellId.getAndIncrement(), this, resourcePool));
        }
    }

    public void killAll() {
        for (Cell cell : cells) cell.stop();
        try { Thread.sleep(50); } catch (Exception ignored) {} 
        cells.clear(); 
        threads.clear();
        resourcePool.clear(); 
        nextCellId.set(1);
        // Reset Stats
        totalReproductions.set(0);
        totalDivisions.set(0);
    }

    public int getAliveCellsCount() { return (int) cells.stream().filter(Cell::isAlive).count(); }
    
    // Getters for Controller
    public int getTotalReproductions() { return totalReproductions.get(); }
    public int getTotalDivisions() { return totalDivisions.get(); } // NEW GETTER
    
    public List<Cell> getCells() { return new ArrayList<>(cells); }
    public void stopAll() { killAll(); for (Thread t : threads) t.interrupt(); }
}