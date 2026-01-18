package com.gameoflife.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ai.LMStudioService;
import com.gameoflife.GameRecord;
import com.gameoflife.GameRecordRepository;
import com.model.AsexualCell;
import com.model.Cell;
import com.model.SexualCell;
import com.service.CellManager;
import com.service.ResourcePool;

@RestController
@RequestMapping("/api/simulation")
@CrossOrigin(origins = "*") 
public class SimulationController {

    private CellManager manager;
    private ResourcePool resourcePool;
    private boolean running = false;
    private boolean paused = false;
    private int movementTick = 0;
    private AtomicInteger idCounter = new AtomicInteger(1);

    @Autowired
    private GameRecordRepository gameRecordRepository;

    @Autowired
    private LMStudioService lmStudioService;

    private GameRecord currentGame;
    private LocalDateTime startTime;
    private int totalCellsCreated = 0;

    @PostMapping("/start")
    public Map<String, Object> startSimulation(@RequestParam(required = false, defaultValue = "20") Integer initialFood) {
        if (manager != null && paused) {
            paused = false;
            running = true;
            manager.setPaused(false);
            return Map.of("status", "resumed");
        }
        
        if (manager != null) manager.stopAll();
        
        resourcePool = new ResourcePool(initialFood);
        manager = new CellManager(resourcePool);
        idCounter.set(1);
        movementTick = 0;

        manager.addCell(new AsexualCell(idCounter.getAndIncrement(), manager, resourcePool));
        manager.addCell(new AsexualCell(idCounter.getAndIncrement(), manager, resourcePool));
        manager.addCell(new SexualCell(idCounter.getAndIncrement(), manager, resourcePool));
        manager.addCell(new SexualCell(idCounter.getAndIncrement(), manager, resourcePool));

        running = true;
        paused = false;
        startTime = LocalDateTime.now();
        currentGame = new GameRecord();
        totalCellsCreated = 4;

        return Map.of("status", "started");
    }

    @PostMapping("/togglePause")
    public Map<String, Object> togglePause() {
        if (!running && !paused) return Map.of("error", "Game not started");
        paused = !paused;
        if (manager != null) manager.setPaused(paused);
        if (paused) updateGameRecord();
        return Map.of("status", paused ? "paused" : "running", "isPaused", paused);
    }

    @PostMapping("/addCell")
    public Map<String, Object> addCell(@RequestParam(required = false, defaultValue = "asexual") String type) {
        if (manager == null) return Map.of("error", "Game not started");
        
        int newId = idCounter.getAndIncrement();
        Cell newCell = type.equalsIgnoreCase("sexual") 
                ? new SexualCell(newId, manager, resourcePool) 
                : new AsexualCell(newId, manager, resourcePool);
                
        manager.addCell(newCell);
        totalCellsCreated++;
        return Map.of("status", "added");
    }

    @PostMapping("/addFood")
    public Map<String, Object> addFood() {
        if (manager == null) return Map.of("error", "Start game first");
        int alive = manager.getAliveCellsCount();
        int amount = Math.max(5, alive + 5); 
        resourcePool.addFood(amount);
        return Map.of("status", "food added", "amount", amount);
    }
    
    @PostMapping("/speed")
    public Map<String, Object> changeSpeed(@RequestParam String action) {
        if (manager == null) return Map.of("error", "Game not started");
        manager.setSpeed(action);
        return Map.of("status", "speed set to " + action);
    }

    @PostMapping("/reset")
    public Map<String, Object> resetSimulation() {
        if (manager != null) manager.stopAll();
        running = false;
        paused = false;
        manager = null;
        resourcePool = null;
        return Map.of("status", "reset");
    }
    
    @PostMapping("/killAll")
    public Map<String, Object> killAll() {
        if (manager != null) {
            manager.killAll();
            updateGameRecord();
        }
        return Map.of("status", "terminated");
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        if (manager == null) {
            return Map.of("running", false, "activeCells", List.of(), "food", List.of(), "aliveCount", 0);
        }

        if (!paused) movementTick++;

        List<Map<String, Object>> cellData = new ArrayList<>();
        List<Cell> currentCells = new ArrayList<>(manager.getCells());

        for (Cell cell : currentCells) {
            if (cell.isAlive()) {
                double seed = cell.getId() * 11.0;
                double x = 45.0 + 42.0 * Math.sin((movementTick * 0.05) + seed);
                double y = 45.0 + 42.0 * Math.cos((movementTick * 0.04) + (seed * 1.5));

                cellData.add(Map.of(
                    "id", cell.getId(),
                    "type", cell instanceof AsexualCell ? "Asexual" : "Sexual",
                    "x", Math.max(2, Math.min(95, x)), 
                    "y", Math.max(2, Math.min(95, y)),
                    "alive", true,
                    "isHungry", cell.isHungry(),
                    "isReproducing", cell.isWantingToReproduce()
                ));
            }
        }

        List<Map<String, Object>> foodData = new ArrayList<>();
        int foodCount = resourcePool.getAvailableFood();
        int renderFood = Math.min(foodCount, 200); 
        for (int i = 0; i < renderFood; i++) {
            Random r = new Random(i * 5555L); 
            foodData.add(Map.of("x", r.nextInt(90) + 5, "y", r.nextInt(90) + 5));
        }

        return Map.of(
            "running", running,
            "paused", paused,
            "activeCells", cellData,
            "food", foodData,
            "availableFood", foodCount,
            "aliveCount", manager.getAliveCellsCount()
        );
    }
@GetMapping("/ai/summary")
    public Map<String, Object> getAISummary(@RequestParam(required = false, defaultValue = "3") Integer lastNgames) {
        
      
        if (manager != null) updateGameRecord();

        List<GameRecord> recentGames = gameRecordRepository.findlastNGames(lastNgames);
        
        String summary = "No data available. Run a simulation first.";
        int totalC = 0, survivors = 0, gens = 0, divs = 0;

        if (!recentGames.isEmpty()) {
            GameRecord last = recentGames.get(0);
            totalC = last.getTotalCells();
            survivors = last.getAliveAsexualCells() + last.getAliveSexualCells();
            gens = last.getNrReproductions() != null ? last.getNrReproductions() : 0;
            divs = last.getNrDivisions() != null ? last.getNrDivisions() : 0;
            
            try {
                summary = lmStudioService.generateSimulationSummary(recentGames);
            } catch (Exception e) {
                summary = "AI SERVICE ERROR: " + e.getMessage();
            }
        }

        return Map.of(
            "aiAnalysis", summary, 
            "totalCells", totalC, 
            "generations", gens, 
            "divisions", divs,   
            "aliveCount", survivors
        );
    }

    private void updateGameRecord() {
        if (currentGame == null || manager == null) return;
        
        int aliveSexual = 0;
        int aliveAsexual = 0;
        for (Cell cell : manager.getCells()) {
            if (cell.isAlive()) {
                if (cell instanceof AsexualCell) aliveAsexual++;
                else aliveSexual++;
            }
        }
        
        LocalDateTime endTime = LocalDateTime.now();
        long duration = java.time.Duration.between(startTime, endTime).getSeconds();
        currentGame.setDurationSeconds((int) duration);
        currentGame.setTotalCells(totalCellsCreated);
        currentGame.setAliveSexualCells(aliveSexual);
        currentGame.setAliveAsexualCells(aliveAsexual);
        
        // --- FIX: Record both types separately ---
        currentGame.setNrReproductions(manager.getTotalReproductions()); // Sexual
        currentGame.setNrDivisions(manager.getTotalDivisions());         // Asexual
        
        gameRecordRepository.save(currentGame);
    }
}