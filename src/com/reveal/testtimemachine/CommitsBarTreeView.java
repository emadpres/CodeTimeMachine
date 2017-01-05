package com.reveal.testtimemachine;


import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.ArrayList;

import javax.swing.ImageIcon;

public class CommitsBarTreeView extends CommitsBarBase implements TreeSelectionListener
{
    private CommitsDataTree commitsDataTree;
    private JScrollPane commitsBarTreeView = null;
    private Tree treeComponent = null;


    protected Dimension COMMITS_BAR_VIEW_DIMENSION = new Dimension(220,750);

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
        commitsBarTreeView.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    }

    @Override
    public void updateCommitsList(ArrayList<CommitWrapper> newCommitsList)
    {
        this.commitList = newCommitsList;
        commitsDataTree = CommitWrapperHelper.convertCommitsListToCommitsTree(newCommitsList);
        /////
        DefaultMutableTreeNode treeViewRoot = SwingTreeHelper.convertCommitsDataTree_to_SwingTreeNode(commitsDataTree);
        treeComponent = new Tree(treeViewRoot);
        Color APP_BG_COLOR = new Color(236,236,236);
        treeComponent.setBackground(APP_BG_COLOR);
        treeComponent.setRootVisible(false);
        treeComponent.setShowsRootHandles(false); // Toplevel 'Root' or (if Root invisible) it's children don't need + to expand)
        treeComponent.putClientProperty("JTree.lineStyle", "Angled"); //"Angled" (default) 	"Horizontal" 	"None"
        treeComponent.getSelectionModel().setSelectionMode (TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeComponent.addTreeSelectionListener(this);
        ///
        DefaultMutableTreeNode currentNode = treeViewRoot.getNextNode();
        while (currentNode != null){
            if (currentNode.getLevel()==1)
                treeComponent.expandPath(new TreePath(currentNode.getPath()));
            currentNode = currentNode.getNextNode();
        }

        ///
        //ImageIcon leafIcon = createImageIcon("images/git-commit.png");
        ImageIcon leafIcon = new ImageIcon(getClass().getResource("/images/git-commit.png"));
        if (leafIcon != null) {
            DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
            renderer.setLeafIcon(leafIcon);
            treeComponent.setCellRenderer(renderer);
        }
        /////
        commitsBarTreeView.setViewportView(treeComponent);

        treeComponent.invalidate();
        treeComponent.revalidate();
        treeComponent.repaint();

        commitsBarTreeView.invalidate();
        commitsBarTreeView.revalidate();
        commitsBarTreeView.repaint();
    }

    @Override
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
            if(commitwrapper == null) return; //We have some leafs (not lowest level which are Year/Month names.

            TTMWindow.activeCommit_cIndex = commitwrapper.cIndex;
            TTMWindow.navigateToCommit(classType.SUBJECT_CLASS, commitwrapper.cIndex);

            setActiveCommit_cIndex();
        }
    }

    @Override
    public JComponent getComponent()
    {
        return commitsBarTreeView;
    }

    @Override
    /*Get Active_cIndex from TTMWindow*/
    public void setActiveCommit_cIndex()
    {
    }
}
