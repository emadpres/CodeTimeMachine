package com.reveal.testtimemachine;


import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;

import java.io.IOException;
import java.util.Date;

public class CommitWrapper // A Wrapper for `fileRevision` class
{
    int cIndex = -1;
    private boolean isFake = false;
    private VcsFileRevision fileRevision = null;
    private FakeCommit fakeCommit = null;

    public CommitWrapper(VcsFileRevision fileRevision, int cIndex)
    {
        isFake = false;
        this.cIndex = cIndex;
        this.fileRevision = fileRevision;
    }

    public CommitWrapper(String content, String commitMessage, Date date, String hash, int cIndex)
    {
        isFake = true;
        this.cIndex = cIndex;
        fakeCommit = new FakeCommit(content, commitMessage, date, hash);
    }

    public boolean isFake()
    {
        return isFake;
    }

    public String getFileContent()
    {
        String content;
        if(!isFake)
        {
            content = VcsFileRevisionHelper.getContent(fileRevision);
        }
        else
        {
            content = fakeCommit.getFileContent();
        }
        return content;
    }

    public Date getDate()
    {
        Date date;
        if (!isFake)
        {
            date = fileRevision.getRevisionDate();
        }
        else
        {
            date = fakeCommit.getDate();
        }
        return date;
    }

    public String getHash()
    {
        String hash;
        if (!isFake)
        {
            hash = fileRevision.getRevisionNumber().asString();
        }
        else
        {
            hash = fakeCommit.getHash();
        }
        return hash;
    }

    public String getCommitMessage()
    {
        String commitMessage;
        if (!isFake)
        {
            commitMessage = fileRevision.getCommitMessage();
        }
        else
        {
            commitMessage = fakeCommit.getCommitMessage();
        }
        return commitMessage;
    }


    private class FakeCommit
    {
        private String content, commitMessage, hash;
        private Date date;

        public FakeCommit(String content, String commitMessage, Date date, String hash)
        {
            this.content = content;
            this.commitMessage = commitMessage;
            this.date = date;
            this.hash = hash;
        }

        public String getFileContent()
        {
            return content;
        }

        public String getCommitMessage()
        {
            return commitMessage;
        }

        public Date getDate()
        {
            return date;
        }

        public String getHash()
        {
            return hash;
        }
    }
}