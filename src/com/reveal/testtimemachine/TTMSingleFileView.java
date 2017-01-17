package com.reveal.testtimemachine;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.reveal.metrics.Metrics;
import com.reveal.metrics.MetricCalculationResults;
import com.reveal.metrics.MetricCalculatorBase;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.*;

///////// ++ UI ++ /////////
///////// ++ UI -- /////////

public class TTMSingleFileView
{
    //////////////////////////////
    enum CommitsBarType {NONE, TREE, OLD};
    CommitsBarType commitsBarType = CommitsBarType.OLD;
    //////////////////////////////
    private JPanel thisComponent;
    private Project project;
    private VirtualFile virtualFile = null;
    private ArrayList<CommitWrapper> commits = null;
    private ArrayList<MetricCalculationResults> metricResults = null;
    ////////////////////////////// UI
    Commits3DView codeHistory3DView = null;
    CommitsBarBase commitsBar = null;
    CommitsTimelineZoomable commitsTimelineZoomable = null;
    //////////////////////////////
    GroupLayout groupLayout = null;
    int activeCommit_cIndex = -1;
    int INVALID = -1;
    int firstMarked_cIndex = INVALID, secondMarked_cIndex = INVALID;

    Metrics.Types currentMetricType = Metrics.Types.values()[0];

    TTMSingleFileView(Project project, VirtualFile virtualFile, ArrayList<CommitWrapper> commits)
    {
        this.project = project;
        this.virtualFile = virtualFile;
        this.commits = commits;
        /////
        metricResults = new ArrayList<>(commits.size());
        for(int i=0;i<commits.size(); i++)
            metricResults.add(new MetricCalculationResults(commits.get(i).getFileContent(), virtualFile.getName()));
        for( Metrics.Types m: Metrics.Types.values() )
            calculateMetricResults(m);
        ////////////////////////////////////////////////////
        groupLayout = createEmptyJComponentAndReturnGroupLayout();
        ////////////
        commitsBar = setupUI_createCommitsBar(virtualFile, commits);
        commitsTimelineZoomable = setupUI_createCommitsTimeline(commits);
        codeHistory3DView = setupUI_createCodeHistory3DView(project, virtualFile, commits);
        ////////////
        setupLayout();

        ///// Initialing
        activeCommit_cIndex = 0 ;
        commitsBar.updateCommitsList(this.commits);
        commitsBar.setActiveCommit_cIndex();
        //codeHistory3DView.showCommit(0, true); //It's initially at 0

        addKeyBindings();
    }

    private void calculateMetricResults(Metrics.Types m)
    {
        if(m == Metrics.Types.NONE) return;

        MetricCalculatorBase calculator = Metrics.getCalculatorForType(m);
        for(int i=0;i<metricResults.size(); i++)
                calculator.calculate(metricResults.get(i));
        int highestValue = metricResults.get(0).getMetricMaxValue(m);
    }

