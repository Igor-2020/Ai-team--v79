package org.example.tools;

import dev.langchain4j.agent.tool.Tool;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Collectors;

public class ProjectInspectionTool {
    private static final int MAX_STRUCTURE_CHARS = 10000;
    private static final int MAX_FILE_CHARS = 12000;
    private final Path workspaceRoot;
    private final Path protectedRoot;

    public ProjectInspectionTool(String workspaceRoot) {
        this(workspaceRoot, Paths.get(System.getProperty("user.dir")).toString());
    }

    public ProjectInspectionTool(String workspaceRoot, String protectedRoot) {
        this.workspaceRoot = Paths.get(workspaceRoot).toAbsolutePath().normalize();
        this.protectedRoot = Paths.get(protectedRoot).toAbsolutePath().normalize();
    }

    @Tool("Проверить, существует ли проект первого уровня workspace с указанным именем.")
    public String projectExists(String projectName) {
        System.out.println("[TOOL] ProjectInspectionTool.projectExists(" + projectName + ")");

        if (projectName == null || projectName.isBlank()) {
            return "ОШИБКА: имя проекта не указано.";
        }

        try {
            Path project = workspaceRoot.resolve(projectName).normalize();

            if (!isDirectChildOfWorkspace(project)) {
                return "ОШИБКА: имя проекта должно обозначать папку первого уровня workspace.";
            }

            if (isProtectedPath(project)) {
                return "ОШИБКА: защищённый проект недоступен.";
            }

            return Files.isDirectory(project)
                    ? "PROJECT_EXISTS=YES\nPROJECT_NAME=" + projectName
                    : "PROJECT_EXISTS=NO\nPROJECT_NAME=" + projectName;
        } catch (InvalidPathException e) {
            return "ОШИБКА: некорректное имя проекта.";
        }
    }

    @Tool("Получить структуру только указанного проекта первого уровня workspace. Не сканирует весь workspace.")
    public String getProjectStructure(String projectName) {
        System.out.println("[TOOL] ProjectInspectionTool.getProjectStructure(" + projectName + ")");

        try {
            Path project = workspaceRoot.resolve(projectName).normalize();

            if (!isDirectChildOfWorkspace(project)) {
                return "ОТКАЗ: можно читать только проект первого уровня workspace.";
            }

            if (isProtectedPath(project)) {
                return "ОТКАЗ: чтение защищённого проекта AI Team запрещено.";
            }

            if (!Files.isDirectory(project)) {
                return "Ошибка: проект не найден: " + projectName;
            }

            try (var stream = Files.walk(project, 8)) {
                String structure = stream
                        .filter(path -> !isIgnoredPath(path))
                        .map(path -> project.relativize(path).toString())
                        .filter(path -> !path.isBlank())
                        .collect(Collectors.joining("\n"));
                return structure.length() <= MAX_STRUCTURE_CHARS
                        ? structure
                        : structure.substring(0, MAX_STRUCTURE_CHARS) + "\n[СТРУКТУРА СОКРАЩЕНА]";
            }
        } catch (IOException e) {
            return "Ошибка при чтении структуры проекта: " + e.getMessage();
        } catch (InvalidPathException e) {
            return "Ошибка: некорректное имя проекта.";
        }
    }

    @Tool("Прочитать содержимое текстового файла по относительному пути внутри конкретного проекта.")
    public String readFileContent(String relativePath) {
        System.out.println("[TOOL] ProjectInspectionTool.readFileContent(" + relativePath + ")");

        try {
            Path filePath = workspaceRoot.resolve(relativePath).normalize();

            if (!filePath.startsWith(workspaceRoot)) {
                return "ОТКАЗ: путь выходит за пределы workspace.";
            }

            if (isProtectedPath(filePath)) {
                return "ОТКАЗ: чтение защищённого проекта AI Team запрещено.";
            }

            if (!Files.isRegularFile(filePath)) {
                return "Ошибка: файл не найден: " + relativePath;
            }

            String content = Files.readString(filePath);
            return content.length() <= MAX_FILE_CHARS
                    ? content
                    : content.substring(0, MAX_FILE_CHARS) + "\n[ФАЙЛ СОКРАЩЁН]";
        } catch (IOException e) {
            return "Ошибка при чтении файла: " + e.getMessage();
        } catch (InvalidPathException e) {
            return "Ошибка: некорректный путь к файлу.";
        }
    }

    private boolean isIgnoredPath(Path path) {
        Path relative = workspaceRoot.relativize(path);
        for (Path part : relative) {
            String name = part.toString().toLowerCase();
            if (name.equals(".git") || name.equals(".idea") || name.equals(".vs") ||
                    name.equals("target") || name.equals("bin") || name.equals("obj") ||
                    name.equals("packages") || name.equals("node_modules") ||
                    name.equals("build") || name.equals("dist") || name.equals("out")) {
                return true;
            }
        }
        return false;
    }

    private boolean isProtectedPath(Path path) {
        return path.equals(protectedRoot) || path.startsWith(protectedRoot);
    }

    private boolean isDirectChildOfWorkspace(Path path) {
        return path.getParent() != null && path.getParent().equals(workspaceRoot);
    }
}
