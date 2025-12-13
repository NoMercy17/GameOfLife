package com.gui;


import com.model.AsexualCell;
import com.model.Cell;
import com.service.CellManager;
import com.service.ResourcePool;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;



// New class: SimulationGUI
class SimulationGUI extends JFrame {
    private final CellManager manager;
    private final ResourcePool resourcePool;
    private final DefaultTableModel tableModel;
    private final JLabel foodLabel;
    private final JTextArea logArea;

    public SimulationGUI(CellManager manager, ResourcePool resourcePool) {
        this.manager = manager;
        this.resourcePool = resourcePool;

        setTitle("Game of Life Simulation");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Food label at the top
        foodLabel = new JLabel("Available Food: ?", SwingConstants.CENTER);
        foodLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(foodLabel, BorderLayout.NORTH);

        // Cell table
        String[] columns = {"ID", "Type", "Alive", "Hungry", "Meals Eaten", "Reproducing"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable cellTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(cellTable);

        // Log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);

        // Split pane for table and log
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, logScroll);
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);

        // Timer to update the display every 500ms
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateDisplay();
            }
        }, 0, 500);
    }

    private void updateDisplay() {
        SwingUtilities.invokeLater(() -> {
            // Update food label
            foodLabel.setText("Available Food: " + resourcePool.getAvailableFood());

            // Update cell table
            tableModel.setRowCount(0);
            List<Cell> currentCells = manager.getCells();
            for (Cell cell : currentCells) {
                Object[] row = {
                        cell.getId(),
                        cell instanceof AsexualCell ? "Asexual" : "Sexual",
                        cell.isAlive(),
                        cell.isHungry(),
                        cell.getMealsEaten(),
                        cell.isWantingToReproduce()
                };
                tableModel.addRow(row);
            }
        });
    }

    public JTextArea getLogArea() {
        return logArea;
    }
}