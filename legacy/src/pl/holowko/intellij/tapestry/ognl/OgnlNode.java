package pl.holowko.intellij.tapestry.ognl;

import com.intellij.psi.PsiMethod;

public interface OgnlNode {
    
    boolean matches(PsiMethod method);
    
    boolean candidates(PsiMethod method);
    
}
