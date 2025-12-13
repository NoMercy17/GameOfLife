package com.model;


import com.service.CellManager;
import com.service.ResourcePool;

public class AsexualCell extends Cell {
    
    public AsexualCell(int id, CellManager manager, ResourcePool resourcePool) {
        super(id, manager, resourcePool);
    }
    
    @Override
    protected void tryToReproduce() throws InterruptedException {
        System.out.println("[Asexual " + id + "] Starting DIVISION!");
        Thread.sleep(1000);
        
        manager.reproduce(this, null);
        
        // After asexual division, the parent cell dies (becomes the children)
        synchronized (this) {
            isAlive = false;
        }
        System.out.println("[Cell " + id + "] DIED from division!");
        
    }
}