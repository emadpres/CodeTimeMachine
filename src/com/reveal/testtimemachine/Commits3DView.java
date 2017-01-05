package com.reveal.testtimemachine;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorTextField;
import com.siyeh.ig.numeric.ImplicitNumericConversionInspection;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Commits3DView extends JComponent implements ComponentListener
{
    ///////// ++ Constant ++ /////////
    ///////// -- Constant -- /////////

    ///////// ++ UI ++ /////////

    Point centerOfThisComponent;
    ///////// -- UI -- /////////

    String debuggingText = "";

    //////// ++ Timer and Timing
    Timer playing3DAnimationTimer;
    final int TICK_INTERVAL_MS = 50;
    //boolean onChangingCommitProcess = false; // instead you can use playing3DAnimationTimer.isRunning()
    ///////
    final Color DUMMY_COLOR = Color.black;

    ///////// ++ UI: 3D Prespective Variables ++ /////////
    final float LAYER_DISTANCE = 0.2f;
    final float LAYERS_DEPTH_ERROR_THRESHOLD = LAYER_DISTANCE/10;
    float maxVisibleDepth = 2f;
    final float MIN_VISIBLE_DEPTH = -LAYER_DISTANCE;
    final float MAX_VISIBLE_DEPTH_CHANGE_VALUE = 0.3f;
    final float EPSILON = 0.01f;
    Point startPointOfTimeLine = new Point(0,0), trianglePoint = new Point(0,0);
    Point startPointOfChartTimeLine = new Point(0,0);
    /////
    int topLayerIndex=0, targetLayerIndex=0 /*if equals to topLayerIndex it means no animation is running*/;
    float topLayerOffset;
    Dimension topIdealLayerDimention = new Dimension(0,0);
    Point topIdealLayerCenterPos = new Point(0,0);
    final int INVALID = -1;
    int lastHighlightVirtualWindowIndex=-1, currentMouseHoveredIndex =INVALID;
    ///////// ++ UI
    final boolean COLORFUL = false;
    final int TOP_BAR_HEIGHT = 25;
    final int VIRTUAL_WINDOW_BORDER_TICKNESS = 1;
    final int TIME_LINE_WIDTH = 3;
    ////////

    enum ChartType{NONE, METRIC1, METRIC2};
    ChartType currentChartType = ChartType.NONE;


    TTMSingleFileView TTMWindow = null;
    Project project;
    CustomEditorTextField mainEditorWindow = null;
    ArrayList<CommitWrapper> commitList = null;
    VirtualEditorWindow[] virtualEditorWindows = null;
    VirtualFile virtualFile;



    public Commits3DView( Project project, VirtualFile virtualFile, ArrayList<CommitWrapper> commitList, TTMSingleFileView TTMWindow)
    {
        super();

        this.TTMWindow = TTMWindow;
        this.project = project;
        this.virtualFile = virtualFile;
        this.commitList = commitList;

        this.setLayout(null);
        this.addComponentListener(this); // Check class definition as : ".. implements ComponentListener"
        if (CommonValues.IS_UI_IN_DEBUGGING_MODE)   this.setBackground(Color.ORANGE);
        this.setOpaque(true);

        setupUI_mainEditorWindow();
        setupUI_virtualWindows();
        initialVirtualWindowsVisualizations(); // initial 3D Variables

        componentResized(null); //to "updateTopIdealLayerBoundary()" and then "updateEverythingAfterComponentResize()"

        playing3DAnimationTimer = new Timer(TICK_INTERVAL_MS, new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                tick(TICK_INTERVAL_MS/1000.f);
                repaint();
            }
        });

        addMouseWheelListener();
        addMouseMotionListener();
        addMouseListener();
    }


    private void addMouseMotionListener()
    {
        this.addMouseMotionListener(new MouseMotionListener()
        {
            @Override
            public void mouseDragged(MouseEvent e)
            {
            }

            @Override
            public void mouseMoved(MouseEvent e)
            {
                Point currentPoint = e.getPoint();

                for (int i=0; i<virtualEditorWindows.length; i++)
                {
                    if(virtualEditorWindows[i].isVisible==false) continue;

                    Rectangle r= new Rectangle(virtualEditorWindows[i].drawingRect.x-virtualEditorWindows[i].drawingRect.width/2,
                            virtualEditorWindows[i].drawingRect.y-virtualEditorWindows[i].drawingRect.height/2,
                            virtualEditorWindows[i].drawingRect.width,
                            virtualEditorWindows[i].drawingRect.height);
                    if(r.contains(currentPoint))
                    {
                        if(currentMouseHoveredIndex ==i)
                            return;
                        if(currentMouseHoveredIndex !=INVALID)
                            virtualEditorWindows[currentMouseHoveredIndex].setTemporaryHighlightTopBar(false,DUMMY_COLOR);
                        virtualEditorWindows[i].setTemporaryHighlightTopBar(true, Color.orange);
                        currentMouseHoveredIndex =i;
                        repaint();
                        return;
                    }
                }

                // Here = mouse hovred no virtualWindows
                if(currentMouseHoveredIndex !=INVALID)
                {
                    virtualEditorWindows[currentMouseHoveredIndex].setTemporaryHighlightTopBar(false, DUMMY_COLOR);
                    currentMouseHoveredIndex = INVALID;
                    repaint();
                }
            }
        });
    }

    private void addMouseListener()
    {
        this.addMouseListener(new MouseListener()
        {
            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e)
            {
                if(currentMouseHoveredIndex!= INVALID)
                {
                    TTMWindow.activeCommit_cIndex = currentMouseHoveredIndex;
                    showCommit(currentMouseHoveredIndex, true);
                    TTMWindow.commitsBar.setActiveCommit_cIndex();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
            }
        });
    }

    private void addMouseWheelListener()
    {
        this.addMouseWheelListener(new MouseWheelListener()
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e)
            {
                int notches = e.getWheelRotation();
                if (notches < 0)
                {
                    //Mouse wheel moved UP for -1*notches

                    int activeCommit_cIndex = TTMWindow.activeCommit_cIndex;
                    //int activeCommit_cIndex = targetLayerIndex;
                    if(activeCommit_cIndex+1 >= commitList.size()) return;
                    activeCommit_cIndex++;
                    TTMWindow.activeCommit_cIndex = activeCommit_cIndex;
                    showCommit(activeCommit_cIndex, true);
                    TTMWindow.commitsBar.setActiveCommit_cIndex();
                }
                else
                {
                    //int activeCommit_cIndex = targetLayerIndex;
                    int activeCommit_cIndex = TTMWindow.activeCommit_cIndex;
                    if(activeCommit_cIndex-1 <0) return;
                    activeCommit_cIndex--;
                    TTMWindow.activeCommit_cIndex = activeCommit_cIndex;
                    showCommit(activeCommit_cIndex, true);
                    TTMWindow.commitsBar.setActiveCommit_cIndex();
                }
            }
        });
    }

    private void setupUI_mainEditorWindow()
    {
        //mainEditorWindow = new CustomEditorTextField(FileDocumentManager.getInstance().getDocument(virtualFile), project, FileTypeRegistry.getInstance().getFileTypeByExtension("java"),true,false);
        mainEditorWindow = new CustomEditorTextField("",project, FileTypeRegistry.getInstance().getFileTypeByExtension("java"));
        //mainEditorWindow.setBounds(100,100,100,50); //TEST
        mainEditorWindow.setEnabled(true);
        mainEditorWindow.setOneLineMode(false);
        //mainEditorWindow.setOpaque(true);
        this.add(mainEditorWindow); // As there's no layout, we should set Bound it. we'll do this in "ComponentResized()" event
    }

    private void setupUI_virtualWindows()
    {
        virtualEditorWindows = new VirtualEditorWindow[commitList.size()];

        for (int i = 0; i< commitList.size() ; i++)
        {
            virtualEditorWindows[i] = new VirtualEditorWindow(i /*not cIndex*/, commitList.get(i));
        }
    }

    private void updateVirtualWindowsBoundaryAfterComponentResize()
    {
        int xCenter, yCenter, w, h;
        w = topIdealLayerDimention.width;
        h = topIdealLayerDimention.height;
        xCenter = topIdealLayerCenterPos.x;
        yCenter = topIdealLayerCenterPos.y;

        for (int i = 0; i< commitList.size() ; i++)
            virtualEditorWindows[i].setDefaultValues(xCenter, yCenter, w, h);
    }

    private void initialVirtualWindowsVisualizations()
    {
        topLayerOffset = 0;
        topLayerIndex = lastHighlightVirtualWindowIndex = 0;

        // Don't forget to call `updateVirtualWindowsBoundryAfterComponentResize()` before
        for (int i = 0; i< commitList.size() ; i++)
        {
            float d = i * LAYER_DISTANCE;
            virtualEditorWindows[i].updateDepth(d);
        }

        highlight(topLayerIndex);
    }

    public void increaseMaxVisibleDepth()
    {
        changeMaxVisibleDepth(MAX_VISIBLE_DEPTH_CHANGE_VALUE);
    }

    public void decreaseMaxVisibleDepth()
    {
        changeMaxVisibleDepth(-1*MAX_VISIBLE_DEPTH_CHANGE_VALUE);
    }

    private void changeMaxVisibleDepth(float delta)
    {
        if(maxVisibleDepth+delta<=LAYER_DISTANCE) return;
        maxVisibleDepth += delta;
        render();
    }

    private void highlight( int virtualWindowIndex)
    {
        virtualEditorWindows[lastHighlightVirtualWindowIndex].setHighlightBorder(false);

        virtualEditorWindows[virtualWindowIndex].setHighlightBorder(true);

        lastHighlightVirtualWindowIndex = virtualWindowIndex;
    }

    @Override
    public void componentResized(ComponentEvent e)
    {
        Dimension size = getSize();
        centerOfThisComponent = new Point(size.width/2, size.height/2);
        //////
        updateTopIdealLayerBoundary();
        //virtualEditorWindows[topLayerIndex].setHighlightBorder(); // Why here?
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
        Graphics2D g2d = (Graphics2D) g;

        super.paintComponent(g);

        if(CommonValues.IS_UI_IN_DEBUGGING_MODE)
        {
            g.setColor(new Color(0,255,255));
            g.fillRect(0, 0,getSize().width,getSize().height);
            g.setColor(new Color(255,0,0));
            g.fillOval(getSize().width/2-10, getSize().height/2-10,20,20); //Show Center
        }

        if(virtualEditorWindows!=null)
        {
            draw_tipOfTimeLine(g2d);

            if(currentChartType!=ChartType.NONE)
            {

                switch (currentChartType)
                {
                    case METRIC1:
                        g.drawString("LOC", startPointOfChartTimeLine.x+10, startPointOfChartTimeLine.y - 30);
                        break;
                    case METRIC2:
                        g.drawString("Cyclomatic", startPointOfChartTimeLine.x+10, startPointOfChartTimeLine.y - 30);
                }

            }

            for(int i = commitList.size()-1; i>=0; i--)
            {
                if(virtualEditorWindows[i].isVisible==false) continue;
//                if(i==targetLayerIndex)
//                {
//                    if(mainEditorWindow!=null)
//                    {
//                        mainEditorWindow.paint(g);
//                        mainEditorWindow.paintComponents(g);
//                    }
//                }
                virtualEditorWindows[i].draw(g);



                ////////////////////////  (Left) TimeLine
                Point timeLineMyPoint = virtualEditorWindows[i].timeLinePoint;
                // TimeLine Lines
                if(i != commitList.size()-1 && virtualEditorWindows[i+1].isVisible)
                {
                    // I'm not the first one in for-loop (OR) I'm not the oldest point of time
                    Point timeLineNextPoint = virtualEditorWindows[i+1].timeLinePoint; // Line between myPoint and NextPoint (=Newer commit = Closer to Camera)
                    g.setColor(new Color(0,0,255,virtualEditorWindows[i].alpha));
                    g.drawLine(timeLineMyPoint.x, timeLineMyPoint.y, timeLineNextPoint.x, timeLineNextPoint.y);
                }
                // TimeLine Point
                if(i==targetLayerIndex)
                    g.setColor(new Color(255,0,0,virtualEditorWindows[i].alpha));
                else
                    g.setColor(new Color(0,0,255,virtualEditorWindows[i].alpha));
                g2d.setStroke(new BasicStroke(TIME_LINE_WIDTH));

                final Dimension TIME_LINE_POINT_SIZE = new Dimension(10,4);

                g.fillRoundRect(timeLineMyPoint.x-TIME_LINE_POINT_SIZE.width/2, timeLineMyPoint.y-TIME_LINE_POINT_SIZE.height/2,
                        TIME_LINE_POINT_SIZE.width,TIME_LINE_POINT_SIZE.height,1,1);

                g.fillRoundRect(timeLineMyPoint.x-TIME_LINE_POINT_SIZE.width/2, timeLineMyPoint.y-TIME_LINE_POINT_SIZE.height/2,
                        TIME_LINE_POINT_SIZE.width,TIME_LINE_POINT_SIZE.height,1,1);

                g2d.setFont(new Font("Arial",Font.BOLD, 10));
                g.drawString( CalendarHelper.convertDateToString(commitList.get(i).getDate()) , timeLineMyPoint.x-68, timeLineMyPoint.y+2);


                ////////////////////////  (Right) Chart
                if(currentChartType==ChartType.NONE) continue;

                Point chartTimeLineMyPoint = virtualEditorWindows[i].chartTimeLinePoint;
                Point chartTimeLineMyValuePoint = virtualEditorWindows[i].getChartValuePoint(currentChartType);


                // TimeLine Lines
                if(i != commitList.size()-1 && virtualEditorWindows[i+1].isVisible)
                {
                    // I'm not the first one in for-loop (OR) I'm not the oldest point of time
                    Point chartTimeLineNextPoint = virtualEditorWindows[i+1].chartTimeLinePoint;
                    Point chartTimeLineNextValuePoint = virtualEditorWindows[i+1].getChartValuePoint(currentChartType);

                    g2d.setStroke(new BasicStroke(TIME_LINE_WIDTH));
                    g.drawLine(chartTimeLineMyPoint.x, chartTimeLineMyPoint.y, chartTimeLineNextPoint.x, chartTimeLineNextPoint.y);
                    g.setColor(new Color(0, 0, 0, virtualEditorWindows[i].alpha));
                    g.drawLine(chartTimeLineMyValuePoint.x, chartTimeLineMyValuePoint.y, chartTimeLineNextValuePoint.x, chartTimeLineNextValuePoint.y);
                }

                // ChartTimeLine Point
                g.fillRoundRect(chartTimeLineMyPoint.x-TIME_LINE_POINT_SIZE.width/2, chartTimeLineMyPoint.y-TIME_LINE_POINT_SIZE.height/2,
                        TIME_LINE_POINT_SIZE.width,TIME_LINE_POINT_SIZE.height,1,1);

                Color metricC = virtualEditorWindows[i].getMetricColor(currentChartType);
                g.setColor(new Color(metricC.getRed(),metricC.getGreen(),metricC.getBlue(),virtualEditorWindows[i].alpha));

                //Vertical Value Line
                g.drawLine(chartTimeLineMyPoint.x, chartTimeLineMyPoint.y, chartTimeLineMyValuePoint.x, chartTimeLineMyValuePoint.y);
                // Point on Value Height
                g.fillOval(chartTimeLineMyValuePoint.x-TIME_LINE_POINT_SIZE.width/2, chartTimeLineMyValuePoint.y-TIME_LINE_POINT_SIZE.height/2,
                        TIME_LINE_POINT_SIZE.width,TIME_LINE_POINT_SIZE.height);
            }
        }


        //g.drawString(debuggingText,20,20);
    }

    private void draw_tipOfTimeLine(Graphics2D g2d)
    {
        //// Line from tip of TimeLine (ACTUALLY: startPoint+0.1depth) of time line to Triangle
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(TIME_LINE_WIDTH));
        g2d.drawLine(startPointOfTimeLine.x, startPointOfTimeLine.y, trianglePoint.x, trianglePoint.y);

        //// Triangle
        int[] triangleVertices_x = new int[]{trianglePoint.x-6,trianglePoint.x-14,trianglePoint.x+4};
        int[] triangleVertices_y = new int[]{trianglePoint.y-2,trianglePoint.y+10,trianglePoint.y+3};
        g2d.fillPolygon(triangleVertices_x, triangleVertices_y, 3);
    }

    private void tick(float dt_sec)
    {

        float targetDepth = virtualEditorWindows[targetLayerIndex].depth;
        float targetDepthAbs = Math.abs(virtualEditorWindows[targetLayerIndex].depth);
        int sign = (int) Math.signum(targetDepth);

        float speed_depthPerSec = 0;

        if(targetDepthAbs<=LAYERS_DEPTH_ERROR_THRESHOLD)
            speed_depthPerSec = targetDepth/dt_sec;
        else if(targetDepthAbs<=LAYER_DISTANCE)
            speed_depthPerSec = 3*LAYER_DISTANCE*sign;
        else if(targetDepthAbs<4*LAYER_DISTANCE)
            speed_depthPerSec = 4*LAYER_DISTANCE*sign;
        else if(targetDepthAbs<5)
            speed_depthPerSec = 2*sign;
        else
            speed_depthPerSec = 5*sign;

        debuggingText = "> "+targetDepth+" > Speed: "+speed_depthPerSec;


        /*/ Slow for debugging
        if(targetDepthAbs<LAYER_DISTANCE)
            speed_depthPerSec = 0.05f*sign;
        else
            speed_depthPerSec = 1*sign;*/

        float deltaDepth = dt_sec * speed_depthPerSec;


        int indexCorrespondingToLowestNonNegativeDepth=-1;
        float lowestNonNegativeDepth=Float.MAX_VALUE;

        for(int i=0; i<commitList.size(); i++)
        {
            float newDepth = virtualEditorWindows[i].depth - deltaDepth;

            virtualEditorWindows[i].updateDepth(newDepth);


            if(newDepth>=0 && newDepth<lowestNonNegativeDepth)
            {
                lowestNonNegativeDepth = newDepth;
                indexCorrespondingToLowestNonNegativeDepth = i;
            }
        }


        topLayerIndex = indexCorrespondingToLowestNonNegativeDepth;
        TTMWindow.updateCommits3DViewActiveRangeOnTimeLine(virtualEditorWindows[topLayerIndex].cIndex);

        targetDepth = virtualEditorWindows[targetLayerIndex].depth;
        if(targetDepth>=0 && targetDepth<0.03)
        {
            // Assert topLayerIndex == targetLayerIndex;
            debuggingText = "<>";
            stopAnimation();
        }


        return;




        /*final int LAST_STEP_SPEED = 4;
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


        ////// TEMP ///////////////
        ////// TEMP ///////////////
        ////// TEMP ///////////////
        numberOfPassingLayersPerSec_forAnimation = sign*4;


        // TODO: maybe we overpass the target cIndex commit
        topLayerOffset += numberOfPassingLayersPerSec_forAnimation * dt_sec * LAYER_DISTANCE;

        // When: numberOfPassingLayersPerSec_forAnimation is NEGATIVE
        // When: Moving direction FROM screen
        // currentCommitIndex = 0 ===> targetCommitIndex = 10
        if(topLayerOffset < 0)
        {
            // TODO: Still the result of sum may be negative
            topLayerOffset = (topLayerOffset+LAYER_DISTANCE)%LAYER_DISTANCE; // Here we may need to pass two layers
            topLayerIndex++;                                                // Here we may need to pass two layers
            //assert topLayerIndex >= commitList.size(); // TODO
            //if(topLayerIndex >= commitList.size())
            //    topLayerIndex=0;
        }

        // When: numberOfPassingLayersPerSec_forAnimation is POSITIVE
        // When: Moving direction INTO screen
        if(topLayerOffset > LAYER_DISTANCE)
        {
            topLayerOffset = topLayerOffset%LAYER_DISTANCE; // Here we may need to pass two layers
            topLayerIndex--;                                // Here we may need to pass two layers
            //assert topLayerIndex < 0; // TODO
            //if(topLayerIndex < 0)
            //    topLayerIndex=commitList.size()-1;
        }

        for(int i=0; i<commitList.size(); i++)
        {
            virtualEditorWindows[i].updateDepth((i-topLayerIndex)*LAYER_DISTANCE + topLayerOffset);
            if(virtualEditorWindows[i].depth<0 || virtualEditorWindows[i].depth>maxVisibleDepth)
                virtualEditorWindows[i].isVisible=false;
            else
                virtualEditorWindows[i].isVisible=true;

            //int layerIndex_ith_after_topLayer = (topLayerIndex+i)%commitList.size();

            //if(layerIndex_ith_after_topLayer < topLayerIndex
                    //|| layerIndex_ith_after_topLayer>topLayerIndex + maxVisibleDepth)
                //virtualEditorWindows[layerIndex_ith_after_topLayer].isVisible = false;
            //else
                //virtualEditorWindows[layerIndex_ith_after_topLayer].isVisible = true;

            //virtualEditorWindows[layerIndex_ith_after_topLayer].updateDepth(i*LAYER_DISTANCE + topLayerOffset);
        }

        float d = virtualEditorWindows[targetLayerIndex].depth - 0;
        float abs = Math.abs(d);
        if(abs<0.1)
        {
            stopAnimation();
            virtualEditorWindows[topLayerIndex].setHighlightBorder();
        }*/
    }

    public void showCommit(int newCommit_cIndex, boolean withAnimation) // TODO: without animation
    {
        if(withAnimation==false)
        {
            // TODO
            //loadMainEditorWindowContent();
            //virtualEditorWindows[topLayerIndex].setHighlightBorder();
            // TODO: Arrange VirtualEditorWindows
        }
        else
        {
            if( targetLayerIndex==newCommit_cIndex) //TODO : is cIndex ?
                return;
            this.targetLayerIndex = newCommit_cIndex;

            highlight(targetLayerIndex);

            if(!playing3DAnimationTimer.isRunning())
                playing3DAnimationTimer.start();

            mainEditorWindow.setVisible(false);
        }
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
    }

    public void render()
    {
        updateTimeLineDrawing();

        for(int i=0; i<commitList.size(); i++)
        {
            float d = virtualEditorWindows[i].depth;
            virtualEditorWindows[i].updateDepth(d);

        }
        repaint();
    }

    private void loadMainEditorWindowContent()
    {
        String content = getStringFromCommits(topLayerIndex);

        ApplicationManager.getApplication().invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                mainEditorWindow.setText(content);
            }
        });

        updateMainEditorWindowBoundaryAfterComponentResize();
        mainEditorWindow.setCaretPosition(0); // TODO: Doesn't Work
        mainEditorWindow.setVisible(true);

    }

    private void updateMainEditorWindowBoundaryAfterComponentResize()
    {
        int x,y,w,h;
        w = virtualEditorWindows[topLayerIndex].drawingRect.width-2*VIRTUAL_WINDOW_BORDER_TICKNESS;
        h = virtualEditorWindows[topLayerIndex].drawingRect.height-TOP_BAR_HEIGHT;
        x = virtualEditorWindows[topLayerIndex].drawingRect.x-w/2+VIRTUAL_WINDOW_BORDER_TICKNESS;
        y = virtualEditorWindows[topLayerIndex].drawingRect.y-h/2+TOP_BAR_HEIGHT/2;
        //mainEditorWindow.setSize(w,h);
        //mainEditorWindow.setLocation(x,y);
        mainEditorWindow.setBounds(x,y,w,h);
    }

    private void updateTopIdealLayerBoundary()
    {
        final int FREE_SPACE_VERTICAL = 100, FREE_SPACE_HORIZONTAL = 60;
        topIdealLayerDimention = new Dimension(  getSize().width/2, 2*getSize().height/3 /*2/3 of whole vertical*/);
        topIdealLayerDimention.width *= MyRenderer.getInstance().BASE_DEPTH; // because Renderer divide it by BASE_DEPTH
        topIdealLayerDimention.height *= MyRenderer.getInstance().BASE_DEPTH;

        topIdealLayerCenterPos = new Point(centerOfThisComponent.x, 2*getSize().height/3 /*Fit from bottom*/);
        ////
        updateEverythingAfterComponentResize();
    }

    private void updateEverythingAfterComponentResize()
    {
        updateVirtualWindowsBoundaryAfterComponentResize();
        updateMainEditorWindowBoundaryAfterComponentResize();
        updateTimeLineDrawing();
    }

    private void updateTimeLineDrawing()
    {
        startPointOfTimeLine = MyRenderer.getInstance().calculateTimeLinePoint(topIdealLayerCenterPos.x, topIdealLayerCenterPos.y,
                                                                        topIdealLayerDimention.width, topIdealLayerDimention.height,
                                                                        0.05f+MyRenderer.getInstance().BASE_DEPTH);


        Point aLittleAfterstartPointOfTimeLine = MyRenderer.getInstance().calculateTimeLinePoint(topIdealLayerCenterPos.x, topIdealLayerCenterPos.y,
                                                                        topIdealLayerDimention.width, topIdealLayerDimention.height,
                                                                        +0.4f+MyRenderer.getInstance().BASE_DEPTH);

        int deltaX = startPointOfTimeLine.x - aLittleAfterstartPointOfTimeLine.x;
        int deltaY = startPointOfTimeLine.y - aLittleAfterstartPointOfTimeLine.y;
        trianglePoint = (Point) startPointOfTimeLine.clone();
        trianglePoint.x += deltaX; //
        trianglePoint.y += deltaY;

        startPointOfChartTimeLine = MyRenderer.getInstance().calculateChartTimeLinePoint(topIdealLayerCenterPos.x, topIdealLayerCenterPos.y,
                topIdealLayerDimention.width, topIdealLayerDimention.height,
                0+MyRenderer.getInstance().BASE_DEPTH);
    }

    public void setTopBarHighlight(int cIndex, boolean newStatus, Color c)
    {
        virtualEditorWindows[cIndex].setHighlightTopBar(newStatus, c);
        repaint();
    }

    public void setChartType(ChartType newChartType)
    {
        currentChartType = newChartType;
        repaint();
    }

    protected class VirtualEditorWindow
    {
        final float Y_OFFSET_FACTOR = 250;
        ////////
        int cIndex =-1;
        private int someRandomMetric1 = 0, someRandomMetric2 = 0;
        private Color someRandomMetric1Color = Color.BLACK, someRandomMetric2Color = Color.BLACK;
        CommitWrapper commitWrapper = null;

        boolean isVisible=true;
        float depth;
        int alpha=255;
        Color DEFAULT_BORDER_COLOR = Color.GRAY;
        Color DEFAULT_TOP_BAR_COLOR = Color.GRAY;

        Color myColor=Color.WHITE, myBorderColor=DEFAULT_BORDER_COLOR, myTopBarColor=DEFAULT_TOP_BAR_COLOR;
        Color myTopBarTempColor;
        boolean isTopBarTempColorValid = false;
        int xCenterDefault, yCenterDefault, wDefault, hDefault;
        Rectangle drawingRect = new Rectangle(0, 0, 0, 0);
        Point timeLinePoint = new Point(0,0), chartTimeLinePoint = new Point(0,0);
        //private Point chartValuePoint= new Point(0,0);
        ////////

        public VirtualEditorWindow(int index, CommitWrapper commitWrapper)
        {
            this.cIndex = index;
            this.commitWrapper = commitWrapper;

            someRandomMetric1 = ThreadLocalRandom.current().nextInt(5, 100);
            someRandomMetric2 = ThreadLocalRandom.current().nextInt(5, 100);

            if(someRandomMetric1<30)
                someRandomMetric1Color = Color.GREEN;
            else if(someRandomMetric1<70)
                someRandomMetric1Color = Color.YELLOW;
            else
                someRandomMetric1Color = Color.RED;

            if(someRandomMetric2<30)
                someRandomMetric2Color = Color.GREEN;
            else if(someRandomMetric2<70)
                someRandomMetric2Color = Color.YELLOW;
            else
                someRandomMetric2Color = Color.RED;

            if(COLORFUL || CommonValues.IS_UI_IN_DEBUGGING_MODE)
            {
                Random rand = new Random();
                float r = rand.nextFloat();
                float g = rand.nextFloat();
                float b = rand.nextFloat();
                this.myColor = new Color(r,g,b);
            }
        }

        public Point getChartValuePoint(ChartType c)
        {
            Point p = (Point) chartTimeLinePoint.clone();
            int MAX_UI_HEIGHT = 200;
            float v = getMetricValue(c)*MAX_UI_HEIGHT/100.f;
            v = MyRenderer.getInstance().render3DTo2D((int)v,depth+MyRenderer.getInstance().BASE_DEPTH);
            p.y -= (int)v;
            return p;
        }

        // this function should be called on each size change
        public void setDefaultValues(int xCenterDefault, int yCenterDefault, int wDefault, int hDefault)
        {
            if(wDefault<=0 || hDefault<=0) return; //Window is not intialized corerctly yet

            this.xCenterDefault = xCenterDefault;
            this.yCenterDefault = yCenterDefault;
            this.wDefault = (int) (wDefault);
            this.hDefault = (int) (hDefault);

            // Now update Boundary according to current depth and new DefaultValues
            updateDepth(depth);

        }

        public void setAlpha(int newAlpha)
        {
            if(newAlpha>255)
                newAlpha=255;
            if(newAlpha<0)
                newAlpha=0;
            alpha = newAlpha;
            myBorderColor = new Color(myBorderColor.getRed(), myBorderColor.getGreen(), myBorderColor.getBlue(), alpha);
            if(!isTopBarTempColorValid)
                myTopBarColor = new Color(myTopBarColor.getRed(), myTopBarColor.getGreen(), myTopBarColor.getBlue(), alpha);
            else
                myTopBarTempColor = new Color(myTopBarTempColor.getRed(), myTopBarTempColor.getGreen(), myTopBarTempColor.getBlue(), alpha);
            myColor = new Color(myColor.getRed(), myColor.getGreen(), myColor.getBlue(), alpha);
        }

        public void updateDepth(float depth)
        {
            this.depth = depth;

            if(depth<MIN_VISIBLE_DEPTH || depth> maxVisibleDepth)
                isVisible=false;
            else
                isVisible=true;

            if(isVisible)
                doRenderCalculation();
        }

        public void doRenderCalculation()
        {
            float renderingDepth = depth + MyRenderer.getInstance().BASE_DEPTH;
            Rectangle rect = new Rectangle(0, 0, 0, 0);

            //////////////// Alpha
            int newAlpha;
            if(depth>0)
                newAlpha = (int)(255*(1-depth/ maxVisibleDepth));
            else
                // By adding LAYERS_DEPTH_ERROR_THRESHOLD we make the layer invisible(alpha=0) before getting depth=-LAYER_DISTANCE
                // It's needed because sometimes layers movement stops while the new top layer is 0+e (not 0) and so the layer
                // which was supposed to go out (go to depth -LAYER_DISTANCE) get stuck at depth = -LAYERDISTANCE+e.
                newAlpha = (int) (255*(1-depth/(MIN_VISIBLE_DEPTH+LAYERS_DEPTH_ERROR_THRESHOLD+EPSILON)));
            setAlpha(newAlpha);


            //////////////// Size
            rect.width = MyRenderer.getInstance().render3DTo2D(wDefault, renderingDepth);
            rect.height = MyRenderer.getInstance().render3DTo2D(hDefault, renderingDepth);
            Point p = MyRenderer.getInstance().render3DTo2D(xCenterDefault, yCenterDefault, renderingDepth);
            rect.x = p.x;
            rect.y = p.y;
            drawingRect = rect;


            ////////////// TimeLine
            // We also could use "MyRenderer.getInstance().calculateTimeLinePoint()". But it's worthless and that function
            // is designed for external user ( check 'updateTimeLineDrawing()' function)
            timeLinePoint = MyRenderer.getInstance().render3DTo2D(xCenterDefault, yCenterDefault, renderingDepth);
            timeLinePoint.x = rect.x - (int)(MyRenderer.getInstance().TIME_LINE_GAP*drawingRect.width/2);
            timeLinePoint.y = rect.y - drawingRect.height/2;

            ///////////// Chart TimeLine
            chartTimeLinePoint = MyRenderer.getInstance().render3DTo2D(xCenterDefault, yCenterDefault, renderingDepth);
            chartTimeLinePoint.x = rect.x + (int)(MyRenderer.getInstance().TIME_LINE_GAP*drawingRect.width/2);
            chartTimeLinePoint.y = rect.y - drawingRect.height/2;
        }

        public void setHighlightBorder(boolean newStatus)
        {
            if(newStatus==true)
                myBorderColor = Color.RED;
            else
                myBorderColor = DEFAULT_BORDER_COLOR;

            setAlpha(alpha); //Apply current alpha to above solid colors
        }

        public void setTemporaryHighlightTopBar(boolean newStatus, Color c)
        {
            if(newStatus==true)
            {
                myTopBarTempColor = c;
                isTopBarTempColorValid = true;
            }
            else
            {
                isTopBarTempColorValid = false;
            }

            setAlpha(alpha); //Apply current alpha to above solid colors
        }
        public void setHighlightTopBar(boolean newStatus, Color c)
        {
            if(newStatus==true)
                myTopBarColor = c;
            else
                myTopBarColor = DEFAULT_TOP_BAR_COLOR;

            setAlpha(alpha); //Apply current alpha to above solid colors
        }

        public void setHighlightTopBar(boolean newStatus)
        {
           setHighlightTopBar(newStatus, Color.ORANGE);
        }

        public void draw(Graphics g)
        {
            Graphics2D g2d = (Graphics2D) g;
            if(this.isVisible!=true) return;

            int x,y,w,h; //TODO: Create drawingRect not centered and make a function for below
            w = this.drawingRect.width;
            h = this.drawingRect.height;
            x = this.drawingRect.x - w/2;
            y = this.drawingRect.y - h/2;

           draw_mainRect(g2d, x, y, w, h);
           draw_mainRectBorder(g2d, x, y, w, h);
           draw_topBar(g2d, x, y, w);
           draw_topBarText(g, x, y, w);
        }

        private void draw_topBarText(Graphics g, int x, int y, int w)
        {
            /// Name
            String text="";
            Graphics g2 = g.create();
            g2.setColor(new Color(0,0,0,alpha));
            Rectangle2D rectangleToDrawIn = new Rectangle2D.Double(x,y,w,TOP_BAR_HEIGHT);
            g2.setClip(rectangleToDrawIn);
            if(cIndex ==topLayerIndex)
            {
                g2.setFont(new Font("Courier", Font.BOLD, 10));
                text = getTopBarMessage();
                //text = "I: "+cIndex+"Depth = "+ Float.toString(depth)+ "FontSize: --  "+ "Alpha: "+alpha;
                DrawingHelper.drawStringCenter(g2, text, x+w/2, y+8);
                String path = virtualFile.getPath();
                path = fit(path, 70, g2);
                text = new String(path);
                DrawingHelper.drawStringCenter(g2, text, x+w/2, y+18);
            }
            else
            {
                float fontSize = 20.f/(MyRenderer.getInstance().BASE_DEPTH+depth);
                g2.setFont(new Font("Courier", Font.BOLD, (int)fontSize));
                text = getTopBarMessage();
                //text = "I: "+cIndex+"Depth = "+ Float.toString(depth)+ "FontSize: "+fontSize + "Alpha: "+alpha;
                DrawingHelper.drawStringCenter(g2, text, x+w/2, y+15);
            }
        }

        private void draw_topBar(Graphics2D g2d, int x, int y, int w)
        {
            /// TopBar
            if(!isTopBarTempColorValid)
                g2d.setColor( this.myTopBarColor);
            else
                g2d.setColor( this.myTopBarTempColor);
            g2d.fillRect(x, y+VIRTUAL_WINDOW_BORDER_TICKNESS, w, TOP_BAR_HEIGHT);
        }

        private void draw_mainRectBorder(Graphics2D g2d, int x, int y, int w, int h)
        {
            /// Border
            g2d.setStroke(new BasicStroke(2));
            g2d.setColor( this.myBorderColor);
            g2d.drawRect(x, y, w, h);
        }

        private void draw_mainRect(Graphics2D g2d, int x, int y, int w, int h)
        {
            /// Rect
            g2d.setColor( this.myColor);
            g2d.fillRect(x, y, w, h);
        }

        public int getMetricValue(ChartType c)
        {
            switch (c)
            {
                case METRIC1:
                    return someRandomMetric1;
                case METRIC2:
                    return  someRandomMetric2;
            }
            return 0;
        }

        public Color getMetricColor(ChartType c)
        {
            switch (c)
            {
                case METRIC1:
                    return someRandomMetric1Color;
                case METRIC2:
                    return  someRandomMetric2Color;
            }
            return Color.BLACK;
        }

        private String getTopBarMessage()
        {
            String text;
            if(commitWrapper.isFake())
                text = new String(commitWrapper.getCommitID());
            else
                text = new String("Commit "+commitWrapper.getCommitID());
            return text;
        }

        private String fit(String s, int maxCharacterCount, Graphics g)
        {
            String result = s;
            int extraCharacterCount = result.length() - maxCharacterCount;
            int cropFrom = result.indexOf('/', extraCharacterCount);
            result = result.substring(cropFrom);
            result = "..."+result;
            return result;
        }


    } // End of VirtualEditorWindow class

    class CustomEditorTextField extends EditorTextField
    {
        // >>>>>>>> Scroll for EditorTextField
        // https://intellij-support.jetbrains.com/hc/en-us/community/posts/206759275-EditorTextField-and-surrounding-JBScrollPane

        final Rectangle INVISBLE_BOUND_RECT = new Rectangle(-100, -100, 0,0);
        Rectangle lastBoundBeforeInvisible;
        boolean isVisible=true;

        public CustomEditorTextField(Document document, Project project, FileType fileType, boolean isViewer, boolean oneLineMode)
        {
            super(document,project,fileType,isViewer,oneLineMode);
        }

        public CustomEditorTextField(@NotNull String text, Project project, FileType fileType) {
            this(EditorFactory.getInstance().createDocument(text), project, fileType, true, true);
        }

        @Override
        protected EditorEx createEditor()
        {
            EditorEx editor = super.createEditor();
            editor.setVerticalScrollbarVisible(true);
            editor.setHorizontalScrollbarVisible(true);
            addLineNumberToEditor(editor);
            return editor;
        }

        private void addLineNumberToEditor(EditorEx editor)
        {
            EditorSettings settings = editor.getSettings();
            settings.setLineNumbersShown(true);
            editor.reinitSettings();
        }

        public void setVisible(boolean newStatus)
        {
            // if we use normal behaviour of setVisible(), while visibility is False the KeyBinding doesn't work strangely.
            if(isVisible == newStatus) return;

            if(newStatus==false)
            {
                isVisible = false;
                lastBoundBeforeInvisible = this.getBounds();
                setBounds(INVISBLE_BOUND_RECT);
            }
            else
            {
                isVisible = true;
                setBounds(lastBoundBeforeInvisible);
            }



        }

    }

} // End of Commits3DView class
