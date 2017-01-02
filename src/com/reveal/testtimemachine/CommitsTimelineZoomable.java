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

        addKeyListener();
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

    private void addKeyListener()
    {
        this.setFocusable(true);
        this.requestFocus();
        /////////
        addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
                BoundedRangeModel model = getHorizontalScrollBar().getModel();
                int newMax = model.getMaximum();
                newMax /= zoomScale;

                char c = e.getKeyChar();
                switch (c)
                {
                    case '+':
                    case '=':
                        if(zoomScale<10)
                            zoomScale ++;
                        break;
                    case '_':
                    case '-':
                        if(zoomScale>1)
                        {
                            zoomScale--;
                        }
                        break;
                }
                ///////////
                newMax *= zoomScale;
                double progress = (model.getValue()+model.getExtent()/2)/(double)(model.getMaximum());
                t.setZoomFactor(zoomScale);

                getHorizontalScrollBar().setValue( (int)(progress*newMax-model.getExtent()/2));
            }

            @Override
            public void keyPressed(KeyEvent e)
            {
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
            }
        });
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
