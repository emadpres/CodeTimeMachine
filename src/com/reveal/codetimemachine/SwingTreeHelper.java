package com.reveal.codetimemachine;


import javax.swing.tree.DefaultMutableTreeNode;

public class SwingTreeHelper
{
    final int ROOT_LEVEL = 0;
    final int YEAR_LEVEL = 1;
    final int MOTH_LEVEL = 2;
    static DefaultMutableTreeNode convertCommitsDataTree_to_SwingTreeNode(CommitsDataTree dataTree)
    {
        DefaultMutableTreeNode treeViewRoot = new DefaultMutableTreeNode("Root");
        new SwingTreeHelper().createYearsNodes(treeViewRoot, dataTree);
        return treeViewRoot;
    }

    private void createYearsNodes(DefaultMutableTreeNode treeViewRoot, CommitsDataTree dataTree)
    {
        /*if(dataTree.yearNodes.size()==1)
        {
            //treeViewRoot.add(new DefaultMutableTreeNode(dataTree.yearNodes.get(0).year));

            CommitsDataTree.CommitTreeYearNode dataTreeYearNode = dataTree.yearNodes.get(0);
            treeViewRoot.setUserObject(dataTreeYearNode.year);
            createMonthNodes(treeViewRoot, dataTreeYearNode);
            return;
        }*/

        for(int yearIndex = 0; yearIndex<dataTree.yearNodes.size(); yearIndex++)
        {
            CommitsDataTree.CommitTreeYearNode dataTreeYearNode = dataTree.yearNodes.get(yearIndex);
            DefaultMutableTreeNode treeViewYearNode = new DefaultMutableTreeNode(Integer.toString(dataTreeYearNode.year)+" ("+dataTreeYearNode.totalNumberOfCommitsOfThisYear+")");
            treeViewRoot.add(treeViewYearNode);
            createMonthNodes(treeViewYearNode, dataTreeYearNode);
        }
    }

    private void createMonthNodes(DefaultMutableTreeNode treeViewYearNode, CommitsDataTree.CommitTreeYearNode dataTreeYearNode)
    {
        if(dataTreeYearNode.monthNodes.size()==1)
        {
            //treeViewYearNode.add(new DefaultMutableTreeNode(dataTreeYearNode.monthNodes.get(0).month));
            CommitsDataTree.CommitTreeMonthNode dataTreeMonthNode = dataTreeYearNode.monthNodes.get(0);
            treeViewYearNode.setUserObject(CalendarHelper.convertMonthIndexToShortName(dataTreeMonthNode.month)+" "+dataTreeYearNode.year+" ("+dataTreeYearNode.totalNumberOfCommitsOfThisYear+")");

            createDayNodes(treeViewYearNode, dataTreeMonthNode);
            return;
        }

        for(int monthIndex = 0; monthIndex<dataTreeYearNode.monthNodes.size(); monthIndex++)
        {
            CommitsDataTree.CommitTreeMonthNode dataTreeMonthNode = dataTreeYearNode.monthNodes.get(monthIndex);
            DefaultMutableTreeNode treeViewMonthNode = new DefaultMutableTreeNode(CalendarHelper.convertMonthIndexToShortName(dataTreeMonthNode.month)+" ("+dataTreeMonthNode.totalNumberOfCommitsOfThisMonth+")");
            treeViewYearNode.add(treeViewMonthNode);
            createDayNodes(treeViewMonthNode, dataTreeMonthNode);
        }
    }

    private void createDayNodes(DefaultMutableTreeNode treeViewMonthNode, CommitsDataTree.CommitTreeMonthNode dataTreeMonthNode)
    {
        if(dataTreeMonthNode.dayNodes.size()==1)
        {
            //treeViewMonthNode.add(new DefaultMutableTreeNode(dataTreeMonthNode.dayNodes.get(0).day));

            CommitsDataTree.CommitTreeDayNode dataTreeDayNode = dataTreeMonthNode.dayNodes.get(0);

            treeViewMonthNode.setUserObject(dataTreeDayNode.day+" "+CalendarHelper.convertMonthIndexToShortName(dataTreeMonthNode.month)+" "+dataTreeMonthNode.parentYearNode.year+" ("+dataTreeMonthNode.totalNumberOfCommitsOfThisMonth+")");

            createLeafNodes(treeViewMonthNode, dataTreeDayNode);
            return;
        }

        for(int dayIndex = 0; dayIndex<dataTreeMonthNode.dayNodes.size(); dayIndex++)
        {
            CommitsDataTree.CommitTreeDayNode dataTreeDayNode = dataTreeMonthNode.dayNodes.get(dayIndex);
            DefaultMutableTreeNode treeViewDayNode = new DefaultMutableTreeNode(Integer.toString(dataTreeDayNode.day)+" ("+dataTreeDayNode.commitsOfTheDay.size()+")");
            treeViewMonthNode.add(treeViewDayNode);
            createLeafNodes(treeViewDayNode, dataTreeDayNode);
        }
    }

    private void createLeafNodes(DefaultMutableTreeNode treeViewDayNode, CommitsDataTree.CommitTreeDayNode dataTreeDayNode)
    {
        for (int commitIndex = 0; commitIndex < dataTreeDayNode.commitsOfTheDay.size(); commitIndex++)
        {
            DefaultMutableTreeNode treeViewCommitNode = new DefaultMutableTreeNode(dataTreeDayNode.commitsOfTheDay.get(commitIndex));
            treeViewDayNode.add(treeViewCommitNode);
        }
    }
}
