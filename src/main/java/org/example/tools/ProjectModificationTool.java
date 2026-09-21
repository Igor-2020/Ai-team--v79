package org.example.tools;

import dev.langchain4j.agent.tool.Tool;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ProjectModificationTool {
    private final Path workspaceRoot;
    private final Path protectedRoot;

    public ProjectModificationTool() {
        this("C:\\Users\\IgorS\\Desktop\\IITeam");
    }

    public ProjectModificationTool(String workspaceRoot) {
        this(workspaceRoot, Paths.get(System.getProperty("user.dir")).toString());
    }

    public ProjectModificationTool(String workspaceRoot, String protectedRoot) {
        this.workspaceRoot = Paths.get(workspaceRoot).toAbsolutePath().normalize();
        this.protectedRoot = Paths.get(protectedRoot).toAbsolutePath().normalize();
    }

    @Tool("Создать новый файл с кодом или перезаписать существующий файл внутри конкретного проекта AI Team.")
    public String writeAbsoluteFile(String absolutePath, String fileContent) {
        System.out.println("[TOOL] ProjectModificationTool.writeAbsoluteFile(" + absolutePath + ")");

        try {
            Path filePath = Paths.get(absolutePath).toAbsolutePath().normalize();

            if (!filePath.startsWith(workspaceRoot)) {
                return "ОТКАЗ: запись запрещена. Путь находится за пределами рабочего пространства AI Team.";
            }

            if (isProtectedPath(filePath)) {
                return "ОТКАЗ: запись в защищённый проект AI Team запрещена.";
            }

            if (filePath.equals(workspaceRoot) || filePath.getParent() == null || filePath.getParent().equals(workspaceRoot)) {
                return "ОТКАЗ: файлы нельзя создавать непосредственно в корне workspace. Сначала создай отдельную папку проекта внутри workspace.";
            }

            Path projectRoot = findProjectRoot(filePath);

            if (projectRoot == null) {
                return "ОТКАЗ: файл должен находиться внутри отдельного проекта первого уровня workspace.";
            }

            Path parent = filePath.getParent();

            Files.createDirectories(parent);
            Files.writeString(filePath, fileContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            return "Успешно записано: " + workspaceRoot.relativize(filePath);
        } catch (IOException e) {
            return "Ошибка при записи файла: " + e.getMessage();
        } catch (InvalidPathException e) {
            return "Ошибка: некорректный путь к файлу.";
        }
    }

    @Tool("Создать стандартную структуру нового Maven-проекта. Новый проект обязательно должен находиться непосредственно внутри workspace.")
    public String createMavenStructure(String projectBasePath) {
        System.out.println("[TOOL] ProjectModificationTool.createMavenStructure(" + projectBasePath + ")");

        try {
            Path base = Paths.get(projectBasePath).toAbsolutePath().normalize();

            if (!base.startsWith(workspaceRoot)) {
                return "ОТКАЗ: создание проекта запрещено. Путь находится за пределами рабочего пространства AI Team.";
            }

            if (isProtectedPath(base)) {
                return "ОТКАЗ: создание проекта в защищённой директории AI Team запрещено.";
            }

            if (!isDirectChildOfWorkspace(base)) {
                return "ОТКАЗ: новый проект должен быть отдельной папкой первого уровня непосредственно внутри workspace: " + workspaceRoot;
            }

            Files.createDirectories(base);
            Files.createDirectories(base.resolve("src/main/java"));
            Files.createDirectories(base.resolve("src/main/resources"));
            Files.createDirectories(base.resolve("src/test/java"));
            Files.createDirectories(base.resolve("src/test/resources"));

            return "Стандартная Maven-структура создана: " + workspaceRoot.relativize(base);
        } catch (IOException e) {
            return "Ошибка при создании структуры проекта: " + e.getMessage();
        } catch (InvalidPathException e) {
            return "Ошибка: некорректный путь проекта.";
        }
    }

    private boolean isProtectedPath(Path path) {
        return path.equals(protectedRoot) || path.startsWith(protectedRoot);
    }

    private boolean isDirectChildOfWorkspace(Path path) {
        return path.getParent() != null && path.getParent().equals(workspaceRoot);
    }

    private Path findProjectRoot(Path filePath) {
        Path current = filePath;

        while (current != null && !current.equals(workspaceRoot)) {
            if (current.getParent() != null && current.getParent().equals(workspaceRoot)) {
                return current;
            }
            current = current.getParent();
        }

        return null;
    }
}
