package com.service;

import com.service.ResourcePool;
import com.model.AsexualCell;
import com.model.Cell;
import com.model.SexualCell;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CellManager {
    private final List<Cell> cells;
    private final List<Thread> threads;
    private final AtomicInteger nextCellId;
    private final ResourcePool resourcePool;
    
    public CellManager(ResourcePool resourcePool) {
        this.cells = new ArrayList<>();
        this.threads = new ArrayList<>();
        this.nextCellId = new AtomicInteger(1);
        this.resourcePool = resourcePool;
    }
    
    public synchronized void addCell(Cell cell) {
        cells.add(cell);
        Thread thread = new Thread(cell, "Thread-Cell-" + cell.id);
        threads.add(thread);
        thread.start();
        System.out.println("[CellManager] Added cell " + cell.id);
    }
    
    public synchronized SexualCell findMatingPartner(SexualCell requester) {
        for (Cell cell : cells) {
            if (cell instanceof SexualCell && 
                cell != requester && 
                cell.isAlive() && 
                cell.isWantingToReproduce()) {
                return (SexualCell) cell;
            }
        }
        return null;
    }
    
    // Correct ID assignment for child cells
    public void reproduce(Cell parent1, Cell parent2) {
        if (parent1 instanceof AsexualCell) {
            // Get TWO unique IDs for asexual reproduction
            int id1 = nextCellId.getAndIncrement();
            int id2 = nextCellId.getAndIncrement();
            
            AsexualCell child1 = new AsexualCell(id1, this, resourcePool);
            AsexualCell child2 = new AsexualCell(id2, this, resourcePool);
            
            addCell(child1);
            addCell(child2);
            
            System.out.println("[CellManager] Asexual cell " + parent1.id +
                             " divided into cells " + child1.id + " and " + child2.id);
        } else if (parent1 instanceof SexualCell && parent2 instanceof SexualCell) {
            // Get ONE unique ID for sexual reproduction
            int newId = nextCellId.getAndIncrement();
            SexualCell child = new SexualCell(newId, this, resourcePool);
            addCell(child);
            
            System.out.println("[CellManager] Sexual cells " + parent1.id + 
                             " and " + parent2.id + " created cell " + child.id);
        }
    }
    
    public synchronized int getAliveCellsCount() {
        int count = 0;
        for (Cell cell : cells) {
            if (cell.isAlive()) count++;
        }
        return count;
    }
    
    public void stopAll() {
        System.out.println("\n[CellManager] Stopping all cells...");
        
        for (Cell cell : cells) {
            cell.stop();
        }
        
        for (Thread thread : threads) {
            thread.interrupt();
        }
        
        for (Thread thread : threads) {
            try {
                thread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("[CellManager] All cells stopped!");
    }
    
    public synchronized void printStats() {
        int alive = 0;
        int asexual = 0;
        int sexual = 0;
        
        for (Cell cell : cells) {
            if (cell.isAlive()) {
                alive++;
                if (cell instanceof AsexualCell) asexual++;
                else if (cell instanceof SexualCell) sexual++;
            }
        }
        
        System.out.println("\n=== STATISTICS ===");
        System.out.println("Total cells: " + cells.size());
        System.out.println("Alive: " + alive);
        System.out.println("Asexual: " + asexual);
        System.out.println("Sexual: " + sexual);
        System.out.println("Food available: " + resourcePool.getAvailableFood());
        System.out.println("==================\n");
    }

    public synchronized List<Cell> getCells() {
        return new ArrayList<>(cells);
    }
}