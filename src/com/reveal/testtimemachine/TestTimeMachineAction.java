package com.reveal.testtimemachine;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsHistoryProvider;
import com.intellij.openapi.vcs.history.VcsHistorySession;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by emadpres on 11/23/16.
 */
public class TestTimeMachineAction extends AnAction
{
    final boolean AUTOMATICALLY_CHOOSE_SAMPLE_FILES = false;
    final int NUM_OF_FILES____TEMP = 1;
    //////////////////////////////
    Project project = null;
    TestTimeMachineWindow mainWindow = null;

    @Override
    public void actionPerformed(AnActionEvent e)
    {
        if(mainWindow!=null) return;

        project = e.getProject();

        VirtualFile[] chosenVirtualFiles = getSubjectAndTestVirtualFiles();
        VcsHistoryProvider myGitVcsHistoryProvider = getGitHistoryProvider();
        ArrayList<List<VcsFileRevision>> _fileRevisionsLists = getRevisionListForSubjectAndTestClass(myGitVcsHistoryProvider, chosenVirtualFiles);

        mainWindow = new TestTimeMachineWindow(project, chosenVirtualFiles, _fileRevisionsLists);
        ToolWindowManager.getInstance(project).registerToolWindow("TTM", mainWindow.getComponent(), ToolWindowAnchor.RIGHT);
    }

    private ArrayList<List<VcsFileRevision>> getRevisionListForSubjectAndTestClass(VcsHistoryProvider myGitVcsHistoryProvider, VirtualFile[] chosenVirtualFiles)
    {
        ArrayList<List<VcsFileRevision>> _fileRevisionsLists = new ArrayList<>(2);

        for(int i=0; i<NUM_OF_FILES____TEMP; i++)
        {
            FilePath filePathOn = VcsContextFactory.SERVICE.getInstance().createFilePathOn(chosenVirtualFiles[i]);
            VcsHistorySession sessionFor = null;
            try
            {
                sessionFor = myGitVcsHistoryProvider.createSessionFor(filePathOn);
            } catch (VcsException e1)
            {
                e1.printStackTrace();
            }
            _fileRevisionsLists.add(sessionFor.getRevisionList());
        }
        return _fileRevisionsLists;
    }

    private VcsHistoryProvider getGitHistoryProvider()
    {
        ProjectLevelVcsManager mgr = ProjectLevelVcsManager.getInstance( project );
        AbstractVcs[] allActiveVcss = mgr.getAllActiveVcss();
        AbstractVcs myGit = allActiveVcss[0];
        return myGit.getVcsHistoryProvider();
    }

    public VirtualFile[] getSubjectAndTestVirtualFiles()
    {
        VirtualFile[] chosenVirtualFiles = null;

        if(AUTOMATICALLY_CHOOSE_SAMPLE_FILES)
        {
            chosenVirtualFiles = new VirtualFile[2];
            chosenVirtualFiles[0] = LocalFileSystem.getInstance().findFileByIoFile(new File("/Users/emadpres/IdeaProjects/SampleProject/src/A.java"));
            chosenVirtualFiles[1] = LocalFileSystem.getInstance().findFileByIoFile(new File("/Users/emadpres/IdeaProjects/SampleProject/testSrc/ATest.java"));
        }
        else
        {
            while(chosenVirtualFiles == null || chosenVirtualFiles.length !=NUM_OF_FILES____TEMP )
            {
                chosenVirtualFiles = FileChooser.chooseFiles(
                        new FileChooserDescriptor(true, false, false, false, false, true),
                        project, null);
            }
        }

        return chosenVirtualFiles;
    }
}
