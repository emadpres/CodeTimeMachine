package com.reveal.testtimemachine;

import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CommitsBar extends CommitsBarBase
{

    protected JBScrollPane commitsBarView = null;
    protected JPanel thisComponentWithoutScroll = null;

    public enum CommitItemDirection {NONE, LTR, RTL};
    public enum CommitItemInfoType {NONE, DATE, TIME}


    private CommitUIItem[] commitUIItems /* Most recent commit at 0*/;
    CommitItemDirection direction = CommitItemDirection.NONE;
    int contentHeight =0;


    public CommitsBar(CommitItemDirection direction, ClassType s, TTMSingleFileView TTMWindow)
    {
        this.TTMWindow = TTMWindow;
        this.classType= s;
        this.direction = direction;

        thisComponentWithoutScroll = createEmptyJComponent();
        commitsBarView = addScrollToThisComponent(thisComponentWithoutScroll);

        //setupToolTipSetting();
    }


    private int findCommitUIItemIndexFromcIndex(int cIndex)
    {
        int commitUIItemIndexIfExist = -1;
        for(int i=0; i<commitList.size(); i++)
            if(commitList.get(i).cIndex == cIndex)
            {
                commitUIItemIndexIfExist = i;
                break;
            }

        return commitUIItemIndexIfExist;
    }

    public void activateCommitUIItemIfExists(int cIndex)
    {
        activeCommit_cIndex = cIndex;
        int commitUIItemIndexIfExist = findCommitUIItemIndexFromcIndex(cIndex);

        if(commitUIItemIndexIfExist != -1)
            commitUIItems[commitUIItemIndexIfExist].setActivated(true);
    }

    @Override
    public void updateCommitsList(ArrayList<CommitWrapper> newCommitList)
    {
        this.commitList = newCommitList;

        thisComponentWithoutScroll.removeAll();

        creatingCommitsUIItem(newCommitList);

        activateCommitUIItemIfExists(activeCommit_cIndex);

        Dimension newDimension  = new Dimension(COMMITS_BAR_VIEW_DIMENSION.width-40/*scroll bar of parent scrollContainer*/, contentHeight);
        thisComponentWithoutScroll.setPreferredSize(newDimension);
        thisComponentWithoutScroll.setSize(newDimension);
        thisComponentWithoutScroll.setMaximumSize(newDimension);
        thisComponentWithoutScroll.setMinimumSize(newDimension);


        commitsBarView.repaint();
        scrollToBottom(commitsBarView);
    }

    private void scrollToBottom(JScrollPane scrollPane) {
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        AdjustmentListener downScroller = new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                Adjustable adjustable = e.getAdjustable();
                adjustable.setValue(adjustable.getMaximum());
                verticalBar.removeAdjustmentListener(this);
            }
        };
        verticalBar.addAdjustmentListener(downScroller);
    }

    private void setupToolTipSetting()
    {
        ToolTipManager.sharedInstance().setEnabled(true);
        ToolTipManager.sharedInstance().setInitialDelay(0); // it needs ToolTipManager.sharedInstance().setEnabled(true); before
    }

    private JBScrollPane addScrollToThisComponent(JPanel internalComponent)
    {
        JBScrollPane s = new JBScrollPane();
        s.setViewportView(internalComponent);
        s.setBorder(null);

        // BoxLayout cannot handle different alignments: see http://download.oracle.com/javase/tutorial/uiswing/layout/box.html
        //commitsBarView.setMaximumSize(new Dimension(commitItems[0].getComponent().getSize().width+10, contentHeight+10));
        s.setMaximumSize(COMMITS_BAR_VIEW_DIMENSION);

        s.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        return s;
    }

    private void creatingCommitsUIItem(ArrayList<CommitWrapper> commitList)
    {
        if(commitList.size()==0) return;
        // commitList: Most recent commit at 0
        // commitItems: Most recent commit at 0

        Calendar currentCommitCal = Calendar.getInstance();
        Calendar lastCommitCal = Calendar.getInstance();
        lastCommitCal.setTime(new Date(Long.MIN_VALUE));

        // UI_items includes CommitItem and other fake UI elements
        ArrayList<JComponent> UI_items = new ArrayList<>(commitList.size()/*reserve at least this much memory*/);

        commitUIItems = new CommitUIItem[commitList.size()];

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

            commitUIItems[i]= new CommitUIItem(direction, i, commitList.get(i), this, CommitItemInfoType.TIME);

            UI_items.add(commitUIItems[i].getComponent());
            ///
            lastCommitCal.setTime(commitList.get(i).getDate());
        }

        NewDayItem newDayMarker = new NewDayItem(direction, commitList.get(commitList.size()-1).getDate());
        UI_items.add(newDayMarker.getComponent());


        contentHeight = 0;
        for(int i=UI_items.size()-1; i>=0 ; i--)
        {
            thisComponentWithoutScroll.add(UI_items.get(i));

            final int GAP_H = 10;
            thisComponentWithoutScroll.add(Box.createRigidArea(new Dimension(1, GAP_H)));
            contentHeight += UI_items.get(i).getSize().height + GAP_H;

        }
    }

    private JPanel createEmptyJComponent()
    {
        // Size of this component according to children's components => CommitItem
        JPanel c = new JPanel();
        BoxLayout boxLayout = new BoxLayout(c, BoxLayout.PAGE_AXIS);
        c.setLayout(boxLayout);

        if(CommonValues.IS_UI_IN_DEBUGGING_MODE)
            c.setBackground(Color.RED);

        return c;
    }

    @Override
    public void setActiveCommit_cIndex(int cIndex)
    {
        if(activeCommit_cIndex!=-1)
        {
            int x = findCommitUIItemIndexFromcIndex(activeCommit_cIndex);
            if(x!= -1)
            {
                /*If we are here, it means last active commit was also in the current CommitsBar list*/
                commitUIItems[x].setActivated(false);
            }
        }

        activeCommit_cIndex = cIndex;
        commitUIItems[cIndex].setActivated(true);
    }

    private void activateCommit(int clickedCommitUIItemIndex)
    {
        int newActivecommit_cIndex = commitList.get(clickedCommitUIItemIndex).cIndex;
        boolean possible = TTMWindow.navigateToCommit(classType, newActivecommit_cIndex);
        if(!possible) return;

        setActiveCommit_cIndex(newActivecommit_cIndex);
    }

    @Override
    public JComponent getComponent()
    {
        return commitsBarView;
    }

    private class NewDayItem
    {
        ///////// ++ Constant ++ /////////
        private final Dimension COMPONENT_SIZE = new Dimension( 140,30 );
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
            commitInfo.setSize(myComponent.getSize().width-30,20);
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
                    Since thisComponentWithoutScroll may change (since it belongs to a layout AND we didn't limit the maximum size),
                    we need to reaarange objects when size chnages.
                    if thisComponentWithoutScroll had layout (for its children) we wouldn't manage its children after each size change.
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

    static private class CommitUIItem
    {
        ///////// ++ Constant ++ /////////
        private final Dimension COMPONENT_SIZE = new Dimension( 140,20 );
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


        private int commitUIItemIndex=-1;
        private CommitItemDirection direction;
        CommitItemInfoType infoType;

        private boolean isActive=false;
        private CommitsBar commitsBar=null;



        public CommitUIItem(CommitItemDirection direction, int commitUIItemIndex, CommitWrapper commitWrapper, CommitsBar commitBar, CommitItemInfoType infoType)
        {
            this.commitsBar = commitBar;
            this.direction = direction;
            this.commitUIItemIndex = commitUIItemIndex;
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
                    Since thisComponentWithoutScroll may change (since it belongs to a layout AND we didn't limit the maximum size),
                    we need to reaarange objects when size chnages.
                    if thisComponentWithoutScroll had layout (for its children) we wouldn't manage its children after each size change.
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
            commitInfo.setSize(myComponent.getSize().width-30,20);
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
                    commitsBar.activateCommit(commitUIItemIndex);
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

