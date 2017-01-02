package com.reveal.testtimemachine;

import java.util.ArrayList;

class CommitsDataTree
{
    ArrayList<CommitTreeYearNode> yearNodes = new ArrayList<>();

    //public void addCommits(ArrayList<CommitWrapper> commitsListOfOneYear, int year)
    public void addCommit(CommitWrapper commit, int year, int month, int day)
    {
        int foundIndex=-1;
        for(int i = 0; i< yearNodes.size(); i++)
        {
            if(yearNodes.get(i).year ==year)
            {
                foundIndex = i;
                break;
            }
        }
        if(foundIndex==-1)
        {
            yearNodes.add(new CommitTreeYearNode(this, year));
            foundIndex = yearNodes.size()-1;
        }
        ////////////////////
        yearNodes.get(foundIndex).addCommit(commit, month, day);
    }


    public class CommitTreeYearNode
    {
        int year=-1;
        CommitsDataTree parentTreeRoot = null;
        int totalNumberOfCommitsOfThisYear=0;
        ArrayList<CommitTreeMonthNode> monthNodes = new ArrayList<>();

        public CommitTreeYearNode(CommitsDataTree parentTreeRoot, int year)
        {
            this.parentTreeRoot = parentTreeRoot;
            this.year = year;
        }

        public void addCommit(CommitWrapper commit, int month, int day)
        {
            totalNumberOfCommitsOfThisYear++;

            int foundIndex=-1;
            for(int i = 0; i< monthNodes.size(); i++)
            {
                if(monthNodes.get(i).month == month)
                {
                    foundIndex = i;
                    break;
                }
            }
            if(foundIndex==-1)
            {
                monthNodes.add(new CommitTreeMonthNode(this, month));
                foundIndex = monthNodes.size()-1;
            }
            ////////////////////
            monthNodes.get(foundIndex).addCommit(commit, day);
        }
    }

    public class CommitTreeMonthNode
    {
        int month=-1;
        CommitTreeYearNode parentYearNode=null;
        int totalNumberOfCommitsOfThisMonth=0;
        ArrayList<CommitTreeDayNode> dayNodes = new ArrayList<>();

        public CommitTreeMonthNode(CommitTreeYearNode parentYearNode, int month)
        {
            this.parentYearNode = parentYearNode;
            this.month = month;
        }

        public void addCommit(CommitWrapper commit, int day)
        {
            totalNumberOfCommitsOfThisMonth++;

            int foundIndex=-1;
            for(int i = 0; i< dayNodes.size(); i++)
            {
                if(dayNodes.get(i).day == day)
                {
                    foundIndex = i;
                    break;
                }
            }
            if(foundIndex==-1)
            {
                dayNodes.add(new CommitTreeDayNode(this, day));
                foundIndex = dayNodes.size()-1;
            }
            ////////////////////
            dayNodes.get(foundIndex).addCommit(commit);
        }
    }

    public class CommitTreeDayNode
    {
        int day = -1;
        CommitTreeMonthNode parentMonthNode = null;
        ArrayList<CommitWrapper> commitsOfTheDay = new ArrayList<>();

        public CommitTreeDayNode(CommitTreeMonthNode parentMonthNode, int day)
        {
            this.parentMonthNode = parentMonthNode;
            this.day = day;
        }

        public void addCommit(CommitWrapper commit)
        {
            commitsOfTheDay.add(commit);
        }
    }
}
