package com.reveal.testtimemachine;

import java.util.ArrayList;

class CommitsDataTree
{
    ArrayList<CommitTreeYearNode> commitTreeYearsNode = new ArrayList<>();

    //public void addCommits(ArrayList<CommitWrapper> commitsListOfOneYear, int year)
    public void addCommit(CommitWrapper commit, int year, int month, int day)
    {
        int foundIndex=-1;
        for(int i=0; i<commitTreeYearsNode.size(); i++)
        {
            if(commitTreeYearsNode.get(i).year ==year)
            {
                foundIndex = i;
                break;
            }
        }
        if(foundIndex==-1)
        {
            commitTreeYearsNode.add(new CommitTreeYearNode(this, year));
            foundIndex = commitTreeYearsNode.size()-1;
        }
        ////////////////////
        commitTreeYearsNode.get(foundIndex).addCommit(commit, month, day);
    }


    public class CommitTreeYearNode
    {
        int year=-1;
        CommitsDataTree parentTreeRoot = null;
        ArrayList<CommitTreeMonthNode> commitTreeMonthsNode = new ArrayList<>();

        public CommitTreeYearNode(CommitsDataTree parentTreeRoot, int year)
        {
            this.parentTreeRoot = parentTreeRoot;
            this.year = year;
        }

        public void addCommit(CommitWrapper commit, int month, int day)
        {
            int foundIndex=-1;
            for(int i=0; i<commitTreeMonthsNode.size(); i++)
            {
                if(commitTreeMonthsNode.get(i).month == month)
                {
                    foundIndex = i;
                    break;
                }
            }
            if(foundIndex==-1)
            {
                commitTreeMonthsNode.add(new CommitTreeMonthNode(this, month));
                foundIndex = commitTreeMonthsNode.size()-1;
            }
            ////////////////////
            commitTreeMonthsNode.get(foundIndex).addCommit(commit, day);
        }
    }

    public class CommitTreeMonthNode
    {
        int month=-1;
        CommitTreeYearNode parentYearNode=null;
        ArrayList<CommitTreeDayNode> commitTreeDaysNode = new ArrayList<>();

        public CommitTreeMonthNode(CommitTreeYearNode parentYearNode, int month)
        {
            this.parentYearNode = parentYearNode;
            this.month = month;
        }

        public void addCommit(CommitWrapper commit, int day)
        {
            int foundIndex=-1;
            for(int i=0; i<commitTreeDaysNode.size(); i++)
            {
                if(commitTreeDaysNode.get(i).day == day)
                {
                    foundIndex = i;
                    break;
                }
            }
            if(foundIndex==-1)
            {
                commitTreeDaysNode.add(new CommitTreeDayNode(this, day));
                foundIndex = commitTreeDaysNode.size()-1;
            }
            ////////////////////
            commitTreeDaysNode.get(foundIndex).addCommit(commit);
        }
    }

    public class CommitTreeDayNode
    {
        int day = -1;
        CommitTreeMonthNode parentMonthNode = null;
        ArrayList<CommitWrapper> commitsOfDay = new ArrayList<>();

        public CommitTreeDayNode(CommitTreeMonthNode parentMonthNode, int day)
        {
            this.parentMonthNode = parentMonthNode;
            this.day = day;
        }

        public void addCommit(CommitWrapper commit)
        {
            commitsOfDay.add(commit);
        }
    }
}
