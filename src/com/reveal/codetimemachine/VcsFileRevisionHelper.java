package com.reveal.codetimemachine;


import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;

import java.io.IOException;

public class VcsFileRevisionHelper
{
    public static String getContent(VcsFileRevision fileRevision)
    {
        byte[] selectedCommitContent = new byte[0];
        try
        {
            selectedCommitContent = fileRevision.loadContent();
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (VcsException e)
        {
            e.printStackTrace();
        }
        String content = new String(selectedCommitContent);
        return content;
    }
}
