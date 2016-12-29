package com.reveal.testtimemachine;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;


import javax.swing.*;
import java.util.*;

///////// ++ UI ++ /////////
///////// ++ UI -- /////////

public class TTMSingleFileView
{
    //////////////////////////////
    private JPanel thisComponent;
    private Project project;
    private VirtualFile virtualFile = null;
    private ArrayList<CommitWrapper> commits = null;
    ////////////////////////////// UI
    Commits3DView codeHistory3DView = null;
    CommitsBar commitsBar = null;
    //////////////////////////////

    TTMSingleFileView(Project project, VirtualFile virtualFile, ArrayList<CommitWrapper> commits)
    {
        this.project = project;
        this.virtualFile = virtualFile;
        this.commits = commits;
        ////////////////////////////////////////////////////
        setupToolTipSetting();
        GroupLayout groupLayout = createEmptyJComponentAndReturnGroupLayout();
        ////////////
        setupUI_createBar(virtualFile, commits);
        setupUI_createCodeHistory3DView(project, virtualFile, commits);
        ////////////

        setup_UILayout_Single(groupLayout);

        codeHistory3DView.showCommit(0, false);
    }

    private void setupUI_createCodeHistory3DView(Project project, VirtualFile virtualFiles, ArrayList<CommitWrapper> commits)
    {
        codeHistory3DView = new Commits3DView(project, virtualFiles, commits);
    }

    private void setupUI_createBar(VirtualFile virtualFiles, ArrayList<CommitWrapper> commits)
    {
        commitsBar = new CommitsBar( CommitsBar.CommitItemDirection.LTR, ClassType.SUBJECT_CLASS, commits, this);
    }

    private void setup_UILayout_Single(GroupLayout groupLayout)
    {
        groupLayout.setHorizontalGroup( groupLayout.createSequentialGroup()
                .addComponent(commitsBar.getComponent())
                .addGroup( groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(codeHistory3DView)
                )
        );

        groupLayout.setVerticalGroup( groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                .addComponent(commitsBar.getComponent())
                .addGroup(groupLayout.createSequentialGroup()
                        .addComponent(codeHistory3DView)
                )
        );
    }

    private void setupToolTipSetting()
    {
        ToolTipManager.sharedInstance().setEnabled(true);
        ToolTipManager.sharedInstance().setInitialDelay(100); // it needs ToolTipManager.sharedInstance().setEnabled(true); before
    }

    private GroupLayout createEmptyJComponentAndReturnGroupLayout()
    {
        thisComponent = new JPanel();
        GroupLayout groupLayout = new GroupLayout(thisComponent);
        thisComponent.setLayout(groupLayout);
        groupLayout.setAutoCreateContainerGaps(true);
        groupLayout.setAutoCreateGaps(true);
        groupLayout.setHonorsVisibility(false);

        return groupLayout;
    }


    public JPanel getComponent()
    {
        return thisComponent;
    }

    public boolean navigateToCommit(ClassType s, int commitIndex)
    {
        if(s==ClassType.SUBJECT_CLASS)
            return codeHistory3DView.showCommit(commitIndex, true);
        else
            return false;
    }

} // End of class
