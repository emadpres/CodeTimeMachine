package com.reveal.testtimemachine;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.requests.ContentDiffRequest;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.diff.SimpleContent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

        addKeyListener();
    }

    private void addKeyListener()
    {
        thisComponent.requestFocusInWindow();

        final String ZOOOM_IN_ACTION_NAME = "zoomInTimeline";
        final String ZOOOM_OUT_ACTION_NAME = "zoomOutTimeline";
        final String SHOW_DIFF_ACTION_NAME = "showDiffWindow";


        /*thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("-"), ZOOOM_OUT_ACTION_NAME);
        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('-'), ZOOOM_OUT_ACTION_NAME);
        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("_"), ZOOOM_OUT_ACTION_NAME);*/
        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), ZOOOM_OUT_ACTION_NAME);
        thisComponent.getActionMap().put(ZOOOM_OUT_ACTION_NAME, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                commitsTimelineZoomable.changeZoomFactor(-1);
            }
        });

        //thisComponent.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, KeyEvent.CTRL_DOWN_MASK), "zoomInTimeline");
        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,0), ZOOOM_IN_ACTION_NAME);
        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,0), ZOOOM_IN_ACTION_NAME);
        thisComponent.getActionMap().put(ZOOOM_IN_ACTION_NAME, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                commitsTimelineZoomable.changeZoomFactor(+1);
            }
        });

        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,0), SHOW_DIFF_ACTION_NAME);
        thisComponent.getActionMap().put(SHOW_DIFF_ACTION_NAME, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                showDiff();
            }
        });
    }

    private void showDiff()
    {
        final String latestCommitContent_str = commits.get(0).getFileContent(), activeCommitContent_str = commits.get(commitsBar.activeCommit_cIndex).getFileContent();
        //SimpleContent latestCommitContent = new SimpleContent(latestCommitContent_str);
        //SimpleContent  activeCommitContent = new SimpleContent(activeCommitContent_str);
        DocumentContent latestCommitContent = DiffContentFactory.getInstance().create(latestCommitContent_str);
        DocumentContent activeCommitContent = DiffContentFactory.getInstance().create(activeCommitContent_str);

        SimpleDiffRequest diffReqFromString = new SimpleDiffRequest("Diff Window", latestCommitContent, activeCommitContent, "Base ("+commits.get(0).getHash()+")", "Selected Commit ("+commits.get(0).getHash()+")");

        DiffManager.getInstance().showDiff(project, diffReqFromString);

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
