package pl.holowko.intellij.tapestry.util;

import com.google.common.base.Predicate;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;

public final class PsiElementPredicates {

    private static final String TAPESTRY_COMPONENT_CLASS_NAME = "org.apache.tapestry.internal.Component";
    private static final String TAPESTRY_PAGE_CLASS_NAME = "org.apache.tapestry.IPage";

    public static Predicate<PsiClass> instanceOfTapestryComponent() {
        return InstanceOfTapestryComponentPredicate.INSTANCE;
    }
    
    public static Predicate<PsiClass> instanceOfTapestryPage() {
        return InstanceOfTapestryPagePredicate.INSTANCE;
    }
    
    public static Predicate<PsiMethod> publicMethod() {
        return PublicPsiMethodPredicate.INSTANCE;
    }
    
    private enum InstanceOfTapestryComponentPredicate implements Predicate<PsiClass> {
        INSTANCE;

        @Override
        public boolean apply(PsiClass psiClass) {
            return JavaPsiUtils.isInstanceOf(psiClass, TAPESTRY_COMPONENT_CLASS_NAME);
        }

    }
    
    private enum InstanceOfTapestryPagePredicate implements Predicate<PsiClass> {
        INSTANCE;

        @Override
        public boolean apply(PsiClass psiClass) {
            return JavaPsiUtils.isInstanceOf(psiClass, TAPESTRY_PAGE_CLASS_NAME);
        }

    }
    
    private enum PublicPsiMethodPredicate implements Predicate<PsiMethod> {
        INSTANCE;

        @Override
        public boolean apply(PsiMethod method) {
            return method.getModifierList().hasExplicitModifier(PsiModifier.PUBLIC);
        }

    }

    private PsiElementPredicates() {
    }

}
