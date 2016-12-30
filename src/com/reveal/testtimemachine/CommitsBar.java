package com.reveal.testtimemachine;

import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CommitsBar
{
    ///////// ++ UI ++ /////////
    private TTMSingleFileView TTMWindow;
    private JPanel myComponent;
    private JBScrollPane scroll;
    private CommitItem[] commitItems /* Most recent commit at 0*/;
    ///////// ++ UI -- /////////

    public enum CommitItemDirection {NONE, LTR, RTL};
    public enum CommitItemInfoType {NONE, DATE, TIME}

    int H=0;
    private ClassType s = ClassType.NONE;
    private int activeCommitIndex = -1;

    public CommitsBar(CommitItemDirection direction, ClassType s, ArrayList<CommitWrapper> commitList,
                      TTMSingleFileView TTMWindow)
    {
        this.TTMWindow = TTMWindow;
        this.s= s;

        setupToolTipSetting();

        createEmptyJComponent();
        creatingCommitsItem(direction, commitList);

        setupScroll();

        myComponent.repaint();

    }

    private void setupToolTipSetting()
    {
        ToolTipManager.sharedInstance().setEnabled(true);
        ToolTipManager.sharedInstance().setInitialDelay(0); // it needs ToolTipManager.sharedInstance().setEnabled(true); before
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

        if(CommonValues.IS_UI_IN_DEBUGGING_MODE)
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


            if(CommonValues.IS_UI_IN_DEBUGGING_MODE)
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
            if(CommonValues.IS_UI_IN_DEBUGGING_MODE)
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

    static private class CommitItem
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

            if(CommonValues.IS_UI_IN_DEBUGGING_MODE)
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
            if(CommonValues.IS_UI_IN_DEBUGGING_MODE)
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
            if(infoType== CommitItemInfoType.DATE)
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
            if(infoType== CommitItemInfoType.DATE)
                commitInfo.setForeground(NORMAL_COLOR_LONG);
            else
                commitInfo.setForeground(NORMAL_COLOR);
            updateCommitInfoLocation();
        }

        private void updateToActiveUI()
        {
            if(infoType== CommitItemInfoType.DATE)
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

