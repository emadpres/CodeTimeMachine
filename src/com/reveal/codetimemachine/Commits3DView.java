package com.reveal.codetimemachine;

import com.github.mauricioaniche.ck.CKNumber;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.ui.EditorTextField;
import com.reveal.metrics.CKNumberReader;
import com.reveal.metrics.MaxCKNumber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.*;

public class Commits3DView extends JComponent implements ComponentListener
{
    ///////// ++ Constant ++ /////////
    ///////// -- Constant -- /////////

    ///////// ++ UI ++ /////////

    Point centerOfThisComponent;
    ///////// -- UI -- /////////

    String debuggingText = "";

    //////// ++ Timer and Timing
    Timer playing3DAnimationTimer=null, mouseHovredItemTimer=null;
    final int TICK_INTERVAL_MS = 50;
    ///////
    final Color DUMMY_COLOR = Color.black;
    static final Color SHARP_RED = new Color(233,80,100);
    static final Color SHARP_GREEN = new Color(91,205,120);
    static final Color SHARP_YELLOW = new Color(216,190,103);
    final Color ACTIVE_WINDOW_COLOR = SHARP_RED;
    final Dimension TIME_LINE_POINT_SIZE = new Dimension(10, 4);
    final int MAX_CHART_BAR_HEIGHT_PX = 200;
    ///////// ++ UI: 3D Prespective Variables ++ /////////
    final float LAYER_DISTANCE = 0.2f;
    final float LAYERS_DEPTH_ERROR_THRESHOLD = LAYER_DISTANCE/10;
    float maxVisibleDepth = 2f;
    final float MIN_VISIBLE_DEPTH = -LAYER_DISTANCE;
    final float MAX_VISIBLE_DEPTH_CHANGE_VALUE = 0.3f;
    final float EPSILON = 0.01f;
    Point startPointOfTimeLine = new Point(0,0), trianglePoint = new Point(0,0);
    Point startPointOfChartTimeLine = new Point(0,0);
    boolean alwaysShowMetricsValue = false;
    Point currentMousePoint = new Point(-100,-100);
    Map<String, Color> authorsColor = null;
    boolean isAuthorsColorMode = false;
    /////
    int topLayerIndex=0, targetLayerIndex=0 /*if equals to topLayerIndex it means no animation is running*/;
    float topLayerOffset;
    Dimension topIdealLayerDimention_doubled = new Dimension(0,0);//Important: the dimension of toplayer is double. because the Renderer divide it by BASE_DEPTH
    Point topIdealLayerCenterPos = new Point(0,0);
    int topIdealLayer_left_x, topIdealLayer_right_x, topIdealLayer_bottom_y, topIdealLayer_top_y;
    final int INVALID = -1;
    int lastBorderHighlighted_VirtualWindowIndex =-1, currentMouseHoveredIndex =INVALID;
    ///////// ++ UI
    final Color MOUSE_HOVERED_COLOR = Color.WHITE;
    final boolean COLORFUL_MODE_FOR_DEBUGGING = false;
    final int TOP_BAR_HEIGHT = 25;
    final int VIRTUAL_WINDOW_BORDER_TICKNESS = 1;
    final int TIME_LINE_WIDTH = 3;
    final Color TIMELINE_COLOR = new Color(64,255,64);
    final Color CHART_TIMELINE_COLOR = TIMELINE_COLOR;//new Color(255,255,0);
    final Color CHART_LINE_COLOR = Color.WHITE;
    ////////
    private ArrayList<SingleViewInformation> allSingleViewsInformation = null;
    private boolean comboBoxFillUpPorcess = false;
    TTMSingleFileView TTMWindow = null;
    Project project;
    CustomEditorTextField mainEditorWindow = null;
    ArrayList<CommitWrapper> commitList = null;
    //ArrayList<MetricCalculationResults> metricResultsList = null;
    private MaxCKNumber maxCKNumber = null;
    ArrayList<CKNumber> fullMetricsReport = null;
    VirtualEditorWindow[] virtualEditorWindows = null;
    VirtualFile virtualFile;

    final String CHECKOUT_LATEST_PROJECT_COMMIT_BUTTON_TEXT="Checkout Latest Commit";
    JButton updateActiveFileToThisCommitBtn = null, updateProjectToThisCommitBtn = null, checkoutProjectLatestCommitBtn = null;
    ComboBox syncComboBox = null;

    CKNumberReader.MetricTypes currentMetric = null;


