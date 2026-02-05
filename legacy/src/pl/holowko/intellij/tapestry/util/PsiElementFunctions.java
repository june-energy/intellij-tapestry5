package pl.holowko.intellij.tapestry.util;

import com.google.common.base.Function;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.PsiShortNamesCache;
import pl.holowko.intellij.tapestry.psi.PsiMethodDecorator;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public final class PsiElementFunctions {

    private PsiElementFunctions() {
    }

    public static Function<PsiElement, PsiFile> fileForElement() {
        return PsiFileFromPsiElementFunction.INSTANCE;
    }
    
    public static Function<PsiFile, Iterable<PsiClass>> classesFromFile() {
        return PsiClassesFromPsiFileFunction.INSTANCE;
    }
    
    public static Function<PsiMethod, String> methodName() {
        return PsiMethodNameFunction.INSTANCE;
    }

    public static Function<PsiMethod, List<String>> candidateMethodNames() {
        return PsiMethodCandidateNamesFunction.INSTANCE;
    }

    private enum PsiMethodCandidateNamesFunction implements Function<PsiMethod, List<String>> {
        INSTANCE;

        @Override
        public List<String> apply(PsiMethod method) {
            return new PsiMethodDecorator(method).getCandidateNames();
        }

    }
    
    private enum PsiMethodNameFunction implements Function<PsiMethod, String> {
        INSTANCE;

        @Override
        public String apply(PsiMethod element) {
            return element.getName();
        }

    }
    
    private enum PsiFileFromPsiElementFunction implements Function<PsiElement, PsiFile> {
        INSTANCE;

        @Override
        public PsiFile apply(PsiElement element) {
            return element.getContainingFile();
        }

    }
    
    private enum PsiClassesFromPsiFileFunction implements Function<PsiFile, Iterable<PsiClass>> {
        INSTANCE;

        @Override
        public Iterable<PsiClass> apply(PsiFile file) {
            PsiShortNamesCache psiShortNamesCache = PsiShortNamesCache.getInstance(file.getProject());
                PsiClass[] classes = psiShortNamesCache.getClassesByName(file.getVirtualFile().getNameWithoutExtension(), file.getResolveScope());
                return newArrayList(classes);
        }

    }
}
