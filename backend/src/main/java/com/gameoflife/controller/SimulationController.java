package com.gameoflife.controller;

import com.ai.LMStudioService;
import com.model.AsexualCell;
import com.model.Cell;
import com.gameoflife.GameRecord;
import com.model.SexualCell;
import com.gameoflife.GameRecordRepository;
import com.service.CellManager;
import com.service.ResourcePool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/simulation")
@CrossOrigin(origins = "*")
public class SimulationController {
    private CellManager manager;
    private ResourcePool resourcePool;
    private boolean running = false;

    @Autowired
    private GameRecordRepository gameRecordRepository; // TODO: to be implemented
    @Autowired
    private LMStudioService lmStudioService; // TODO: to be implemented

    private GameRecord currentGame; // TODO: to be implemented
    private LocalDateTime startTime;

    // Statistics during game
    private int totalCellsCreated = 0;
    private int totalSexualCellsCreated = 0;
    private int totalAsexualCellsCreated = 0;
    private int nrDivisions = 0;
    private int nrReproductions = 0;


    @PostMapping("/start")
    public Map<String, Object> startSimulation(
            @RequestParam(required = false, defaultValue = "15") Integer initialFood) {
        if (running) {
            return Map.of("error", "Simulation already running");
        }
        
        resourcePool = new ResourcePool(initialFood);
        manager = new CellManager(resourcePool);
        
        // Create initial cells
        manager.addCell(new AsexualCell(1, manager, resourcePool));
        manager.addCell(new AsexualCell(2, manager, resourcePool));
        manager.addCell(new SexualCell(3, manager, resourcePool));
        manager.addCell(new SexualCell(4, manager, resourcePool));
        
        running = true;
        startTime = LocalDateTime.now();

        // initial statistics
        totalCellsCreated = 4;
        totalAsexualCellsCreated = 2;
        totalSexualCellsCreated = 2;
        nrDivisions = 0;
        nrReproductions = 0;

        // initialize game record
        currentGame = new GameRecord();


        return Map.of(
            "status", "started",
            "initialFood", initialFood,
            "cells", manager.getAliveCellsCount()
        );
    }
    
    @PostMapping("/stop")
    public Map<String, Object> stopSimulation() {
        if (!running) {
            return Map.of("error", "No simulation running");
        }

        manager.stopAll();

        // Cell count
        int aliveSexual = 0;
        int aliveAsexual = 0;
        for (Cell cell : manager.getCells()) {
            if(cell instanceof AsexualCell) aliveAsexual++;
            else aliveSexual++;
        }

        // Game record
        LocalDateTime endTime = LocalDateTime.now();
        long duration = java.time.Duration.between(startTime, endTime).getSeconds();

        currentGame.setDurationSeconds((int)duration);
        currentGame.setTotalCells(totalCellsCreated);
        currentGame.setTotalAsexualCells(totalAsexualCellsCreated);
        currentGame.setTotalSexualCells(totalSexualCellsCreated);
        currentGame.setAliveAsexualCells(aliveAsexual);
        currentGame.setAliveSexualCells(aliveSexual);
        currentGame.setNrDivisions(nrDivisions);
        currentGame.setNrReproductions(nrReproductions);
        currentGame.setSummary(String.format(
                "Game lasted %d seconds with %d total cells (%d survived)",
                duration, totalCellsCreated, aliveSexual + aliveAsexual
        ));

        gameRecordRepository.save(currentGame);

        running = false;

        return Map.of(
                "status", "stopped",
                "gameId", currentGame.getId(),
                "duration", duration,
                "statistics", Map.of(
                        "totalCells", totalCellsCreated,
                        "totalSexualCells", totalSexualCellsCreated,
                        "totalAsexualCells", totalAsexualCellsCreated,
                        "aliveSexualCells", aliveSexual,
                        "aliveAsexualCells", aliveAsexual,
                        "nrDivisions", nrDivisions,
                        "nrReproductions", nrReproductions
                )
        );
    }


    // TODO: Probably remove this
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        if (!running) {
            return Map.of("running", false);
        }

        // count current cells
        int currentAsexual = 0;
        int currentSexual = 0;

        List<Map<String, Object>> cellData = new ArrayList<>();
        for (Cell cell : manager.getCells()) {
            if (cell.isAlive()) {
                if (cell instanceof AsexualCell) {
                    currentAsexual++;
                } else if (cell instanceof SexualCell) {
                    currentSexual++;
                }
            }

            cellData.add(Map.of(
                    "id", cell.getId(),
                    "type", cell instanceof AsexualCell ? "asexual" : "sexual",
                    "alive", cell.isAlive(),
                    "hungry", cell.isHungry(),
                    "reproducing", cell.isWantingToReproduce(),
                    "mealsEaten", cell.getMealsEaten()
            ));
        }

        return Map.of(
                "running", true,
                "aliveCells", manager.getAliveCellsCount(),
                "totalCells", manager.getCells().size(),
                "availableFood", resourcePool.getAvailableFood(),
                "currentAsexual", currentAsexual,
                "currentSexual", currentSexual,
                "statistics", Map.of(
                        "totalCellsCreated", totalCellsCreated,
                        "totalSexualCellsCreated", totalSexualCellsCreated,
                        "totalAsexualCellsCreated", totalAsexualCellsCreated,
                        "nrDivisions", nrDivisions,
                        "nrReproductions", nrReproductions
                ),
                "cells", cellData
        );
    }
    
    @PostMapping("/cells/add")
    public Map<String, Object> addCell(@RequestParam String type) {
        if (!running) {
            return Map.of("error", "Start simulation first");
        }
        
        int newId = manager.getCells().size() + 1;
        Cell newCell = type.equals("asexual") 
            ? new AsexualCell(newId, manager, resourcePool)
            : new SexualCell(newId, manager, resourcePool);
            
        manager.addCell(newCell);

        totalCellsCreated++;
        if (type.equals("asexual")) {
            totalAsexualCellsCreated++;
        } else {
            totalSexualCellsCreated++;
        }
        
        return Map.of(
            "status", "added",
            "cellId", newId,
            "type", type
        );
    }

    @PostMapping("/food/add")
    public Map<String, Object> addFood(@RequestParam Integer amount) {
        if (!running) {
            return Map.of("error", "Start simulation first");
        }
        resourcePool.addFood(amount);

        return Map.of(
            "status", "food added",
            "amount", amount,
            "availableFood", resourcePool.getAvailableFood()
        );
    }

    @GetMapping("/ai/summary")
    public Map<String,Object> getAISummary(
            @RequestParam(required = false, defaultValue = "3") Integer lastNgames){

        // Get the last n games
        List<GameRecord> recentGames = gameRecordRepository.findlastNGames(lastNgames);

        if(recentGames.isEmpty()){
            return Map.of("error", "No game history available");
        }

        String summary = lmStudioService.generateSimulationSummary(recentGames);

        return Map.of(
                "ai_summary", summary,
                "gamesAnalyzed", recentGames.size(),
                "games", recentGames
        );
    }


    @GetMapping("/history")
    public Map<String, Object> getGameHistory(){
        List<GameRecord> allGames = gameRecordRepository.findAll();

        return Map.of(
                "games", allGames,
                "count", allGames.size()
        );
    }

    @GetMapping("/ai/test")
    public Map<String, Object> testLMStudio() {
        try {
            String testResult = lmStudioService.testConnection();
            return Map.of(
                    "status", "success",
                    "message", testResult
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
        }
    }
}