// ---------------------------------------------------------------
//  VisualSimulationGUI – animated dots + log + stats
// ---------------------------------------------------------------
package com.gui;

import com.model.AsexualCell;
import com.model.Cell;
import com.service.CellManager;
import com.service.ResourcePool;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;




public class VisualSimulationGUI extends JFrame {
    private final CellManager manager;
    private final ResourcePool resourcePool;
    private final JTextArea logArea;
    private final SimulationPanel simulationPanel;
    private final JLabel statsLabel;

    public VisualSimulationGUI(CellManager manager, ResourcePool resourcePool) {
        this.manager = manager;
        this.resourcePool = resourcePool;

        setTitle("Game of Life – Visual Simulation");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top: stats
        statsLabel = new JLabel("Initializing...");
        statsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(statsLabel, BorderLayout.NORTH);

        // Center: animation
        simulationPanel = new SimulationPanel();
        add(simulationPanel, BorderLayout.CENTER);

        // Bottom: log
        logArea = new JTextArea();
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(900, 150));
        add(logScroll, BorderLayout.SOUTH);

        // Refresh timer (100 ms)
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateSimulation();
            }
        }, 0, 100);
    }

    private void updateSimulation() {
        SwingUtilities.invokeLater(() -> {
            simulationPanel.updateState(manager.getCells(), resourcePool.getAvailableFood());
            updateStats();
            repaint();
        });
    }

    private void updateStats() {
        int alive = manager.getAliveCellsCount();
        int total = manager.getCells().size();
        int food  = resourcePool.getAvailableFood();
        statsLabel.setText(
                String.format("Alive: %d | Total: %d | Food: %d", alive, total, food)
        );
    }

    public JTextArea getLogArea() {
        return logArea;
    }


    //  Inner panel that draws the moving dots
    class SimulationPanel extends JPanel {
        private final List<VisualCell> visualCells = new ArrayList<>();
        private final List<Point> foodPoints = new ArrayList<>();
        private final Random random = new Random();
        private final int CELL_SIZE = 16;
        private final int FOOD_SIZE = 6;

        public void updateState(List<Cell> cells, int availableFood) {
            // cells
            synchronized (visualCells) {
                visualCells.removeIf(vc -> !vc.cell.isAlive());

                for (Cell c : cells) {
                    if (c.isAlive()) {
                        visualCells.stream()
                                .filter(vc -> vc.cell == c)
                                .findFirst()
                                .ifPresentOrElse(
                                        VisualCell::update,
                                        () -> visualCells.add(new VisualCell(c))
                                );
                    }
                }
            }

            // food
            synchronized (foodPoints) {
                foodPoints.clear();
                int visible = Math.min(availableFood, 50);
                for (int i = 0; i < visible; i++) {
                    foodPoints.add(new Point(
                            random.nextInt(getWidth() - 50) + 25,
                            random.nextInt(getHeight() - 50) + 25
                    ));
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(new Color(20, 30, 40));

            // food
            synchronized (foodPoints) {
                g.setColor(new Color(100, 255, 100));
                for (Point p : foodPoints) {
                    g.fillOval(p.x - FOOD_SIZE/2, p.y - FOOD_SIZE/2, FOOD_SIZE, FOOD_SIZE);
                }
            }

            // cells
            synchronized (visualCells) {
                for (VisualCell vc : visualCells) {
                    if (vc.cell.isAlive()) vc.draw(g);
                }
            }
        }


        //  One visual cell (position + movement logic)
        class VisualCell {
            final Cell cell;
            Point pos;
            Point target;
            int speed = 2;

            VisualCell(Cell cell) {
                this.cell = cell;
                pos = new Point(random.nextInt(800) + 50, random.nextInt(500) + 50);
                target = pos;
            }

            void update() {
                // move toward target
                if (pos.distance(target) > 5) {
                    double dx = target.x - pos.x;
                    double dy = target.y - pos.y;
                    double dist = pos.distance(target);
                    pos.x += (int) (dx / dist * speed);
                    pos.y += (int) (dy / dist * speed);
                } else {
                    target = new Point(
                            random.nextInt(getWidth() - 100) + 50,
                            random.nextInt(getHeight() - 100) + 50
                    );
                }

                // hungry → chase nearest food
                if (cell.isHungry()) {
                    synchronized (foodPoints) {
                        foodPoints.stream()
                                .min((a, b) -> Double.compare(pos.distance(a), pos.distance(b)))
                                .ifPresent(food -> {
                                    if (pos.distance(food) < 30) {
                                        speed = 4;
                                        target = food;
                                    }
                                });
                    }
                } else {
                    speed = 2;
                }
            }

            void draw(Graphics g) {
                Color base = cell instanceof AsexualCell ?
                        new Color(70, 130, 255) : new Color(255, 100, 180);
                Color fill = cell.isHungry() ? base.brighter() : base.darker();

                // body
                g.setColor(fill);
                g.fillOval(pos.x - CELL_SIZE/2, pos.y - CELL_SIZE/2, CELL_SIZE, CELL_SIZE);

                // border
                g.setColor(Color.WHITE);
                g.drawOval(pos.x - CELL_SIZE/2, pos.y - CELL_SIZE/2, CELL_SIZE, CELL_SIZE);

                // ID
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 10));
                g.drawString(String.valueOf(cell.getId()), pos.x - 6, pos.y + 3);

                // reproducing ring
                if (cell.isWantingToReproduce()) {
                    g.setColor(new Color(255, 255, 0, 180));
                    g.drawOval(pos.x - CELL_SIZE, pos.y - CELL_SIZE,
                            CELL_SIZE * 2, CELL_SIZE * 2);
                }
            }
        }
    }
}