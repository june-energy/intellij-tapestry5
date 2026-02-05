package pl.holowko.intellij.tapestry.partner;

import ch.mjava.intellij.PluginHelper;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import pl.holowko.intellij.tapestry.util.PsiElementFunctions;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Predicates.or;
import static pl.holowko.intellij.tapestry.util.PsiElementFunctions.classesFromFile;
import static pl.holowko.intellij.tapestry.util.PsiElementPredicates.instanceOfTapestryComponent;
import static pl.holowko.intellij.tapestry.util.PsiElementPredicates.instanceOfTapestryPage;
import static pl.holowko.intellij.tapestry.util.PsiFileUtils.HTML_EXT;
import static pl.holowko.intellij.tapestry.util.PsiFileUtils.JAVA_EXT;
import static pl.holowko.intellij.tapestry.util.PsiFileUtils.JWC_EXT;

public class HtmlPartnerElementFinder extends PartnerElementFinder {

    /*package*/ HtmlPartnerElementFinder(PsiFile file) {
        super(file);
    }

    @Override
    protected String getPartnerFileName() {
        return file.getName().replace(HTML_EXT, JAVA_EXT);
    }

    @Override
    public List<? extends PsiElement> findPartnerElements() {
        List<PsiFile> partnerFiles = findPartnerFilesByName();
        if (partnerFiles.isEmpty()) {
            List<PsiFile> partnerFilesFromJwcFiles = findFromJwcFiles();
            return ImmutableList.copyOf(partnerFilesFromJwcFiles);
        } else {
            return FluentIterable.from(partnerFiles)
                    .transformAndConcat(classesFromFile())
                    .filter(or(instanceOfTapestryComponent(), instanceOfTapestryPage()))
                    .toList();
        }
    }

    @Override
    public List<PsiFile> findPartnerFiles() {
        List<? extends PsiElement> partnerElements = findPartnerElements();
        return FluentIterable.from(partnerElements)
                .transform(PsiElementFunctions.fileForElement())
                .toList();
    }

    private List<PsiFile> findFromJwcFiles() {
        String jwcFileName = file.getName().replace(HTML_EXT, JWC_EXT);
        Project project = file.getProject();
        List<PsiFile> jwcFiles = PluginHelper.searchFiles(jwcFileName, project);
        
        if (!jwcFiles.isEmpty()) {
            // looking for files inside jwc files
            PsiFile jwcFile = jwcFiles.get(0);
            JwcPartnerElementFinder jwcPartnerElementFinder = new JwcPartnerElementFinder(jwcFile);
            return jwcPartnerElementFinder.findPartnerFilesFromDefinition();
        }
        return Collections.EMPTY_LIST;
    }
}
