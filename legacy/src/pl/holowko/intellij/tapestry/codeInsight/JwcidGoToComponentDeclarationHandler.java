package pl.holowko.intellij.tapestry.codeInsight;

import ch.mjava.intellij.PluginHelper;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static pl.holowko.intellij.tapestry.util.PsiElementFunctions.classesFromFile;
import static pl.holowko.intellij.tapestry.util.PsiElementPredicates.instanceOfTapestryComponent;
import static pl.holowko.intellij.tapestry.util.PsiFileUtils.*;

public class JwcidGoToComponentDeclarationHandler implements GotoDeclarationHandler {

    public static final String AT = "@";

    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement psiElement, int i, Editor editor) {
        PsiFile containingFile = psiElement.getContainingFile();
        if (isHtmlFile(containingFile) && isJwcid(psiElement)) {
            List<PsiFile> htmlComponents = getHtmlComponents(psiElement);
            List<PsiClass> javaComponents = getJavaComponents(psiElement);
            Iterable<PsiElement> elements = Iterables.<PsiElement>concat(htmlComponents, javaComponents);
            return Iterables.toArray(elements, PsiElement.class);
        }
        return new PsiElement[0];
    }

    private List<PsiFile> getHtmlComponents(PsiElement psiElement) {
        String componentName = getHtmlComponentName(psiElement.getText());
        return PluginHelper.searchFiles(componentName, psiElement.getProject());
    }

    private List<PsiClass> getJavaComponents(PsiElement psiElement) {
        String componentName = getJavaComponentName(psiElement.getText());
        return findComponentClass(componentName, psiElement.getProject());
    }

    private List<PsiClass> findComponentClass(String componentName, Project project) {
        List<PsiFile> psiFiles = PluginHelper.searchFiles(componentName, project);
        return FluentIterable.from(psiFiles)
                .transformAndConcat(classesFromFile())
                .filter(instanceOfTapestryComponent())
                .toList();
    }

    private String getJavaComponentName(String jwcid) {
        return getComponentName(jwcid) + JAVA_EXT;
    }
    
    private String getHtmlComponentName(String jwcid) {
        return getComponentName(jwcid) + HTML_EXT;
    }
    
    private String getComponentName(String jwcid) {
        int atIndex = jwcid.indexOf(AT);
        return jwcid.substring(atIndex + 1);
    }

    private boolean isJwcid(PsiElement psiElement) {
        String text = psiElement.getText();
        return text.contains(AT);
    }
    
    @Nullable
    @Override
    public String getActionText(DataContext dataContext) {
        return null;
    }
}
