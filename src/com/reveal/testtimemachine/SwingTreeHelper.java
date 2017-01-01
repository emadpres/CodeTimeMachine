package com.reveal.testtimemachine;


import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Objects;

public class SwingTreeHelper
{
    static DefaultMutableTreeNode convertCommitsDataTree_to_SwingTreeNode(CommitsDataTree dataTree)
    {
        DefaultMutableTreeNode treeViewRoot = new DefaultMutableTreeNode("Root");
        new SwingTreeHelper().createYearsNodes(treeViewRoot, dataTree);
        return treeViewRoot;
    }

    private void createYearsNodes(DefaultMutableTreeNode treeViewRoot, CommitsDataTree dataTree)
    {
        for(int yearIndex=0; yearIndex<dataTree.commitTreeYearsNode.size(); yearIndex++)
        {
            CommitsDataTree.CommitTreeYearNode dataTreeyearNode = dataTree.commitTreeYearsNode.get(yearIndex);
            DefaultMutableTreeNode treeViewYearNode = new DefaultMutableTreeNode(Integer.toString(dataTreeyearNode.year));
            treeViewRoot.add(treeViewYearNode);
            createMonthNodes(treeViewYearNode, dataTreeyearNode);
        }
    }

    private void createMonthNodes(DefaultMutableTreeNode treeViewYearNode, CommitsDataTree.CommitTreeYearNode dataTreeYearNode)
    {
        for(int monthIndex=0; monthIndex<dataTreeYearNode.commitTreeMonthsNode.size(); monthIndex++)
        {
            CommitsDataTree.CommitTreeMonthNode dataTreeMonthNode = dataTreeYearNode.commitTreeMonthsNode.get(monthIndex);
            DefaultMutableTreeNode treeViewMonthNode = new DefaultMutableTreeNode(CalendarHelper.convertMonthIndexToShortName(dataTreeMonthNode.month));
            treeViewYearNode.add(treeViewMonthNode);
            createDayNodes(treeViewYearNode, dataTreeMonthNode);
        }
    }

    private void createDayNodes(DefaultMutableTreeNode treeViewMonthNode, CommitsDataTree.CommitTreeMonthNode dataTreeMonthNode)
    {
        for(int dayIndex=0; dayIndex<dataTreeMonthNode.commitTreeDaysNode.size(); dayIndex++)
        {
            CommitsDataTree.CommitTreeDayNode dataTreeDayNode = dataTreeMonthNode.commitTreeDaysNode.get(dayIndex);
            DefaultMutableTreeNode treeViewDayNode = new DefaultMutableTreeNode(Integer.toString(dataTreeDayNode.day));
            treeViewMonthNode.add(treeViewDayNode);
            createLeafNodes(treeViewDayNode, dataTreeDayNode);
        }
    }

    private void createLeafNodes(DefaultMutableTreeNode treeViewDayNode, CommitsDataTree.CommitTreeDayNode dataTreeDayNode)
    {
        for (int commitIndex = 0; commitIndex < dataTreeDayNode.commitsOfDay.size(); commitIndex++)
        {
            DefaultMutableTreeNode treeViewCommitNode = new DefaultMutableTreeNode(dataTreeDayNode.commitsOfDay.get(commitIndex));
            treeViewDayNode.add(treeViewCommitNode);
        }
    }
}
