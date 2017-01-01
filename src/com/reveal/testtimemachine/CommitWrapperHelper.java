package com.reveal.testtimemachine;


import java.util.ArrayList;
import java.util.Calendar;

public class CommitWrapperHelper
{
    public static CommitsTree convertCommitsListToCommitsTree(ArrayList<CommitWrapper> commitsList)
    {
        CommitsTree tree = new CommitWrapperHelper().new CommitsTree();
        ////
        Calendar cal = Calendar.getInstance();
        int lastYear=-1, year=-1, month=-1, day=-1;
        for(int i=0;i<commitsList.size(); i++)
        {
            cal.setTime(commitsList.get(i).getDate());
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            day = cal.get(Calendar.DAY_OF_MONTH);
            tree.addCommit(commitsList.get(i), year, month, day);
        }

        return tree;
    }

    public class CommitsTree
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
                commitTreeYearsNode.add(new CommitTreeYearNode(this));
                foundIndex = commitTreeYearsNode.size()-1;
            }
            ////////////////////
            commitTreeYearsNode.get(foundIndex).addCommit(commit, month, day);
        }
    }

    public class CommitTreeYearNode
    {
        int year=-1;
        CommitsTree parentTreeRoot = null;
        ArrayList<CommitTreeMonthNode> commitTreeMonthsNode = new ArrayList<>();

        public CommitTreeYearNode(CommitsTree parentTreeRoot)
        {
            this.parentTreeRoot = parentTreeRoot;
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
                commitTreeMonthsNode.add(new CommitTreeMonthNode(this));
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
        ArrayList<CommitTreeDayNode> commitTreeDaysNode = null;

        public CommitTreeMonthNode(CommitTreeYearNode parentYearNode)
        {
            this.parentYearNode = parentYearNode;
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
                commitTreeDaysNode.add(new CommitTreeDayNode(this));
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

        public CommitTreeDayNode(CommitTreeMonthNode parentMonthNode)
        {
            this.parentMonthNode = parentMonthNode;
        }

        public void addCommit(CommitWrapper commit)
        {
            commitsOfDay.add(commit);
        }
    }
}
