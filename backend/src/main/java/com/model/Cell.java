package com.model;

import com.service.CellManager;
import com.service.ResourcePool;

public abstract class Cell implements Runnable {
    public int id;
    protected int mealsEaten;
    protected boolean isAlive;
    protected boolean isHungry;
    protected boolean wantsToReproduce;
    protected CellManager manager;
    protected ResourcePool resourcePool;
    
    protected static final int BASE_FULL_TIME = 5000;    
    
    protected static final int BASE_STARVE_TIME = 4000;  // slightly longer starve time too
    protected static final int MEALS_TO_REPRODUCE = 2;   
    
    public Cell(int id, CellManager manager, ResourcePool resourcePool) {
        this.id = id;
        this.manager = manager;
        this.resourcePool = resourcePool;
        this.mealsEaten = 0;
        this.isAlive = true;
        this.isHungry = true; 
        this.wantsToReproduce = false;
    }

    @Override
    public void run() {
        while (isAlive && !Thread.currentThread().isInterrupted()) {
            try {
                manager.checkPause();

                if (isHungry) {
                    boolean ate = tryToEat();
                    if (!ate) {
                        die("starvation"); 
                        break;
                    }
                } else {
                    waitUntilHungry();
                }
                
                manager.checkPause();

                if (isAlive && mealsEaten >= MEALS_TO_REPRODUCE && !wantsToReproduce) {
                    wantsToReproduce = true;
                    tryToReproduce();
                    if (!isAlive) break;
                }
                
                manager.sleepFor(100);

            } catch (InterruptedException e) {
                if (isAlive) die("interrupted");
                break;
            }
        }
    }
    
    protected boolean tryToEat() throws InterruptedException {
        boolean success = resourcePool.tryToEat(id, BASE_STARVE_TIME); 
        if (success) {
            mealsEaten++;
            isHungry = false;
            return true;
        }
        return false; 
    }

    protected void waitUntilHungry() throws InterruptedException {
        // Sleep for scaled time (5 seconds base)
        manager.sleepFor(BASE_FULL_TIME);
        isHungry = true;
    }

    protected synchronized void die(String reason) {
        if (!isAlive) return;
        isAlive = false;
        resourcePool.addFoodFromDeadCell(id);
    }
    
    public synchronized void stop() { 
        if (isAlive) die("simulation end"); 
    }
    
    public synchronized void resetAfterReproduction() {
        this.mealsEaten = 0;
        this.wantsToReproduce = false;
        this.isHungry = true; 
    }
    
    protected abstract void tryToReproduce() throws InterruptedException;
    
    public synchronized boolean isWantingToReproduce() { return wantsToReproduce; }
    public synchronized boolean isAlive() { return isAlive; }
    public synchronized int getId() { return id; }
    public synchronized int getMealsEaten() { return mealsEaten; }
    public synchronized boolean isHungry() { return isHungry; }
}