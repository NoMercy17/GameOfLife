package com.model;


import com.service.CellManager;
import com.service.ResourcePool;

public class SexualCell extends Cell {
    
    public SexualCell(int id, CellManager manager, ResourcePool resourcePool) {
        super(id, manager, resourcePool);
    }
    
    @Override
    protected void tryToReproduce() throws InterruptedException {
        System.out.println("[Sexual cell " + id + "] Looking for partner...");
        
        int attempts = 0;
        SexualCell partner = null;
        
        while (attempts < 5 && partner == null && isAlive) {
            Thread.sleep(1000);
            partner = manager.findMatingPartner(this);
            attempts++;
            
            if (partner == null) {
                System.out.println("[Sexual Cell " + id + "] No partner found (attempt " + attempts + "/5)");
            }
        }
        
        if (partner != null) {
            System.out.println("[Sexual Cell " + id + "] Found partner: Cell " + partner.id + "!");
            
            // Lock on the manager to synchronize reproduction
            synchronized (manager) {
                if (partner.isWantingToReproduce() && partner.isAlive()) {
                    manager.reproduce(this, partner);
                    
                    // FIX #5: Use synchronized method to reset both cells safely
                    this.resetAfterReproduction();
                    partner.resetAfterReproduction();
                }
            }
        } else {
            System.out.println("[Sexual Cell " + id + "] Failed to find partner");
            // Use synchronized method
            this.resetAfterReproduction();
        }
    }
}