package com.reveal.testtimemachine;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;


import javax.swing.*;
import java.util.*;

///////// ++ UI ++ /////////
///////// ++ UI -- /////////

public class TTMSingleFileView
{
    enum CommitsBarType {NONE, TREE, OLD};
    final CommitsBarType commitsBarType = CommitsBarType.OLD;
    //////////////////////////////
    private JPanel thisComponent;
    private Project project;
    private VirtualFile virtualFile = null;
    private ArrayList<CommitWrapper> commits = null;
    ////////////////////////////// UI
    Commits3DView codeHistory3DView = null;
    CommitsBarBase commitsBar = null;
    CommitsTimelineZoomable commitsTimelineZoomable = null;
    //////////////////////////////

    TTMSingleFileView(Project project, VirtualFile virtualFile, ArrayList<CommitWrapper> commits)
    {
        this.project = project;
        this.virtualFile = virtualFile;
        this.commits = commits;
        ////////////////////////////////////////////////////
        GroupLayout groupLayout = createEmptyJComponentAndReturnGroupLayout();
        ////////////
        commitsBar = setupUI_createCommitsBar(virtualFile, commits);
        commitsTimelineZoomable = setupUI_createCommitsTimeline(commits);
        codeHistory3DView = setupUI_createCodeHistory3DView(project, virtualFile, commits);
        ////////////
        setupLayout(groupLayout);

        codeHistory3DView.showCommit(0, false);
    }

    private Commits3DView setupUI_createCodeHistory3DView(Project project, VirtualFile virtualFiles, ArrayList<CommitWrapper> commits)
    {
         return new Commits3DView(project, virtualFiles, commits);
    }

    private CommitsBarBase setupUI_createCommitsBar(VirtualFile virtualFiles, ArrayList<CommitWrapper> commitsList)
    {
        CommitsBarBase commitsBar = null;
        switch (commitsBarType)
        {
            case OLD:
                commitsBar = new CommitsBar(CommitsBar.CommitItemDirection.LTR, ClassType.SUBJECT_CLASS, this);
                break;
            case TREE:
            default:
                commitsBar = new CommitsBarTreeView(this);
                break;
        }
        commitsBar.updateCommitsList(commitsList);
        commitsBar.setActiveCommit_cIndex(0);
        return commitsBar;
    }

    private CommitsTimelineZoomable setupUI_createCommitsTimeline(ArrayList<CommitWrapper> commitsList)
    {
        return new CommitsTimelineZoomable(commitsList, this);
    }

    private void setupLayout(GroupLayout groupLayout)
    {
        groupLayout.setHorizontalGroup( groupLayout.createSequentialGroup()
                .addComponent(commitsBar.getComponent())
                .addGroup( groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(codeHistory3DView)
                        .addComponent(commitsTimelineZoomable)
                )
        );

        groupLayout.setVerticalGroup( groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(commitsBar.getComponent())
                .addGroup(groupLayout.createSequentialGroup()
                        .addComponent(commitsTimelineZoomable)
                        .addComponent(codeHistory3DView)
                )
        );
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

    public void updateCommitsBar(Date activeRange_startDate, Date activeRange_endDate)
    {
        ArrayList<CommitWrapper> commitsForRequestedRange = new ArrayList<>();
        for(int i=0; i<commits.size(); i++)
        {
            Date currentCommitDate = commits.get(i).getDate();
            if(!currentCommitDate.before(activeRange_startDate) && !currentCommitDate.after(activeRange_endDate))
                commitsForRequestedRange.add(commits.get(i));
        }
        //////////
        commitsBar.updateCommitsList(commitsForRequestedRange);
    }

    public boolean navigateToCommit(ClassType s, int commitIndex)
    {
        if(s==ClassType.SUBJECT_CLASS)
            return codeHistory3DView.showCommit(commitIndex, true);
        else
            return false;
    }

} // End of class
