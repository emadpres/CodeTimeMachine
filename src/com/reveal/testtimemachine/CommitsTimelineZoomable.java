package com.reveal.testtimemachine;

import com.intellij.ui.components.JBScrollPane;
import com.sun.org.apache.regexp.internal.RE;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;


public class CommitsTimelineZoomable extends JBScrollPane
{
    private static final Dimension COMPONENT_SIZE = new Dimension(1000, 110); // Area which dedicated to this component in UI
    CommitsTimeline t = null;
    int zoomScale = 1;


    public CommitsTimelineZoomable(ArrayList<CommitWrapper> commitList, TTMSingleFileView TTMWindow)
    {
        setSize(COMPONENT_SIZE);
        setPreferredSize(COMPONENT_SIZE);
        setMaximumSize(COMPONENT_SIZE); //The internal component should obviously be bigger than this Dimension for scrolling
        setMinimumSize(COMPONENT_SIZE);


        t = new CommitsTimeline(commitList, TTMWindow, this); //After setting scrollComponent size
        setViewportView(t);
        setBorder(null);

        setupScrollBarProperties();
    }

    private void setupScrollBarProperties()
    {
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
        getHorizontalScrollBar().setForeground(Color.RED);
        getHorizontalScrollBar().setOpaque(true);

        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
    }

    public JComponent getComponent()
    {
        return this;
    }

    // Value: +1 or -1
    public void changeZoomFactor(int value)
    {
        if(zoomScale+value>getMaximumZoomFactor() || zoomScale+value<1) return;

        BoundedRangeModel model = getHorizontalScrollBar().getModel();
        int newMax = model.getMaximum();
        newMax /= zoomScale;


        zoomScale += value;


        newMax *= zoomScale;
        double progress = (model.getValue()+model.getExtent()/2)/(double)(model.getMaximum());
        t.setZoomFactor(zoomScale);

        getHorizontalScrollBar().setValue( (int)(progress*newMax-model.getExtent()/2));
    }

    public void moveActiveRange(int monthMov)
    {
        // argument=+2 means: move ActiveRange 2month right
        if(t.activeRange_endIndex+monthMov < t.n_monthes && t.activeRange_startIndex+monthMov >= 0)
        {
            t.setActiveRange(t.activeRange_startIndex+monthMov, t.activeRange_endIndex+monthMov);
        }
    }

    public int getMaximumZoomFactor()
    {
        int max = getSize().width / t.original_sectorsLength;
        return max;
    }

    public void updateCommits3DViewActiveRange(int topLayer_cIndex)
    {
        t.updateCommits3DViewActiveRange(topLayer_cIndex);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.WHITE);
        g2d.drawString("Scale: "+Integer.toString(zoomScale), 30, 30);
    }
}
