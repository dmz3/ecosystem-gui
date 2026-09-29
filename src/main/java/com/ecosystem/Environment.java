package com.ecosystem;

import java.util.ArrayList;
import java.util.List;

public class Environment {
    public int W;// (ширина по X)
    public int H;// (высота по Y)
    private final Agents[][] grid;
    public record Stats(int plant_count, int herbivore_count, int predator_count, int total) {}

    public Environment(int width, int height) {
        this.W = width;
        this.H = height;
        this.grid = new Agents[H][W];
    }

    public void showEnv() {
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                String display = (grid[i][j] == null) ? "00" : grid[i][j].getSymbol();
                System.out.print(display + " ");
            }
            System.out.println();
        }
    }

    public Agents getAgent(int x, int y) {
        if (x < 0 || x >= W || y < 0 || y >= H)
            return null;
        return grid[y][x];
    }

    public void setAgent(Agents agent) {
        if (agent == null)
            return;
        Agents.Position pos = agent.getPosition();
        int x = pos.x();
        int y = pos.y();
        grid[y][x] = agent;

    }

    public void setAgent(int x, int y) {
        if (x < 0 || x >= W || y < 0 || y >= H)
            return;
        grid[y][x] = null;
    }

    public List<Agents> getNeighbors(int x, int y, int radius) {
        List<Agents> neighbors = new ArrayList<>();

        for (int i = Math.max(0, y - radius); i <= Math.min(H - 1, y + radius); i++) {
            for (int j = Math.max(0, x - radius); j <= Math.min(W - 1, x + radius); j++) {
                if (i == x && j == y)
                    continue;
                if (grid[i][j] != null) {
                    neighbors.add(grid[i][j]);
                }

            }
        }
        return neighbors;
    }

    public Stats countAgents() {
        int predator_count = 0;
        int herbivore_count = 0;
        int plant_count = 0;
        for (int i=0; i < H; i++) {
            for (int j=0; j < W; j++) {
                if (grid[i][j] instanceof Plant) {
                    plant_count++;
                }
                if (grid[i][j] instanceof Predator) {
                    predator_count++;
                }
                if (grid[i][j] instanceof Herbivore) {
                    herbivore_count++;
                }
            }
        }
        int total_count = plant_count + herbivore_count + predator_count;
        return new Stats(plant_count, herbivore_count, predator_count, total_count);
    }
}
