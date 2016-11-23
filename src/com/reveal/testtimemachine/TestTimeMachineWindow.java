package com.reveal.testtimemachine;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


public class TestTimeMachineWindow
{
    private JPanel myJComponent;
    private Project project;
    private VirtualFile[] virtualFiles = new VirtualFile[2];
    private ArrayList<List<VcsFileRevision>> fileRevisionLists = new ArrayList<List<VcsFileRevision>>();

    TestTimeMachineWindow(Project project, VirtualFile[] virtualFiles, ArrayList<List<VcsFileRevision>> fileRevisionsLists)
    {
        this.project = project;
        this.virtualFiles = virtualFiles;
        this.fileRevisionLists = fileRevisionsLists;
        ////////////////////////////////////////////////////
        setupToolTipSetting();
        GroupLayout groupLayout = createEmptyJComponentAndReturnGroupLayout();

    }

    private void setupToolTipSetting()
    {
        ToolTipManager.sharedInstance().setEnabled(true);
        ToolTipManager.sharedInstance().setInitialDelay(100); // it needs ToolTipManager.sharedInstance().setEnabled(true); before
    }

    private GroupLayout createEmptyJComponentAndReturnGroupLayout()
    {
        myJComponent = new JPanel();
        GroupLayout groupLayout = new GroupLayout(myJComponent);
        myJComponent.setLayout(groupLayout);
        groupLayout.setAutoCreateContainerGaps(true);
        groupLayout.setAutoCreateGaps(true);
        groupLayout.setHonorsVisibility(false);

        return groupLayout;
    }

    public JPanel getComponent()
    {
        return myJComponent;
    }
}
