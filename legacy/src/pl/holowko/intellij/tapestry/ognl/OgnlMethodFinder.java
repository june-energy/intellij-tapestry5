package pl.holowko.intellij.tapestry.ognl;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.PsiShortNamesCache;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.any;
import static pl.holowko.intellij.tapestry.util.PsiElementFunctions.candidateMethodNames;
import static pl.holowko.intellij.tapestry.util.PsiElementPredicates.publicMethod;

public class OgnlMethodFinder {
    
    final private PsiFile file;
    final private OgnlParser ognlParser;
    final private List<OgnlNode> ognlNodes;

    public OgnlMethodFinder(PsiFile javaFile, String expression) {
        checkState(javaFile.getFileType().equals(StdFileTypes.JAVA));
        
        this.file = javaFile;
        this.ognlParser = new OgnlParser(expression);
        this.ognlNodes = ognlParser.findNodes();
    }
    
    public String getExpressionPrefix() {
        return ognlParser.getPrefix();
    }
    
    private Optional<OgnlNode> getLastOgnlNode() {
        return Optional.fromNullable(Iterables.getLast(ognlNodes, null));
    }
    
    public List<String> findMethodCandidateNames() {
        return FluentIterable.from(findMethodCandidates()).transformAndConcat(candidateMethodNames()).toList();
    }
    
    public List<PsiMethod> findMethodCandidates() {
        ImmutableList.Builder<PsiMethod> methodCandidatesBuilder = ImmutableList.builder();
        
        PsiShortNamesCache psiShortNamesCache = PsiShortNamesCache.getInstance(file.getProject());
        PsiClass[] classes = psiShortNamesCache.getClassesByName(file.getVirtualFile().getNameWithoutExtension(), file.getResolveScope());

        Optional<OgnlNode> lastOgnlNode = getLastOgnlNode();

        for (PsiClass aClass : classes) {
            PsiMethod[] methods = aClass.getAllMethods();
            List<PsiMethod> publicMethods = FluentIterable.from(Lists.newArrayList(methods)).filter(publicMethod()).toList();
            if (lastOgnlNode.isPresent()) {
                for (PsiMethod method : publicMethods) {
                    if (lastOgnlNode.get().candidates(method)) {
                        methodCandidatesBuilder.add(method);
                    }
                }
            } else {
                methodCandidatesBuilder.addAll(publicMethods);
            }
        }
        return methodCandidatesBuilder.build();
    }
    
    public List<PsiMethod> findAll() {
        ImmutableList.Builder<PsiMethod> builder = ImmutableList.builder();

        PsiShortNamesCache psiShortNamesCache = PsiShortNamesCache.getInstance(file.getProject());
        PsiClass[] classes = psiShortNamesCache.getClassesByName(file.getVirtualFile().getNameWithoutExtension(), file.getResolveScope());

        for (PsiClass aClass : classes) {
            PsiMethod[] methods = aClass.getAllMethods();
            for (final PsiMethod method : methods) {
                boolean anyMatches = any(ognlNodes, new Predicate<OgnlNode>() {
                    @Override
                    public boolean apply(OgnlNode ognlNode) {
                        return ognlNode.matches(method);
                    }
                });
                
                if (anyMatches) {
                    builder.add(method);
                }
            }
        }
        
        return builder.build();
    }
    
}
