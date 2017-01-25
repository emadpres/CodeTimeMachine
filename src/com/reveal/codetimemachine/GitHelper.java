package com.reveal.codetimemachine;


import com.intellij.execution.ExecutionException;
import com.intellij.execution.OutputListener;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;

import java.util.HashMap;
import java.util.Map;

public class GitHelper
{
    private boolean isChangesStashed = false;
    private final String INVALID = "INVALID";
    private String latestCommitID = INVALID;

    Project project = null;
    StringBuilder outContent = null;
    StringBuilder errContent = null;

    public GitHelper(Project project)
    {
        this.project = project;
        this.backupAllChangesAsStash();
    }

    private GeneralCommandLine createGitCommandLine()
    {
        GeneralCommandLine myCommandLine = new GeneralCommandLine();
        myCommandLine.setExePath("git");
        myCommandLine.setWorkDirectory(project.getBasePath());
        return myCommandLine;
    }

    private OSProcessHandler createNewCommandLineProcessor(GeneralCommandLine myCommandLine)
    {
        OSProcessHandler handler = null;
        try
        {
            handler = new OSProcessHandler(myCommandLine.createProcess(), myCommandLine.getCommandLineString(), myCommandLine.getCharset());
        } catch (ExecutionException e1)
        {
            e1.printStackTrace();
        }

        if(handler!=null)
        {
            outContent = new StringBuilder();
            errContent = new StringBuilder();
            handler.addProcessListener(new OutputListener(outContent, errContent));
        }

        return handler;
    }

    public String getListOfAllFile(String commitIdAsTime)
    {
        GeneralCommandLine myCommandLine = createGitCommandLine();
        myCommandLine.addParameter("ls-tree");
        myCommandLine.addParameter("--name-status");
        myCommandLine.addParameter("-r");
        myCommandLine.addParameter(commitIdAsTime);
        String s = myCommandLine.toString(); //Debugging

        OSProcessHandler handler = createNewCommandLineProcessor(myCommandLine);
        handler.startNotify();
        handler.waitFor();
        int exitCode = handler.getProcess().exitValue();
        if(exitCode==0)
            return outContent.toString();
        else
            return errContent.toString();
    }

    public String getListOfChangedFile(String commitIdAsTime)
    {
        GeneralCommandLine myCommandLine = createGitCommandLine();
        myCommandLine.addParameter("diff-tree");
        myCommandLine.addParameter("--no-commit-id");
        myCommandLine.addParameter("--name-status");
        myCommandLine.addParameter("-r");
        myCommandLine.addParameter(commitIdAsTime);
        String s = myCommandLine.toString(); //Debugging

        OSProcessHandler handler = createNewCommandLineProcessor(myCommandLine);
        handler.startNotify();
        handler.waitFor();
        int exitCode = handler.getProcess().exitValue();
        if(exitCode==0)
            return outContent.toString();
        else
            return errContent.toString();
    }

    private void backupAllChangesAsStash()
    {
        clearStashList();
        stashChanges();
        applyStash(); //now we have changes not only on Stash List, but also in working dir
        /////////////////
        saveLatestCommitID();
    }

    private void saveLatestCommitID()
    {
        GeneralCommandLine myCommandLine = createGitCommandLine();
        myCommandLine.addParameter("rev-parse");
        myCommandLine.addParameter("HEAD");
        String s = myCommandLine.toString(); //Debugging

        OSProcessHandler handler = createNewCommandLineProcessor(myCommandLine);
        handler.startNotify();
        handler.waitFor();
        int exitCode = handler.getProcess().exitValue();
        if(exitCode==0)
        {
            latestCommitID = outContent.toString();
            latestCommitID = latestCommitID.substring(0, latestCommitID.length()-2); //remove last character '\n'
        }
        else
            latestCommitID = INVALID;
    }

    public String stashChanges()
    {
        // This command, save changes as stash and clear them from working dir
        GeneralCommandLine myCommandLine = createGitCommandLine();
        myCommandLine.addParameter("stash");
        String s = myCommandLine.toString(); //Debugging

        OSProcessHandler handler = createNewCommandLineProcessor(myCommandLine);
        handler.startNotify();
        handler.waitFor();
        int exitCode = handler.getProcess().exitValue();
        if(exitCode==0)
        {
            isChangesStashed=true;
            return outContent.toString();
        }
        else
            return errContent.toString();
    }

    public String clearStashList()
    {
        GeneralCommandLine myCommandLine = createGitCommandLine();
        myCommandLine.addParameter("stash");
        myCommandLine.addParameter("clear");
        String s = myCommandLine.toString(); //Debugging

        OSProcessHandler handler = createNewCommandLineProcessor(myCommandLine);
        handler.startNotify();
        handler.waitFor();
        int exitCode = handler.getProcess().exitValue();
        if(exitCode==0)
        {
            isChangesStashed=false;
            return outContent.toString();
        }
        else
            return errContent.toString();
    }

    public String applyStash()
    {
        if(isChangesStashed==false)
            return "Stash list is clear";

        GeneralCommandLine myCommandLine = createGitCommandLine();
        myCommandLine.addParameter("stash");
        myCommandLine.addParameter("apply");
        String s = myCommandLine.toString(); //Debugging

        OSProcessHandler handler = createNewCommandLineProcessor(myCommandLine);
        handler.startNotify();
        handler.waitFor();
        int exitCode = handler.getProcess().exitValue();
        if(exitCode==0)
        {
            return outContent.toString();
        }
        else
            return errContent.toString();
    }

    public String checkoutLatestCommit()
    {
        if(latestCommitID==INVALID) return "";

        return checkoutCommitID(latestCommitID);
    }

    public String checkoutCommitID(String commitIdAsTime)
    {
        GeneralCommandLine myCommandLine = createGitCommandLine();
        myCommandLine.addParameter("reset");
        myCommandLine.addParameter("--hard");
        myCommandLine.addParameter(commitIdAsTime);
        String s = myCommandLine.toString(); //Debugging

        OSProcessHandler handler = createNewCommandLineProcessor(myCommandLine);
        handler.startNotify();
        handler.waitFor();
        int exitCode = handler.getProcess().exitValue();
        if(exitCode==0)
            return outContent.toString();
        else
            return errContent.toString();
    }
}
