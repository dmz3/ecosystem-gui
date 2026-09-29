package com.ecosystem;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.print.PrinterAttributes;
import javafx.scene.Camera;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import java.io.IOException;

public class App extends Application {

    private Environment env;
    private Canvas canvas;
    private Timeline gameloop;
    private Label statsLabel;
    private Slider speedSlider;
    private Button playPauseBtn;

    private double CELL_SIZE;
    private final int GRID_W = 100;
    private final int GRID_H = 100;
    private int stepCount;
    private boolean isSystemDead;

    private Slider predSlider;
    private Slider herbSlider;
    private Slider plantSlider;
    private int currentPredLimit = 40;
    private int currentHerbLimit = 400;
    private int currentPlantLimit = 2500;

    @Override
    public void start(Stage primaryStage) {
        initSimulation(primaryStage, currentPredLimit, currentHerbLimit, currentPlantLimit);
    }

    private void initSimulation(Stage primaryStage, int predLimit, int herbLimit, int plantLimit) {
        Random rand = new Random();
        env = new Environment(GRID_W, GRID_H);
        stepCount = 0;
        isSystemDead = false;

        // Спавним существ --------------------------------------------
        int predCount = 0;
        while (predCount < predLimit) {
            int prx = rand.nextInt(env.W);
            int pry = rand.nextInt(env.H);

            if (env.getAgent(prx, pry) == null) {
                env.setAgent(new Predator(prx, pry, 40));
                predCount++;
            }

        }
        int herbCount = 0;
        while (herbCount < herbLimit) {
            int hx = rand.nextInt(env.W);
            int hy = rand.nextInt(env.H);

            if (env.getAgent(hx, hy) == null) {
                env.setAgent(new Herbivore(hx, hy, 25));
                herbCount++;
            }
        }
        int plantCount = 0;
        while (plantCount < plantLimit) {
            int px = rand.nextInt(env.W);
            int py = rand.nextInt(env.H);

            if (env.getAgent(px, py) == null) {
                env.setAgent(new Plant(px, py, 10));
                plantCount++;
            }
        }
        // Спавним существ --------------------------------------------

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        CELL_SIZE = bounds.getWidth() / GRID_W;

        canvas = new Canvas(bounds.getWidth(), GRID_H * CELL_SIZE);
        drawGrid();

        // Верхний этаж: Основные кнопки и скорость
        HBox mainControls = new HBox(15);
        mainControls.setStyle("-fx-alignment: center;");

        playPauseBtn = new Button("Старт");

        speedSlider = new Slider(0.1, 10.0, 1.0);
        speedSlider.setPrefWidth(150);
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);
        speedSlider.setMajorTickUnit(1.0);
        speedSlider.setBlockIncrement(0.1);

        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            gameloop.setRate(newValue.doubleValue());
        });

        Button resetBtn = new Button("Сброс");
        resetBtn.setOnAction(e -> {
            if (gameloop != null)
                gameloop.stop();
            initSimulation(primaryStage, (int) predSlider.getValue(),
                    (int) herbSlider.getValue(),
                    (int) plantSlider.getValue());
        });

        mainControls.getChildren().addAll(playPauseBtn, speedSlider, resetBtn);

        // слайдеры настройки популяции

        predSlider = new Slider(0, 150, predLimit);
        predSlider.setPrefWidth(200);
        Label predInfo = new Label("Хищ: " + predLimit);
        predInfo.setPrefWidth(80);

        herbSlider = new Slider(0, 800, herbLimit);
        herbSlider.setPrefWidth(200);
        Label herbInfo = new Label("Трав: " + herbLimit);
        herbInfo.setPrefWidth(80);

        plantSlider = new Slider(0, 3000, plantLimit);
        plantSlider.setPrefWidth(200);
        Label plantInfo = new Label("Раст: " + plantLimit);
        plantInfo.setPrefWidth(200);

        // Обновляем значения при движении ползунков
        predSlider.valueProperty().addListener((obs, old, val) -> predInfo.setText("Хищ: " + val.intValue()));
        herbSlider.valueProperty().addListener((obs, old, val) -> herbInfo.setText("Трав: " + val.intValue()));
        plantSlider.valueProperty().addListener((obs, old, val) -> plantInfo.setText("Раст: " + val.intValue()));

        // Панель управления внизу
        HBox predBox = new HBox(10);
        predBox.setStyle("-fx-alignment: center;");
        predBox.getChildren().addAll(predInfo, predSlider);

        HBox herbBox = new HBox(10);
        herbBox.setStyle("-fx-alignment: center;");
        herbBox.getChildren().addAll(herbInfo, herbSlider);

        HBox plantBox = new HBox(10);
        plantBox.setStyle("-fx-alignment: center;");
        plantBox.getChildren().addAll(plantInfo, plantSlider);

        VBox controlPanel = new VBox(15);
        controlPanel.setStyle("-fx-padding: 15px 5px 25px 5px; -fx-alignment: center; -fx-background-color: #e0e0e0;");
        controlPanel.getChildren().addAll(mainControls, predBox, herbBox, plantBox);
        HBox statsPanel = new HBox(10);
        statsPanel.setStyle("-fx-padding: 10px; -fx-alignment: center; -fx-background-color: BLACK;");

        statsLabel = new Label("Всего: 0 | Хищников: 0 | Травоядных: 0 | Растений: 0 | Шаг: 0 | Скорость: 0");
        statsLabel.setTextFill(Color.WHITE);
        statsLabel.setWrapText(true);
        statsLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        statsPanel.getChildren().add(statsLabel);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-padding 40 0 0 0");
        root.setTop(statsPanel);
        root.setCenter(canvas);
        root.setBottom(controlPanel);

        Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
        primaryStage.setTitle("Ecosystem Simulation");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        gameloop = new Timeline(new KeyFrame(Duration.millis(100), e -> makeStep()));
        gameloop.setCycleCount(Timeline.INDEFINITE);

        playPauseBtn.setOnAction(e -> {
            if (gameloop.getStatus() == Animation.Status.RUNNING) {
                gameloop.pause();
                playPauseBtn.setText("Старт");
            } else {
                gameloop.play();
                playPauseBtn.setText("Пауза");
            }
        });
    }

    private void makeStep() {
        stepCount++;
        List<Agents> activeAgents = new ArrayList<>();

        for (int y = 0; y < env.H; y++) {
            for (int x = 0; x < env.W; x++) {
                Agents agent = env.getAgent(x, y);

                if (agent != null) {
                    activeAgents.add(agent);
                }
            }
        }

        for (Agents agent : activeAgents) {
            if (agent.energy > 0) {
                agent.step(env);
            }
        }

        drawGrid();

        Environment.Stats stats = env.countAgents();
        String statistics = String.format(
                "Всего: %d | Хищников: %d | Травоядных: %d | \nРастений: %d | Шаг: %d | Скорость: %.1f",
                stats.total(), stats.predator_count(), stats.herbivore_count(), stats.plant_count(), stepCount,
                gameloop.getRate());

        if (isSystemDead) {
            statistics += " \nСИСТЕМА УМЕРЛА";
        } else if (stats.plant_count() == 0 || stats.herbivore_count() == 0
                || stats.predator_count() == 0) {
            gameloop.pause();
            playPauseBtn.setText("Старт");
            statistics += "\nСИСТЕМА УМЕРЛА";
            isSystemDead = true;
        }

        statsLabel.setText(statistics);

    }

    private void drawGrid() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (int y = 0; y < env.H; y++) {
            for (int x = 0; x < env.W; x++) {
                Agents agent = env.getAgent(x, y);

                if (agent != null) {
                    if (agent instanceof Plant) {
                        gc.setFill(Color.GREEN);
                    }
                    if (agent instanceof Herbivore) {
                        gc.setFill(Color.BLUE);
                    }
                    if (agent instanceof Predator) {
                        gc.setFill(Color.RED);
                    }
                    gc.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}