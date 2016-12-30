package com.reveal.testtimemachine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class CommitsTimeline extends JComponent
{
    //General Notice: All calculation are INT since Point class is INT.

    private static final Dimension COMPONENT_SIZE = new Dimension(1100, 100);

    int activeMonthIndex = -1; //0-based
    Point line_effectiveBegin = null;
    int n_monthes = 0;
    int line_effectiveLength = 0;
    int line_sectorsLength = 0;
    int PRIMARY_LINE_TICKNESS = 3;

    int start_year=0, start_month=0, end_year=0, end_month=0; // Notice: All months are 0-based as Cal.get(Calendar.MONTH) does.


    public CommitsTimeline(ArrayList<CommitWrapper> commitList, TTMSingleFileView TTMWindow)
    {
        super();

        this.setLayout(null);
        setSize(COMPONENT_SIZE);
        setPreferredSize(COMPONENT_SIZE);
        setMinimumSize(COMPONENT_SIZE);
        setMaximumSize(COMPONENT_SIZE);
        ////////////////////////////////////

        preCalculation(commitList);

        addMouseListener();


        activeMonthIndex = n_monthes-1;
        repaint();
    }

    private void preCalculation(ArrayList<CommitWrapper> commitList)
    {
        final int PRIMARY_LINE_GAP_FROM_SIDES = 20;
        ///////////
        Date startDate = new Date(commitList.get(commitList.size()-1).getDate().getTime());
        Date endDate = new Date(commitList.get(0).getDate().getTime());

        Calendar firstCommitCal = Calendar.getInstance();
        firstCommitCal.setTime(startDate);

        Calendar lastCommitCal = Calendar.getInstance();
        lastCommitCal.setTime(endDate);

        // Notice: All months are 0-based as Cal.get(Calendar.MONTH) does.
        start_month = firstCommitCal.get(Calendar.MONTH);
        start_year = firstCommitCal.get(Calendar.YEAR);

        end_month = lastCommitCal.get(Calendar.MONTH);
        end_year = lastCommitCal.get(Calendar.YEAR);

        int diffYear = end_year - start_year;
        n_monthes = (diffYear * 12) + end_month - start_month + 1;

        int maxPossibleLineLength = getSize().width - 2 * PRIMARY_LINE_GAP_FROM_SIDES;
        line_sectorsLength = maxPossibleLineLength / n_monthes; // => int value
        line_effectiveLength = n_monthes * line_sectorsLength; // Because "Ponit(x,y)" and therefor the "line_sectorsLength" shoudl be INT.
        line_effectiveBegin = new Point( (getSize().width-line_effectiveLength)/2, getSize().height/2);
    }

    private void addMouseListener()
    {
        addMouseListener(new MouseListener()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                Point p = e.getPoint();
                final int Y_ACCEPTABLE_CLICK_MARGINE = 35;
                if (p.x < line_effectiveBegin.x || p.x > line_effectiveBegin.x + line_effectiveLength
                        || p.y > line_effectiveBegin.y+Y_ACCEPTABLE_CLICK_MARGINE || p.y < line_effectiveBegin.y-Y_ACCEPTABLE_CLICK_MARGINE )
                {
                    return;
                }

                int m = (p.x - line_effectiveBegin.x) / line_sectorsLength;
                activeMonthIndex = m;

                CommitsTimeline.this.repaint();

            }

            @Override
            public void mousePressed(MouseEvent e)
            {

            }

            @Override
            public void mouseReleased(MouseEvent e)
            {

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

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g.setColor(new Color(50, 50, 50));
        g.fillRect(0, 0, getSize().width, getSize().height);

        if (CommonValues.IS_UI_IN_DEBUGGING_MODE)
        {
            g.setColor(new Color(255, 0, 0));
            g.fillOval(getSize().width / 2 - 10, getSize().height / 2 - 10, 20, 20); //Show Center
        }

        draw_primaryLine(g2d);
        draw_primaryLineSectors(g2d);
        draw_highlightActiveMonth(g2d);

        g2d.drawString("Date:"+getYearForSector(activeMonthIndex)+"/"+getMonthForSector(activeMonthIndex),50,50);

    }

    private void draw_highlightActiveMonth(Graphics2D g2d)
    {
        g2d.setColor(Color.GREEN);
        g2d.fillRoundRect(line_effectiveBegin.x+activeMonthIndex*line_sectorsLength, line_effectiveBegin.y, line_sectorsLength, PRIMARY_LINE_TICKNESS, 3, 3);
    }

    private void draw_primaryLineSectors(Graphics2D g2d)
    {

        final int SECTOR_SPLITTER_LENGTH_MONTH = 5;
        final Stroke SECTOR_SPLITTER_STROKE_MONTH = new BasicStroke(2);

        final int SECTOR_SPLITTER_LENGTH_YEAR = 15;
        final Stroke SECTOR_SPLITTER_STROKE_YEAR = new BasicStroke(3);

        int sectorSplitter_vecticalLineLength;

        Point lineIterator = (Point) line_effectiveBegin.clone();
        int yearIterator = start_year;
        for (int splitter = 0; splitter <= n_monthes; splitter++)
        {
            int FIRST_MONTH_OF_YEAR_INDEX = 0;
            if(getMonthForSector(splitter)== FIRST_MONTH_OF_YEAR_INDEX)
            {
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.setStroke(SECTOR_SPLITTER_STROKE_YEAR);
                sectorSplitter_vecticalLineLength = SECTOR_SPLITTER_LENGTH_YEAR;
            }
            else
            {
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.setStroke(SECTOR_SPLITTER_STROKE_MONTH);
                sectorSplitter_vecticalLineLength = SECTOR_SPLITTER_LENGTH_MONTH;
            }

            g2d.drawLine(lineIterator.x, lineIterator.y + sectorSplitter_vecticalLineLength / 2,
                    lineIterator.x, lineIterator.y - sectorSplitter_vecticalLineLength / 2);

            if(getMonthForSector(splitter)== FIRST_MONTH_OF_YEAR_INDEX)
            {
                yearIterator++;
                g2d.drawString(Integer.toString(yearIterator),lineIterator.x,lineIterator.y-SECTOR_SPLITTER_LENGTH_YEAR);

            }
            /////
            lineIterator.x += line_sectorsLength;
        }
    }

    private void draw_primaryLine(Graphics2D g2d)
    {
        final int PRIMARY_LINE_SIDES_EMPTINESS = 10;
        /////////
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRoundRect(line_effectiveBegin.x-PRIMARY_LINE_SIDES_EMPTINESS, line_effectiveBegin.y, line_effectiveLength+2*PRIMARY_LINE_SIDES_EMPTINESS, PRIMARY_LINE_TICKNESS, 3, 3);
    }

    // Return value is 0-based
    private int getMonthForSector(int sectorIndex)
    {
        int m = (start_month+sectorIndex)%12;
        return m;
    }

    private int getYearForSector(int sectorIndex)
    {
        int m = (start_month+sectorIndex)/12;
        return start_year+m;
    }
}
