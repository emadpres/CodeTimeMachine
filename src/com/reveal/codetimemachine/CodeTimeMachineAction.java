package com.reveal.codetimemachine;


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
import com.intellij.psi.*;

import java.io.File;
import java.io.IOException;
import java.util.*;



public class CodeTimeMachineAction extends AnAction
{
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


        ArrayList<VirtualFile> chosenJavaVirtualFiles = selectJavaVirtualFiles_auto(e);


        VcsHistoryProvider myGitVcsHistoryProvider = getGitHistoryProvider(project);
        if(myGitVcsHistoryProvider == null)
            return; // It means project doesn't have "Git".

        ArrayList<CommitWrapper>[] javaFilesCommitsList = new ArrayList[chosenJavaVirtualFiles.size()];
        CommitWrapper aCommitWrapper = null;
        for(int i=0; i< chosenJavaVirtualFiles.size(); i++)
        {

            List<VcsFileRevision> _fileRevisionsLists = getRevisionListForSubjectAndTestClass(myGitVcsHistoryProvider, chosenJavaVirtualFiles.get(i));

            int realCommitsSize = _fileRevisionsLists.size();
            javaFilesCommitsList[i] = new ArrayList<>(realCommitsSize + 1);


            int cIndex = 0;

            ///// First Fake (UncommitedChanges)
            String currentContent = "";
            try
            {
                byte[] currentBytes = chosenJavaVirtualFiles.get(i).contentsToByteArray();
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
                javaFilesCommitsList[i].add(0, aCommitWrapper);
            }

            ///// Other Real
            for (int j = 0; j < realCommitsSize; j++)
            {
                aCommitWrapper = new CommitWrapper(_fileRevisionsLists.get(j), -1);
                javaFilesCommitsList[i].add(aCommitWrapper);
            }


            /// Sort by Date all commits
            // index 0 will contain most recent commit
            Collections.sort(javaFilesCommitsList[i], new Comparator<CommitWrapper>()
            {
                @Override
                public int compare(CommitWrapper o1, CommitWrapper o2)
                {
                    return o2.getDate().compareTo(o1.getDate());
                }
            });


            // Assign cIndex
            for (int j = 0; j < javaFilesCommitsList[i].size(); j++)
            {
                javaFilesCommitsList[i].get(j).cIndex = j;
            }

            String contentName = chosenJavaVirtualFiles.get(i).getName();
            TTMSingleFileView mainWindow = new TTMSingleFileView(project, chosenJavaVirtualFiles.get(i), javaFilesCommitsList[i]);
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

        chosenVirtualFiles = FileChooser.chooseFiles(
                new FileChooserDescriptor(true, false, false, false, false, true),
                project, null);

        return chosenVirtualFiles;
    }

    private ArrayList<VirtualFile> selectJavaVirtualFiles_auto(AnActionEvent e)
    {
        // If you run this action from Tools menu, and some file is open in Editor, the file will be retrieved.
        // If you run this action from ProjectView by right-clicking on some files, all selected files will be retrieved.
        // otherwise (i.e. right clicking on some empty space in Project View, or when there is no open file in Editor) it is null.
        VirtualFile[] files = e.getData(LangDataKeys.VIRTUAL_FILE_ARRAY);

        ArrayList<VirtualFile> onlyJavaFiles = new ArrayList<>();

        if(files==null)
            return onlyJavaFiles;

        for(int i=0; i<files.length; i++)
        {
            if(files[i].getFileType().getDefaultExtension() == "java")
                onlyJavaFiles.add(files[i]);
        }

        return onlyJavaFiles;
    }

    @Override
    public void update(AnActionEvent e)
    {
        ArrayList<VirtualFile> files = selectJavaVirtualFiles_auto(e);


        if(files.size()==0)
            e.getPresentation().setEnabled(false);
        else
            e.getPresentation().setEnabled(true);
    }
}
