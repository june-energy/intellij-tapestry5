package pl.holowko.intellij.tapestry.codeInsight;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.Nullable;
import pl.holowko.intellij.tapestry.ognl.OgnlMethodFinder;
import pl.holowko.intellij.tapestry.ognl.OgnlParser;
import pl.holowko.intellij.tapestry.partner.PartnerElementFinder;
import pl.holowko.intellij.tapestry.util.PsiFileUtils;

import java.util.List;

import static pl.holowko.intellij.tapestry.util.PsiFileUtils.isHtmlFile;


public class OgnlGoToDeclarationHandler implements GotoDeclarationHandler {
    
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement psiElement, int i, Editor editor) {
        PsiFile containingFile = psiElement.getContainingFile();
        if (isHtmlFile(containingFile)) {
            String text = psiElement.getText();
            
            if (OgnlParser.isOgnlExpression(text)) {
                Optional<? extends PartnerElementFinder> partnerElementFinder = PartnerElementFinder.forFile(containingFile);
            
                if (partnerElementFinder.isPresent()) {
                    List<PsiFile> partnerFiles = partnerElementFinder.get().findPartnerFiles();
                    
                    List<PsiMethod> methods = Lists.newArrayList();
                    for (PsiFile partnerFile : partnerFiles) {
                        if (PsiFileUtils.isJavaFile(partnerFile)) {
                            OgnlMethodFinder ognlMethodFinder = new OgnlMethodFinder(partnerFile, text);
                            methods.addAll(ognlMethodFinder.findAll());
                        }
                    }
                    return methods.toArray(new PsiElement[methods.size()]);
                }
            }
        }

        return new PsiElement[0];
    }

    @Nullable
    @Override
    public String getActionText(DataContext dataContext) {
        return null;
    }
    
}
