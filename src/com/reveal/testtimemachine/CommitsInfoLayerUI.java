package com.reveal.testtimemachine;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;


public class CommitsInfoLayerUI extends LayerUI<JComponent>
{
    boolean isThereSomthingToDisplay = false;
    CommitWrapper commitToDisplay = null;

    final Color bgColor = new Color(33,33,33,230);
    final Color TITLE_COLOR = Color.LIGHT_GRAY;
    final Color TEXT_COLOR = Color.WHITE;

    Font NORM_FONT = new Font("Arial", Font.PLAIN, 12);
    Font BOLD_FONT = new Font("Arial", Font.BOLD, 12);
    Font BOLDER_FONT = new Font("Arial", Font.BOLD, 20);

    @Override
    public void paint(Graphics g, JComponent c)
    {
        super.paint(g, c);
        if(!isThereSomthingToDisplay) return;


        Graphics2D g2d = (Graphics2D) g.create();

        int w = c.getWidth();
        int h = c.getHeight();
        final int LEFT_MARGIN = 20;
        final int LEFT_TEXT_MARGIN = LEFT_MARGIN+45;
        final int RIGHT_MARGIN = 20;


        // Background
        g2d.setColor(bgColor);
        g2d.fillRect(0,0,w, h-10 /*10: because the scroll-handle is not black, so if we make it black it's not pretty*/);

        // Text



        ////////1
        g2d.setFont(NORM_FONT);
        g2d.setColor(TITLE_COLOR);
        g2d.drawString("ID: ",LEFT_MARGIN,20);
        g2d.setFont(BOLD_FONT);
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(commitToDisplay.getCommitID(),LEFT_TEXT_MARGIN,20);

        ////////2
        g2d.setFont(NORM_FONT);
        g2d.setColor(TITLE_COLOR);
        g2d.drawString("Author: ",LEFT_MARGIN,35);
        g2d.setFont(BOLD_FONT);
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(commitToDisplay.getAuthor(),LEFT_TEXT_MARGIN,35);

        ////////3
        g2d.setFont(NORM_FONT);
        g2d.setColor(TITLE_COLOR);
        g2d.drawString("Date: ",LEFT_MARGIN,50);
        g2d.setFont(BOLD_FONT);
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(CalendarHelper.convertDateToStringYMDHM(commitToDisplay.getDate()),LEFT_TEXT_MARGIN,50);

        //////// line
        g2d.drawLine(LEFT_MARGIN,55,w-RIGHT_MARGIN, 55);

        ////////4
        String s1="Message: ";
        String s2=commitToDisplay.getCommitMessage();
        final int Y = 80;

        g2d.setFont(NORM_FONT);
        g2d.setColor(TITLE_COLOR);
        g2d.drawString(s1,LEFT_MARGIN,Y);
        int k = g2d.getFontMetrics().stringWidth(s1);

        g2d.setFont(BOLDER_FONT);
        g2d.setColor(TEXT_COLOR);
        int n =  DrawingHelper.howManyCharFitsInWidth(g2d,s2, w-k-LEFT_MARGIN-RIGHT_MARGIN);
        g2d.drawString(s2.substring(0,n-3/*for ...*/)+"...",LEFT_MARGIN+k,Y);


        g2d.dispose();
    }

    void invisble()
    {
        isThereSomthingToDisplay = false;
    }

    void displayInfo(CommitWrapper commit)
    {
        isThereSomthingToDisplay = true;

        commitToDisplay = commit;
    }


}
