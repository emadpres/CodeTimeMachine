package com.reveal.codetimemachine;


public class SingleViewInformation
{
    private CommitWrapper activeCommit = null;
    private  String filename = "";

    public SingleViewInformation(CommitWrapper activeCommit, String filename)
    {
        this.activeCommit = activeCommit;
        this.filename = filename;
    }

    public CommitWrapper getActiveCommit()
    {
        return activeCommit;
    }

    @Override
    public String toString()
    {
        return filename;
    }
}
