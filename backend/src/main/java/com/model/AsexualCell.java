package com.model;

import com.service.CellManager;
import com.service.ResourcePool;

public class AsexualCell extends Cell {
    
    public AsexualCell(int id, CellManager manager, ResourcePool resourcePool) {
        super(id, manager, resourcePool);
    }
    
    @Override
    protected void tryToReproduce() throws InterruptedException {
        // Simulate "Mitosis" time, scaled by game speed
        manager.sleepFor(1000); 
        
        // Asexual reproduction: One cell splits into two NEW cells
        manager.reproduce(this, null);
        
        // Parent dies (splits apart)
        synchronized (this) {
            isAlive = false;
        }
    }
}