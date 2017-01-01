package com.reveal.testtimemachine;


import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.ArrayList;

public class CommitsBarTreeView extends CommitsBarBase implements TreeSelectionListener
{
    private CommitsDataTree commitsDataTree;
    private JScrollPane commitsBarTreeView = null;
    private Tree treeComponent = null;


    protected Dimension COMMITS_BAR_VIEW_DIMENSION = new Dimension(250,1000);

    CommitsBarTreeView(TTMSingleFileView TTMWindow)
    {
        this.TTMWindow = TTMWindow;

        commitsBarTreeView = new JBScrollPane();
        commitsBarTreeView.setBorder(null);
        commitsBarTreeView.setMaximumSize(COMMITS_BAR_VIEW_DIMENSION);
        commitsBarTreeView.setPreferredSize(COMMITS_BAR_VIEW_DIMENSION);
        commitsBarTreeView.setSize(COMMITS_BAR_VIEW_DIMENSION);
        commitsBarTreeView.setMinimumSize(COMMITS_BAR_VIEW_DIMENSION);
        commitsBarTreeView.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

    }

    @Override
    public void updateCommitsList(ArrayList<CommitWrapper> newCommitsList)
    {
        this.commitList = newCommitsList;
        commitsDataTree = CommitWrapperHelper.convertCommitsListToCommitsTree(newCommitsList);
        /////
        DefaultMutableTreeNode treeViewRoot = SwingTreeHelper.convertCommitsDataTree_to_SwingTreeNode(commitsDataTree);
        treeComponent = new Tree(treeViewRoot);
        treeComponent.setRootVisible(false);
        //treeComponent.putClientProperty("JTree.lineStyle", "None");
        treeComponent.getSelectionModel().setSelectionMode (TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeComponent.addTreeSelectionListener(this);
        /////
        commitsBarTreeView.setViewportView(treeComponent);

        treeComponent.invalidate();
        treeComponent.revalidate();
        treeComponent.repaint();

        commitsBarTreeView.invalidate();
        commitsBarTreeView.revalidate();
        commitsBarTreeView.repaint();
    }

    public void valueChanged(TreeSelectionEvent e)
    {
        //Returns the last path element of the selection.
        //This method is useful only when the selection model allows a single selection.
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeComponent.getLastSelectedPathComponent();

        if (node == null) return;

        Object nodeInfo = node.getUserObject();
        if (node.isLeaf())
        {
            CommitWrapper commitwrapper = (CommitWrapper)nodeInfo;

            boolean possible = TTMWindow.navigateToCommit(classType.SUBJECT_CLASS, commitwrapper.cIndex);
            if(!possible) return;

            setActiveCommit_cIndex(commitwrapper.cIndex);
        }
    }

    @Override
    public JComponent getComponent()
    {
        return commitsBarTreeView;
    }

    @Override
    public void setActiveCommit_cIndex(int cIndex)
    {
    }
}
