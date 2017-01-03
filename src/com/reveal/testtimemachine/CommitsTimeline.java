package com.reveal.testtimemachine;

import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class CommitsTimeline extends JComponent
{
    //General Notice: All calculation are INT since Point class is INT.
    //General Notice: "First commits" means "Oldest commit" and is the last element in commits list.

    TTMSingleFileView TTMWindow = null;
    JBScrollPane scrollComponent = null;

    int FIRST_MONTH_OF_YEAR_INDEX = 0;

    final int INVALIDE_VALUE = -10;
    int activeRange_startIndex = INVALIDE_VALUE, activeRange_endIndex=INVALIDE_VALUE; //0-based - we use "*_temp" variables unless they are INVALID_VALUE
    int activeRange_startIndex_temp = INVALIDE_VALUE, activeRange_endIndex_temp = INVALIDE_VALUE; //0-based
    Point line_effectiveBegin = null;
    int n_monthes = 0;
    int line_effectiveLength = 0;
    int original_sectorsLength=0;
    int line_sectorsLength = 0;
    int PRIMARY_LINE_TICKNESS = 3;
    int LOD = 1;

    final int PRIMARY_LINE_OUTTER_SIDES_GAP = 20;

    int start_year=0, start_month=0, end_year=0, end_month=0; // Notice: All months are 0-based as Cal.get(Calendar.MONTH) does.

    ArrayList<CommitWrapper> commitList = null;
    int[] numberOfCommitsPerMonth = null;
    int[] percentgeOfCommitsPerMonth = null;

    final int INITIAL_ACTIVE_RANGE_MONTH = 3;

    public CommitsTimeline(ArrayList<CommitWrapper> commitList, TTMSingleFileView TTMWindow, JBScrollPane scrollComponent)
    {
        super();
        this.commitList = commitList;
        this.TTMWindow = TTMWindow;
        this.scrollComponent = scrollComponent;

        this.setLayout(null);
        // setSize(COMMITS_BAR_VIEW_DIMENSION); By "updateDrawingVariablesForNewSectorsLength()"
        ////////////////////////////////////

        preCalculation_basic();
        preCalculation_commitsStat();
        preCalculation_originalSectorsLength();

        addMouseListener();

        activeRange_endIndex = n_monthes-1;
        activeRange_startIndex = Integer.max(0,activeRange_endIndex- INITIAL_ACTIVE_RANGE_MONTH);
        setActiveRange(activeRange_startIndex, activeRange_endIndex);


        repaint();
    }

    private void preCalculation_originalSectorsLength()
    {
        int maxPossibleLineLength = scrollComponent.getSize().width - 2 * PRIMARY_LINE_OUTTER_SIDES_GAP;
        original_sectorsLength = maxPossibleLineLength / n_monthes;  // => "int" value
        setSectorsLength(original_sectorsLength);
    }

    public void setZoomFactor(int newZoomFactor)
    {
        setSectorsLength(original_sectorsLength*newZoomFactor);
    }

    private void setSectorsLength(int newSectorsLength)
    {
        line_sectorsLength = newSectorsLength;
        updateDrawingVariablesForNewSectorsLength();
    }

    private void updateDrawingVariablesForNewSectorsLength()
    {
        int BIG_ENOUGH_LENGTH = 50;
        if(line_sectorsLength > BIG_ENOUGH_LENGTH)
            LOD=2;
        else
            LOD=1;
        //////////////////

        line_effectiveLength = n_monthes * line_sectorsLength; // Because "Ponit(x,y)" and therefor the "line_sectorsLength" shoudl be INT.

        Dimension componentDimension = new Dimension(line_effectiveLength+50, scrollComponent.getSize().height);
        setSize(componentDimension);
        setPreferredSize(componentDimension);
        setMaximumSize(componentDimension);
        setMinimumSize(componentDimension);

        line_effectiveBegin = new Point( (getSize().width-line_effectiveLength)/2, getSize().height/2);
    }

    private void preCalculation_commitsStat()
    {
        if(commitList.size()<=0) return;

        numberOfCommitsPerMonth = new int[n_monthes];
        for(int i=0; i<n_monthes; i++)
            numberOfCommitsPerMonth[i]=0;


        for(int i=0; i<commitList.size(); i++)
        {
            int currentSectorIndex = getSectorIndexForDate(commitList.get(i).getDate());
            numberOfCommitsPerMonth[currentSectorIndex]++;
        }

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
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                Point p = e.getPoint();
                int m = findMonthIndexFromPoint(p);
                activeRange_startIndex_temp = activeRange_endIndex_temp = m;

                CommitsTimeline.this.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                int m =findMonthIndexFromPoint(e.getPoint());
                activeRange_endIndex_temp = m;

                ///// Update setHighlightBorder Area
                if(activeRange_startIndex_temp>activeRange_endIndex_temp)
                {
                    int dummy = activeRange_endIndex_temp;
                    activeRange_endIndex_temp = activeRange_startIndex_temp;
                    activeRange_startIndex_temp = dummy;
                }

                setActiveRange(activeRange_startIndex_temp, activeRange_endIndex_temp);
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

        addMouseMotionListener(new MouseMotionListener()
        {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                if(activeRange_startIndex_temp==INVALIDE_VALUE) return;

                int m =findMonthIndexFromPoint(e.getPoint());
                activeRange_endIndex_temp = m;

                CommitsTimeline.this.repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e)
            {

            }
        });
    }

    public void setActiveRange(int startMonthIndex, int endMonthIndex)
    {
        activeRange_startIndex = startMonthIndex;
        activeRange_endIndex = endMonthIndex;
        activeRange_startIndex_temp = activeRange_endIndex_temp = INVALIDE_VALUE; // Validate above variables to render.
        CommitsTimeline.this.repaint();

        ///// Update CommitsBar
        Date startDate = commitList.get(commitList.size()-1).getDate();
        Calendar cal = Calendar.getInstance();
        ////
        cal.setTime(startDate);
        cal.add(Calendar.MONTH, activeRange_startIndex);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date activeRange_startDate_inclusive = cal.getTime();
        /////
        cal.add(Calendar.MONTH, activeRange_endIndex-activeRange_startIndex);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date activeRange_endDate_inclusive = cal.getTime();

        TTMWindow.updateCommitsBar(activeRange_startDate_inclusive, activeRange_endDate_inclusive);
    }

    private int findMonthIndexFromPoint(Point p)
    {
        final int Y_ACCEPTABLE_CLICK_MARGINE = 35;
        int endPoint_x = line_effectiveBegin.x + n_monthes*line_sectorsLength - 5/*floating point error*/;

        //if ( p.y > line_effectiveBegin.y+Y_ACCEPTABLE_CLICK_MARGINE || p.y < line_effectiveBegin.y-Y_ACCEPTABLE_CLICK_MARGINE )
        if (p.x < line_effectiveBegin.x)
        {
            p.x = line_effectiveBegin.x;
        }
        else if( p.x > endPoint_x)
            p.x = endPoint_x;

        int m = (p.x - line_effectiveBegin.x) / line_sectorsLength;
        return m;
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

        g2d.drawString("Date:" +getYearForSector(activeRange_startIndex)+"/"+getMonthForSector(activeRange_startIndex) +"--"
                                    +getYearForSector(activeRange_endIndex)+"/"+getMonthForSector(activeRange_endIndex),50,10);

        draw_primaryLine(g2d);
        draw_primaryLineSectors(g2d);
        draw_highlightActiveMonth(g2d);
        draw_commits(g2d);
        if(CommonValues.IS_UI_IN_DEBUGGING_MODE)
            draw_helpingInformation(g2d);
    }

    private void draw_helpingInformation(Graphics2D g2d)
    {
        g2d.setColor(Color.RED);
        g2d.fillOval(5,5,10,10);
        g2d.setColor(Color.GREEN);
        g2d.fillOval(5,getSize().height-15,10,10);
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(getSize().width-15,5,10,10);
        g2d.setColor(Color.BLUE);
        g2d.fillOval(getSize().width-15,getSize().height-15,10,10);
    }

    private void draw_commits(Graphics2D g2d)
    {
        if(LOD==1)
            draw_commitsSummarized(g2d);
        if(LOD==2)
            draw_commitsIndivually(g2d);

    }

    private void draw_commitsIndivually(Graphics2D g2d)
    {
        /// Drawing Points
        final Dimension CIRCLE_SIZE = new Dimension(7,7);

        g2d.setStroke(new BasicStroke(0.5f));

        Date currentDate;

        for(int i=0 ; i<commitList.size()-1; i++)
        {
            currentDate = commitList.get(i).getDate();
            int sectorIndex = getSectorIndexForDate(currentDate);
            ////
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);
            int dayInMonth = cal.get(Calendar.DAY_OF_MONTH);

            Point p = (Point) line_effectiveBegin.clone();
            p.x = p.x + (int) ((sectorIndex+(dayInMonth/31.0))*line_sectorsLength);

            g2d.setColor(Color.BLUE);
            g2d.fillOval(p.x - CIRCLE_SIZE.width/2, p.y - CIRCLE_SIZE.height/2, CIRCLE_SIZE.width, CIRCLE_SIZE.height);
            g2d.setColor(Color.WHITE);
            g2d.drawOval(p.x - CIRCLE_SIZE.width/2, p.y - CIRCLE_SIZE.height/2, CIRCLE_SIZE.width, CIRCLE_SIZE.height);

        }
    }

    private void draw_commitsSummarized(Graphics2D g2d)
    {
        g2d.setColor(Color.WHITE);


        final int MAX_LENGTH = 30;
        for(int sectorIndex = 0; sectorIndex<n_monthes; sectorIndex++)
        {
            if(numberOfCommitsPerMonth[sectorIndex]==0) continue;
            Point centerOfSector = getCenterOfSector(sectorIndex);

            /// Drawing Pillars
            int l = (int)((MAX_LENGTH*percentgeOfCommitsPerMonth[sectorIndex])/100.0);
            g2d.fillRect(centerOfSector.x-line_sectorsLength/3, centerOfSector.y-l, 2*line_sectorsLength/3, l );

            /// Drawing Number of commits (string)
            g2d.setFont(new Font("Arial",Font.BOLD, 10));
            DrawingHelper.drawStringCenter(g2d, Integer.toString(numberOfCommitsPerMonth[sectorIndex]),centerOfSector.x,centerOfSector.y-l-5);
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
        Color TRANSPARENT_GREEN = new Color(0,255,0,70);
        g2d.setColor(TRANSPARENT_GREEN);
        final int GAP=1;


        int s=0, e=0;
        if(activeRange_startIndex_temp!=INVALIDE_VALUE)
        {
            // Notice "activeRange_startIndex_temp" could be bigger than "activeRange_endIndex_temp"
            s = activeRange_startIndex_temp;
            e = activeRange_endIndex_temp;
            if(s>e)
            {
                int dummmy = s;
                s = e;
                e = dummmy;
            }
        }
        else
        {
            s = activeRange_startIndex;
            e = activeRange_endIndex;
        }

        int activeRange_length = e - s +1;
        g2d.fillRoundRect(line_effectiveBegin.x+ s*line_sectorsLength+GAP, line_effectiveBegin.y-HIGHLIGHT_HEIGHT/2-4/*experiential*/, activeRange_length*line_sectorsLength-2*GAP, HIGHLIGHT_HEIGHT, 3, 3);
    }

    private void draw_primaryLineSectors(Graphics2D g2d)
    {

        int SECTOR_SPLITTER_LENGTH_MONTH = 5;
        if(LOD>1)
            SECTOR_SPLITTER_LENGTH_MONTH *=2;

        int month_strokeWidth = 2;
        if(LOD>1)
            month_strokeWidth *=2;
        final Stroke SECTOR_SPLITTER_STROKE_MONTH = new BasicStroke(month_strokeWidth,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);

        int SECTOR_SPLITTER_LENGTH_YEAR = 15;
        if(LOD>1)
            SECTOR_SPLITTER_LENGTH_YEAR *=2;

        int year_strokeWidth = 3;
        if(LOD>1)
            year_strokeWidth *=2;
        final Stroke SECTOR_SPLITTER_STROKE_YEAR = new BasicStroke(year_strokeWidth,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);

        int sectorSplitter_vecticalLineLength;

        Point lineIterator = (Point) line_effectiveBegin.clone();

        int yearIterator = start_year;
        for (int splitter = 0; splitter < n_monthes; splitter++)
        {
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

            if(getMonthForSector(splitter) == FIRST_MONTH_OF_YEAR_INDEX)
            {
                yearIterator++;
                int y_pos = 0;
                if(LOD==1)
                    y_pos= lineIterator.y+SECTOR_SPLITTER_LENGTH_YEAR+5/*5: text position considered as bottom-left*/;
                else
                    y_pos = lineIterator.y-SECTOR_SPLITTER_LENGTH_YEAR;


                DrawingHelper.drawStringCenter(g2d, Integer.toString(yearIterator),lineIterator.x,y_pos);

            }

            if(LOD>1)
            {
                String month_name = CalendarHelper.convertMonthIndexToShortName(getMonthForSector(splitter));
                if(start_year==end_year /*We have one year altogether*/ && splitter==0/*First Month*/ )
                    month_name = month_name + " " +Integer.toString(start_year);
                g2d.drawString(month_name, lineIterator.x,lineIterator.y+SECTOR_SPLITTER_LENGTH_YEAR+5/*5: text position considered as bottom-left*/);
            }

            lineIterator.x += line_sectorsLength;
        }

        //g2d.setStroke(null);
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
