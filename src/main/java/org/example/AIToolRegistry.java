package org.example;

import org.example.tools.ProjectInspectionTool;
import org.example.tools.ProjectModificationTool;
import org.example.tools.TerminalExecutionTool;

import java.nio.file.Path;

/** Общие экземпляры инструментов. Создаются один раз вместе с AIEngine. */
public final class AIToolRegistry {
    private final ProjectInspectionTool inspectionTool;
    private final ProjectModificationTool modificationTool;
    private final TerminalExecutionTool terminalTool;

    public AIToolRegistry(Path workspaceRoot, Path protectedRoot) {
        String workspace = workspaceRoot.toString();
        String protectedPath = protectedRoot.toString();
        inspectionTool = new ProjectInspectionTool(workspace, protectedPath);
        modificationTool = new ProjectModificationTool(workspace, protectedPath);
        terminalTool = new TerminalExecutionTool(workspace, protectedPath);
    }

    public ProjectInspectionTool inspection() { return inspectionTool; }
    public ProjectModificationTool modification() { return modificationTool; }
    public TerminalExecutionTool terminal() { return terminalTool; }
}
