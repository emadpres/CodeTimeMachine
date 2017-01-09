package com.reveal.testtimemachine;


import com.intellij.execution.ExecutionException;
import com.intellij.execution.OutputListener;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.openapi.project.Project;

public class GitHelper
{
    private static GitHelper instance = null;


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
        String s = myCommandLine.toString();
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
        String s = myCommandLine.toString();
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
