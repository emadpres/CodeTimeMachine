package com.reveal.testtimemachine;

import com.intellij.execution.*;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

///////// ++ UI ++ /////////
///////// ++ UI -- /////////

public class TestTimeMachineWindow
{
    private JPanel myJComponent;
    private Project project;
    private VirtualFile[] virtualFiles = new VirtualFile[2];
    //private ArrayList<List<VcsFileRevision>> fileRevisionsLists = new ArrayList<List<VcsFileRevision>>();
    private ArrayList<CommitWrapper>[] subjectAndTestClassCommitsList = null;

    ///////// ++ CommitBar and CommitItem ++ /////////
    private enum CommitItemDirection {NONE, LTR, RTL};
    private enum SubjectOrTest {NONE, SUBJECT, TEST};
    private enum CommitItemInfoType {NONE, DATE, TIME}
    ///////// -- CommitBar and CommitItem -- /////////

    ///////// ++ Constant ++ /////////
    final boolean DEBUG_MODE_UI = false;
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
        leftBar = new CommitsBar(CommitItemDirection.LTR, SubjectOrTest.SUBJECT,  subjectAndTestClassCommitsList[0], this);
        if(virtualFiles.length >1)
            rightBar = new CommitsBar(CommitItemDirection.RTL, SubjectOrTest.TEST,  subjectAndTestClassCommitsList[1], this);
        else
        {
            rightBar = new CommitsBar(CommitItemDirection.RTL, SubjectOrTest.TEST, subjectAndTestClassCommitsList[0], this); //fake
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

    private boolean navigateToCommit(SubjectOrTest s, int commitIndex)
    {
        if(s==SubjectOrTest.SUBJECT)
            return leftEditor.showCommit(commitIndex, true);
        else
            return rightEditor.showCommit(commitIndex, true);
    }

    private class CommitsBar
    {
        ///////// ++ UI ++ /////////
        private TestTimeMachineWindow TTMWindow;
        private JPanel myComponent;
        private JBScrollPane scroll;
        private CommitItem[] commitItems /* Most recent commit at 0*/;
        ///////// ++ UI -- /////////

        int H=0;
        private SubjectOrTest s = SubjectOrTest.NONE;
        private int activeCommitIndex = -1;

        public CommitsBar(CommitItemDirection direction, SubjectOrTest s, ArrayList<CommitWrapper> commitList,
                          TestTimeMachineWindow TTMWindow)
        {
            this.TTMWindow = TTMWindow;
            this.s= s;

            createEmptyJComponent();
            creatingCommitsItem(direction, commitList);

            setupScroll();

            myComponent.repaint();

        }

        private void setupScroll()
        {
            scroll = new JBScrollPane();
            scroll.setViewportView(myComponent);
            scroll.setBorder(null);

            // BoxLayout cannot handle different alignments: see http://download.oracle.com/javase/tutorial/uiswing/layout/box.html
            scroll.setMaximumSize(new Dimension(commitItems[0].getComponent().getSize().width+10, H+10));
            JScrollBar vertical = scroll.getVerticalScrollBar();
            vertical.setValue( vertical.getMaximum() ); // TODO : Doesn't work correctly. maybe we should call after resize

            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        }

        private void creatingCommitsItem(CommitItemDirection direction, ArrayList<CommitWrapper> commitList)
        {
            // commitList: Most recent commit at 0
            // commitItems: Most recent commit at 0

            Calendar currentCommitCal = Calendar.getInstance();
            Calendar lastCommitCal = Calendar.getInstance();
            lastCommitCal.setTime(new Date(Long.MIN_VALUE));

            // UI_items includes CommitItem and other fake UI elements
            ArrayList<JComponent> UI_items = new ArrayList<>(commitList.size()/*reserve at least this much memort*/);

            commitItems = new CommitItem[commitList.size()];

            for(int i=0; i<commitList.size() ; i++)
            {
                currentCommitCal.setTime(commitList.get(i).getDate());
                boolean sameDay = lastCommitCal.get(Calendar.YEAR) == currentCommitCal.get(Calendar.YEAR) &&
                        lastCommitCal.get(Calendar.DAY_OF_YEAR) == currentCommitCal.get(Calendar.DAY_OF_YEAR);
                /////

                if(!sameDay)
                {
                    if(i==0)
                    {
                        //TODO
                    }
                    else
                    {
                        NewDayItem newDayMarker = new NewDayItem(direction, commitList.get(i-1).getDate());
                        UI_items.add(newDayMarker.getComponent());
                    }

                }

                commitItems[i]= new CommitItem(direction, i, commitList.get(i), this, CommitItemInfoType.TIME);

                UI_items.add(commitItems[i].getComponent());
                ///
                lastCommitCal.setTime(commitList.get(i).getDate());
            }

            NewDayItem newDayMarker = new NewDayItem(direction, commitList.get(commitList.size()-1).getDate());
            UI_items.add(newDayMarker.getComponent());

            for(int i=UI_items.size()-1; i>=0 ; i--)
            {
                myComponent.add(UI_items.get(i));

                final int GAP_H = 10;
                myComponent.add(Box.createRigidArea(new Dimension(1, GAP_H)));
                H += UI_items.get(i).getSize().height + GAP_H;

            }
            activeCommitIndex = 0;
            commitItems[0].setActivated(true);

        }

        private void createEmptyJComponent()
        {
            // Size of this component according to children's components = CommitItem
            myComponent = new JPanel();
            BoxLayout boxLayout = new BoxLayout(myComponent, BoxLayout.PAGE_AXIS);
            myComponent.setLayout(boxLayout);

            if(DEBUG_MODE_UI)
                myComponent.setBackground(Color.RED);
        }


        private void activateCommit(int newCommitIndex)
        {
            boolean possible = TTMWindow.navigateToCommit(s, newCommitIndex);
            if(!possible) return;

            if(activeCommitIndex!=-1)
                commitItems[activeCommitIndex].setActivated(false);
            activeCommitIndex = newCommitIndex;
            commitItems[activeCommitIndex].setActivated(true);

        }

        public JComponent getComponent()
        {
            return scroll;
        }

        private class NewDayItem
        {
            ///////// ++ Constant ++ /////////
            private final Dimension COMPONENT_SIZE = new Dimension( 170,20 );
            private final Dimension MARKERT_NORMAL_SIZE = new Dimension( 10,5 );//
            private final Color NORMAL_COLOR = Color.DARK_GRAY;
            ///////// ++ Constant -- /////////

            ///////// ++ UI ++ /////////
            private JPanel myComponent;
            private JLabel marker, commitInfo;
            ///////// ++ UI -- /////////

            private CommitItemDirection direction;
            private Date date;

            public NewDayItem(CommitItemDirection direction, Date date)
            {
                this.direction = direction;
                this.date = date;

                createEmptyJComponent();

                if(direction == CommitItemDirection.LTR)
                    myComponent.setAlignmentX(Component.LEFT_ALIGNMENT); // make it left_align within parent layout (Hbox)
                else
                    myComponent.setAlignmentX(Component.RIGHT_ALIGNMENT);


                if(DEBUG_MODE_UI)
                    myComponent.setBackground(Color.YELLOW);


                setupUI_marker();
                setupUI_commitInfo();

                updateUIToNewSize();
                setupComponentResizingBehaviour();
            }

            private void setupUI_marker()
            {
                marker = new JLabel("");
                marker.setOpaque(true);
                myComponent.add(marker);
            }

            private void setupUI_commitInfo()
            {
                String commitInfoStr = "";

                Calendar cal = Calendar.getInstance();
                cal.setTime(date);


                long tillCommit = date.getTime();
                long tillToday = new Date().getTime();
                long daysTillCommit = tillCommit  / (24 * 60 * 60 * 1000);
                long daysTillToday = tillToday / (24 * 60 * 60 * 1000); // TODO: BUG: when we are between 12:00am to 1:00am

                if(daysTillToday - daysTillCommit == 0)
                {
                    commitInfoStr = "Today";
                }
                else if(daysTillToday - daysTillCommit == 1)
                {
                    commitInfoStr = "Yesterday";
                }
                else
                {
                    // Month
                    int mInt = cal.get(Calendar.MONTH);
                    String mStr = getMonthName(mInt);
                    commitInfoStr = mStr;
                    // Day
                    commitInfoStr += " "+cal.get(Calendar.DAY_OF_MONTH);
                    // Year
                    commitInfoStr += " "+ cal.get(Calendar.YEAR);
                }


                commitInfo = new JLabel(commitInfoStr);
                commitInfo.setSize(30,10); //updated in ComponentSizeChanged
                if(DEBUG_MODE_UI)
                    commitInfo.setBackground(Color.CYAN);
                commitInfo.setOpaque(true);
                Font font = commitInfo.getFont();
                Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
                commitInfo.setFont(boldFont);
                if(direction == CommitItemDirection.LTR)
                    commitInfo.setHorizontalAlignment(SwingConstants.LEFT);
                else
                    commitInfo.setHorizontalAlignment(SwingConstants.RIGHT);
                commitInfo.setSize(myComponent.getSize().width-30,10);
                myComponent.add(commitInfo);
            }

            private void createEmptyJComponent()
            {
                myComponent = new JPanel(null);
                myComponent.setSize(COMPONENT_SIZE);
                myComponent.setPreferredSize(COMPONENT_SIZE);
                myComponent.setMinimumSize(COMPONENT_SIZE);
                myComponent.setMaximumSize(COMPONENT_SIZE);
            }

            private String getMonthName(int month){
                //String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
                String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                return monthNames[month];
            }

            private void updateMarkerLocation() //TODO: Move this duplicate to Base class
            {
                if(direction== CommitItemDirection.LTR)
                    marker.setLocation( 0/*Align Left*/,
                            myComponent.getSize().height/2 - marker.getSize().height/2);
                else
                {
                    marker.setLocation( myComponent.getSize().width - marker.getSize().width/*Align Right*/,
                            myComponent.getSize().height / 2 - marker.getSize().height / 2);
                }
            }

            private void updateCommitInfoLocation() //TODO: Move this duplicate to Base class
            {
                final int DELTA_DIS_FROM_MARKER = 3;
                if(direction== CommitItemDirection.LTR)
                    commitInfo.setLocation( marker.getLocation().x+marker.getSize().width+DELTA_DIS_FROM_MARKER,
                            marker.getLocation().y+marker.getSize().height/2-commitInfo.getSize().height/2);
                else
                {
                    commitInfo.setLocation( marker.getLocation().x - DELTA_DIS_FROM_MARKER - commitInfo.getSize().width,
                            marker.getLocation().y + marker.getSize().height / 2 - commitInfo.getSize().height / 2);
                }
            }

            private void updateUIToNewSize()
            {
                marker.setSize(MARKERT_NORMAL_SIZE);
                marker.setBackground(NORMAL_COLOR);

                updateMarkerLocation();

                commitInfo.setForeground(NORMAL_COLOR);
                updateCommitInfoLocation();
            }

            private void setupComponentResizingBehaviour() //TODO: Move this duplicate to Base class
            {
                myComponent.addComponentListener(new ComponentListener()
                {
                    @Override
                    public void componentResized(ComponentEvent e)
                    {
                    /*
                    Since myComponent may change (since it belongs to a layout AND we didn't limit the maximum size),
                    we need to reaarange objects when size chnages.
                    if myComponent had layout (for its children) we wouldn't manage its children after each size change.
                     */
                        int sd=0;
                        sd++;
                        updateUIToNewSize();
                    }

                    @Override
                    public void componentMoved(ComponentEvent e)
                    {
                        int sd=0;
                        sd++;
                    }

                    @Override
                    public void componentShown(ComponentEvent e)
                    {
                        int sd=0;
                        sd++;
                    }

                    @Override
                    public void componentHidden(ComponentEvent e)
                    {
                        int sd=0;
                        sd++;
                    }
                });
            }

            public JPanel getComponent()
            {
                return myComponent;
            }
        }

        /*private class DateTimeUtil
        {
            static private DateTimeUtil instance = null;
            static public getInstance()
            {
                if(instance==null)
                    instance = new DateTimeUtil();
                return instance;
            }

            private DateTimeUtil()
            {

            }
        }*/

        private class CommitItem
        {
            ///////// ++ Constant ++ /////////
            private final Dimension COMPONENT_SIZE = new Dimension( 170,20 );
            private final int LONG_FACTOR = 5;
            private final Dimension MARKERT_NORMAL_SIZE = new Dimension( 17,5 );
            private final Dimension MARKER_HOVERED_SIZE = new Dimension( 20,8 );
            private final Dimension MARKERT_NORMAL_SIZE_LONG = new Dimension( 10+LONG_FACTOR,5 );
            private final Dimension MARKER_HOVERED_SIZE_LONG = new Dimension( 15+LONG_FACTOR,8 );
            //
            private final Color NORMAL_COLOR = Color.LIGHT_GRAY;
            private final Color NORMAL_COLOR_LONG = Color.GRAY; //TODO : LONG ==> BOLD_COMMITS
            private final Color HOVERED_COLOR = new Color(255,0,0,150);
            ///////// ++ Constant -- /////////

            ///////// ++ UI ++ /////////
            private JPanel myComponent;
            private JLabel marker, commitInfo;
            ///////// ++ UI -- /////////


            private int commitIndex=-1;
            private CommitItemDirection direction;
            CommitItemInfoType infoType;

            private boolean isActive=false;
            private CommitsBar commitsBar=null;



            public CommitItem(CommitItemDirection direction, int commitIndex, CommitWrapper commitWrapper, CommitsBar commitBar, CommitItemInfoType infoType)
            {
                this.commitsBar = commitBar;
                this.direction = direction;
                this.commitIndex = commitIndex;
                this.infoType = infoType;

                setupUI(commitWrapper, infoType);

                setupMouseBeahaviour();
                setupComponentResizingBehaviour();
            }

            private void setupUI(CommitWrapper commitWrapper, CommitItemInfoType infoType)
            {
                createEmptyJComponent();
                if(direction == CommitItemDirection.LTR)
                    myComponent.setAlignmentX(Component.LEFT_ALIGNMENT); // make it left_align within parent layout (Hbox)
                else
                    myComponent.setAlignmentX(Component.RIGHT_ALIGNMENT);

                myComponent.setToolTipText(commitWrapper.getCommitMessage());

                if(DEBUG_MODE_UI)
                    myComponent.setBackground(Color.GREEN);

                setupUI_marker();
                setupUI_commitInfo(commitWrapper, infoType);

                updateToNormalUI();
            }

            private void setupComponentResizingBehaviour()
            {
                myComponent.addComponentListener(new ComponentListener()
                {
                    @Override
                    public void componentResized(ComponentEvent e)
                    {
                    /*
                    Since myComponent may change (since it belongs to a layout AND we didn't limit the maximum size),
                    we need to reaarange objects when size chnages.
                    if myComponent had layout (for its children) we wouldn't manage its children after each size change.
                     */
                        int sd=0;
                        sd++;
                        updateToNormalUI();
                    }

                    @Override
                    public void componentMoved(ComponentEvent e)
                    {
                        int sd=0;
                        sd++;
                    }

                    @Override
                    public void componentShown(ComponentEvent e)
                    {
                        int sd=0;
                        sd++;
                    }

                    @Override
                    public void componentHidden(ComponentEvent e)
                    {
                        int sd=0;
                        sd++;
                    }
                });
            }

            private void setupUI_marker()
            {
                marker = new JLabel("");
                marker.setOpaque(true);
                myComponent.add(marker);
            }

            private void setupUI_commitInfo(CommitWrapper commitWrapper, CommitItemInfoType infoType)
            {
                String commitInfoStr = "";

                if(commitWrapper.isFake())
                    commitInfoStr = "Uncommited";
                else
                {
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm"); //"yyyy-MM-dd HH:mm:ss.SSS"
                    commitInfoStr = format.format(commitWrapper.getDate());
                }


                commitInfo = new JLabel(commitInfoStr);
                commitInfo.setSize(30,10); //updated in ComponentSizeChanged
                if(DEBUG_MODE_UI)
                    commitInfo.setBackground(Color.CYAN);
                commitInfo.setOpaque(true);
                Font font = commitInfo.getFont();
                Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
                commitInfo.setFont(boldFont);
                if(direction == CommitItemDirection.LTR)
                    commitInfo.setHorizontalAlignment(SwingConstants.LEFT);
                else
                    commitInfo.setHorizontalAlignment(SwingConstants.RIGHT);
                commitInfo.setSize(myComponent.getSize().width-30,10);
                myComponent.add(commitInfo);
            }

            private void setupMouseBeahaviour()
            {
                myComponent.addMouseListener(new MouseListener()
                {
                    @Override
                    public void mouseClicked(MouseEvent e) {}

                    @Override
                    public void mousePressed(MouseEvent e) {}

                    @Override
                    public void mouseReleased(MouseEvent e)
                    {
                        commitsBar.activateCommit(commitIndex);
                    }

                    @Override
                    public void mouseEntered(MouseEvent e)
                    {
                        if(!isActive)
                            updateToActiveUI();
                    }

                    @Override
                    public void mouseExited(MouseEvent e)
                    {
                        if(!isActive)
                            updateToNormalUI();
                    }
                });
            }

            private void createEmptyJComponent()
            {
                myComponent = new JPanel(null);
                myComponent.setSize(COMPONENT_SIZE);
                myComponent.setPreferredSize(COMPONENT_SIZE);
                myComponent.setMinimumSize(COMPONENT_SIZE);
                myComponent.setMaximumSize(COMPONENT_SIZE);
            }

            private void updateToNormalUI()
            {
                if(infoType==CommitItemInfoType.DATE)
                {
                    marker.setSize(MARKERT_NORMAL_SIZE_LONG);
                    marker.setBackground(NORMAL_COLOR_LONG);
                }
                else
                {
                    marker.setSize(MARKERT_NORMAL_SIZE);
                    marker.setBackground(NORMAL_COLOR);
                }

                updateMarkerLocation();
                if(infoType==CommitItemInfoType.DATE)
                    commitInfo.setForeground(NORMAL_COLOR_LONG);
                else
                    commitInfo.setForeground(NORMAL_COLOR);
                updateCommitInfoLocation();
            }

            private void updateToActiveUI()
            {
                if(infoType==CommitItemInfoType.DATE)
                    marker.setSize(MARKER_HOVERED_SIZE_LONG);
                else
                    marker.setSize(MARKER_HOVERED_SIZE);
                marker.setBackground(HOVERED_COLOR);
                updateMarkerLocation();

                updateCommitInfoLocation();
                commitInfo.setForeground(HOVERED_COLOR);
            }

            private void updateMarkerLocation()
            {
                if(direction== CommitItemDirection.LTR)
                    marker.setLocation( 0/*Align Left*/,
                            myComponent.getSize().height/2 - marker.getSize().height/2);
                else
                {
                    marker.setLocation( myComponent.getSize().width - marker.getSize().width/*Align Right*/,
                            myComponent.getSize().height / 2 - marker.getSize().height / 2);
                }
            }

            private void updateCommitInfoLocation()
            {
                final int DELTA_DIS_FROM_MARKER = 3;
                if(direction== CommitItemDirection.LTR)
                    commitInfo.setLocation( marker.getLocation().x+marker.getSize().width+DELTA_DIS_FROM_MARKER,
                            marker.getLocation().y+marker.getSize().height/2-commitInfo.getSize().height/2);
                else
                {
                    commitInfo.setLocation( marker.getLocation().x - DELTA_DIS_FROM_MARKER - commitInfo.getSize().width,
                            marker.getLocation().y + marker.getSize().height / 2 - commitInfo.getSize().height / 2);
                }
            }

            private void setActivated(boolean newStatus)
            {
                isActive = newStatus;
                if(isActive)
                    updateToActiveUI();
                else
                    updateToNormalUI();
            }

            public JPanel getComponent()
            {
                return myComponent;
            }
        }
    }

    protected class Commits3DView extends JComponent implements ComponentListener
    {
        ///////// ++ Constant ++ /////////
        ///////// -- Constant -- /////////

        ///////// ++ UI ++ /////////
        CustomEditorTextField mainEditorWindow;
        Point centerOfComponent;
        ///////// -- UI -- /////////

        ///////// ++ UI: 3D Stuff ++ /////////
        final boolean COLORFUL = false;
        final int TICK_INTERVAL_MS = 50;
        final float LAYER_DISTANCE = 0.2f;
        final int MAX_VISIBLE_VIRTUAL_WINDOW = 10;
        //////
        boolean onChangingCommitProcess = false;
        final int TOP_BAR_HEIGHT = 25;
        int topLayerIndex=0, targetLayerIndex=0 /*if equals to topLayerIndex it means no animation is running*/;
        float topLayerOffset;
        VirtualEditorWindow[] virtualEditorWindows = null;
        Timer playing3DAnimationTimer;
        int numberOfPassingLayersPerSec_forAnimation = 1;
        //
        Dimension topLayerDimention = new Dimension(0,0);
        Point topLayerCenterPos = new Point(0,0);
        ///////// -- UI: 3D Stuff -- /////////

        Project project;
        VirtualFile virtualFile;
        List<CommitWrapper> commitList = null;


        public Commits3DView( Project project, VirtualFile virtualFile, List<CommitWrapper> commitList)
        {
            super();

            this.project = project;
            this.virtualFile = virtualFile;
            this.commitList = commitList;

            this.setLayout(null);
            this.addComponentListener(this);
            if (DEBUG_MODE_UI)
                this.setBackground(Color.ORANGE);
            this.setOpaque(true);


            mainEditorWindow = new CustomEditorTextField(FileDocumentManager.getInstance().getDocument(virtualFile), project, FileTypeRegistry.getInstance().getFileTypeByExtension("java"),true,false);
            mainEditorWindow.setEnabled(true);
            mainEditorWindow.setRequestFocusEnabled(true);
            mainEditorWindow.setOneLineMode(false);
            add(mainEditorWindow); // we setBound in ComponentResized() event


            setup3DAnimationStuff();

            componentResized(null);
        }

        private void setup3DAnimationStuff()
        {

            virtualEditorWindows = new VirtualEditorWindow[commitList.size()];

            for (int i = 0; i< commitList.size() ; i++)
            {
                virtualEditorWindows[i] = new VirtualEditorWindow(i, commitList.get(i));
            }

            setVirtualWindowsDefaultValues();
            placeVirtualWindowsInStandardPosition();


            playing3DAnimationTimer = new Timer(TICK_INTERVAL_MS, new ActionListener(){
                public void actionPerformed(ActionEvent e)
                {
                    updateVirtualWindowsInfo(TICK_INTERVAL_MS/1000.f);
                    repaint();
                }
            });
        }

        private void setVirtualWindowsDefaultValues()
        {
            for (int i = 0; i< commitList.size() ; i++)
            {
                int xCenter, yCenter, w, h;
                w = topLayerDimention.width;
                h = topLayerDimention.height;
                xCenter = topLayerCenterPos.x;
                yCenter = topLayerCenterPos.y;
                virtualEditorWindows[i].setDefaultValues(xCenter, yCenter, w, h);
            }
        }

        private void placeVirtualWindowsInStandardPosition()
        {
            topLayerOffset = 0;
            topLayerIndex=0;
            // Don't forget to call `setVirtualWindowsDefaultValues()` before
            for (int i = 0; i< commitList.size() ; i++)
            {
                virtualEditorWindows[i].updateDepth(i * LAYER_DISTANCE);
                if(i>topLayerIndex + MAX_VISIBLE_VIRTUAL_WINDOW )
                    virtualEditorWindows[i].isVisible = false;
            }
            virtualEditorWindows[topLayerIndex].highlightTopLayer();
            repaint();
        }

        @Override
        public void componentResized(ComponentEvent e)
        {
            Dimension size = getSize();
            centerOfComponent = new Point(size.width/2, size.height/2);
            //////
            updateTopLayerIdealBoundary();
            virtualEditorWindows[topLayerIndex].highlightTopLayer();
        }

        @Override
        public void componentMoved(ComponentEvent e) {}

        @Override
        public void componentShown(ComponentEvent e) {}

        @Override
        public void componentHidden(ComponentEvent e) {}

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            g.setColor(new Color(255,0,0));
            if(DEBUG_MODE_UI)
                g.fillOval(getSize().width/2-10, getSize().height/2-10,20,20); //Show Center

            if(virtualEditorWindows!=null)
            {
                for(int i = commitList.size()-1; i>=0; i--)
                {
                    int layerIndex_ith_after_topLayer = (topLayerIndex+i)%commitList.size();
                    virtualEditorWindows[layerIndex_ith_after_topLayer].draw(g);

                }
            }
        }

        private void updateVirtualWindowsInfo(float dt_sec)
        {
            final int LAST_STEP_SPEED = 4;
            int sign = (int) Math.signum(topLayerIndex - targetLayerIndex);
            int diff = Math.abs(targetLayerIndex - topLayerIndex);
            if(targetLayerIndex == topLayerIndex)
                numberOfPassingLayersPerSec_forAnimation = -LAST_STEP_SPEED;
            else if(diff < 2)
                numberOfPassingLayersPerSec_forAnimation = LAST_STEP_SPEED*sign;
            else if(diff<6)
                numberOfPassingLayersPerSec_forAnimation = 6*sign;
            else
                numberOfPassingLayersPerSec_forAnimation = 9*sign;

            // TODO: maybe we overpass the target index commit
            topLayerOffset += numberOfPassingLayersPerSec_forAnimation * dt_sec * LAYER_DISTANCE;

            // When: numberOfPassingLayersPerSec_forAnimation is NEGATIVE
            // When: Moving direction FROM screen
            // currentCommitIndex = 0 ===> targetCommitIndex = 10
            if(topLayerOffset < 0)
            {
                // TODO: Still the result of sum may be negative
                topLayerOffset = (topLayerOffset+LAYER_DISTANCE)%LAYER_DISTANCE;
                topLayerIndex++;
                //assert topLayerIndex >= commitList.size(); // TODO
                if(topLayerIndex >= commitList.size())
                    topLayerIndex=0;
            }

            // When: numberOfPassingLayersPerSec_forAnimation is POSITIVE
            // When: Moving direction INTO screen
            if(topLayerOffset > LAYER_DISTANCE)
            {
                topLayerOffset = topLayerOffset%LAYER_DISTANCE;
                topLayerIndex--;
                //assert topLayerIndex < 0; // TODO
                if(topLayerIndex < 0)
                    topLayerIndex=commitList.size()-1;
            }

            for(int i=0; i<commitList.size(); i++)
            {
                int layerIndex_ith_after_topLayer = (topLayerIndex+i)%commitList.size();

                if(layerIndex_ith_after_topLayer < topLayerIndex
                        || layerIndex_ith_after_topLayer>topLayerIndex + MAX_VISIBLE_VIRTUAL_WINDOW )
                    virtualEditorWindows[layerIndex_ith_after_topLayer].isVisible = false;
                else
                    virtualEditorWindows[layerIndex_ith_after_topLayer].isVisible = true;
                virtualEditorWindows[layerIndex_ith_after_topLayer].updateDepth(i*LAYER_DISTANCE + topLayerOffset);
            }

            float d = topLayerOffset - 0;
            float abs = Math.abs(d);
            if(topLayerIndex == targetLayerIndex && abs<0.06)
            {
                stopAnimation();
                virtualEditorWindows[topLayerIndex].highlightTopLayer();
            }

            repaint();
        }

        public boolean showCommit(int newCommitIndex, boolean withAnimation) // TODO: without animation
        {
            if(withAnimation==false)
            {
                loadMainEditorWindowContent();
                virtualEditorWindows[topLayerIndex].highlightTopLayer();
                // TODO: Arrange VirtualEditorWindows
                return true;
            }
            else
            {
                if( targetLayerIndex==newCommitIndex || onChangingCommitProcess == true)
                    return false;

                playAnimation(newCommitIndex);
                mainEditorWindow.setVisible(false);
                return true;
            }
        }

        private void playAnimation(int newCommitIndex)
        {
            onChangingCommitProcess = true;
            this.targetLayerIndex = newCommitIndex;
            playing3DAnimationTimer.start();
        }

        private String getStringFromCommits(int commitIndex)
        {
            String content= commitList.get(commitIndex).getFileContent();
            return content;
        }

        public void stopAnimation()
        {
            loadMainEditorWindowContent();

            playing3DAnimationTimer.stop();
            onChangingCommitProcess = false;
        }

        private void loadMainEditorWindowContent()
        {
            String content = getStringFromCommits(topLayerIndex);
            mainEditorWindow.setText(content);
            mainEditorWindow.setCaretPosition(0);
            updateMainEditorWindowBoundary();
            mainEditorWindow.setVisible(true);
        }

        private void updateMainEditorWindowBoundary()
        {
            int x,y,w,h;
            w = virtualEditorWindows[topLayerIndex].drawingRect.width;
            h = virtualEditorWindows[topLayerIndex].drawingRect.height-TOP_BAR_HEIGHT;
            x = virtualEditorWindows[topLayerIndex].drawingRect.x-w/2;
            y = virtualEditorWindows[topLayerIndex].drawingRect.y-h/2+TOP_BAR_HEIGHT/2;
            mainEditorWindow.setBounds(x,y,w,h);
        }

        private void updateTopLayerIdealBoundary()
        {
            final int FREE_SPACE_VERTICAL = 100, FREE_SPACE_HORIZONTAL = 60;
            ////
            topLayerDimention = new Dimension(getSize().width - FREE_SPACE_HORIZONTAL /*Almost Fill Width*/,
                    2*getSize().height/3 /*2/3 of whole vertical*/);
            topLayerCenterPos = new Point(centerOfComponent.x, 2*getSize().height/3 /*Fit from bottom*/);
            ////
            setVirtualWindowsDefaultValues();
            updateMainEditorWindowBoundary();
        }

        protected class VirtualEditorWindow
        {
            final float BASE_DEPTH = 2; // Min:1.0
            final float Y_OFFSET_FACTOR = 250;
            ////////
            int index=-1;
            CommitWrapper commitWrapper = null;

            boolean isVisible=true;
            float depth;
            Color DEFAULT_BORDER_COLOR = Color.GRAY;
            Color myColor=Color.WHITE, myBorderColor=DEFAULT_BORDER_COLOR;
            int xCenterDefault, yCenterDefault, wDefault, hDefault;
            Rectangle drawingRect = new Rectangle(0, 0, 0, 0);
            ////////

            public VirtualEditorWindow(int index, CommitWrapper commitWrapper)
            {
                this.index = index;
                this.commitWrapper = commitWrapper;


                if(COLORFUL || DEBUG_MODE_UI)
                {
                    Random rand = new Random();
                    float r = rand.nextFloat();
                    float g = rand.nextFloat();
                    float b = rand.nextFloat();
                    this.myColor = new Color(r,g,b);
                }
            }

            // this function should be called on each size change
            public void setDefaultValues(int xCenterDefault, int yCenterDefault, int wDefault, int hDefault)
            {
                if(wDefault<=0 || hDefault<=0) return; //Window is not intialized corerctly yet

                this.xCenterDefault = xCenterDefault;
                this.yCenterDefault = yCenterDefault;
                this.wDefault = (int) (wDefault*BASE_DEPTH);
                this.hDefault = (int) (hDefault*BASE_DEPTH);

                updateDepth(depth);
            }

            public void applyAlpha(int newAlpha)
            {
                if(newAlpha>255)
                    newAlpha=255;
                myBorderColor = new Color(myBorderColor.getRed(), myBorderColor.getGreen(), myBorderColor.getBlue(), newAlpha);
                myColor = new Color(myColor.getRed(), myColor.getGreen(), myColor.getBlue(), newAlpha);
            }

            public void updateDepth(float depth)
            {
                this.depth = depth;

                float calculatingDepth = depth + BASE_DEPTH;
                Rectangle rect = new Rectangle(0, 0, 0, 0);

                myBorderColor = DEFAULT_BORDER_COLOR; //change to RED by highlightTopLayer()
                int newAlpha = 255;
                newAlpha = (int)(BASE_DEPTH*255.0/(calculatingDepth));
                applyAlpha(newAlpha);


                /////// Size    
                rect.width = (int) (wDefault / calculatingDepth);
                rect.height = (int) (hDefault / calculatingDepth);
                //
                rect.x = xCenterDefault;
                rect.y = yCenterDefault - (int) (Math.log(calculatingDepth - BASE_DEPTH + Math.exp(0)) * Y_OFFSET_FACTOR);

                drawingRect = rect;
            }

            public void highlightTopLayer()
            {
                if(index == topLayerIndex && onChangingCommitProcess==false)
                {
                    myBorderColor = Color.RED;
                    applyAlpha(255);
                }
            }
            public void draw(Graphics g)
            {
                if(this.isVisible!=true) return;

                int x,y,w,h; //TODO: Create drawingRect not centered and make a function for below
                w = this.drawingRect.width;
                h = this.drawingRect.height;
                x = this.drawingRect.x - w/2;
                y = this.drawingRect.y - h/2;
                /// Rect
                g.setColor( this.myColor);
                g.fillRect(x, y, w, h);
                /// Border
                g.setColor( this.myBorderColor);
                g.drawRect(x, y, w, h);

                /// TopBar
                g.setColor( this.myBorderColor);
                g.fillRect(x, y, w, TOP_BAR_HEIGHT);

                /// Name
                g.setColor(Color.BLACK);
                String text="";
                Graphics g2 = g.create();
                Rectangle2D rectangleToDrawIn = new Rectangle2D.Double(x,y,w,TOP_BAR_HEIGHT);
                g2.setClip(rectangleToDrawIn);
                if(index==topLayerIndex)
                {
                    g2.setFont(new Font("Courier", Font.BOLD, 10));
                    text = getTopBarMessage();
                    drawStringCenter(g2, text, x, y+8, w);
                    text = new String(virtualFile.getPath());
                    drawStringCenter(g2, text, x, y+18, w);
                }
                else
                {
                    float fontSize = 20/(BASE_DEPTH+depth);
                    g2.setFont(new Font("Courier", Font.BOLD, (int)fontSize));
                    text = getTopBarMessage();
                    drawStringCenter(g2, text, x, y+15, w);
                }
            }

            @NotNull
            private String getTopBarMessage()
            {
                String text;
                if(commitWrapper.isFake())
                    text = new String(commitWrapper.getHash());
                else
                    text = new String("Commit "+commitWrapper.getHash());
                return text;
            }

            void drawStringCenter(Graphics g2, String text, int x, int y, int w)
            {
                int textLengthInPixel= g2.getFontMetrics().stringWidth(text);
                g2.drawString(text, x+w/2-textLengthInPixel/2, y);

            }


        } // End of VirtualEditorWindow class

        class CustomEditorTextField extends EditorTextField
        {
            // >>>>>>>> Scroll for EditorTextField
            // https://intellij-support.jetbrains.com/hc/en-us/community/posts/206759275-EditorTextField-and-surrounding-JBScrollPane

            public CustomEditorTextField(Document document, Project project, FileType fileType, boolean isViewer, boolean oneLineMode)
            {
                super(document,project,fileType,isViewer,oneLineMode);
            }

            public CustomEditorTextField(@NotNull String text, Project project, FileType fileType) {
                this(EditorFactory.getInstance().createDocument(text), project, fileType, false, true);
            }

            @Override
            protected EditorEx createEditor()
            {
                EditorEx editor = super.createEditor();
                editor.setVerticalScrollbarVisible(true);
                editor.setHorizontalScrollbarVisible(true);
                showLineNumber(editor);
                return editor;
            }

            private void showLineNumber(EditorEx editor)
            {
                EditorSettings settings = editor.getSettings();
                settings.setLineNumbersShown(true);
                editor.reinitSettings();
            }

        }

    } // End of Commits3DView class

} // End of class
