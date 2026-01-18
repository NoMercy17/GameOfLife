package com.model;

import com.service.CellManager;
import com.service.ResourcePool;

public class SexualCell extends Cell {
    
    public SexualCell(int id, CellManager manager, ResourcePool resourcePool) {
        super(id, manager, resourcePool);
    }
    
    @Override
    protected void tryToReproduce() throws InterruptedException {
        int attempts = 0;
        SexualCell partner = null;
        
        // Try to find a partner 5 times
        while (attempts < 5 && partner == null && isAlive) {
            // Wait a bit before looking again (Scaled by Game Speed)
            manager.sleepFor(1000);
            
            partner = manager.findMatingPartner(this);
            attempts++;
        }
        
        if (partner != null) {
            // Lock to ensure atomic reproduction
            synchronized (manager) {
                // Double check partner is still valid
                if (partner.isWantingToReproduce() && partner.isAlive()) {
                    manager.reproduce(this, partner);
                    
                    // Reset both parents so they can eat and try again later
                    this.resetAfterReproduction();
                    partner.resetAfterReproduction();
                }
            }
        } else {
            // Failed to find partner, give up for now and eat
            this.resetAfterReproduction();
        }
    }
}