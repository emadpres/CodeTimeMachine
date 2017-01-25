package com.reveal.codetimemachine;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ToolWindowType;
import javax.swing.*;

public class CodeTimeMachine
{
    private Project project = null;
    private ToolWindow toolWindow = null;
    private GitHelper gitHelper = null;

    public CodeTimeMachine(Project project)
    {
        this.project = project;

        gitHelper = new GitHelper(project);

        toolWindow = ToolWindowManager.getInstance(project).registerToolWindow("Code Time Machine ", true/*Can close tabs?*/, ToolWindowAnchor.RIGHT);
        toolWindow.setType(ToolWindowType.WINDOWED,null); // make it window, and not docked or anything else
        ImageIcon icon = new ImageIcon(getClass().getResource("/images/appIcon.png"));
        toolWindow.setIcon(icon);
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
