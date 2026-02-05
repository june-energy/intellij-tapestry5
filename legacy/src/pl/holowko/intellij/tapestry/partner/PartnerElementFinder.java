package pl.holowko.intellij.tapestry.partner;

import ch.mjava.intellij.PluginHelper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.List;

import static pl.holowko.intellij.tapestry.util.PsiFileUtils.isHtmlFile;
import static pl.holowko.intellij.tapestry.util.PsiFileUtils.isJavaFile;
import static pl.holowko.intellij.tapestry.util.PsiFileUtils.isJwcFile;

public abstract class PartnerElementFinder {

    protected PsiFile file;

    protected PartnerElementFinder(PsiFile file) {
        this.file = file;
    }
    
    public static Optional<? extends PartnerElementFinder> forFile(PsiFile file) {
        if (file == null) {
            return Optional.absent();
        }
        
        if (isJavaFile(file)) {
            return Optional.of(new JavaPartnerElementFinder(file));
        } else if (isHtmlFile(file)) {
            return Optional.of(new HtmlPartnerElementFinder(file));
        } else if (isJwcFile(file)) {
            return Optional.of(new JwcPartnerElementFinder(file));
        } else {
            return Optional.absent();
        }
    }
    
    public List<? extends PsiElement> findPartnerElements() {
        return findPartnerFiles();
    }
    
    public List<PsiFile> findPartnerFiles() {
        return findPartnerFilesByName();
    }

    protected List<PsiFile> findPartnerFilesByName() {
        String partnerFileName = getPartnerFileName();
        List<PsiFile> files = PluginHelper.searchFiles(partnerFileName, file.getProject());
        return ImmutableList.copyOf(files);
    }

    protected abstract String getPartnerFileName();

}
