package com.service;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


public class ResourcePool {
    private int availableFood;
    private final Semaphore foodSemaphore;
    private final Random random;
    
    public ResourcePool(int initialFood) {
        this.availableFood = initialFood;
        this.foodSemaphore = new Semaphore(initialFood, true); 
        this.random = new Random();
    }
    
    // Synchronize the read of availableFood before printing
    public boolean tryToEat(int cellId, long timeoutMs) throws InterruptedException {
        int currentFood;
        synchronized (this) {
            currentFood = availableFood;
        }
        System.out.println("[Cell " + cellId + "] Tries to acquire food... (available: " + currentFood + ")");
        
        // Tries to acquire a permit within the timeout
        boolean acquired = foodSemaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
        
        if (acquired) {
            synchronized (this) {
                availableFood--;
                System.out.println("[Cell " + cellId + "] GOT FOOD! (remaining: " + availableFood + ")");
            }
            return true;
        } else {
            System.out.println("[Cell " + cellId + "] NO FOOD AVAILABLE!");
            return false;
        }
    }
    
    // When a cell dies, it adds food back to the pool
    public void addFoodFromDeadCell(int cellId) {
        int foodProduced = random.nextInt(5) + 1; // 1-5 unități
        
        synchronized (this) {
            availableFood += foodProduced;
        }
        
        // Restore the permits in the semaphore
        foodSemaphore.release(foodProduced);
        
        System.out.println("[ResourcePool] Cell " + cellId + " died and produced " + 
                         foodProduced + " food units (total: " + availableFood + ")");
    }
    
    public synchronized int getAvailableFood() {
        return availableFood;
    }
    
    
    public void addFood(int amount) {
        synchronized (this) {
            availableFood += amount;
        }
        
        // Release permits in the semaphore
        foodSemaphore.release(amount);
        
        System.out.println("[ResourcePool] Added " + amount + " food units (total: " + availableFood + ")");
    }
}