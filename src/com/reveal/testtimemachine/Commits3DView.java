package com.reveal.testtimemachine;

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
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Rectangle2D;
import java.util.*;

public class Commits3DView extends JComponent implements ComponentListener
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
    java.util.List<CommitWrapper> commitList = null;


    public Commits3DView( Project project, VirtualFile virtualFile, java.util.List<CommitWrapper> commitList)
    {
        super();

        this.project = project;
        this.virtualFile = virtualFile;
        this.commitList = commitList;

        this.setLayout(null);
        this.addComponentListener(this); // Check class definition as : ".. implements ComponentListener"
        if (CommonValues.IS_UI_IN_DEBUGGING_MODE)
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

        if(CommonValues.IS_UI_IN_DEBUGGING_MODE)
        {
            g.setColor(new Color(0,255,255));
            g.fillRect(0, 0,getSize().width,getSize().height);
            g.setColor(new Color(255,0,0));
            g.fillOval(getSize().width/2-10, getSize().height/2-10,20,20); //Show Center
        }

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


            if(COLORFUL || CommonValues.IS_UI_IN_DEBUGGING_MODE)
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
                DrawingHelper.drawStringCenter(g2, text, x+w/2, y+8);
                text = new String(virtualFile.getPath());
                DrawingHelper.drawStringCenter(g2, text, x+w/2, y+18);
            }
            else
            {
                float fontSize = 20/(BASE_DEPTH+depth);
                g2.setFont(new Font("Courier", Font.BOLD, (int)fontSize));
                text = getTopBarMessage();
                DrawingHelper.drawStringCenter(g2, text, x+w/2, y+15);
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
