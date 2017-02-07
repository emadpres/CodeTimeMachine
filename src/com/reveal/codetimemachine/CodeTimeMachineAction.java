package com.reveal.codetimemachine;

import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKNumber;
import com.github.mauricioaniche.ck.CKReport;
import com.google.common.io.Files;
import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
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
import com.intellij.psi.*;
import com.intellij.ui.content.Content;
import com.reveal.metrics.MaxCKNumber;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by emadpres on 11/23/16.
 */
public class CodeTimeMachineAction extends AnAction
{
    final boolean AUTOMATICALLY_CHOOSE_SAMPLE_FILES = false;
    //////////////////////////////

    static Map<Project, CodeTimeMachine> runningCodeTimeMachines = new HashMap<>();


    @Override
    public void actionPerformed(AnActionEvent e)
    {
        // We should not save project "project = e.getProject()" as this Action-class member,
        // because this function call for different open IDE windows
        // and thus for different project. So assume this function like a static function and don't keep project specific
        // data. For solving this problem we have a high-level Code Time Machine concept which is stored as a map
        // of <projoect, CodeTimeMachine>.
        Project project = e.getProject();


        VirtualFile[] chosenVirtualFiles = selectVirtualFiles_auto(e);
        if(chosenVirtualFiles[0] == null)
            chosenVirtualFiles = selectVirtualFiles_manually(project);

        if(chosenVirtualFiles == null || chosenVirtualFiles.length==0)
            return;

        VcsHistoryProvider myGitVcsHistoryProvider = getGitHistoryProvider(project);

        ArrayList<CommitWrapper>[] subjectAndTestClassCommitsList = new ArrayList[chosenVirtualFiles.length];
        CommitWrapper aCommitWrapper = null;
        for(int i=0; i< chosenVirtualFiles.length; i++)
        {

            List<VcsFileRevision> _fileRevisionsLists = getRevisionListForSubjectAndTestClass(myGitVcsHistoryProvider, chosenVirtualFiles[i]);

            int realCommitsSize = _fileRevisionsLists.size();
            subjectAndTestClassCommitsList[i] = new ArrayList<>(realCommitsSize + 1);


            int cIndex = 0;

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

            String mostRecentCommitContent = "";
            if (_fileRevisionsLists.size() > 0)
                mostRecentCommitContent = VcsFileRevisionHelper.getContent(_fileRevisionsLists.get(0));

            if (!mostRecentCommitContent.equals(currentContent))
            {
                final String UNCOMMITED_CHANGE_TEXT = "Uncommitted Changes";
                aCommitWrapper = new CommitWrapper(currentContent, UNCOMMITED_CHANGE_TEXT, new Date(), UNCOMMITED_CHANGE_TEXT, -1);
                subjectAndTestClassCommitsList[i].add(0, aCommitWrapper);
            }

            ///// Other Real
            for (int j = 0; j < realCommitsSize; j++)
            {
                aCommitWrapper = new CommitWrapper(_fileRevisionsLists.get(j), -1);
                subjectAndTestClassCommitsList[i].add(aCommitWrapper);
            }


            /// Sort by Date all commits
            // index 0 will contain most recent commit
            Collections.sort(subjectAndTestClassCommitsList[i], new Comparator<CommitWrapper>()
            {
                @Override
                public int compare(CommitWrapper o1, CommitWrapper o2)
                {
                    return o2.getDate().compareTo(o1.getDate());
                }
            });


            // Assign cIndex
            for (int j = 0; j < subjectAndTestClassCommitsList[i].size(); j++)
            {
                subjectAndTestClassCommitsList[i].get(j).cIndex = j;
            }

            String contentName = chosenVirtualFiles[i].getName();
            TTMSingleFileView mainWindow = new TTMSingleFileView(project, chosenVirtualFiles[i], subjectAndTestClassCommitsList[i]);
            getCodeTimeMachine(project).addNewContent(mainWindow, contentName);
        }
    }

    static public CodeTimeMachine getCodeTimeMachine(Project project)
    {
        CodeTimeMachine ctm = null;

        if(runningCodeTimeMachines.containsKey(project)==false)
        {
            ctm = new CodeTimeMachine(project);
            runningCodeTimeMachines.put(project, ctm);
        }
        else
        {
            ctm = runningCodeTimeMachines.get(project);
        }
        return ctm;
    }

    public void findAllPackages(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        final Project project = DataKeys.PROJECT.getData(dataContext);
        final Module module = DataKeys.MODULE.getData(dataContext);

        final Set<String> packageNameSet = new HashSet<String>();

        AnalysisScope moduleScope = new AnalysisScope(module);
        moduleScope.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitFile(final PsiFile file) {
                if (file instanceof PsiJavaFile) {
                    PsiJavaFile psiJavaFile = (PsiJavaFile) file;
                    final PsiPackage aPackage =
                            JavaPsiFacade.getInstance(project).findPackage(psiJavaFile.getPackageName());
                    if (aPackage != null) {
                        packageNameSet.add(aPackage.getQualifiedName());
                    }
                }
            }
        });

        String allPackageNames = "";
        for (String packageName : packageNameSet) {
            allPackageNames = allPackageNames + packageName + "\n";
        }

        Messages.showMessageDialog(project, allPackageNames,
                "All packages in selected module", Messages.getInformationIcon());
    }

    private List<VcsFileRevision> getRevisionListForSubjectAndTestClass(VcsHistoryProvider myGitVcsHistoryProvider, VirtualFile virtualFiles)
    {

        FilePath filePathOn = VcsContextFactory.SERVICE.getInstance().createFilePathOn(virtualFiles);
        VcsHistorySession sessionFor = null;
        try
        {
            sessionFor = myGitVcsHistoryProvider.createSessionFor(filePathOn);
        } catch (VcsException e1)
        {
            e1.printStackTrace();
        }
        List<VcsFileRevision> revisionList = sessionFor.getRevisionList();

        return revisionList;
    }

    private VcsHistoryProvider getGitHistoryProvider(Project project)
    {
        ProjectLevelVcsManager mgr = ProjectLevelVcsManager.getInstance( project );
        AbstractVcs[] allActiveVcss = mgr.getAllActiveVcss();
        AbstractVcs myGit = allActiveVcss[0];
        return myGit.getVcsHistoryProvider();
    }

    public VirtualFile[] selectVirtualFiles_manually(Project project)
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

    private VirtualFile[] selectVirtualFiles_auto(AnActionEvent e)
    {
        VirtualFile[] chosenVirtualFiles = new VirtualFile[1];

        if(e.getData(LangDataKeys.PSI_FILE) != null)
            chosenVirtualFiles[0] = e.getData(LangDataKeys.PSI_FILE).getVirtualFile();
        else
            chosenVirtualFiles[0] = null;

        return chosenVirtualFiles;
    }
}