    private void addKeyBindings()
    {
        thisComponent.requestFocusInWindow();

        final String ZOOOM_IN_ACTION_NAME = "zoomInTimeline";
        final String ZOOOM_OUT_ACTION_NAME = "zoomOutTimeline";
        final String SHOW_DIFF_ACTION_NAME = "showDiffWindow";
        final String PREV_MONTH_ACTION_NAME = "showPrevMonthInTimeline";
        final String NEXT_MONTH_ACTION_NAME = "showNextMonthInTimeline";
        final String PREV_COMMIT_ACTION_NAME = "showPrevCommitIn3DView";
        final String NEXT_COMMIT_ACTION_NAME = "showNextCommitIn3DView";
        final String INCREASE_MAX_VISIBLE_DEPTH = "increaseMaxVisibleDepth";
        final String DECREASE_MAX_VISIBLE_DEPTH = "decreaseMaxVisibleDepth";
        final String INCREASE_RENDERER_Y_OFFSET = "increaseRendererYOffset";
        final String DECREASE_RENDERER_Y_OFFSET = "decreaseRendererYOffset";
        final String MARK_AS_FIRST = "markAsFirst";
        final String MARK_AS_SECOND = "markAsSecond";
        final String TOGGLE_CHART_TYPE = "toggleChartType";
        final String TOGGLE_COMMITS_BAR_TYPE = "toggleCommitsBarType";
        final String SHOW_ALL_FILES = "showAllFiles";
        final String SHOW_CHANGED_FILES = "showChangedFiles";




        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_B,0), MARK_AS_FIRST);
        thisComponent.getActionMap().put(MARK_AS_FIRST, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int newPlace = codeHistory3DView.currentMouseHoveredIndex;

                if( newPlace == codeHistory3DView.INVALID )
                {
                    if(firstMarked_cIndex != INVALID)
                        codeHistory3DView.setTopBarHighlight(firstMarked_cIndex, false, Color.WHITE);
                    firstMarked_cIndex = INVALID;
                }
                else
                {
                    if( newPlace == secondMarked_cIndex) //we know "secondMarked_cIndex" also can't be INVALID here
                    {
                        codeHistory3DView.setTopBarHighlight(secondMarked_cIndex, false, Color.WHITE);
                        secondMarked_cIndex = INVALID;
                    }
                    if(firstMarked_cIndex!=INVALID)
                    {
                        codeHistory3DView.setTopBarHighlight(firstMarked_cIndex, false, Color.WHITE);
                    }

                    firstMarked_cIndex = newPlace;
                    codeHistory3DView.setTopBarHighlight(newPlace, true, Color.GREEN);
                }
            }
        });

        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_N,0), MARK_AS_SECOND);
        thisComponent.getActionMap().put(MARK_AS_SECOND, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {

                int newPlace = codeHistory3DView.currentMouseHoveredIndex;

                if( newPlace == codeHistory3DView.INVALID )
                {
                    if(secondMarked_cIndex != INVALID)
                        codeHistory3DView.setTopBarHighlight(secondMarked_cIndex, false, Color.WHITE);
                    secondMarked_cIndex = INVALID;
                }
                else
                {
                    if( newPlace == firstMarked_cIndex) //we know "firstMarked_cIndex" also can't be INVALID here
                    {
                        codeHistory3DView.setTopBarHighlight(firstMarked_cIndex, false, Color.WHITE);
                        firstMarked_cIndex = INVALID;
                    }
                    if(secondMarked_cIndex!=INVALID)
                    {
                        codeHistory3DView.setTopBarHighlight(secondMarked_cIndex, false, Color.WHITE);
                    }

                    secondMarked_cIndex = newPlace;
                    codeHistory3DView.setTopBarHighlight(newPlace, true, Color.CYAN);
                }
            }
        });

        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,0), SHOW_DIFF_ACTION_NAME);
        thisComponent.getActionMap().put(SHOW_DIFF_ACTION_NAME, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(firstMarked_cIndex == INVALID && secondMarked_cIndex==INVALID)
                    return;
                else if(firstMarked_cIndex != INVALID && secondMarked_cIndex!=INVALID)
                {
                    if(firstMarked_cIndex > secondMarked_cIndex)
                        showDiff(firstMarked_cIndex, secondMarked_cIndex);
                    else
                        showDiff(secondMarked_cIndex, firstMarked_cIndex);
                }
                else
                {
                    if(firstMarked_cIndex==INVALID)
                        showDiff(secondMarked_cIndex);
                    else
                        showDiff(firstMarked_cIndex);
                }
            }
        });

        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,0), SHOW_CHANGED_FILES);
        thisComponent.getActionMap().put(SHOW_CHANGED_FILES, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final String DIALOG_TITLE = "Other changes files";
                if(commits.get(activeCommit_cIndex).isFake())
                {
                    Messages.showInfoMessage(project, "Please select a valid commit ID.", DIALOG_TITLE);
                    return;
                }

                String commitId = commits.get(activeCommit_cIndex).getCommitID();
                GitHelper instance = GitHelper.getInstance(project);
                String allFiles = instance.getListOfChangedFile(commitId);
                Messages.showInfoMessage(project, allFiles, DIALOG_TITLE);
            }
        });

        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD,0), SHOW_ALL_FILES);
        thisComponent.getActionMap().put(SHOW_ALL_FILES, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final String DIALOG_TITLE = "All files at this time";
                if(commits.get(activeCommit_cIndex).isFake())
                {
                    Messages.showInfoMessage(project, "Please select a valid commit ID.", DIALOG_TITLE);
                    return;
                }

                String commitId = commits.get(activeCommit_cIndex).getCommitID();
                GitHelper instance = GitHelper.getInstance(project);
                String allFiles = instance.getListOfAllFile(commitId);
                Messages.showInfoMessage(project, allFiles, DIALOG_TITLE);
            }
        });



        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,0), ZOOOM_IN_ACTION_NAME);
        thisComponent.getActionMap().put(ZOOOM_IN_ACTION_NAME, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                commitsTimelineZoomable.changeZoomFactor(+1);
            }
        });


        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), ZOOOM_OUT_ACTION_NAME);
        thisComponent.getActionMap().put(ZOOOM_OUT_ACTION_NAME, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                commitsTimelineZoomable.changeZoomFactor(-1);
            }
        });


        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_A,0), PREV_MONTH_ACTION_NAME);
        thisComponent.getActionMap().put(PREV_MONTH_ACTION_NAME, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                commitsTimelineZoomable.moveActiveRange(-1);
            }
        });

        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_D,0), NEXT_MONTH_ACTION_NAME);
        thisComponent.getActionMap().put(NEXT_MONTH_ACTION_NAME, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                commitsTimelineZoomable.moveActiveRange(+1);
            }
        });

        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W,0), PREV_COMMIT_ACTION_NAME);
        thisComponent.getActionMap().put(PREV_COMMIT_ACTION_NAME, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(activeCommit_cIndex+1 >= commits.size()) return;
                activeCommit_cIndex++;
                navigateToCommit(ClassType.SUBJECT_CLASS, activeCommit_cIndex);
                commitsBar.setActiveCommit_cIndex();
            }
        });

        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S,0), NEXT_COMMIT_ACTION_NAME);
        thisComponent.getActionMap().put(NEXT_COMMIT_ACTION_NAME, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(activeCommit_cIndex-1 < 0) return;
                activeCommit_cIndex--;
                navigateToCommit(ClassType.SUBJECT_CLASS, activeCommit_cIndex);
                commitsBar.setActiveCommit_cIndex();
            }
        });

        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_I,0), INCREASE_MAX_VISIBLE_DEPTH);
        thisComponent.getActionMap().put(INCREASE_MAX_VISIBLE_DEPTH, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                codeHistory3DView.increaseMaxVisibleDepth();
            }
        });

        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_K,0), DECREASE_MAX_VISIBLE_DEPTH);
        thisComponent.getActionMap().put(DECREASE_MAX_VISIBLE_DEPTH, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                codeHistory3DView.decreaseMaxVisibleDepth();
            }
        });

        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_O,0), INCREASE_RENDERER_Y_OFFSET);
        thisComponent.getActionMap().put(INCREASE_RENDERER_Y_OFFSET, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                MyRenderer.getInstance().Y_OFFSET_FACTOR += 10;
                codeHistory3DView.render();
            }
        });

        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_L,0), DECREASE_RENDERER_Y_OFFSET);
        thisComponent.getActionMap().put(DECREASE_RENDERER_Y_OFFSET, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                MyRenderer.getInstance().Y_OFFSET_FACTOR -= 10;
                codeHistory3DView.render();
            }
        });

        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_C,0), TOGGLE_CHART_TYPE);
        thisComponent.getActionMap().put(TOGGLE_CHART_TYPE, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Metrics.Types[] allMetrics = Metrics.Types.values();
                int nextMetricIndex = (currentMetricType.ordinal()+1)%allMetrics.length;
                currentMetricType = allMetrics[nextMetricIndex];
                codeHistory3DView.setMetricCalculator(currentMetricType);
            }
        });

        thisComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F,0), TOGGLE_COMMITS_BAR_TYPE);
        thisComponent.getActionMap().put(TOGGLE_COMMITS_BAR_TYPE, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                switch (commitsBarType)
                {
                    case OLD:
                        commitsBarType = CommitsBarType.TREE;
                        break;
                    case TREE:
                    default:
                        commitsBarType = CommitsBarType.OLD;
                        break;
                }
                changeCommitsBarType(commitsBarType);
            }
        });

    }

    private void showDiff(int first_cIndex, int second_cIndex)
    {
        String firstCommitContent_str = commits.get(first_cIndex).getFileContent();
        String secondCommitContent_str = commits.get(second_cIndex).getFileContent();
        DocumentContent firstCommitContent = DiffContentFactory.getInstance().create(firstCommitContent_str);
        DocumentContent secondCommitContent = DiffContentFactory.getInstance().create(secondCommitContent_str);

        SimpleDiffRequest diffReqFromString = new SimpleDiffRequest("Diff Window", firstCommitContent, secondCommitContent, "First: "+commits.get(first_cIndex).getCommitID()+" | "+commits.get(first_cIndex).getCommitMessage(), "Second: "+commits.get(second_cIndex).getCommitID()+" | "+commits.get(second_cIndex).getCommitMessage());

        DiffManager.getInstance().showDiff(project, diffReqFromString);
    }

    private void showDiff(int cIndex)
    {
        int latestsCommittedRevision = -1;
        for( int i=0; i<commits.size(); i++)
            if(!commits.get(i).isFake())
            {
                latestsCommittedRevision = i;
                break;
            }
        if(latestsCommittedRevision==-1) return;
        ///////////

        showDiff(latestsCommittedRevision, cIndex);
    }

    private Commits3DView setupUI_createCodeHistory3DView(Project project, VirtualFile virtualFiles, ArrayList<CommitWrapper> commits)
    {
         return new Commits3DView(project, virtualFiles, commits, metricResults, this);
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
        return commitsBar;
    }

    private void changeCommitsBarType(CommitsBarType newType)
    {
        CommitsBarBase newCommitsBar = null;

        switch (commitsBarType)
        {
            case OLD:
                newCommitsBar = new CommitsBar(CommitsBar.CommitItemDirection.LTR, ClassType.SUBJECT_CLASS, this);
                break;
            case TREE:
            default:
                newCommitsBar = new CommitsBarTreeView(this);
                break;
        }

        groupLayout.replace(commitsBar.getComponent(), newCommitsBar.getComponent());
        commitsBar = newCommitsBar;

        commitsTimelineZoomable.t.updateCommitsBarWithActiveRange();
        commitsBar.setActiveCommit_cIndex();

    }

    private CommitsTimelineZoomable setupUI_createCommitsTimeline(ArrayList<CommitWrapper> commitsList)
    {
        return new CommitsTimelineZoomable(commitsList, this);
    }

    private void setupLayout()
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

    public void updateCommits3DViewActiveRangeOnTimeLine(int topLayer_cIndex)
    {
        commitsTimelineZoomable.updateCommits3DViewActiveRange(topLayer_cIndex);
    }

    public void navigateToCommit(ClassType s, int commitcIndex)
    {
        if(s==ClassType.SUBJECT_CLASS)
            codeHistory3DView.showCommit(commitcIndex, true);
    }

} // End of class
