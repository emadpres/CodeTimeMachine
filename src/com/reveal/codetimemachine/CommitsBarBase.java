package com.reveal.codetimemachine;


import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public abstract class CommitsBarBase
{
    final int INVALID = -1;
    final Color BG_COLOR = CommonValues.APP_COLOR_THEME; //new Color(236,236,236): Default Light gray

    protected TTMSingleFileView TTMWindow = null;

    public static Dimension COMMITS_BAR_VIEW_DIMENSION = new Dimension(140/*including scroll-handle of CommitsView scrollContainer*/, Toolkit.getDefaultToolkit().getScreenSize().height);

    protected ArrayList<CommitWrapper> commitList /* Most recent commit at 0*/;

    protected ClassType classType = ClassType.NONE;



    public abstract void updateCommitsList(ArrayList<CommitWrapper> newCommitList);

    public abstract JComponent getComponent();

    public abstract void setActiveCommit_cIndex();
}
