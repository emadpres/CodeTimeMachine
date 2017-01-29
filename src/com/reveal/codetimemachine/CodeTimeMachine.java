package com.reveal.codetimemachine;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.ui.content.Content;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;

// The CodeTimeMachine represents the plugin window per project
// and contains one ToolWindow.
public class CodeTimeMachine
{
    private Project project = null;
    private ToolWindow toolWindow = null;
    private GitHelper gitHelper = null;
    private ArrayList<TTMSingleFileView> singleViews = new ArrayList<>(1);

    public CodeTimeMachine(Project project)
    {
        this.project = project;

        gitHelper = new GitHelper(project);

        toolWindow = ToolWindowManager.getInstance(project).registerToolWindow("Code Time Machine ", true/*Can close tabs?*/, ToolWindowAnchor.RIGHT);
        ImageIcon icon = new ImageIcon(getClass().getResource("/images/appIcon.png"));
        toolWindow.setIcon(icon);
        toolWindow.setAutoHide(false);
    }

    ToolWindow getToolWindow()
    {
        return toolWindow;
    }

    GitHelper getGitHelper()
    {
        return gitHelper;
    }

    void addNewContent(TTMSingleFileView singleView, String contentName)
    {
        Content ttm_content = toolWindow.getContentManager().getFactory().createContent(singleView.getComponent(), contentName, true);
        toolWindow.getContentManager().addContent(ttm_content);
        //toolWindow.setAvailable(true,null);
        singleViews.add(singleView);
    }

    public ArrayList<String> getOpenFileNames()
    {
        int contentsCount = getToolWindow().getContentManager().getContentCount();
        ArrayList<String> allOpenFileNames = new ArrayList<>(contentsCount);
        for (int i=0; i<contentsCount; i++)
        {
            String fileName = singleViews.get(i).virtualFile.getName();
            allOpenFileNames.add(fileName);
        }
        return allOpenFileNames;
    }

    public ArrayList<CommitWrapper> getActiveCommits()
    {
        int contentsCount = getToolWindow().getContentManager().getContentCount();
        ArrayList<CommitWrapper> activeCommits = new ArrayList<>(contentsCount);

        for (int i=0; i<contentsCount; i++)
        {
            int activeCommit_cIndex = singleViews.get(i).activeCommit_cIndex;
            CommitWrapper activeCommit = singleViews.get(i).commits.get(activeCommit_cIndex);
            activeCommits.add(activeCommit);
        }
        return activeCommits;
    }

//    public CommitWrapper getActiveCommits(int singleViewIndex)
//    {
//        int activeCommit_cIndex = singleViews.get(singleViewIndex).activeCommit_cIndex;
//        CommitWrapper activeCommit = singleViews.get(singleViewIndex).commits.get(activeCommit_cIndex);
//        return activeCommit;
//    }

}
