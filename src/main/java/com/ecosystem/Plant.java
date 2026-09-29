package com.ecosystem;

public class Plant extends Agents {
    public Plant(int x, int y, int energy) {
        super(x, y, energy, "🌲");
    }

    @Override
    public void step(Environment env) {
        if (this.energy <= 0) {
            if (env.getAgent(this.getPosition().x(), this.getPosition().y()) == this) {
                env.setAgent(this.getPosition().x(), this.getPosition().y());
            }
            return;
        }
        this.energy += 10;
        if (this.energy > 15) {
            reproduce(env);
        }
    }

    private boolean isWalkable(int x, int y, Environment env) {
        if (x >= env.W || x < 0 || y >= env.H || y < 0) {
            return false;
        }
        Agents occupant = env.getAgent(x, y);
        return occupant == null;
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
            Plant baby = new Plant(babyPosX, babyPosY, 10);

            this.energy -= -10;

            env.setAgent(baby);
        }
    }
}
