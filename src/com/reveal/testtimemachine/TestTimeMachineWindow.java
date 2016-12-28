package com.reveal.testtimemachine;

import com.intellij.execution.*;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import com.intellij.ui.components.JBScrollPane;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

///////// ++ UI ++ /////////
///////// ++ UI -- /////////

public class TestTimeMachineWindow
{
    private JPanel myJComponent;
    private Project project;
    private VirtualFile[] virtualFiles = new VirtualFile[2];
    private ArrayList<CommitWrapper>[] subjectAndTestClassCommitsList = null;

    ///////// ++ CommitBar and CommitItem ++ /////////

    ///////// -- CommitBar and CommitItem -- /////////

    ///////// ++ Constant ++ /////////
    ///////// -- Constant -- /////////

    ///////// ++ UI ++ /////////
    Commits3DView leftEditor = null;
    Commits3DView rightEditor = null;
    CommitsBar leftBar=null, rightBar=null;
    JButton runBtn = null;
    JTextArea textArea = null;
    JBScrollPane logTextArea_scrolled = null;
    ///////// -- UI -- /////////

    TestTimeMachineWindow(Project project, VirtualFile[] virtualFiles, ArrayList<CommitWrapper>[] subjectAndTestClassCommitsList)
    {
        this.project = project;
        this.virtualFiles = virtualFiles;
        this.subjectAndTestClassCommitsList = subjectAndTestClassCommitsList;
        ////////////////////////////////////////////////////
        setupToolTipSetting();
        GroupLayout groupLayout = createEmptyJComponentAndReturnGroupLayout();
        ////////////
        setupUI_createBars(virtualFiles, subjectAndTestClassCommitsList);
        setupUI_createTextArea();
        setupUI_createRunButton();
        setupUI_createEditorsView(project, virtualFiles, subjectAndTestClassCommitsList);
        ////////////
        if(virtualFiles.length==1)
            setup_UILayout_Single(groupLayout);
        else// if(virtualFiles.length==2)
            setup_UILayout_Double(groupLayout);

        leftEditor.showCommit(0, false);
        if(virtualFiles.length >1)
            rightEditor.showCommit(0, false);
    }

    private void setupUI_createEditorsView(Project project, VirtualFile[] virtualFiles, ArrayList<CommitWrapper>[] subjectAndTestClassCommitsList)
    {
        leftEditor = new Commits3DView(project, virtualFiles[0], subjectAndTestClassCommitsList[0]);
        if(virtualFiles.length >1)
        {
            rightEditor = new Commits3DView(project, virtualFiles[1], subjectAndTestClassCommitsList[1]);
        }
        else
        {
            rightEditor = new Commits3DView(project, virtualFiles[0], subjectAndTestClassCommitsList[0]);
            rightEditor.setVisible(false);
        }
    }

    private void setupUI_createBars(VirtualFile[] virtualFiles, ArrayList<CommitWrapper>[] subjectAndTestClassCommitsList)
    {
        leftBar = new CommitsBar( CommitsBar.CommitItemDirection.LTR, ClassType.SUBJECT_CLASS,  subjectAndTestClassCommitsList[0], this);
        if(virtualFiles.length >1)
            rightBar = new CommitsBar(CommitsBar.CommitItemDirection.RTL, ClassType.TEST_CLASS,  subjectAndTestClassCommitsList[1], this);
        else
        {
            rightBar = new CommitsBar(CommitsBar.CommitItemDirection.RTL, ClassType.TEST_CLASS, subjectAndTestClassCommitsList[0], this); //fake
            rightBar.getComponent().setVisible(false);
        }
    }

