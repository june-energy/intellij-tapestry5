package pl.holowko.intellij.tapestry.psi;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.intellij.psi.PsiMethod;
import pl.holowko.intellij.tapestry.util.Utils;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;

public class PsiMethodDecorator {

    public static final String GET_PREFIX = "get";
    public static final String IS_PREFIX = "is";
    private final PsiMethod method;

    public PsiMethodDecorator(PsiMethod method) {
        this.method = method;
    }
    
    public List<String> getCandidateNames() {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        builder.add(getName());
        if (isProperty()) {
            builder.add(getPropertyName());
        }
        return builder.build();
    }
    
    private String getName() {
        return method.getName();
    }
    
    private String getPropertyName() {
        checkState(isProperty());
        
        String name = getName().replaceFirst(isGetter() ? GET_PREFIX : IS_PREFIX, "");
        return Utils.firstCharToLower(name);
    }
    
    private boolean isProperty() {
        return isGetter() || isBooleanGetter();
    }
    
    private boolean isGetter() {
        return getName().startsWith(GET_PREFIX);
    }
    
    private boolean isBooleanGetter() {
        return getName().startsWith(IS_PREFIX);
    }
}
