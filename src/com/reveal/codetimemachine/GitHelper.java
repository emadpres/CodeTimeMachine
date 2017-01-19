package com.reveal.codetimemachine;


import com.intellij.execution.ExecutionException;
import com.intellij.execution.OutputListener;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.openapi.project.Project;

public class GitHelper
{
    private static GitHelper instance = null;
    private boolean isChangesStashed = false;

    Project project = null;
    StringBuilder outContent = null;
    StringBuilder errContent = null;

    private GitHelper(Project project)
    {
        this.project = project;
    }

    static public GitHelper getInstance(Project project)
    {
        if(instance == null)
            instance = new GitHelper(project);
        return instance;
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

    public void backupAllChangesAsStash()
    {
        clearStashList();
        stashChanges();
        applyStash(); //now we have changes not only on Stash List, but also in working dir
    }

    public String stashChanges()
    {
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
