package pl.holowko.intellij.tapestry.util;

import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.psi.PsiFile;

public final class PsiFileUtils {

    public static final String JAVA_EXT = ".java";
    public static final String HTML_EXT = ".html";
    public static final String JWC_EXT = ".jwc";
    
    public static boolean isHtmlFile(PsiFile psiFile) {
        return psiFile.getFileType() == StdFileTypes.HTML;
    }

    public static boolean isJavaFile(PsiFile file) {
        return file.getFileType().equals(StdFileTypes.JAVA);
    }
    
    public static boolean isJwcFile(PsiFile file) {
        return file.getName().endsWith(JWC_EXT);
    }

    private PsiFileUtils() {
    }
}
