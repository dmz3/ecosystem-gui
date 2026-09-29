package com.ecosystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Predator extends Agents {
    public Predator(int x, int y, int energy) {
        super(x, y, energy, "🐺");
    }

    public record VisionResult(Herbivore nearestHerbivore, List<Plant> plants) {
    }

    private Random random = new Random();

    @Override
    public void step(Environment env) {
        this.energy--;
        if (this.energy <= 0) {
            if (env.getAgent(this.getPosition().x(), this.getPosition().y()) == this) {
                env.setAgent(this.getPosition().x(), this.getPosition().y()); // убили
            }
            return;
        } else {
            VisionResult see = this.vision(env); // смотрим вокруг, получаем VisionResult
            Agents.Position nextMove = null;
            if (see.nearestHerbivore != null) {
                nextMove = this.findPrey(see.nearestHerbivore, env);
            } else {
                nextMove = this.findRandom(env);
            }
            if (nextMove != null) {
                Agents occupant = env.getAgent(nextMove.x(), nextMove.y());
                if (occupant instanceof Herbivore herbivore) {
                    this.eat(herbivore);
                }
                move(this.getPosition().x(), this.getPosition().y(), nextMove, env);
            }
        }
        if (this.energy > 90) { // Можем контролировать порог размножения
            reproduce(env);
        }
    }

    @Override
    public void eat(Agents food) {
        if (food instanceof Herbivore herbivore) {
            this.energy += 30; // Можем контролировать кол-во энергии за еду
            herbivore.energy = 0;
        }
    }

    private boolean isWalkable(int x, int y, Environment env) {
        if (x >= env.W || x < 0 || y >= env.H || y < 0) {
            return false;
        }
        Agents occupant = env.getAgent(x, y);
        return occupant == null || occupant instanceof Herbivore;
    }

    private VisionResult vision(Environment env) {
        int myX = this.getPosition().x();
        int myY = this.getPosition().y();
        List<Agents> neighbors = env.getNeighbors(myX, myY, 2);

        Herbivore nearestHerbivore = null;
        List<Plant> plants = new ArrayList<>();

        int minDistance = 1000;

        for (Agents agent : neighbors) {
            if (agent instanceof Plant plant) {
                plants.add(plant);
            }
            if (agent instanceof Herbivore herb) {
                int herbX = herb.getPosition().x();
                int herbY = herb.getPosition().y();

                int distance = Math.abs(myX - herbX) + Math.abs(myY - herbY);

                if (distance < minDistance) {
                    minDistance = distance;
                    nearestHerbivore = herb;
                }
            }
        }
        return new VisionResult(nearestHerbivore, plants);
    }

    private Agents.Position findPrey(Herbivore nearestHerbivore, Environment env) {
        int myX = this.getPosition().x();
        int myY = this.getPosition().y();
        int targetX = nearestHerbivore.getPosition().x();
        int targetY = nearestHerbivore.getPosition().y();
        int bestX = myX;
        int bestY = myY;
        int minimumDistance = 1000;

        int[][] directions = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };

        for (int[] dir : directions) {
            int nextX = myX + dir[0];
            int nextY = myY + dir[1];

            if (!isWalkable(nextX, nextY, env)) {
                continue;
            }

            int currentDistance = Math.abs(targetX - nextX) + Math.abs(targetY - nextY);

            if (currentDistance < minimumDistance) {
                minimumDistance = currentDistance;
                bestX = nextX;
                bestY = nextY;
            }

        }
        return new Agents.Position(bestX, bestY);

    }

    private Agents.Position findRandom(Environment env) {
        int myX = this.getPosition().x();
        int myY = this.getPosition().y();

        int[][] directions = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };

        List<Agents.Position> possibleDir = new ArrayList<>();

        for (int[] dir : directions) {
            int nextX = myX + dir[0];
            int nextY = myY + dir[1];

            if (isWalkable(nextX, nextY, env)) {
                possibleDir.add(new Agents.Position(nextX, nextY));
            }
        }
        if (possibleDir.isEmpty()) {
            return null;
        }
        int randomIndex = random.nextInt(possibleDir.size());
        return possibleDir.get(randomIndex);

    }

    private void reproduce(Environment env) {
        int myX = this.getPosition().x();
        int myY = this.getPosition().y();

        int babyPosX = -1;
        int babyPosY = -1;

        int[][] directions = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };

        for (int[] dir : directions) {
            int nextX = myX + dir[0];
            int nextY = myY + dir[1];

            if (isWalkable(nextX, nextY, env) && env.getAgent(nextX, nextY) == null) {
                babyPosX = nextX;
                babyPosY = nextY;
                break;
            }
        }

        if (babyPosX != -1) {
            Predator baby = new Predator(babyPosX, babyPosY, 40);

            this.energy -= 40;

            env.setAgent(baby);
        }
    }
}