    public Commits3DView(Project project, VirtualFile virtualFile, ArrayList<CommitWrapper> commitList, @Nullable ArrayList<CKNumber> fullMetricsReport,@Nullable MaxCKNumber maxCKNumber, TTMSingleFileView TTMWindow)
    {
        super();

        this.TTMWindow = TTMWindow;
        this.project = project;
        this.virtualFile = virtualFile;
        this.commitList = commitList;
        this.currentMetric = CKNumberReader.MetricTypes.loc;
        this.maxCKNumber = maxCKNumber;
        this.fullMetricsReport = fullMetricsReport;

        this.setLayout(null);
        this.addComponentListener(this); // Check class definition as : ".. implements ComponentListener"
        if (CommonValues.IS_UI_IN_DEBUGGING_MODE)   this.setBackground(Color.ORANGE);
        this.setOpaque(true);

        preCalculateAuthorsColor();
        setupUI_mainEditorWindow();
        setupUI_buttons();
        setupUI_syncCombobox();
        setupUI_virtualWindows();
        initialVirtualWindowsVisualizations(); // initial 3D Variables

        playing3DAnimationTimer = new Timer(TICK_INTERVAL_MS, new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                tick(TICK_INTERVAL_MS/1000.f);
                repaint();
            }
        });

        mouseHovredItemTimer = new Timer(TICK_INTERVAL_MS,  new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                updateHoveredWindow();
            }
        });
        mouseHovredItemTimer.start();

        addMouseWheelListener();
        addMouseMotionListener();
        addMouseListener();

        loadMainEditorWindowContent();

        componentResized(null); //to "updateTopIdealLayerBoundary()" and then "updateEverythingAfterComponentResize()"
    }

    public void setMetricsData(ArrayList<CKNumber> fullMetricsReport, MaxCKNumber maxCKNumber)
    {
        this.fullMetricsReport = fullMetricsReport;
        this.maxCKNumber = maxCKNumber;
        for(int i=0; i<commitList.size(); i++)
        {
            virtualEditorWindows[i].metricsResult = fullMetricsReport.get(i);
            virtualEditorWindows[i].doRenderCalculation_chart();
        }
        repaint();
    }

    private void preCalculateAuthorsColor()
    {
        authorsColor = new HashMap<String, Color>();
        Color c;
        String s;
        for(int i=0; i<commitList.size(); i++)
        {
            s = commitList.get(i).getAuthor();
            if(authorsColor.containsKey(s)==true) continue;

            authorsColor.put(s, DistinctColorsProvider.GetDistinctColor(authorsColor.size()));
        }
    }

    public void toggleAuthorsColorMode()
    {
        isAuthorsColorMode = !isAuthorsColorMode;

        if(isAuthorsColorMode==true)
        {
            for(int i=0; i<commitList.size(); i++)
                virtualEditorWindows[i].setTemporaryHighlightTopBar(true, authorsColor.get(commitList.get(i).getAuthor()));
        }
        else
        {
            for(int i=0; i<commitList.size(); i++)
                virtualEditorWindows[i].setTemporaryHighlightTopBar(false, DUMMY_COLOR);
        }
        repaint();
    }

    private void setupUI_buttons()
    {
        setupUI_buttons_updateActiveFile();
        setupUI_buttons_updateProjectFile();
        setupUI_buttons_checkoutProjectLatestCommit();
    }

    private void setupUI_syncCombobox()
    {
        syncComboBox = new ComboBox(); //Note: if you add two same item (like "1" and "1" strings) it becomes buggy.
        final String SYNC_COMBO_BOX_DEFAULT_TEXT = "Sync...";
        syncComboBox.addItem(SYNC_COMBO_BOX_DEFAULT_TEXT);
        syncComboBox.addPopupMenuListener(new PopupMenuListener()
        {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e)
            {
                comboBoxFillUpPorcess = true;
                syncComboBox.removeAllItems();
                ArrayList<SingleViewInformation> temp_all_includingMe = CodeTimeMachineAction.getCodeTimeMachine(project).getAllSingleViewsInformation();
                String myFileName = virtualFile.getName();

                allSingleViewsInformation = new ArrayList<>(temp_all_includingMe.size()-1/*except me*/);
                for(SingleViewInformation svi: temp_all_includingMe)
                {
                    if(svi.toString().equals(myFileName))
                        // Don't show me
                        continue;
                    allSingleViewsInformation.add(svi);
                }

                for(SingleViewInformation svi: allSingleViewsInformation)
                {
                    syncComboBox.addItem(svi);
                }
                comboBoxFillUpPorcess = false;

            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
            {
                comboBoxFillUpPorcess = true;
                syncComboBox.removeAllItems();
                syncComboBox.addItem(SYNC_COMBO_BOX_DEFAULT_TEXT);
                comboBoxFillUpPorcess = false;
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e)
            {
            }
        });

        syncComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(comboBoxFillUpPorcess )
                    // We ignore the call which is happening after adding first item in fill-up-with-open-files process.
                    // Check here for problem: http://stackoverflow.com/questions/28960142/listener-gets-fired-only-once-after-adding-all-items-in-jcombobox-though-fireint
                    // By the way, for one item, sync doesn't make sense.
                    return;

                if(allSingleViewsInformation.size()==0) return;

                // 0-based
                int selectedIndex = syncComboBox.getSelectedIndex();


                CommitWrapper selectedCommitToSyncWith = allSingleViewsInformation.get(selectedIndex).getActiveCommit();

                ///////// Find nearest date
                Date targetDate = selectedCommitToSyncWith.getDate();
                int theLastCommitJustBeforeTargetCommit_cIndex = -1;
                for(int i=0; i<commitList.size(); i++)
                {
                    if( commitList.get(i).getDate().equals(targetDate) || commitList.get(i).getDate().before(targetDate))
                    {
                        theLastCommitJustBeforeTargetCommit_cIndex = i;
                        break;
                    }
                }

                if(theLastCommitJustBeforeTargetCommit_cIndex==-1)
                    // it means, target commit is older than the oldest commit in crrent file
                    theLastCommitJustBeforeTargetCommit_cIndex = commitList.size()-1;

                //////// fly to there
                TTMWindow.activeCommit_cIndex = theLastCommitJustBeforeTargetCommit_cIndex;
                TTMWindow.commitsBar.setActiveCommit_cIndex();
                TTMWindow.navigateToCommit(ClassType.SUBJECT_CLASS,theLastCommitJustBeforeTargetCommit_cIndex);

            }
        });

        //syncComboBox.addActionListener() //Despite the new selected item is diff, this function will be called for new item
        //syncComboBox.addItemListener() // In case of selecting a diff item, this function always called twice: DESELECTED and SELECTED.

        syncComboBox.setFocusable(false);
        syncComboBox.setOpaque(false);
        this.add(syncComboBox); // As there's no layout, we should set Bound it. we'll do this in "ComponentResized()" event
    }
    private void setupUI_buttons_updateActiveFile()
    {
        ImageIcon icon = new ImageIcon(getClass().getResource("/images/travelTime.png"));
        updateActiveFileToThisCommitBtn = new JButton("Revert File",icon);
        updateActiveFileToThisCommitBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Runnable readRunner = new Runnable() {
                    @Override
                    public void run() {

                        updateVirtualFileIfNeeded();
                        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
                        document.setText(commitList.get(TTMWindow.activeCommit_cIndex).getFileContent());
                    }
                };

                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                            @Override
                            public void run() {
                                ApplicationManager.getApplication().runWriteAction(readRunner);
                            }
                        }, "Update__ActiveFile__ToThisCommitBtn", null);
                    }
                });

                CodeTimeMachineAction.getCodeTimeMachine(project).getToolWindow().hide(null);
            }
        });
        updateActiveFileToThisCommitBtn.setFocusable(false);
        updateActiveFileToThisCommitBtn.setForeground(SHARP_GREEN);
        updateActiveFileToThisCommitBtn.setOpaque(false);
        this.add(updateActiveFileToThisCommitBtn); // As there's no layout, we should set Bound it. we'll do this in "ComponentResized()" event
    }

    private void setupUI_buttons_updateProjectFile()
    {
        ImageIcon icon = new ImageIcon(getClass().getResource("/images/travelTimeProject.png"));
        updateProjectToThisCommitBtn = new JButton("Revert Project",icon);
        updateProjectToThisCommitBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                GitHelper gitHelper = CodeTimeMachineAction.getCodeTimeMachine(project).getGitHelper();
                if(commitList.get(TTMWindow.activeCommit_cIndex).isFake() == true)
                {
                    // Fact: It means that: TTMWindow.activeCommit_cIndex is 0 , Plus, we have uncommitted changes
                    // Since  we are rebasing whole project using below code is wrong:
                    // gitHelper.checkoutCommitID(commitList.get(1).getCommitID());
                    // Instead we should use:
                    // gitHelper.checkoutLatestCommit();
                    // because by reverting project to index 1, we may revert projects to a lot older than latest commits
                    // ( assume in scenario that last commit of this file done very earlier

                    gitHelper.checkoutLatestCommit();
                    gitHelper.applyStash();
                }
                else
                    gitHelper.checkoutCommitID(commitList.get(TTMWindow.activeCommit_cIndex).getCommitID());

                updateVirtualFileIfNeeded();

                checkoutProjectLatestCommitBtn.setText("*"+CHECKOUT_LATEST_PROJECT_COMMIT_BUTTON_TEXT);
                CodeTimeMachineAction.getCodeTimeMachine(project).getToolWindow().hide(null);
            }
        });
        updateProjectToThisCommitBtn.setFocusable(false);
        updateProjectToThisCommitBtn.setForeground(SHARP_RED);
        updateProjectToThisCommitBtn.setOpaque(false);
        this.add(updateProjectToThisCommitBtn); // As there's no layout, we should set Bound it. we'll do this in "ComponentResized()" event
    }

    private void setupUI_buttons_checkoutProjectLatestCommit()
    {
        ImageIcon icon = new ImageIcon(getClass().getResource("/images/checkout.png"));

        checkoutProjectLatestCommitBtn = new JButton(CHECKOUT_LATEST_PROJECT_COMMIT_BUTTON_TEXT,icon);
        checkoutProjectLatestCommitBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                GitHelper gitHelper = CodeTimeMachineAction.getCodeTimeMachine(project).getGitHelper();
                gitHelper.checkoutLatestCommit();
                //gitHelper.applyStash(); not including uncommitted-changes !
                checkoutProjectLatestCommitBtn.setText(CHECKOUT_LATEST_PROJECT_COMMIT_BUTTON_TEXT);
                CodeTimeMachineAction.getCodeTimeMachine(project).getToolWindow().hide(null);

            }
        });
        checkoutProjectLatestCommitBtn.setFocusable(false);
        checkoutProjectLatestCommitBtn.setForeground(SHARP_YELLOW);
        checkoutProjectLatestCommitBtn.setOpaque(false);
        this.add(checkoutProjectLatestCommitBtn); // As there's no layout, we should set Bound it. we'll do this in "ComponentResized()" event
    }

    void updateVirtualFileIfNeeded()
    {
        // if after reverting project, package names or ... changes the virtualFile path get invalid.
        // So we search for the new path of file.
        // Since we assume filename has not changed, we use valid part of old virtualFile (the filename).
        if(virtualFile.isValid()==false)
        {
            PsiFile[] filesByName = FilenameIndex.getFilesByName(project, virtualFile.getName(), new EverythingGlobalScope(project));
            if(filesByName.length>0)
            {
                virtualFile = filesByName[0].getVirtualFile();
            }
            else
            {
                // It means that the filename has also changed.
                //#TODO: in such case, we can't renew virtualFile variable.
            }
        }
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
                // Important Note: When mouse enter TextArea, we no longer get called until we get out of it.
                // So "currentMousePoint" show that last location just before entering TextArea.

                currentMousePoint = e.getPoint();
                //updateHovredWindow //This is not enough. Sometimes we don't move mosue, but underneath the windows are moving.
            }
        });
    }

    private void updateHoveredWindow()
    {
        for (int i=0; i<virtualEditorWindows.length; i++)
        {
            if(virtualEditorWindows[i].isVisible==false) continue;

            if(virtualEditorWindows[i].drawingRect.contains(currentMousePoint))
            {
                if(currentMouseHoveredIndex ==i)
                    return;
                UpdateHoveredVirtualWindow(i);
                return;
            }
        }
        if(currentMouseHoveredIndex!=INVALID)
            UpdateHoveredVirtualWindow(INVALID);// Here = mouse hovered no virtualWindows
    }

    private void UpdateHoveredVirtualWindow(int new_cIndex)
    {
        if(currentMouseHoveredIndex != INVALID)
        {
            virtualEditorWindows[currentMouseHoveredIndex].setHighlightBorder(false, DUMMY_COLOR);
            if(!isAuthorsColorMode)
                virtualEditorWindows[currentMouseHoveredIndex].setTemporaryHighlightTopBar(false, DUMMY_COLOR);
        }

        currentMouseHoveredIndex =new_cIndex;

        if(currentMouseHoveredIndex!=INVALID )
        {
            virtualEditorWindows[new_cIndex].setHighlightBorder(true, MOUSE_HOVERED_COLOR);
            if(!isAuthorsColorMode)
                virtualEditorWindows[new_cIndex].setTemporaryHighlightTopBar(true, MOUSE_HOVERED_COLOR);
        }

        TTMWindow.updateTopLayerCommitsInfoData(currentMouseHoveredIndex);

        repaint();
    }

    public void toggleAlwaysShowMetricsValue()
    {
        alwaysShowMetricsValue = !alwaysShowMetricsValue;
        repaint();
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
                if (notches > 0)
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
        mainEditorWindow = new CustomEditorTextField("Loading...",project, FileTypeRegistry.getInstance().getFileTypeByExtension("java"));
        //mainEditorWindow.setBounds(100,100,100,50); //TEST
        mainEditorWindow.setEnabled(true);
        mainEditorWindow.setOneLineMode(false);
        //mainEditorWindow.setOpaque(true);
        this.add(mainEditorWindow); // As there's no layout, we should set Bound it. we'll do this in "ComponentResized()" event
    }

    private void setupUI_virtualWindows()
    {
        virtualEditorWindows = new VirtualEditorWindow[commitList.size()];

        CKNumber ckNumber;
        for (int i = 0; i< commitList.size() ; i++)
        {
            if(fullMetricsReport != null)
                ckNumber = fullMetricsReport.get(i);
            else
                ckNumber = null;
            virtualEditorWindows[i] = new VirtualEditorWindow(i /*not cIndex*/, commitList.get(i), ckNumber);
        }
    }

    private void updateVirtualWindowsBoundaryAfterComponentResize()
    {
        int xCenter, yCenter, w, h;
        w = topIdealLayerDimention_doubled.width;
        h = topIdealLayerDimention_doubled.height;
        xCenter = topIdealLayerCenterPos.x;
        yCenter = topIdealLayerCenterPos.y;

        for (int i = 0; i< commitList.size() ; i++)
            virtualEditorWindows[i].setDefaultValues(xCenter, yCenter, w, h);
    }

    private void initialVirtualWindowsVisualizations()
    {
        topLayerOffset = 0;
        topLayerIndex = lastBorderHighlighted_VirtualWindowIndex = 0;

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
        virtualEditorWindows[lastBorderHighlighted_VirtualWindowIndex].setHighlightBorder(false, DUMMY_COLOR);

        virtualEditorWindows[virtualWindowIndex].setHighlightBorder(true, ACTIVE_WINDOW_COLOR);

        lastBorderHighlighted_VirtualWindowIndex = virtualWindowIndex;
    }

    @Override
    public void componentResized(ComponentEvent e)
    {
        Dimension size = getSize();
        centerOfThisComponent = new Point(size.width/2, size.height/2);
        //////
        updateTopIdealLayerBoundary();
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

        if(CommonValues.IS_UI_IN_DEBUGGING_MODE)
            draw_debuggingInformation(g);


        if(virtualEditorWindows!=null)
            draw_main(g);
    }

    private void draw_main(Graphics g)
    {

        for(int i = commitList.size()-1; i>=0; i--)
        {
            if (virtualEditorWindows[i].isVisible == false) continue;
            virtualEditorWindows[i].draw(g);
        }

        Graphics2D g2d = (Graphics2D) g;
        //////// STROKE
        g2d.setStroke(new BasicStroke(TIME_LINE_WIDTH));

        draw_leftTimeline(g2d);
        draw_rightChart(g2d);
    }

    private void draw_rightChart(Graphics2D g2d)
    {

        if(currentMetric == CKNumberReader.MetricTypes.NONE)
            return;

        ///// STROKE
        g2d.setStroke(new BasicStroke(TIME_LINE_WIDTH));

        // Chart: Metric name
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.setColor(Color.LIGHT_GRAY);
        Point chartNamePos = (Point) startPointOfChartTimeLine.clone();
        chartNamePos.x -= 25;
        chartNamePos.y += 30;
        g2d.drawString(CKNumberReader.MetricTypes_StringRepresntation[currentMetric.ordinal()], chartNamePos.x, chartNamePos.y);

        // Show "calculating logo" when metrics values are not available
        if(fullMetricsReport == null)
        {
            ImageIcon waitIcon = new ImageIcon(getClass().getResource("/images/wait.gif"));
            waitIcon.paintIcon(this,g2d,startPointOfChartTimeLine.x+30, startPointOfChartTimeLine.y-70);
            return;
        }


        for(int i = commitList.size()-1/*Oldest Commit*/; i>=0/*Most recent Commit*/; i--)
        {
            if (virtualEditorWindows[i].isVisible == false) continue;


            Point chartTimeLineMyPoint = virtualEditorWindows[i].chartTimeLinePoint;
            Point chartTimeLineMyValuePoint = virtualEditorWindows[i].chartValuePoint;


            // Chart: Timeline segments
//            if (i != commitList.size() - 1 && virtualEditorWindows[i + 1].isVisible)
//            {
//                Point chartTimeLineNextPoint = virtualEditorWindows[i + 1].chartTimeLinePoint;
//                g2d.setStroke(new BasicStroke(TIME_LINE_WIDTH / 3));
//                g2d.setColor(new Color(CHART_TIMELINE_COLOR.getRed(), CHART_TIMELINE_COLOR.getGreen(), CHART_TIMELINE_COLOR.getBlue(), virtualEditorWindows[i].alpha));
//                g2d.drawLine(chartTimeLineMyPoint.x, chartTimeLineMyPoint.y, chartTimeLineNextPoint.x, chartTimeLineNextPoint.y);
//            }

            // Chart: Segments Connecting ValuePoint ( = "Chart" Segments)
            if (i != commitList.size() - 1 && virtualEditorWindows[i + 1].isVisible)
            {
                Point chartTimeLineNextValuePoint = virtualEditorWindows[i + 1].chartValuePoint;

                if (i != currentMouseHoveredIndex && i + 1 != currentMouseHoveredIndex)
                    g2d.setColor(new Color(CHART_LINE_COLOR.getRed(), CHART_LINE_COLOR.getGreen(), CHART_LINE_COLOR.getBlue(), virtualEditorWindows[i].alpha));
                else
                    g2d.setColor(MOUSE_HOVERED_COLOR); // No transparency. otherwise: g.setColor(new Color(MOUSE_HOVERED_COLOR.getRed(), MOUSE_HOVERED_COLOR.getGreen(), MOUSE_HOVERED_COwLOR.getBlue(), virtualEditorWindows[i].alpha));
                g2d.drawLine(chartTimeLineMyValuePoint.x, chartTimeLineMyValuePoint.y, chartTimeLineNextValuePoint.x, chartTimeLineNextValuePoint.y);
            }


            /////// Color Chart Bars
            Color metricC = virtualEditorWindows[i].getMetricColor();
            if (i == currentMouseHoveredIndex)
                g2d.setColor(MOUSE_HOVERED_COLOR);// No transparency. otherwise: g.setColor(new Color(MOUSE_HOVERED_COLOR.getRed(), MOUSE_HOVERED_COLOR.getGreen(), MOUSE_HOVERED_COLOR.getBlue(), virtualEditorWindows[i].alpha));
            else
                g2d.setColor(new Color(metricC.getRed(), metricC.getGreen(), metricC.getBlue(), virtualEditorWindows[i].alpha));

            // Chart: Vertical bar - Bottom point
            g2d.fillRoundRect(chartTimeLineMyPoint.x - TIME_LINE_POINT_SIZE.width / 2, chartTimeLineMyPoint.y - TIME_LINE_POINT_SIZE.height / 2,
                    TIME_LINE_POINT_SIZE.width, TIME_LINE_POINT_SIZE.height, 1, 1);

            // Chart: Vertical bar
            g2d.drawLine(chartTimeLineMyPoint.x, chartTimeLineMyPoint.y, chartTimeLineMyValuePoint.x, chartTimeLineMyValuePoint.y);

            // Chart: Vertical bar - Top point
            g2d.fillOval(chartTimeLineMyValuePoint.x - TIME_LINE_POINT_SIZE.width / 2, chartTimeLineMyValuePoint.y - TIME_LINE_POINT_SIZE.height / 2,
                    TIME_LINE_POINT_SIZE.width, TIME_LINE_POINT_SIZE.height);

            // Chart: numerical metric value
            if (alwaysShowMetricsValue || i == currentMouseHoveredIndex)
            {
                int value = virtualEditorWindows[i].value;
                g2d.drawString(Integer.toString(value),chartTimeLineMyValuePoint.x,chartTimeLineMyValuePoint.y-20);
            }

        }
    }

    private void draw_leftTimeline(Graphics2D g2d)
    {
        draw_tipOfTimeLine(g2d);

        for(int i = commitList.size()-1/*Oldest Commit*/;  i>=0/*Most recent Commit*/; i--)
        {
            if (virtualEditorWindows[i].isVisible == false) continue;

            Point timeLineMyPoint = virtualEditorWindows[i].timeLinePoint;

            // Timeline: Segment
            if (i != commitList.size() - 1 && virtualEditorWindows[i + 1].isVisible)
            {
                Point timelineErlierPoint = virtualEditorWindows[i + 1].timeLinePoint; // Line between myPoint and NextPoint (Closer to Camera)
                g2d.setColor(new Color(TIMELINE_COLOR.getRed(), TIMELINE_COLOR.getGreen(), TIMELINE_COLOR.getBlue(), virtualEditorWindows[i].alpha));
                g2d.drawLine(timeLineMyPoint.x, timeLineMyPoint.y, timelineErlierPoint.x, timelineErlierPoint.y);
            }

            /////// COLOR:  Point & Date
            if (i == targetLayerIndex)
                g2d.setColor(new Color(ACTIVE_WINDOW_COLOR.getRed(), ACTIVE_WINDOW_COLOR.getGreen(), ACTIVE_WINDOW_COLOR.getBlue(), virtualEditorWindows[i].alpha));
            else if (i == currentMouseHoveredIndex)
                g2d.setColor(MOUSE_HOVERED_COLOR); // No transparency. otherwise: g.setColor(new Color(MOUSE_HOVERED_COLOR.getRed(), MOUSE_HOVERED_COLOR.getGreen(), MOUSE_HOVERED_COLOR.getBlue(), virtualEditorWindows[i].alpha));
            else
                g2d.setColor(new Color(TIMELINE_COLOR.getRed(), TIMELINE_COLOR.getGreen(), TIMELINE_COLOR.getBlue(), virtualEditorWindows[i].alpha));


            // Timeline: Point
            g2d.fillRoundRect(timeLineMyPoint.x - TIME_LINE_POINT_SIZE.width / 2, timeLineMyPoint.y - TIME_LINE_POINT_SIZE.height / 2,
                    TIME_LINE_POINT_SIZE.width, TIME_LINE_POINT_SIZE.height, 1, 1);

            // Timeline: Date next to point
            if (i == targetLayerIndex)
            {
                g2d.setFont(new Font("Arial", Font.BOLD, 11));
                g2d.drawString(CalendarHelper.convertDateToStringYMD(commitList.get(i).getDate()), timeLineMyPoint.x - 70, timeLineMyPoint.y + 2);
                g2d.drawString(CalendarHelper.convertDateToTime(commitList.get(i).getDate()), timeLineMyPoint.x + 15, timeLineMyPoint.y + 2);
            }
            else if (i == commitList.size() - 1 || !CalendarHelper.isSameDay(commitList.get(i).getDate(), commitList.get(i + 1).getDate()))
            {
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                g2d.drawString(CalendarHelper.convertDateToStringYMD(commitList.get(i).getDate()), timeLineMyPoint.x - 68, timeLineMyPoint.y + 2);
            }
            else
            {
                g2d.setFont(new Font("Arial", Font.ITALIC, 9));
                g2d.drawString(CalendarHelper.convertDateToTime(commitList.get(i).getDate()), timeLineMyPoint.x - 30, timeLineMyPoint.y + 2);
            }

        }
    }

    private void draw_debuggingInformation(Graphics g)
    {
        g.setColor(new Color(0,255,255));
        g.fillRect(0, 0,getSize().width,getSize().height);
        g.setColor(new Color(255,0,0));
        g.fillOval(getSize().width/2-10, getSize().height/2-10,20,20); //Show Center
    }

    private void draw_tipOfTimeLine(Graphics2D g2d)
    {
        g2d.setColor(TIMELINE_COLOR);
        g2d.setStroke(new BasicStroke(TIME_LINE_WIDTH));

        //// Line from tip of TimeLine (ACTUALLY: startPoint+0.1depth) of time line to Triangle
        g2d.drawLine(startPointOfTimeLine.x, startPointOfTimeLine.y, trianglePoint.x, trianglePoint.y);

        //// Tip Triangle
        int[] triangleVertices_x = new int[]{trianglePoint.x-6,trianglePoint.x-14,trianglePoint.x+4};
        int[] triangleVertices_y = new int[]{trianglePoint.y-2,trianglePoint.y+10,trianglePoint.y+3};
        g2d.fillPolygon(triangleVertices_x, triangleVertices_y, 3);
    }

    private void tick(float dt_sec)
    {

        // Moving virtual windows towards active_cIndex codes
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

        //debuggingText = "> "+targetDepth+" > Speed: "+speed_depthPerSec;


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

    }

    public void showCommit(int newCommit_cIndex, boolean withAnimation)
    {
        if(withAnimation==false)
        {
            // TODO: without animation
            //loadMainEditorWindowContent();
            //virtualEditorWindows[topLayerIndex].setHighlightBorder();
            // Arrange VirtualEditorWindows
            // After implementing this function, we could call "codeHistory3DView.showCommit(0, false);" after instancing this class
        }
        else
        {
            if( targetLayerIndex==newCommit_cIndex)
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
                //mainEditorWindow.setCaretPosition(0);
                if(mainEditorWindow.getEditor()!=null)
                {
                    // We scroll top after each setText(..) (that make scroll go down). But we don't like to be animated.
                    // Calling this once in initialization of Editor doesn't work. so we call it every time before scrolling.
                    mainEditorWindow.getEditor().getScrollingModel().disableAnimation();
                    mainEditorWindow.getEditor().getScrollingModel().scroll(0, 0);
                }
                // To solve the white screen problem (earlier, I resized the window to solve the problem)
                mainEditorWindow.revalidate();

            }
        });

        updateMainEditorWindowBoundaryAfterComponentResize();
        mainEditorWindow.setVisible(true);

    }

    private void updateMainEditorWindowBoundaryAfterComponentResize()
    {
        int x,y,w,h;
        w = virtualEditorWindows[topLayerIndex].drawingRect.width-2*VIRTUAL_WINDOW_BORDER_TICKNESS;
        h = virtualEditorWindows[topLayerIndex].drawingRect.height-TOP_BAR_HEIGHT;
        x = virtualEditorWindows[topLayerIndex].drawingRect.x+VIRTUAL_WINDOW_BORDER_TICKNESS;
        y = virtualEditorWindows[topLayerIndex].drawingRect.y+TOP_BAR_HEIGHT;
        //mainEditorWindow.setSize(w,h);
        //mainEditorWindow.setLocation(x,y);
        mainEditorWindow.setBounds(x,y,w,h);
    }

    private void updateTopIdealLayerBoundary()
    {
        final int BOTTOM_FREE_SPACE_VERTICAL = 20;
        topIdealLayerDimention_doubled = new Dimension(  3*getSize().width/5, 2*getSize().height/3 /*2/3 of whole vertical*/ - BOTTOM_FREE_SPACE_VERTICAL);
        topIdealLayerDimention_doubled.width *= MyRenderer.getInstance().BASE_DEPTH;
        topIdealLayerDimention_doubled.height *= MyRenderer.getInstance().BASE_DEPTH;

        topIdealLayerCenterPos = new Point(centerOfThisComponent.x, 2*getSize().height/3 - BOTTOM_FREE_SPACE_VERTICAL /*Fit from bottom minus BOTTOM_FREE_SPACE_VERTICAL*/);

        topIdealLayer_left_x =  topIdealLayerCenterPos.x - topIdealLayerDimention_doubled.width/4;
        topIdealLayer_right_x =  topIdealLayerCenterPos.x + topIdealLayerDimention_doubled.width/4;
        topIdealLayer_bottom_y =  topIdealLayerCenterPos.y + topIdealLayerDimention_doubled.height/4;
        topIdealLayer_top_y =  topIdealLayerCenterPos.y - topIdealLayerDimention_doubled.height/4;
        ////
        updateEverythingAfterComponentResize();
    }

    private void updateEverythingAfterComponentResize()
    {
        updateVirtualWindowsBoundaryAfterComponentResize();
        updateMainEditorWindowBoundaryAfterComponentResize();
        updateButtonsAfterComponentResize();
        updateTimeLineDrawing();
    }

    private void updateButtonsAfterComponentResize()
    {
        final int HEIGHT = 26;
        final int GAP_FROM_BOTTOM = 20;
        ////////// Right-hand side
        // Top
        syncComboBox.setBounds(topIdealLayer_right_x+10,topIdealLayer_bottom_y-2*HEIGHT-GAP_FROM_BOTTOM,
                                100,HEIGHT);
        // Bottom
        checkoutProjectLatestCommitBtn.setBounds(topIdealLayer_right_x+10,topIdealLayer_bottom_y-HEIGHT-GAP_FROM_BOTTOM,
                200,HEIGHT);
        ////////// Left-hand side
        // Top
        final int W3 = 100;
        updateActiveFileToThisCommitBtn.setBounds(topIdealLayer_left_x-W3-10,topIdealLayer_bottom_y-2*HEIGHT-GAP_FROM_BOTTOM,
                W3,HEIGHT);
        // Bottom
        final int W4 = 120;
        updateProjectToThisCommitBtn.setBounds(topIdealLayer_left_x-W4-10,topIdealLayer_bottom_y-HEIGHT-GAP_FROM_BOTTOM,
                W4,HEIGHT);
    }

    private void updateTimeLineDrawing()
    {
        startPointOfTimeLine = MyRenderer.getInstance().calculateTimeLinePoint(topIdealLayerCenterPos.x, topIdealLayerCenterPos.y,
                                                                        topIdealLayerDimention_doubled.width, topIdealLayerDimention_doubled.height,
                                                                        0.05f+MyRenderer.getInstance().BASE_DEPTH);


        Point aLittleAfterstartPointOfTimeLine = MyRenderer.getInstance().calculateTimeLinePoint(topIdealLayerCenterPos.x, topIdealLayerCenterPos.y,
                                                                        topIdealLayerDimention_doubled.width, topIdealLayerDimention_doubled.height,
                                                                        +0.4f+MyRenderer.getInstance().BASE_DEPTH);

        int deltaX = startPointOfTimeLine.x - aLittleAfterstartPointOfTimeLine.x;
        int deltaY = startPointOfTimeLine.y - aLittleAfterstartPointOfTimeLine.y;
        trianglePoint = (Point) startPointOfTimeLine.clone();
        trianglePoint.x += deltaX; //
        trianglePoint.y += deltaY;

        startPointOfChartTimeLine = MyRenderer.getInstance().calculateChartTimeLinePoint(topIdealLayerCenterPos.x, topIdealLayerCenterPos.y,
                topIdealLayerDimention_doubled.width, topIdealLayerDimention_doubled.height,
                0+MyRenderer.getInstance().BASE_DEPTH);
    }

    public void setTopBarHighlight(int cIndex, boolean newStatus, Color c)
    {
        virtualEditorWindows[cIndex].setHighlightTopBar(newStatus, c);
        repaint();
    }

    public void displayMetric(CKNumberReader.MetricTypes newMetric)
    {
        currentMetric = newMetric;
        render();
    }

    protected class VirtualEditorWindow
    {
        //TODO: Extract whole class to another file
        ////////
        int cIndex =-1;
        private Color someRandomMetric1Color = Color.BLACK;
        CommitWrapper commitWrapper = null;
        CKNumber metricsResult = null;

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
        Point timeLinePoint = new Point(0,0), chartTimeLinePoint = new Point(0,0), chartValuePoint = new Point(0,0);;
        int value = 0;
        //private Point chartValuePoint= new Point(0,0);
        ////////

        public VirtualEditorWindow(int index, CommitWrapper commitWrapper, CKNumber metricsResult)
        {
            this.cIndex = index;
            this.commitWrapper = commitWrapper;
            this.metricsResult = metricsResult;

            if(COLORFUL_MODE_FOR_DEBUGGING || CommonValues.IS_UI_IN_DEBUGGING_MODE)
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

            // we shouldn't "&& alpha<X". because if get invisible we never enter "doRenderCalculation()" and we never
            // get visible again. :D
            if(depth<MIN_VISIBLE_DEPTH || depth> maxVisibleDepth)
                isVisible=false;
            else
                isVisible=true;

            if(isVisible)
                doRenderCalculation();
        }

        public void doRenderCalculation()
        {
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

            if(newAlpha < 20)
            {
                // Optimization
                isVisible=false;
                return;
            }

            //////////////// Size
            float renderingDepth = depth + MyRenderer.getInstance().BASE_DEPTH;
            drawingRect.width = MyRenderer.getInstance().render3DTo2D(wDefault, renderingDepth);
            drawingRect.height = MyRenderer.getInstance().render3DTo2D(hDefault, renderingDepth);
            Point p = MyRenderer.getInstance().render3DTo2D(xCenterDefault, yCenterDefault, renderingDepth);
            drawingRect.x = p.x - drawingRect.width/2;
            drawingRect.y = p.y - drawingRect.height/2;

            ////////////// TimeLine
            doRenderCalculation_timeline();

            ///////////// Chart TimeLine
            doRenderCalculation_chart();

        }

        private void doRenderCalculation_timeline()
        {
            float renderingDepth = depth + MyRenderer.getInstance().BASE_DEPTH;

            // We also could use "MyRenderer.getInstance().calculateTimeLinePoint()". But it's worthless and that function
            // is designed for external user ( check 'updateTimeLineDrawing()' function)
            timeLinePoint = MyRenderer.getInstance().render3DTo2D(xCenterDefault, yCenterDefault, renderingDepth);
            timeLinePoint.x = drawingRect.x - (int)(MyRenderer.getInstance().TIME_LINE_GAP*drawingRect.width);
            timeLinePoint.y = drawingRect.y;
        }

        public void doRenderCalculation_chart()
        {
            float renderingDepth = depth + MyRenderer.getInstance().BASE_DEPTH;

            chartTimeLinePoint = MyRenderer.getInstance().render3DTo2D(xCenterDefault, yCenterDefault, renderingDepth);
            chartTimeLinePoint.x = drawingRect.x + drawingRect.width + (int)(MyRenderer.getInstance().TIME_LINE_GAP*drawingRect.width);
            chartTimeLinePoint.y = drawingRect.y;

            chartValuePoint = (Point) chartTimeLinePoint.clone();
            value = 0;

            if(currentMetric != CKNumberReader.MetricTypes.NONE && metricsResult!=null)
            {
                value = CKNumberReader.getInstance().getValueForMetric(metricsResult,currentMetric);
                int max = CKNumberReader.getInstance().getValueForMetric(maxCKNumber,currentMetric);
                float valuePercent = 0;
                if(max!=0)
                    valuePercent = value*100.0f/max;

                if(valuePercent<45)
                    someRandomMetric1Color = SHARP_GREEN;
                else if(valuePercent<80)
                    someRandomMetric1Color = SHARP_YELLOW;
                else
                    someRandomMetric1Color = SHARP_RED;

                float lengthAtWindow = valuePercent * MAX_CHART_BAR_HEIGHT_PX / 100;
                lengthAtWindow = MyRenderer.getInstance().render3DTo2D((int)lengthAtWindow,depth+MyRenderer.getInstance().BASE_DEPTH);
                chartValuePoint.y -= (int)lengthAtWindow; //Moving up
            }
        }

        public void setHighlightBorder(boolean newStatus, Color newColor)
        {
            if(newStatus==true)
                myBorderColor = newColor;
            else if(cIndex == TTMWindow.activeCommit_cIndex)
                myBorderColor = ACTIVE_WINDOW_COLOR;
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

           draw_mainRect(g2d, drawingRect);
           draw_mainRectBorder(g2d, drawingRect);
           draw_topBar(g2d, drawingRect);
           draw_topBarText(g, drawingRect);
        }

        private void draw_topBarText(Graphics g, Rectangle drawingRect)
        {
            /// Name
            String text="";
            Graphics g2 = g.create();
            g2.setColor(new Color(0,0,0,alpha));
            Rectangle2D rectangleToDrawIn = new Rectangle2D.Double(drawingRect.x,drawingRect.y,drawingRect.width,TOP_BAR_HEIGHT);
            g2.setClip(rectangleToDrawIn);
            if(cIndex ==topLayerIndex)
            {
                g2.setFont(new Font("Arial", Font.BOLD, 10));
                text = getTopBarMessage();
                //text = "I: "+cIndex+"Depth = "+ Float.toString(depth)+ "FontSize: --  "+ "Alpha: "+alpha;
                DrawingHelper.drawStringCenter(g2, text, drawingRect.x+drawingRect.width/2, drawingRect.y+10);
                String path = virtualFile.getPath();
                int maxPossible = DrawingHelper.howManyCharFitsInWidth(g2,path, 2*drawingRect.width/3 /*in 2/3 width*/ );
                if(maxPossible<path.length())
                {
                    int extraCharacterCount = path.length() - maxPossible;
                    int cropFrom = path.indexOf('/', extraCharacterCount);
                    if(cropFrom!=-1)
                        path = "..."+path.substring(cropFrom);
                    else
                        // If window's width be little => maxPossible->0 => path.indexOf(..) found nothing: -1 => HERE
                        path = virtualFile.getName();
                }
                DrawingHelper.drawStringCenter(g2, path, drawingRect.x+drawingRect.width/2, drawingRect.y+20);
            }
            else
            {
                float fontSize = 20.f/(MyRenderer.getInstance().BASE_DEPTH+depth);
                g2.setFont(new Font("Arial", Font.BOLD, (int)fontSize));
                text = getTopBarMessage();
                //text = "I: "+cIndex+"Depth = "+ Float.toString(depth)+ "FontSize: "+fontSize + "Alpha: "+alpha;
                DrawingHelper.drawStringCenter(g2, text, drawingRect.x+drawingRect.width/2, drawingRect.y+15);
            }
        }

        private void draw_topBar(Graphics2D g2d, Rectangle drawingRect)
        {
            /// TopBar
            if(!isTopBarTempColorValid)
                g2d.setColor( this.myTopBarColor);
            else
                g2d.setColor( this.myTopBarTempColor);
            g2d.fillRect(drawingRect.x, drawingRect.y+VIRTUAL_WINDOW_BORDER_TICKNESS, drawingRect.width, TOP_BAR_HEIGHT);
        }

        private void draw_mainRectBorder(Graphics2D g2d, Rectangle drawingRect)
        {
            /// Border
            g2d.setStroke(new BasicStroke(2));
            g2d.setColor( this.myBorderColor);
            g2d.drawRect(drawingRect.x, drawingRect.y, drawingRect.width, drawingRect.height);
        }

        private void draw_mainRect(Graphics2D g2d, Rectangle drawingRect)
        {
            /// Rect
            g2d.setColor( this.myColor);
            g2d.fillRect(drawingRect.x, drawingRect.y, drawingRect.width, drawingRect.height);
        }

        public Color getMetricColor()
        {
            return someRandomMetric1Color;
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

            //Because this editor initialized after all of my initialization (and after an unknown delay)
            // So we can't call loadMainEditorWindowContent() before and we have to do it here
            loadMainEditorWindowContent();

            return editor;
        }

        private String getSelectedText()
        {
            String s = getEditor().getSelectionModel().getSelectedText();
            if(s==null)
                s = "";
            return s;
        }

        private void addLineNumberToEditor(EditorEx editor)
        {
            EditorSettings settings = editor.getSettings();
            settings.setLineNumbersShown(true);
            editor.reinitSettings();
        }

        public void _setVisible(boolean newStatus)
        {
            super.setVisible(newStatus);
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
