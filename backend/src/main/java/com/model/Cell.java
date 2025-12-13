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
    
    protected static final int T_FULL = 2000;
    protected static final int T_STARVE = 3000;
    protected static final int MEALS_TO_REPRODUCE = 5;
    
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
        System.out.println("[Cell " + id + "] Has been born!");
        
        while (isAlive && !Thread.currentThread().isInterrupted()) {
            try {
                if (isHungry) {
                    // Încearcă să mănânce cu timeout de T_STARVE
                    boolean ate = tryToEat();
                    
                    if (!ate) {
                        // A MURIT DE FOAME!
                        die("starvation");
                        break;
                    }
                } else {
                    // Așteaptă să îi fie foame
                    waitUntilHungry();
                }
                
                // Verifică dacă poate să se reproducă
                // Check isAlive again before reproduction attempt
                if (isAlive && mealsEaten >= MEALS_TO_REPRODUCE && !wantsToReproduce) {
                    wantsToReproduce = true;
                    tryToReproduce();
                    
                    // After reproduction, check if still alive (asexual cells die)
                    if (!isAlive) {
                        break; // Exit the loop if cell died during reproduction
                    }
                }
                
            } catch (InterruptedException e) {
                System.out.println("[Cell " + id + "] has been interrupted!");
                if (isAlive) {
                    die("interrupted");
                }
                break;
            }
        }
    }
    
    protected boolean tryToEat() throws InterruptedException {
        // Încearcă să obțină hrană în timpul T_STARVE
        boolean success = resourcePool.tryToEat(id, T_STARVE);
        
        if (success) {
            mealsEaten++;
            isHungry = false;
            System.out.println("[Cell " + id + "] ATE! Total meals: " + mealsEaten);
            return true;
        } else {
            return false;
        }
    }
    
    protected void waitUntilHungry() throws InterruptedException {
        System.out.println("[Cell " + id + "] Is full, waiting...");
        Thread.sleep(T_FULL);
        
        isHungry = true;
        System.out.println("[Cell " + id + "] Is HUNGRY!");
    }
    
    protected synchronized void die(String reason) {
        if (!isAlive) return; // Deja moartă
        
        isAlive = false;
        System.out.println("[Cell " + id + "] DIED from " + reason + "!");
        
        // Produce hrană când moare
        resourcePool.addFoodFromDeadCell(id);
    }
    
    public synchronized void stop() {
        if (isAlive) {
            die("simulation end");
        }
    }
    
    //  Synchronized method for resetting after reproduction
    public synchronized void resetAfterReproduction() {
        this.mealsEaten = 0;
        this.wantsToReproduce = false;
        this.isHungry = true;
    }
    
    protected abstract void tryToReproduce() throws InterruptedException;
    
    public synchronized boolean isWantingToReproduce() {
        return wantsToReproduce;
    }
    
    public synchronized boolean isAlive() {
        return isAlive;
    }

    public synchronized int getId() {
        return id;
    }

    public synchronized int getMealsEaten() {
        return mealsEaten;
    }

    public synchronized boolean isHungry() {
        return isHungry;
    }
}