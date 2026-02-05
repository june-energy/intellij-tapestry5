package pl.holowko.intellij.tapestry.ognl;

import com.intellij.psi.PsiMethod;
import ognl.ASTMethod;

public class OgnlMethodNode implements OgnlNode {
    
    private ASTMethod methodNode;

    /*package*/ OgnlMethodNode(ASTMethod method) {
        this.methodNode = method;
    }

    @Override
    public boolean matches(PsiMethod method) {
        String methodName = method.getName();
        return methodNode.getMethodName().equals(methodName);
    }

    @Override
    public boolean candidates(PsiMethod method) {
        return false;
    }
}
