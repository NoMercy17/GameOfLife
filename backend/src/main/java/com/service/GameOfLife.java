package com.service;

import com.gui.GuiPrintStream;
import com.model.AsexualCell;
import com.model.Cell;
import com.model.SexualCell;
import com.service.ResourcePool;
import com.gui.VisualSimulationGUI;

import java.io.PrintStream;


public class GameOfLife {
    public static void main(String[] args) {
        System.out.println("=== Game of Life ===\n");

        ResourcePool resourcePool = new ResourcePool(15);
        CellManager manager = new CellManager(resourcePool);

        // ----- NEW: visual GUI -----
        VisualSimulationGUI gui = new VisualSimulationGUI(manager, resourcePool);
        gui.setVisible(true);

        // Duplicate console → GUI log
        PrintStream guiOut = new GuiPrintStream(System.out, gui.getLogArea());
        System.setOut(guiOut);

        // ----- create initial cells -----
        Cell cell1 = new AsexualCell(1, manager, resourcePool);
        Cell cell2 = new AsexualCell(2, manager, resourcePool);
        Cell cell3 = new SexualCell(3, manager, resourcePool);
        Cell cell4 = new SexualCell(4, manager, resourcePool);

        manager.addCell(cell1);
        manager.addCell(cell2);
        manager.addCell(cell3);
        manager.addCell(cell4);

        System.out.println("\n>>> Cells starting...\n");

        // Simulation
        try {
            Thread.sleep(8000);
            manager.printStats();

            Thread.sleep(8000);
            manager.printStats();

            Thread.sleep(8000);
            manager.printStats();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\n>>> Simulation ending...");
        manager.stopAll();
        manager.printStats();

        System.out.println("\n>>> Simulation COMPLETED!");
    }
}