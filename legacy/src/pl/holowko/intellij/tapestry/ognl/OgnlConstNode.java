package pl.holowko.intellij.tapestry.ognl;

import com.google.common.base.Ascii;
import com.google.common.collect.ImmutableSet;
import com.intellij.psi.PsiMethod;
import ognl.ASTConst;
import pl.holowko.intellij.tapestry.util.Utils;

import java.util.Set;

import static pl.holowko.intellij.tapestry.util.Utils.firstCharToUpper;

public class OgnlConstNode implements OgnlNode {

    private static final String GET_PREFIX = "get";
    private static final String IS_PREFIX = "is";
    
    final private ASTConst property;
    final private Set<String> possibleMethodNames;
    
    /*package*/ OgnlConstNode(ASTConst property) {
        this.property = property;
        this.possibleMethodNames = ImmutableSet.of(name(), getterName(), booleanGetterName());
    }

    @Override
    public boolean matches(PsiMethod method) {
        String methodName = method.getName();
        return possibleMethodNames.contains(methodName);
    }

    @Override
    public boolean candidates(PsiMethod method) {
        String methodName = method.getName();
        return methodName.toLowerCase().contains(name().toLowerCase());
    }

    private String name() {
        return property.toString().replaceAll("\"", "");
    }
    
    private String getterName() {
        return GET_PREFIX + firstCharToUpper(name());
    }
    
    private String booleanGetterName() {
        return IS_PREFIX + firstCharToUpper(name());
    }
    
    
}
