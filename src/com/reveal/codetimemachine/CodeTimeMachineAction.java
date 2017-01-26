package com.reveal.codetimemachine;

import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKReport;
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

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by emadpres on 11/23/16.
 */
public class CodeTimeMachineAction extends AnAction
{
    final boolean AUTOMATICALLY_CHOOSE_SAMPLE_FILES = false;
    final int MAX_NUM_OF_FILES = 2;
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

        String content="";
        try
        {
            byte[] bytes = e.getData(LangDataKeys.PSI_FILE).getVirtualFile().contentsToByteArray();
            content = new String(bytes);
        } catch (IOException e1)
        {
            e1.printStackTrace();
        }
        CK ck = new CK();

        //CKReport ckReport = ck.calculateOnSingleFile(content);
        CKReport ckReport2 = ck.calculate("/Users/emadpres/IdeaProjects/Gomo/src");


        VirtualFile[] chosenVirtualFiles = selectVirtualFiles_auto(e);
        if(chosenVirtualFiles[0] == null)
            chosenVirtualFiles = selectVirtualFiles_manually(project);

        if(chosenVirtualFiles == null || chosenVirtualFiles.length==0 || chosenVirtualFiles.length > MAX_NUM_OF_FILES)
            return;

        VcsHistoryProvider myGitVcsHistoryProvider = getGitHistoryProvider(project);
        ArrayList<List<VcsFileRevision>> _fileRevisionsLists = getRevisionListForSubjectAndTestClass(myGitVcsHistoryProvider, chosenVirtualFiles);

        ArrayList<CommitWrapper>[] subjectAndTestClassCommitsList = new ArrayList[2];
        CommitWrapper aCommitWrapper = null;
        for(int i=0; i< chosenVirtualFiles.length; i++)
        {
            int realCommitsSize = _fileRevisionsLists.get(i).size();
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
            if(_fileRevisionsLists.get(i).size()>0)
                mostRecentCommitContent = VcsFileRevisionHelper.getContent(_fileRevisionsLists.get(i).get(0));

            if(! mostRecentCommitContent.equals(currentContent) )
            {
                final String UNCOMMITED_CHANGE_TEXT  = "Uncommitted Changes";
                aCommitWrapper = new CommitWrapper(currentContent, UNCOMMITED_CHANGE_TEXT,new Date(),UNCOMMITED_CHANGE_TEXT, -1);
                subjectAndTestClassCommitsList[i].add(0,aCommitWrapper);
            }

            ///// Other Real
            for(int j=0; j< realCommitsSize; j++)
            {
                aCommitWrapper = new CommitWrapper(_fileRevisionsLists.get(i).get(j),-1);
                subjectAndTestClassCommitsList[i].add(aCommitWrapper);
            }






            /// Sort by Date all commits
            Collections.sort(subjectAndTestClassCommitsList[i], new Comparator<CommitWrapper>()
            {
                @Override
                public int compare(CommitWrapper o1, CommitWrapper o2)
                {
                    return o2.getDate().compareTo(o1.getDate());
                }
            });


            // Assign cIndex
            for(int j=0; j< subjectAndTestClassCommitsList[i].size(); j++)
            {
                subjectAndTestClassCommitsList[i].get(j).cIndex = j;
            }


        }


        ToolWindow toolWindow = getCodeTimeMachine(project).getToolWindow();


        String contentName = "";
        contentName += chosenVirtualFiles[0].getNameWithoutExtension();
        for(int i=1; i< chosenVirtualFiles.length; i++)
        {
            contentName += " vs. ";
            contentName += chosenVirtualFiles[1].getNameWithoutExtension();
        }

        TTMSingleFileView mainWindow = new TTMSingleFileView(project, chosenVirtualFiles[0], subjectAndTestClassCommitsList[0]);
        Content ttm_content = toolWindow.getContentManager().getFactory().createContent(mainWindow.getComponent(), contentName, true);
        toolWindow.getContentManager().addContent(ttm_content);
        toolWindow.setAutoHide(false);
        toolWindow.setAvailable(true,null);



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