    private void setupUI_createRunButton()
    {
        runBtn = new JButton("Run");
        runBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                RunIt();
            }
        });
    }

    private void setupUI_createTextArea()
    {
        textArea = new JTextArea("Ready.",2,3);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(false);
        //Border border = BorderFactory.createLineBorder(Color.GRAY, 1);
        logTextArea_scrolled = new JBScrollPane(textArea);
        //outputTextArea.setBorder(border);
        //logTextArea.setPreferredSize(new Dimension(500,100));
        logTextArea_scrolled.setMaximumSize(new Dimension(500,100));
    }

    private void setup_UILayout_Double(GroupLayout groupLayout)
    {
        groupLayout.setHorizontalGroup( groupLayout.createSequentialGroup()
                .addComponent(leftBar.getComponent())
                .addGroup( groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addGroup( groupLayout.createSequentialGroup()
                                .addComponent(leftEditor)
                                .addComponent(rightEditor)
                        )
                        .addGroup( groupLayout.createSequentialGroup()
                                .addComponent(runBtn)
                                .addComponent(logTextArea_scrolled)
                        )
                )
                .addComponent(rightBar.getComponent())
        );

        groupLayout.setVerticalGroup( groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                .addComponent(leftBar.getComponent())
                .addGroup(groupLayout.createSequentialGroup()
                        .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(leftEditor)
                                .addComponent(rightEditor)
                        )
                        .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(runBtn)
                                .addComponent(logTextArea_scrolled)
                        )
                )
                .addComponent(rightBar.getComponent())
        );
    }

    private void setup_UILayout_Single(GroupLayout groupLayout)
    {
        groupLayout.setHorizontalGroup( groupLayout.createSequentialGroup()
                .addComponent(leftBar.getComponent())
                .addGroup( groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(leftEditor)
                        .addGroup( groupLayout.createSequentialGroup()
                                .addComponent(runBtn)
                                .addComponent(logTextArea_scrolled)
                        )
                )
        );

        groupLayout.setVerticalGroup( groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                .addComponent(leftBar.getComponent())
                .addGroup(groupLayout.createSequentialGroup()
                        .addComponent(leftEditor)
                        .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(runBtn)
                                .addComponent(logTextArea_scrolled)
                        )
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
        myJComponent = new JPanel();
        GroupLayout groupLayout = new GroupLayout(myJComponent);
        myJComponent.setLayout(groupLayout);
        groupLayout.setAutoCreateContainerGaps(true);
        groupLayout.setAutoCreateGaps(true);
        groupLayout.setHonorsVisibility(false);

        return groupLayout;
    }

    private void RunIt()
    {
        textArea.setText("");
        RunManager runManager = RunManager.getInstance(project);
        RunManagerImpl runManagerImp = (RunManagerImpl) RunManager.getInstance(project);

        RunnerAndConfigurationSettings selectedConfiguration1 = runManager.getSelectedConfiguration();
        if(selectedConfiguration1==null)
        {
            textArea.append("FAILED: No Configuration.");
            return;
        }
        RunConfiguration runConfiguration = selectedConfiguration1.getConfiguration();
        Executor executor = DefaultRunExecutor.getRunExecutorInstance();


        ProgramRunner runner = RunnerRegistry.getInstance().getRunner(executor.getId(),runConfiguration);
        ExecutionEnvironment environment = new ExecutionEnvironment(executor, runner, selectedConfiguration1, project);

        try {
            runner.execute(environment, new ProgramRunner.Callback()
            {
                @Override
                public void processStarted(RunContentDescriptor descriptor)
                {
                    int runningStarted = 23;
                    textArea.setText("(Execution Started) \n ID:"+descriptor.getExecutionId()+" \n Display Name:"+descriptor.getDisplayName()+
                            " "+descriptor.toString());
                }
            });

        } catch (ExecutionException e1) {
            textArea.setText("(Execution Exception) "+e1.getMessage()+" \n "+ e1.toString());
            JavaExecutionUtil.showExecutionErrorMessage(e1, "We faced some compilation Error", project);
        }

    }

    public JPanel getComponent()
    {
        return myJComponent;
    }

    public boolean navigateToCommit(ClassType s, int commitIndex)
    {
        if(s==ClassType.SUBJECT_CLASS)
            return leftEditor.showCommit(commitIndex, true);
        else
            return rightEditor.showCommit(commitIndex, true);
    }

} // End of class
