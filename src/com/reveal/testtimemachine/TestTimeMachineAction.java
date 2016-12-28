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
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by emadpres on 11/23/16.
 */
public class TestTimeMachineAction extends AnAction
{
    final boolean AUTOMATICALLY_CHOOSE_SAMPLE_FILES = false;
    final int MAX_NUM_OF_FILES = 2;
    //////////////////////////////
    Project project = null;
    ToolWindow toolWindow = null;

    @Override
    public void actionPerformed(AnActionEvent e)
    {
        project = e.getProject();

        VirtualFile[] chosenVirtualFiles = getSubjectAndTestVirtualFiles();
        if(chosenVirtualFiles == null || chosenVirtualFiles.length > MAX_NUM_OF_FILES)
            return;

        VcsHistoryProvider myGitVcsHistoryProvider = getGitHistoryProvider();
        ArrayList<List<VcsFileRevision>> _fileRevisionsLists = getRevisionListForSubjectAndTestClass(myGitVcsHistoryProvider, chosenVirtualFiles);

        ArrayList<CommitWrapper>[] subjectAndTestClassCommitsList = new ArrayList[2];
        CommitWrapper aCommitWrapper = null;
        for(int i=0; i< chosenVirtualFiles.length; i++)
        {
            int realCommitsSize = _fileRevisionsLists.get(i).size();
            subjectAndTestClassCommitsList[i] = new ArrayList<>(realCommitsSize + 1);


            ///// Other Real
            for(int j=0; j< realCommitsSize; j++)
            {
                aCommitWrapper = new CommitWrapper(_fileRevisionsLists.get(i).get(j));
                subjectAndTestClassCommitsList[i].add(aCommitWrapper);
            }



            ///// First Fake (UncommitedChanges)
            String currentContent = "";
            try
            {
                byte[] currentBytes = chosenVirtualFiles[i].contentsToByteArray();
                currentContent = new String(currentBytes);
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }

            String mostRecentCommitContent = subjectAndTestClassCommitsList[i].get(0).getFileContent();
            if(! mostRecentCommitContent.equals(currentContent) )
            {
                aCommitWrapper = new CommitWrapper(currentContent, "",new Date(),"Uncommited Changes");
                subjectAndTestClassCommitsList[i].add(0,aCommitWrapper);
            }
        }


        if(toolWindow == null)
            toolWindow = ToolWindowManager.getInstance(project).registerToolWindow("TTM", true, ToolWindowAnchor.TOP);



        String contentName = "";
        contentName += chosenVirtualFiles[0].getNameWithoutExtension();
        for(int i=1; i< chosenVirtualFiles.length; i++)
        {
            contentName += " vs. ";
            contentName += chosenVirtualFiles[1].getNameWithoutExtension();
        }

        TestTimeMachineWindow mainWindow = new TestTimeMachineWindow(project, chosenVirtualFiles, subjectAndTestClassCommitsList);
        Content ttm_content = toolWindow.getContentManager().getFactory().createContent(mainWindow.getComponent(), contentName, true);
        toolWindow.getContentManager().addContent(ttm_content);
        toolWindow.setAutoHide(false);
        toolWindow.setAvailable(true,null);



    }

    private ArrayList<List<VcsFileRevision>> getRevisionListForSubjectAndTestClass(VcsHistoryProvider myGitVcsHistoryProvider, VirtualFile[] chosenVirtualFiles)
    {
        ArrayList<List<VcsFileRevision>> _fileRevisionsLists = new ArrayList<>(chosenVirtualFiles.length);

        for(int i = 0; i< chosenVirtualFiles.length; i++)
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
            chosenVirtualFiles[0] = LocalFileSystem.getInstance().findFileByIoFile(new File("/Users/emadpres/IdeaProjects/Vector/src/com/math/vector/Vector.java"));
            chosenVirtualFiles[1] = LocalFileSystem.getInstance().findFileByIoFile(new File("/Users/emadpres/IdeaProjects/Vector/testSrc/com/math/vector/VectorTest.java"));
        }
        else
        {
            chosenVirtualFiles = FileChooser.chooseFiles(
                    new FileChooserDescriptor(true, false, false, false, false, true),
                    project, null);
        }

        return chosenVirtualFiles;
    }
}
