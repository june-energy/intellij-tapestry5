package pl.holowko.intellij.tapestry.partner;

import ch.mjava.intellij.PluginHelper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.codec.Charsets.UTF_8;
import static pl.holowko.intellij.tapestry.util.PsiFileUtils.JAVA_EXT;
import static pl.holowko.intellij.tapestry.util.PsiFileUtils.JWC_EXT;

public class JwcPartnerElementFinder extends PartnerElementFinder {

    public static final String CLASS_ATTRIBUTE_REGEX = ".*class=\"(.*?)\"";

    protected JwcPartnerElementFinder(PsiFile file) {
        super(file);
    }

    @Override
    protected String getPartnerFileName() {
        return file.getName().replace(JWC_EXT, JAVA_EXT);
    }

    @Override
    public List<PsiFile> findPartnerFiles() {
        List<PsiFile> partnerFilesFromDefinition = findPartnerFilesFromDefinition();
        if (partnerFilesFromDefinition.isEmpty()) {
            return findPartnerFilesByName();
        } else {
            return partnerFilesFromDefinition;
        }
    }
    
    /*package*/ List<PsiFile> findPartnerFilesFromDefinition() {
        VirtualFile jwcFile = file.getVirtualFile();
        try {
            String content = new String(jwcFile.contentsToByteArray(), UTF_8);
            Pattern pattern = Pattern.compile(CLASS_ATTRIBUTE_REGEX);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String fqClassName = matcher.group(1);
                String javaName = fqClassName.substring(fqClassName.lastIndexOf(".") + 1);
                return PluginHelper.searchFiles(javaName + JAVA_EXT, file.getProject());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Collections.EMPTY_LIST;
        
    }
}
