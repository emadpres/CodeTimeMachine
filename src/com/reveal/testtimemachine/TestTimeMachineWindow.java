package com.reveal.testtimemachine;

import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorTextField;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
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
    private enum SubjectOrTest {NONE, SUBJECT, TEST};
    private enum CommitItemInfoType {NONE, DATE, TIME}
    ///////// -- CommitBar and CommitItem -- /////////

    ///////// ++ Constant ++ /////////
    final boolean DEBUG_MODE_UI = false;
    ///////// -- Constant -- /////////

    ///////// ++ UI ++ /////////
    Commits3DView myLeftEditor = null;
    ///////// -- UI -- /////////

    TestTimeMachineWindow(Project project, VirtualFile[] virtualFiles, ArrayList<List<VcsFileRevision>> fileRevisionsLists)
    {
        this.project = project;
        this.virtualFiles = virtualFiles;
        this.fileRevisionLists = fileRevisionsLists;
        ////////////////////////////////////////////////////
        setupToolTipSetting();
        GroupLayout groupLayout = createEmptyJComponentAndReturnGroupLayout();


        CommitsBar leftBar = new CommitsBar(CommitItemDirection.LTR, SubjectOrTest.SUBJECT,  fileRevisionLists.get(0), this);

        myLeftEditor = new Commits3DView(project, fileRevisionsLists.get(0));

        groupLayout.setHorizontalGroup( groupLayout.createSequentialGroup().addComponent(leftBar.getComponent()).addComponent(myLeftEditor));
        groupLayout.setVerticalGroup( groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(leftBar.getComponent()).addComponent(myLeftEditor));
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

    private boolean navigateToCommit(SubjectOrTest s, int commitIndex)
    {
        return myLeftEditor.showCommitByIndexNumber(commitIndex, true);
    }

    private class CommitsBar
    {
        ///////// ++ UI ++ /////////
        private TestTimeMachineWindow TTMWindow;
        private JPanel myComponent;
        private CommitItem[] commitItems;
        ///////// ++ UI -- /////////

        private SubjectOrTest s = SubjectOrTest.NONE;
        private int activeCommitIndex = -1;

        public CommitsBar(CommitItemDirection direction, SubjectOrTest s, List<VcsFileRevision> fileRevisionsList,
                          TestTimeMachineWindow TTMWindow)
        {
            this.TTMWindow = TTMWindow;
            this.s= s;

            createEmptyJComponent();
            creatingCommitsItem(direction, fileRevisionsList);
            myComponent.repaint();

        }

        private void creatingCommitsItem(CommitItemDirection direction, List<VcsFileRevision> fileRevisionsList)
        {
            commitItems = new CommitItem[fileRevisionsList.size()];

            Calendar lastCommitCal = Calendar.getInstance();
            Calendar currentCommitCal = Calendar.getInstance();

            lastCommitCal.setTime(new Date(Long.MIN_VALUE));

            for(int i=0; i< fileRevisionsList.size(); i++)
            {
                currentCommitCal.setTime(fileRevisionsList.get(i).getRevisionDate());
                boolean sameDay = lastCommitCal.get(Calendar.YEAR) == currentCommitCal.get(Calendar.YEAR) &&
                        lastCommitCal.get(Calendar.DAY_OF_YEAR) == currentCommitCal.get(Calendar.DAY_OF_YEAR);
                /////
                if(sameDay)
                    commitItems[i]= new CommitItem(direction, i, fileRevisionsList.get(i), this, CommitItemInfoType.TIME);
                else
                    commitItems[i]= new CommitItem(direction, i, fileRevisionsList.get(i), this, CommitItemInfoType.DATE);
                myComponent.add(commitItems[i].getComponent());
                myComponent.add(Box.createRigidArea(new Dimension(1,10)));
                ///
                lastCommitCal.setTime(fileRevisionsList.get(i).getRevisionDate());
            }
        }

        private void createEmptyJComponent()
        {
            // Size of this component according to children's components = CommitItem
            myComponent = new JPanel();
            BoxLayout boxLayout = new BoxLayout(myComponent, BoxLayout.Y_AXIS);
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

        public JPanel getComponent()
        {
            return myComponent;
        }

        private class CommitItem
        {
            ///////// ++ Constant ++ /////////
            private final Dimension COMPONENT_SIZE = new Dimension( 170,20 );
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



            public CommitItem(CommitItemDirection direction, int commitIndex,  VcsFileRevision fileRevision, CommitsBar commitBar, CommitItemInfoType infoType)
            {
                this.commitsBar = commitBar;
                this.direction = direction;
                this.commitIndex = commitIndex;

                setupUI(fileRevision, infoType);

                setupMouseBeahaviour();
                setupComponentResizingBehaviour();
            }

            private void setupUI(VcsFileRevision fileRevision, CommitItemInfoType infoType)
            {
                createEmptyJComponent();
                if(direction == CommitItemDirection.LTR)
                    myComponent.setAlignmentX(Component.LEFT_ALIGNMENT); // make it left_align within parent layout (Hbox)
                else
                    myComponent.setAlignmentX(Component.RIGHT_ALIGNMENT);

                myComponent.setToolTipText(fileRevision.getCommitMessage());

                if(DEBUG_MODE_UI)
                    myComponent.setBackground(Color.GREEN);

                setupUI_marker();
                setupUI_commitInfo(fileRevision, infoType);

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

            private void setupUI_commitInfo(VcsFileRevision fileRevision,  CommitItemInfoType infoType)
            {
                String commitInfoStr = "";

                Calendar cal = Calendar.getInstance();
                cal.setTime(fileRevision.getRevisionDate());

                if(infoType == CommitItemInfoType.DATE)
                {
                    // Month
                    int mInt = cal.get(Calendar.MONTH);
                    String mStr = getMonthName(mInt);
                    commitInfoStr = mStr;
                    // Day
                    commitInfoStr += " "+cal.get(Calendar.DAY_OF_MONTH);
                    // Year
                    commitInfoStr += ", "+ cal.get(Calendar.YEAR);
                }
                else if(infoType == CommitItemInfoType.TIME)
                {
                    // Time
                    commitInfoStr = cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE);
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

            private String getMonthName(int month){
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

            private void updateToActiveUI()
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
        EditorTextField mainEditorWindow;
        Point centerOfComponent;
        ///////// -- UI -- /////////

        ///////// ++ UI: 3D Stuff ++ /////////
        final boolean COLORFUL = true;
        final int TICK_INTERVAL_MS = 50;
        final float LAYER_DISTANCE = 0.2f;

        //////
        final int TOP_BAR_HEIGHT = 25;
        int topLayerIndex, targetLayerIndex /*if equals to topLayerIndex it means no animation is running*/;
        float topLayerOffset;
        VirtualEditorWindow[] virtualEditorWindows = null;
        Timer playing3DAnimationTimer;
        int numberOfPassingLayersPerSec_forAnimation = 1;
        //
        Dimension topLayerDimention = new Dimension(0,0);
        Point topLayerCenterPos = new Point(0,0);
        ///////// -- UI: 3D Stuff -- /////////

        Project project;
        List<VcsFileRevision> commitList = null;


        public Commits3DView( Project project, List<VcsFileRevision> commitList)
        {
            super();

            this.project = project;
            this.commitList = commitList;

            this.setLayout(null);
            this.addComponentListener(this);
            if (DEBUG_MODE_UI)
                this.setBackground(Color.ORANGE);
            this.setOpaque(true);


            mainEditorWindow = new EditorTextField("First commit did not load correctly! ", project, FileTypeRegistry.getInstance().getFileTypeByExtension("java"));
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
                virtualEditorWindows[i] = new VirtualEditorWindow(i);
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
                virtualEditorWindows[i].updateDepth(i* LAYER_DISTANCE);
            repaint();
        }

        @Override
        public void componentResized(ComponentEvent e)
        {
            Dimension size = getSize();
            centerOfComponent = new Point(size.width/2, size.height/2);
            //////
            updateMainEditorWidnowPositionAndScale();
            setVirtualWindowsDefaultValues();
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
            int sign = (int) Math.signum(topLayerIndex - targetLayerIndex);
            int diff = Math.abs(targetLayerIndex - topLayerIndex);
            if(diff < 1)
                numberOfPassingLayersPerSec_forAnimation = 1*sign;
            if(diff<5)
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

                if(layerIndex_ith_after_topLayer < topLayerIndex || layerIndex_ith_after_topLayer>topLayerIndex+9 )
                    virtualEditorWindows[layerIndex_ith_after_topLayer].isVisible = false;
                else
                    virtualEditorWindows[layerIndex_ith_after_topLayer].isVisible = true;
                virtualEditorWindows[layerIndex_ith_after_topLayer].updateDepth(i*LAYER_DISTANCE + topLayerOffset);
            }

            repaint();

            if(topLayerIndex == targetLayerIndex)
                stopAnimation();

        }

        public boolean showCommitByIndexNumber(int newCommitIndex, boolean withAnimation) // TODO: without animation
        {
            if( targetLayerIndex==newCommitIndex || topLayerIndex != targetLayerIndex)
                return false;

            playAnimation(newCommitIndex);
            mainEditorWindow.setVisible(false);
            return true;
        }

        private void playAnimation(int newCommitIndex)
        {
            this.targetLayerIndex = newCommitIndex;
            playing3DAnimationTimer.start();
        }

        private String getStringFromCommits(int commitIndex)
        {
            String content="";
            try
            {

                VcsFileRevision vcsFileRevision = commitList.get(commitIndex);
                byte[] selectedCommitContent = vcsFileRevision.loadContent();
                content = new String(selectedCommitContent);
            } catch (IOException e1)
            {
                e1.printStackTrace();
            } catch (VcsException e1)
            {
                e1.printStackTrace();
            }
            return content;
        }

        public void stopAnimation()
        {
            playing3DAnimationTimer.stop();
            String content = getStringFromCommits(topLayerIndex);
            myLeftEditor.mainEditorWindow.setText(content);
            mainEditorWindow.setVisible(true);
        }

        private void updateMainEditorWidnowPositionAndScale()
        {
            final int FREE_SPACE_VERTICAL = 100, FREE_SPACE_HORIZONTAL = 60;
            ////
            topLayerDimention = new Dimension(getSize().width - FREE_SPACE_HORIZONTAL /*Almost Fill Width*/,
                    2*getSize().height/3 /*2/3 of whole vertical*/);
            topLayerCenterPos = new Point(centerOfComponent.x, 2*getSize().height/3 /*Fit from bottom*/);
            ////
            Point mainEditorWindow_topLeftPos = new Point(topLayerCenterPos.x - topLayerDimention.width/2, topLayerCenterPos.y-topLayerDimention.height/2+TOP_BAR_HEIGHT);
            Dimension mainEditorWindow_dimension = new Dimension(topLayerDimention.width, topLayerDimention.height - TOP_BAR_HEIGHT);
            mainEditorWindow.setBounds(new Rectangle(mainEditorWindow_topLeftPos, mainEditorWindow_dimension));
            ////
            placeVirtualWindowsInStandardPosition();
        }

        protected class VirtualEditorWindow
        {
            final float BASE_DEPTH = 2; // Min:1.0
            final float Y_OFFSET_FACTOR = 250;
            ////////
            int index=-1;
            boolean isVisible=true;
            float depth;
            Color DEFAULT_BORDER_COLOR = Color.GRAY;
            Color myColor=Color.WHITE, myBorderColor=DEFAULT_BORDER_COLOR;
            int xCenterDefault, yCenterDefault, wDefault, hDefault;
            Rectangle drawingRect = new Rectangle(0, 0, 0, 0);
            ////////

            public VirtualEditorWindow(int index)
            {
                this.index = index;

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

            public void updateDepth(float depth)
            {
                this.depth = depth;

                float calculatingDepth = depth + BASE_DEPTH;
                Rectangle rect = new Rectangle(0, 0, 0, 0);
                int newAlpha = 255;

                if(index == topLayerIndex)
                {
                    newAlpha = 255;
                    myBorderColor = Color.RED;
                }
                else
                {
                    newAlpha = (int)(BASE_DEPTH*255.0/(calculatingDepth));
                    if(newAlpha>255) newAlpha=255;
                    myBorderColor = new Color(DEFAULT_BORDER_COLOR.getRed(), DEFAULT_BORDER_COLOR.getGreen(), DEFAULT_BORDER_COLOR.getBlue(), newAlpha);
                }

                myColor = new Color(myColor.getRed(), myColor.getGreen(), myColor.getBlue(), newAlpha);

                /////// Size    
                rect.width = (int) (wDefault / calculatingDepth);
                rect.height = (int) (hDefault / calculatingDepth);
                //
                rect.x = xCenterDefault;
                rect.y = yCenterDefault - (int) (Math.log(calculatingDepth - BASE_DEPTH + Math.exp(0)) * Y_OFFSET_FACTOR);

                drawingRect = rect;
            }

            public void draw(Graphics g)
            {
                if(this.isVisible!=true) return;

                int x,y,w,h;
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
                String text = new String("Number: "+Integer.toString(index)+" ");
                g.drawChars(text.toCharArray(), 0, text.length(), x+w/2, y+15);
            }

        } // End of VirtualEditorWindow class

    } // End of Commits3DView class

} // End of class
