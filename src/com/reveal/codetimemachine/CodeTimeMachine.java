package com.reveal.codetimemachine;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;

public class CodeTimeMachine
{
    private Project project = null;
    private ToolWindow toolWindow = null;
    private GitHelper gitHelper = null;

    public CodeTimeMachine(Project project)
    {
        this.project = project;
        toolWindow = ToolWindowManager.getInstance(project).registerToolWindow("Code Time Machine      ", false, ToolWindowAnchor.RIGHT);
        gitHelper = new GitHelper(project);
    }

    public ToolWindow getToolWindow()
    {
        return toolWindow;
    }

    public GitHelper getGitHelper()
    {
        return gitHelper;
    }
}
