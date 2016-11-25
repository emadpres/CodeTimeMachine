package com.reveal.testtimemachine;

import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

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

    ///////// ++ Constant ++ /////////
    final boolean DEBUG_MODE_UI = true;
    private final int MIN_ANIMATION_DURATION_MS=1500, MAX_EXTRA_ANIMATION_DURATION_MS=1000;
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

    private String getStringFromCommits(SubjectOrTest _s, int _commitId)
    {
        String content="";
        try
        {
            List<VcsFileRevision> vcsFileRevisions;
            if(_s==SubjectOrTest.SUBJECT)
                vcsFileRevisions = fileRevisionLists.get(0);
            else
                vcsFileRevisions = fileRevisionLists.get(1);
            ///
            VcsFileRevision vcsFileRevision = vcsFileRevisions.get(_commitId);
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

    private void navigateToCommit(SubjectOrTest s, int commitIndex)
    {
        Random rand = new Random();
        int t = rand.nextInt(MIN_ANIMATION_DURATION_MS+MAX_EXTRA_ANIMATION_DURATION_MS);
        if(s==SubjectOrTest.SUBJECT)
            myLeftEditor.show3dAnimation();
       /* else
            myRightEditor.show3dAnimation();*/
        Timer stoppingAnimationTimer = new Timer(t, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String content = getStringFromCommits(s, commitIndex);

                if( s==SubjectOrTest.SUBJECT)
                {
                    myLeftEditor.mainEditorWindow.setText(content);
                    myLeftEditor.requestStop3dAnimation();
                }
                /*else
                {
                    myRightEditor.mainEditorWindow.setText(content);
                    myRightEditor.requestStop3dAnimation();
                }*/

            }
        });
        stoppingAnimationTimer.setRepeats(false);
        stoppingAnimationTimer.start();
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

            for(int i=0; i< fileRevisionsList.size(); i++)
            {
                commitItems[i]= new CommitItem(direction, i, fileRevisionsList.get(i), this);
                myComponent.add(commitItems[i].getComponent());
                myComponent.add(Box.createRigidArea(new Dimension(1,10)));
            }
        }

        private void createEmptyJComponent()
        {
            myComponent = new JPanel();
            BoxLayout boxLayout = new BoxLayout(myComponent, BoxLayout.Y_AXIS);
            myComponent.setLayout(boxLayout);
            if(DEBUG_MODE_UI)
                myComponent.setBackground(Color.RED);
        }


        private void activateCommit(int commitIndex)
        {
            if(activeCommitIndex!=-1)
                commitItems[activeCommitIndex].setActivated(false);
            activeCommitIndex = commitIndex;
            commitItems[activeCommitIndex].setActivated(true);
            TTMWindow.navigateToCommit(s, commitIndex);
        }

        public JPanel getComponent()
        {
            return myComponent;
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
            this.commitIndex = commitIndex;

            setupUI(fileRevision);

            setupMouseBeahaviour();
            setupComponentResizingBehaviour();
        }

        private void setupUI(VcsFileRevision fileRevision)
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
        float N_PASSED_LAYERS_PER_SEC = 4f;
        //////
        int topLayerIndex;
        float topLayerOffset;
        int demoFactor = +1;
        VirtualEditorWindow[] virtualEditorWindows = null;
        Timer playing3DAnimationTimer;
        boolean stop3dAnimationRequested = false;
        ///////// -- UI: 3D Stuff -- /////////

        Project project;
        List<VcsFileRevision> commitList;



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


            mainEditorWindow = new EditorTextField("import com.a; public class Emad{} ", project, FileTypeRegistry.getInstance().getFileTypeByExtension("java"));
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
                virtualEditorWindows[i] = new VirtualEditorWindow();
            }

            setVirtualWindowsDefaultValues();
            placeVirtualWindowsInStandardPosition();


            playing3DAnimationTimer = new Timer(TICK_INTERVAL_MS, new ActionListener(){
                public void actionPerformed(ActionEvent e)
                {
                    updateVirtualWindows();
                }
            });
        }

        private void setVirtualWindowsDefaultValues()
        {
            for (int i = 0; i< commitList.size() ; i++)
            {
                int xCenter, yCenter, w, h;
                w = mainEditorWindow.getSize().width;
                h = mainEditorWindow.getSize().height;
                xCenter = mainEditorWindow.getLocation().x + w/2;
                yCenter = mainEditorWindow.getLocation().y + h/2;
                virtualEditorWindows[i].setDefaultValues(xCenter, yCenter, w, h);
            }
        }

        public void requestStop3dAnimation()
        {
            N_PASSED_LAYERS_PER_SEC = 4;
            Timer fullStopTimer = new Timer(1000, new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    stop3dAnimationRequested=true;
                }
            });
            fullStopTimer.setRepeats(false);
            fullStopTimer.start();

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
            repaint();

            /*if(dummyWindow!=null)
            {
                g.fillRect((int) dummyWindow.currentX, (int) dummyWindow.currentY,
                        (int) dummyWindow.currentW, (int) dummyWindow.currentH);
            }*/



            //btn.paint(g);
            //g.dispose();
        }

        private void updateVirtualWindows()
        {
            topLayerOffset += N_PASSED_LAYERS_PER_SEC * demoFactor * (TICK_INTERVAL_MS/1000.f) * LAYER_DISTANCE;

            if(topLayerOffset > LAYER_DISTANCE)
            {
                if(stopAnimationIfRequstedAndReturnTrue()) return;
                topLayerOffset = topLayerOffset%LAYER_DISTANCE;
                topLayerIndex--;
                if(topLayerIndex < 0)
                    topLayerIndex=commitList.size();
            }

            if(topLayerOffset < 0)
            {
                if(stopAnimationIfRequstedAndReturnTrue()) return;
                topLayerOffset = (topLayerOffset+LAYER_DISTANCE)%LAYER_DISTANCE;
                topLayerIndex++;
                if(topLayerIndex >= commitList.size())
                    topLayerIndex=0;
            }

            for(int i=0; i<commitList.size(); i++)
            {
                int layerIndex_ith_after_topLayer = (topLayerIndex+i)%commitList.size();
                virtualEditorWindows[layerIndex_ith_after_topLayer].updateDepth(i*LAYER_DISTANCE + topLayerOffset);
            }

            repaint();
        }

        public void show3dAnimation()
        {
            ///////////////////////////////
            double rand = Math.random();
            if(rand>0.5) demoFactor = demoFactor*-1;
            N_PASSED_LAYERS_PER_SEC = 8;
            ///////////////////////////////
            playing3DAnimationTimer.start();
            mainEditorWindow.setVisible(false);
        }

        private boolean stopAnimationIfRequstedAndReturnTrue()
        {
            if(stop3dAnimationRequested)
            {
                stop3dAnimation();
                return true;
            }
            return false;
        }

        public void stop3dAnimation()
        {
            stop3dAnimationRequested=false;
            playing3DAnimationTimer.stop();
            mainEditorWindow.setVisible(true);
        }

        private void updateMainEditorWidnowPositionAndScale()
        {
            final int FREE_SPACE_VERTICAL = 100, FREE_SPACE_HORIZONTAL = 60;
            ////
            Dimension mainEditorWindowsSize = new Dimension(getSize().width - FREE_SPACE_HORIZONTAL /*Almost Fill Width*/,
                    2*getSize().height/3 /*2/3 of whole vertical*/);
            Point positionOfLeftTopOfMainEditor = new Point(centerOfComponent.x - mainEditorWindowsSize.width/2,
                    getSize().height/6 /*Fit from bottom*/);

            mainEditorWindow.setBounds(new Rectangle(positionOfLeftTopOfMainEditor, mainEditorWindowsSize));
            ////
            placeVirtualWindowsInStandardPosition();
        }

        protected class VirtualEditorWindow
        {
            final float BASE_DEPTH = 2; // Min:1.0
            final float Y_OFFSET_FACTOR = 200;
            ////////
            float depth;
            Color myColor=Color.WHITE, myBorderColor=Color.GRAY;
            int xCenterDefault, yCenterDefault, wDefault, hDefault;
            Rectangle drawingRect = new Rectangle(0, 0, 0, 0);
            ////////

            public VirtualEditorWindow()
            {
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
                /////// Color
                final int N_TRANSPARENT_LAYERS = 3;
                final int MIN_ALPHA = 100;
                int newAlpha = 255;


                if(calculatingDepth-BASE_DEPTH< LAYER_DISTANCE)
                {
                    newAlpha = (int)((calculatingDepth-BASE_DEPTH)* (255/LAYER_DISTANCE));

                }
                else
                {
                    newAlpha = (int)(1.3*BASE_DEPTH*255.0/(calculatingDepth));
                    if(newAlpha>255) newAlpha=255;
                }

                myColor = new Color(myColor.getRed(), myColor.getGreen(), myColor.getBlue(), newAlpha);
                myBorderColor = new Color(myBorderColor.getRed(), myBorderColor.getGreen(), myBorderColor.getBlue(), newAlpha);
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
            }

        } // End of VirtualEditorWindow class

    } // End of Commits3DView class

} // End of class
