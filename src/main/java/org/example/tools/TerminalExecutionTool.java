package org.example.tools;

import dev.langchain4j.agent.tool.Tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class TerminalExecutionTool {
    private final Path workspaceRoot;
    private final Path protectedRoot;

    public TerminalExecutionTool(String workspaceRoot) {
        this(workspaceRoot, Paths.get(System.getProperty("user.dir")).toString());
    }

    public TerminalExecutionTool(String workspaceRoot, String protectedRoot) {
        this.workspaceRoot = Paths.get(workspaceRoot).toAbsolutePath().normalize();
        this.protectedRoot = Paths.get(protectedRoot).toAbsolutePath().normalize();
    }

    @Tool("Выполнить Maven-команду внутри конкретного проекта первого уровня workspace.")
    public String executeMavenInFolder(String projectFolderPath, String command) {
        System.out.println("[TOOL] TerminalExecutionTool.executeMavenInFolder(" +
                projectFolderPath + ", " + command + ")");

        try {
            Path folder = Paths.get(projectFolderPath).toAbsolutePath().normalize();

            if (!isDirectChildProject(folder)) {
                return "ОТКАЗ: запуск разрешён только внутри проекта первого уровня workspace.";
            }

            if (isProtectedPath(folder)) {
                return "ОТКАЗ: запуск в защищённом проекте AI Team запрещён.";
            }

            if (!Files.isDirectory(folder)) {
                return "Ошибка: папка проекта не существует: " + projectFolderPath;
            }

            String actualCommand = normalizeMavenCommand(command);
            if (actualCommand == null) {
                return "ОТКАЗ: разрешены только Maven-команды без shell chaining.";
            }

            if (actualCommand.contains("&") || actualCommand.contains("|") ||
                    actualCommand.contains(">") || actualCommand.contains("<") ||
                    actualCommand.contains("\n") || actualCommand.contains("\r")) {
                return "ОТКАЗ: команда содержит запрещённые элементы оболочки Windows.";
            }

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                String ideaMavenPath =
                        "C:\\Program Files\\JetBrains\\IntelliJ IDEA 2025.2\\plugins\\maven\\lib\\maven3\\bin\\mvn.cmd";

                if (new File(ideaMavenPath).exists()) {
                    actualCommand = "\"" + ideaMavenPath + "\" " +
                            actualCommand.substring(4);
                }
            }

            ProcessBuilder processBuilder =
                    new ProcessBuilder("cmd.exe", "/c", actualCommand);

            processBuilder.directory(folder.toFile());
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append('\n');
                }
            }

            int exitCode = process.waitFor();

            return "MAVEN_EXIT_CODE=" + exitCode +
                    "\nPROJECT=" + folder.getFileName() +
                    "\nOUTPUT:\n" + output;
        } catch (Exception e) {
            return "Критическая ошибка терминала: " + e.getMessage();
        }
    }

    private String normalizeMavenCommand(String command) {
        if (command == null || command.isBlank()) return null;

        String value = command.trim();

        if (!value.startsWith("mvn ")) {
            value = "mvn " + value;
        }

        String[] allowed = {
                "clean", "test", "package", "install", "compile",
                "clean test", "clean package", "clean install"
        };

        String goals = value.substring(4).trim();

        for (String allowedCommand : allowed) {
            if (goals.equals(allowedCommand) ||
                    goals.startsWith(allowedCommand + " -")) {
                return "mvn " + goals;
            }
        }

        return null;
    }

    private boolean isProtectedPath(Path path) {
        return path.equals(protectedRoot) || path.startsWith(protectedRoot);
    }

    private boolean isDirectChildProject(Path folder) {
        return folder.getParent() != null &&
                folder.getParent().equals(workspaceRoot);
    }
}
