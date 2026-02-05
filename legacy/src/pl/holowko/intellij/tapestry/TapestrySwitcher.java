package pl.holowko.intellij.tapestry;

import ch.mjava.intellij.PluginHelper;
import com.google.common.base.Optional;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.psi.PsiElement;
import pl.holowko.intellij.tapestry.partner.PartnerElementFinder;

import java.util.List;

public class TapestrySwitcher extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Optional<? extends PartnerElementFinder> partnerElementFinder = PartnerElementFinder.forFile(e.getData(LangDataKeys.PSI_FILE));
        if (partnerElementFinder.isPresent()) {
            List<? extends PsiElement> partnerElements = partnerElementFinder.get().findPartnerElements();
            if (partnerElements.isEmpty()) {
                PluginHelper.showErrorBalloonWith("No partner file found", e.getDataContext());
            } else {
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(e.getProject());
                for (PsiElement element : partnerElements) {
                    fileEditorManager.openFile(element.getContainingFile().getVirtualFile(), true, true);
                }
            }
        }
    }
}
