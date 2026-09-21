package org.example.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.AIEngine;
import org.example.agents.classes.Architect;
import org.example.agents.classes.Middle;
import org.example.agents.classes.Senior;
import org.example.agents.classes.Tester;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class MainApp extends Application {
    private static final Path WORKSPACE_ROOT = AIEngine.WORKSPACE_ROOT;
    private static final int MAX_FIX_ATTEMPTS = 3;

    // Блок агентов. Каждый агент создаётся один раз на задачу.
    private Architect architect;
    private Senior senior;
    private Middle middle;
    private Tester tester;
    private String currentTaskId;

    private TextArea logArea;
    private TextField inputField;
    private ComboBox<String> modelSelector;
    private Button runButton;

    @Override
    public void start(Stage primaryStage) {
        // Общий AI-контекст создаётся один раз при запуске приложения.
        AIEngine.initialize();

        primaryStage.setTitle("🚀 AI Team - Панель управления агентами");

        Label modelLabel = new Label("Режим ИИ:");
        modelSelector = new ComboBox<>();
        modelSelector.getItems().addAll("Локальный (LM Studio)", "Облачный (OpenRouter)");
        modelSelector.setValue("Локальный (LM Studio)");

        HBox settingsBox = new HBox(10, modelLabel, modelSelector);
        settingsBox.setPadding(new Insets(10));

        inputField = new TextField();
        inputField.setPromptText("Введите задачу для ИИ-разработчиков (например: Создай калькулятор)");

        runButton = new Button("Запустить команду");
        HBox inputBox = new HBox(10, inputField, runButton);
        inputBox.setPadding(new Insets(10));
        inputField.prefWidthProperty().bind(inputBox.widthProperty().subtract(150));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPromptText("Здесь будут отображаться логи работы AI Team...");
        logArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");

        VBox root = new VBox(10, settingsBox, inputBox, logArea);
        root.setPadding(new Insets(15));
        logArea.prefHeightProperty().bind(root.heightProperty().subtract(100));

        runButton.setOnAction(event -> handleAiTask());

        primaryStage.setScene(new Scene(root, 700, 500));
        primaryStage.show();
    }

    private void handleAiTask() {
        String userTask = inputField.getText().trim();
        if (userTask.isEmpty()) {
            logArea.setText("⚠️ Ошибка: Пожалуйста, введите текст задачи!");
            return;
        }

        runButton.setDisable(true);
        inputField.setDisable(true);
        logArea.clear();

        appendLog("🚀 ЗАПУСК AI TEAM");
        appendLog("Задача Директора: " + userTask);
        appendLog("📁 Рабочее пространство: " + WORKSPACE_ROOT);

        new Thread(() -> runPipeline(userTask)).start();
    }

    private void runPipeline(String userTask) {
        try {
            Files.createDirectories(WORKSPACE_ROOT);

            currentTaskId = UUID.randomUUID().toString();
            appendLog("🧠 AIEngine: используется единый ChatModel/Memory/Tools.");
            appendLog("🆔 Task ID: " + currentTaskId);

            // Все сотрудники этой задачи используют один AIEngine и одну модель.
            architect = new Architect(userTask, currentTaskId);
            senior = new Senior(currentTaskId);
            tester = new Tester(currentTaskId);
            middle = null;

            architect.Run();

            appendLog("\n🏗️ [ГЛАВНЫЙ АРХИТЕКТОР]");
            appendLog(architect.Response);

            runDevelopmentIterations();

        } catch (Exception e) {
            appendLog("\n💥 Ошибка конвейера разработки: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Platform.runLater(() -> {
                runButton.setDisable(false);
                inputField.setDisable(false);
            });
        }
    }

    private void runDevelopmentIterations() {
        boolean projectPassed = false;

        for (int iteration = 1; iteration <= MAX_FIX_ATTEMPTS; iteration++) {
            appendLog("\n==================================================");
            appendLog("🔄 ИТЕРАЦИЯ " + iteration + "/" + MAX_FIX_ATTEMPTS);
            appendLog("==================================================");

            runSenior(iteration);

            appendLog("\n👨‍💻 [ГЛАВНЫЙ РАЗРАБОТЧИК]");
            appendLog(senior.Response);

            runMiddle(iteration);
            runTester(iteration);

            appendLog("\n🧪 [ТЕСТЕР]");
            appendLog(tester.Response);

            if (isTestPassed(tester.Response)) {
                projectPassed = true;
                appendLog("\n✅ ТЕСТЕР: ПРОЕКТ ПРОШЁЛ ПРОВЕРКУ.");
                break;
            }

            appendLog("\n❌ ТЕСТЕР: ПРОЕКТ НЕ ПРОШЁЛ ПРОВЕРКУ.");
            if (iteration < MAX_FIX_ATTEMPTS) {
                appendLog("🔧 Следующая итерация передаст отчёт Tester Главному Разработчику.");
            }
        }

        appendLog("\n==================================================");
        if (projectPassed) {
            appendLog("🎉 AI TEAM ЗАВЕРШИЛА РАЗРАБОТКУ.");
            appendLog("✅ Статус проекта: PASS");
        } else {
            appendLog("⚠️ AI TEAM НЕ СМОГЛА ДОСТИЧЬ PASS.");
            appendLog("❌ Статус проекта: FAIL");
        }
        appendLog("📁 Рабочее пространство:\n" + WORKSPACE_ROOT);
        appendLog("==================================================");
    }

    private void runSenior(int iteration) {
        if (iteration == 1) {
            senior.setImplementationRequest(architect.Response);
        } else {
            senior.setFixRequest(
                    architect.Response,
                    senior.Response,
                    tester.Response,
                    iteration
            );
        }

        senior.Run();
    }

    private void runMiddle(int iteration) {
        if (iteration == 1 && needsMiddleDeveloper(architect.Response, senior.Response)) {
            appendLog("\n👨‍💻 [СРЕДНИЙ РАЗРАБОТЧИК]");

            if (middle == null) {
                middle = new Middle(currentTaskId);
            }

            middle.setRequest(architect.Response, senior.Response);
            middle.Run();
            appendLog(middle.Response);
        } else {
            appendLog("\nℹ️ Помощь Среднего Разработчика не требуется.");
        }
    }

    private void runTester(int iteration) {
        tester.setRequest(architect.Response, senior.Response, iteration);
        tester.Run();
    }

    private boolean needsMiddleDeveloper(String architectureResponse, String developerResponse) {
        return (architectureResponse != null && architectureResponse.contains("NEED_MIDDLE=YES"))
                || (developerResponse != null && developerResponse.contains("NEED_MIDDLE=YES"));
    }

    private boolean isTestPassed(String testerResponse) {
        return testerResponse != null && testerResponse.contains("TEST_RESULT=PASS");
    }

    private void appendLog(String message) {
        Platform.runLater(() -> logArea.appendText(message + "\n"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
