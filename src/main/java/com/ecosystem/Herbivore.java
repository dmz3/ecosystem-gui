package com.ecosystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Herbivore extends Agents {
    public Herbivore(int x, int y, int energy) {
        super(x, y, energy, "🐇");
    }

    public record VisionResult(List<Predator> predators, Plant nearestPlant) {
    }

    private Random random = new Random();

    @Override
    public void step(Environment env) {
        this.energy--; // вычитаем энергию
        if (this.energy <= 0) {
            if (env.getAgent(this.getPosition().x(), this.getPosition().y()) == this) {
                env.setAgent(this.getPosition().x(), this.getPosition().y()); // убили
            }
            return;
        } else {
            VisionResult see = this.vision(env); // смотрим вокруг, получаем VisionResult
            Agents.Position nextMove = null;
            if (!see.predators.isEmpty()) { // если рядом хищник бежим от него
                nextMove = this.findEscape(see.predators, env);
            } else if (see.nearestPlant != null) { // если рядом растение идем в сторону растения
                nextMove = this.findPlant(see.nearestPlant, env);
                if (nextMove.equals(see.nearestPlant.getPosition())) {
                    this.eat(see.nearestPlant);
                }
            } else {
                nextMove = this.findRandom(env);
            }
            if (nextMove != null) {
                move(this.getPosition().x(), this.getPosition().y(), nextMove, env);
            }
        }
        if (this.energy > 100) {// Можем контролировать порог размножения
            reproduce(env);
        }
    }

    @Override 
    public void eat(Agents food) {
        if (food instanceof Plant) {
            this.energy += 7; // Можем контролировать кол-во энергии за еду
            food.energy = 0;
        }
    }

    private VisionResult vision(Environment env) {
        int myX = this.getPosition().x();
        int myY = this.getPosition().y();
        List<Agents> neighbors = env.getNeighbors(myX, myY, 2);

        List<Predator> predators = new ArrayList<>();
        Plant nearestPlant = null;

        int minDistance = 1000;

        for (Agents agent : neighbors) {
            if (agent instanceof Predator predator) {
                predators.add(predator);
                nearestPlant = null;
            } else if (agent instanceof Plant plant && predators.isEmpty()) {
                int plantX = plant.getPosition().x();
                int plantY = plant.getPosition().y();

                int distance = Math.abs(myX - plantX) + Math.abs(myY - plantY);

                if (distance < minDistance) {
                    minDistance = distance;
                    nearestPlant = plant;
                }

            }
        }
        return new VisionResult(predators, nearestPlant);
    }

    private boolean isWalkable(int x, int y, Environment env) {
        if (x >= env.W || x < 0 || y >= env.H || y < 0) {
            return false;
        }
        Agents occupant = env.getAgent(x, y);
        return occupant == null || occupant instanceof Plant;
    }

    private Agents.Position findEscape(List<Predator> predators, Environment env) {
        int myX = this.getPosition().x();
        int myY = this.getPosition().y();

        int[][] directions = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };

        int bestX = myX;
        int bestY = myY;
        int maximumDistance = -1;

        for (int[] dir : directions) {
            int nextX = myX + dir[0];
            int nextY = myY + dir[1];

            if (!isWalkable(nextX, nextY, env)) {
                continue;
            }

            int currentDistance = 0;

            for (Predator predator : predators) {
                currentDistance += Math.abs(nextX - predator.getPosition().x())
                        + Math.abs(nextY - predator.getPosition().y());

            }

            if (currentDistance > maximumDistance) {
                maximumDistance = currentDistance;
                bestX = nextX;
                bestY = nextY;
            }

        }
        return new Agents.Position(bestX, bestY);

    }

    private Agents.Position findPlant(Plant nearestPlant, Environment env) {
        int myX = this.getPosition().x();
        int myY = this.getPosition().y();
        int targetX = nearestPlant.getPosition().x();
        int targetY = nearestPlant.getPosition().y();
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
            Herbivore baby = new Herbivore(babyPosX, babyPosY, 25);

            this.energy -= 25;

            env.setAgent(baby);
        }
    }
}
