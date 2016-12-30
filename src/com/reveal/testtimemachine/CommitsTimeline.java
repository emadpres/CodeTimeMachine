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
    //General Notice: "First commits" means "Oldest commit" and is the last element in commits list.

    private static final Dimension COMPONENT_SIZE = new Dimension(1100, 100);

    int activeMonthIndex = -1; //0-based
    Point line_effectiveBegin = null;
    int n_monthes = 0;
    int line_effectiveLength = 0;
    int line_sectorsLength = 0;
    int PRIMARY_LINE_TICKNESS = 3;

    int start_year=0, start_month=0, end_year=0, end_month=0; // Notice: All months are 0-based as Cal.get(Calendar.MONTH) does.

    ArrayList<CommitWrapper> commitList = null;
    int[] numberOfCommitsPerMonth = null;
    int[] percentgeOfCommitsPerMonth = null;


    public CommitsTimeline(ArrayList<CommitWrapper> commitList, TTMSingleFileView TTMWindow)
    {
        super();
        this.commitList = commitList;

        this.setLayout(null);
        setSize(COMPONENT_SIZE);
        setPreferredSize(COMPONENT_SIZE);
        setMinimumSize(COMPONENT_SIZE);
        setMaximumSize(COMPONENT_SIZE);
        ////////////////////////////////////

        preCalculation_basic();
        preCalculation_commitsStat();
        preCalculation_drawing();

        addMouseListener();


        activeMonthIndex = n_monthes-1;
        repaint();
    }

    private void preCalculation_drawing()
    {
        final int PRIMARY_LINE_GAP_FROM_SIDES = 20;
        //////////
        int maxPossibleLineLength = getSize().width - 2 * PRIMARY_LINE_GAP_FROM_SIDES;
        line_sectorsLength = maxPossibleLineLength / n_monthes; // => int value
        line_effectiveLength = n_monthes * line_sectorsLength; // Because "Ponit(x,y)" and therefor the "line_sectorsLength" shoudl be INT.
        line_effectiveBegin = new Point( (getSize().width-line_effectiveLength)/2, getSize().height/2);
    }

    private void preCalculation_commitsStat()
    {
        if(commitList.size()<=0) return;



        numberOfCommitsPerMonth = new int[n_monthes];
        for(int i=0; i<n_monthes; i++)
            numberOfCommitsPerMonth[i]=0;


        int lastSectorIndex = -1;
        int n_commitsForCurrentSector=0;

        for(int i=commitList.size()-1; i>=0; i--)
        {
            int currentSectorIndex = getSectorIndexForDate(commitList.get(i).getDate());
            if(lastSectorIndex!=currentSectorIndex)
            {
                ///// Save lastSectorData
                if(lastSectorIndex!=-1)
                    numberOfCommitsPerMonth[lastSectorIndex] = n_commitsForCurrentSector;
                /////
                n_commitsForCurrentSector = 1;
                lastSectorIndex = currentSectorIndex;
            }
            else
            {
                n_commitsForCurrentSector++;
            }
        }

        if(lastSectorIndex!=-1)
            numberOfCommitsPerMonth[lastSectorIndex] = n_commitsForCurrentSector;

        ///////////////////////////


        percentgeOfCommitsPerMonth = new int[n_monthes];
        int maxValue = numberOfCommitsPerMonth[0];
        for(int i=1; i<n_monthes; i++)
            if(numberOfCommitsPerMonth[i]>maxValue)
                maxValue = numberOfCommitsPerMonth[i];
        for(int i=0; i<n_monthes; i++)
        {
            float f = numberOfCommitsPerMonth[i] / (float)maxValue;
            percentgeOfCommitsPerMonth[i] = (int)(f*100);
        }



    }

    private void preCalculation_basic()
    {
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

        n_monthes = countDiffMonth(startDate, endDate)+1;
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

        g2d.drawString("Date:"+getYearForSector(activeMonthIndex)+"/"+getMonthForSector(activeMonthIndex),50,20);

        draw_primaryLine(g2d);
        draw_primaryLineSectors(g2d);
        draw_highlightActiveMonth(g2d);
        draw_commitsPoints(g2d);
    }

    private void draw_commitsPoints(Graphics2D g2d)
    {



        g2d.setColor(Color.WHITE);

        /// Drawing Points
        final Dimension CIRCLE_SIZE = new Dimension(5,5);
        for(int sectorIndex = 0; sectorIndex<n_monthes; sectorIndex++)
        {
            if(numberOfCommitsPerMonth[sectorIndex]==0) continue;
            Point centerOfSector = getCenterOfSector(sectorIndex);
            //g2d.fillOval(centerOfSector.x-CIRCLE_SIZE.width/2,centerOfSector.y-CIRCLE_SIZE.height/2, CIRCLE_SIZE.width,CIRCLE_SIZE.height);
        }


        /// Drawing Pillars
        final int MAX_LENGTH = 30;
        for(int sectorIndex = 0; sectorIndex<n_monthes; sectorIndex++)
        {
            if(numberOfCommitsPerMonth[sectorIndex]==0) continue;
            Point centerOfSector = getCenterOfSector(sectorIndex);
            double l = (MAX_LENGTH*percentgeOfCommitsPerMonth[sectorIndex])/100.0;
            g2d.fillRect(centerOfSector.x-line_sectorsLength/3, centerOfSector.y-(int)l, 2*line_sectorsLength/3, (int)l );
        }

        /// Drawing Number of commits (string)
        for(int sectorIndex = 0; sectorIndex<n_monthes; sectorIndex++)
        {
            if(numberOfCommitsPerMonth[sectorIndex]==0) continue;
            Point centerOfSector = getCenterOfSector(sectorIndex);
            g2d.drawString(Integer.toString(numberOfCommitsPerMonth[sectorIndex]),centerOfSector.x-5,centerOfSector.y-MAX_LENGTH-5);
        }
    }

    private Point getCenterOfSector(int sectorIndex)
    {
        int center_of_diffMonth_th_sector = line_effectiveBegin.x+ (int)((sectorIndex+0.5)*line_sectorsLength);
        Point center = new Point(center_of_diffMonth_th_sector, line_effectiveBegin.y);
        return center;
    }

    private int countDiffMonth(Date firstDate, Date laterDate)
    {
        // Notice: Two dates in the same month => result=0
        Calendar firstCommitCal = Calendar.getInstance();
        firstCommitCal.setTime(firstDate);

        Calendar lastCommitCal = Calendar.getInstance();
        lastCommitCal.setTime(laterDate);

        // Notice: All months are 0-based as Cal.get(Calendar.MONTH) does.
        int start_month = firstCommitCal.get(Calendar.MONTH);
        int start_year = firstCommitCal.get(Calendar.YEAR);

        int end_month = lastCommitCal.get(Calendar.MONTH);
        int end_year = lastCommitCal.get(Calendar.YEAR);

        int diffYear = end_year - start_year;
        int diffMonth = (diffYear * 12) + end_month - start_month;
        return diffMonth;
    }

    private void draw_highlightActiveMonth(Graphics2D g2d)
    {
        final int HIGHLIGHT_HEIGHT = 100;
        Color TRANSPARENT_GREEN = new Color(0,255,0,100);
        g2d.setColor(TRANSPARENT_GREEN);
        final int GAP=1;
        g2d.fillRoundRect(line_effectiveBegin.x+activeMonthIndex*line_sectorsLength+GAP, line_effectiveBegin.y-HIGHLIGHT_HEIGHT/2, line_sectorsLength-2*GAP, HIGHLIGHT_HEIGHT, 3, 3);
    }

    private void draw_primaryLineSectors(Graphics2D g2d)
    {

        final int SECTOR_SPLITTER_LENGTH_MONTH = 5;
        final Stroke SECTOR_SPLITTER_STROKE_MONTH = new BasicStroke(2,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);

        final int SECTOR_SPLITTER_LENGTH_YEAR = 15;
        final Stroke SECTOR_SPLITTER_STROKE_YEAR = new BasicStroke(3,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);

        int sectorSplitter_vecticalLineLength;

        Point lineIterator = (Point) line_effectiveBegin.clone();

        int yearIterator = start_year;
        for (int splitter = 1; splitter < n_monthes; splitter++)
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

            lineIterator.x += line_sectorsLength;
            g2d.drawLine(lineIterator.x, lineIterator.y + sectorSplitter_vecticalLineLength / 2,
                    lineIterator.x, lineIterator.y - sectorSplitter_vecticalLineLength / 2);

            if(getMonthForSector(splitter)== FIRST_MONTH_OF_YEAR_INDEX)
            {
                yearIterator++;
                DrawingHelper.drawStringCenter(g2d, Integer.toString(yearIterator),lineIterator.x,lineIterator.y+SECTOR_SPLITTER_LENGTH_YEAR+5/*5: text position considered as bottom-left*/);

            }

        }
    }

    private void draw_primaryLine(Graphics2D g2d)
    {
        final int PRIMARY_LINE_SIDES_EMPTINESS = 10;
        /////////
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRoundRect(line_effectiveBegin.x, line_effectiveBegin.y, line_effectiveLength, PRIMARY_LINE_TICKNESS, 3, 3);
    }

    // Return value is 0-based
    private int getSectorIndexForDate(Date date)
    {
        final Date firstCommitDate = commitList.get(commitList.size()-1).getDate();
        int sectorIndex = countDiffMonth(firstCommitDate, date);
        return sectorIndex;
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
