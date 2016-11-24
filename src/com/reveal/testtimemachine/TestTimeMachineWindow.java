package com.reveal.testtimemachine;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

///////// ++ UI ++ /////////
///////// ++ UI -- /////////

public class TestTimeMachineWindow
{
    private JPanel myJComponent;
    private Project project;
    private VirtualFile[] virtualFiles = new VirtualFile[2];
    private ArrayList<List<VcsFileRevision>> fileRevisionLists = new ArrayList<List<VcsFileRevision>>();

    ///////// ++ CommitBar and CommitItem ++ /////////
    private enum CommitItemDirection {NONE, LTR, RTL};
    public enum SubjectOrTest {NONE, SUBJECT, TEST};
    ///////// -- CommitBar and CommitItem -- /////////

    ///////// ++ Debugging ++ /////////
    final boolean DEBUG_MODE_UI = true;
    ///////// -- Debugging -- /////////

    TestTimeMachineWindow(Project project, VirtualFile[] virtualFiles, ArrayList<List<VcsFileRevision>> fileRevisionsLists)
    {
        this.project = project;
        this.virtualFiles = virtualFiles;
        this.fileRevisionLists = fileRevisionsLists;
        ////////////////////////////////////////////////////
        setupToolTipSetting();
        GroupLayout groupLayout = createEmptyJComponentAndReturnGroupLayout();

        //CommitsBar leftBar = new CommitsBar(CommitBarDirection.LTR, SubjectOrTest.SUBJECT,  fileRevisionLists.get(0), this);

        //CommitsBar rightBar = new CommitsBar(CommitBarDirection.RTL, SubjectOrTest.TEST, fileRevisionLists.get(1), this);

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

    private class CommitsBar
    {
        private void activateCommit(int commitIndex)
        {

        }
    }

    private class CommitItem
    {
        ///////// ++ Constant ++ /////////
        private final Dimension COMPONENT_SIZE = new Dimension( 120,20 );
        private final Dimension MARKERT_NORMAL_SIZE = new Dimension( 10,5 );
        private final Dimension MARKER_HOVERED_SIZE = new Dimension( 15,8 );
        //
        private final Color NORMAL_COLOR = Color.LIGHT_GRAY;
        private final Color HOVERED_COLOR = new Color(255,0,0,150);
        ///////// ++ Constant -- /////////

        ///////// ++ UI ++ /////////
        private JPanel myComponent;
        private JLabel marker, commitInfo;
        ///////// ++ UI -- /////////


        private int commitIndex=-1;
        private CommitItemDirection direction;
        private boolean isActive=false;
        private CommitsBar commitsBar=null;



        public CommitItem(CommitItemDirection direction, int commitIndex,  VcsFileRevision fileRevision, CommitsBar commitBar)
        {
            this.commitsBar = commitBar;
            this.direction = direction;

            setupUI(fileRevision);

            setupMouseBeahaviour();
            setupComponentResizingBehaviour();
        }

        private void setupUI(VcsFileRevision fileRevision)
        {
            createEmptyJComponent();
            myComponent.setToolTipText(fileRevision.getCommitMessage());
            if(DEBUG_MODE_UI)
                myComponent.setBackground(Color.GREEN);

            setupUI_marker();
            setupUI_commitInfo(fileRevision);

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

        private void setupUI_commitInfo(VcsFileRevision fileRevision)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fileRevision.getRevisionDate());
            String m = getMonthName(fileRevision.getRevisionDate().getMonth());
            commitInfo = new JLabel(m+" "+fileRevision.getRevisionDate().getDay()+", "+ cal.get(Calendar.YEAR));
            commitInfo.setSize(30,10); //updated in ComponentSizeChanged
            if(DEBUG_MODE_UI)
                commitInfo.setBackground(Color.CYAN);
            commitInfo.setOpaque(true);
            Font font = commitInfo.getFont();
            Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
            commitInfo.setFont(boldFont);
            commitInfo.setHorizontalAlignment(SwingConstants.CENTER);
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
                        updateToHoveredUI();
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

        public String getMonthName(int month){
            //String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
            String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            return monthNames[month];
        }

        private void updateToNormalUI()
        {
            marker.setSize(MARKERT_NORMAL_SIZE);
            marker.setBackground(NORMAL_COLOR);
            updateMarkerLocation();

            commitInfo.setForeground(NORMAL_COLOR);
            updateCommitInfoLocation();
        }

        private void updateToHoveredUI()
        {
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
            final int DELTA_DIS_FROM_MARKER = 1;
            if(direction== CommitItemDirection.LTR)
                commitInfo.setLocation( marker.getLocation().x+marker.getSize().width+DELTA_DIS_FROM_MARKER,
                                        marker.getLocation().y+marker.getSize().height/2-commitInfo.getSize().height/2);
            else
            {
                commitInfo.setLocation( marker.getLocation().x - DELTA_DIS_FROM_MARKER - commitInfo.getSize().width,
                                        marker.getLocation().y + marker.getSize().height / 2 - commitInfo.getSize().height / 2);
            }
        }

        JPanel getComponent()
        {
            return myComponent;
        }

    }
}
