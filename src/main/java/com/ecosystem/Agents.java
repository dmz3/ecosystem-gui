package com.ecosystem;

public class Agents {
    protected int energy;
    private String symbol;

    protected record Position(int x, int y) {
    }

    protected Position position;

    public Agents(int x, int y, int energy, String symbol) {
        this.position = new Position(x, y);
        this.energy = energy;
        this.symbol = symbol;
    }

    public void showInfo() {
        System.out.println("Координаты: " + position.x() + " " + position.y());
        System.out.println("Энергия: " + energy);
        System.out.println("Символ: " + symbol);
    }

    public String getSymbol() {
        return symbol;
    }

    public Position getPosition() {
        return position;
    };

    public void move(int x, int y, Position nextMove, Environment env) {
        env.setAgent(x, y); // удаляем со старой позиции

        this.position = nextMove;

        env.setAgent(this); // ставим на новую

    }

    public void step(Environment env) {

    }

    public void eat(Agents food) {
        
    }
}
