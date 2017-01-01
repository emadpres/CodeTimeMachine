package com.reveal.testtimemachine;


import java.util.ArrayList;
import java.util.Calendar;

public class CommitWrapperHelper
{
    public static CommitsDataTree convertCommitsListToCommitsTree(ArrayList<CommitWrapper> commitsList)
    {
        CommitsDataTree tree = new CommitsDataTree();
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
}
