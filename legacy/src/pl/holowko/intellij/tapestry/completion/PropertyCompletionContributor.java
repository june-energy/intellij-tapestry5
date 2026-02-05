package pl.holowko.intellij.tapestry.completion;

import com.google.common.base.Optional;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlToken;
import pl.holowko.intellij.tapestry.ognl.OgnlMethodFinder;
import pl.holowko.intellij.tapestry.partner.PartnerElementFinder;

import java.util.List;

import static pl.holowko.intellij.tapestry.ognl.OgnlParser.isOgnlExpression;
import static pl.holowko.intellij.tapestry.util.PsiFileUtils.isHtmlFile;

public class PropertyCompletionContributor extends CompletionContributor {

    public static final String XML_ATTRIBUTE_VALUE_TOKEN = "XML_ATTRIBUTE_VALUE_TOKEN";

    @Override
    public void fillCompletionVariants(final CompletionParameters parameters, final CompletionResultSet result) {
        ApplicationManager.getApplication().runReadAction(new Runnable() {
            @Override
            public void run() {
                PsiFile file = parameters.getOriginalFile();
                PsiElement position = parameters.getPosition();
                if (isHtmlFile(file) && isXmlAttribute(position)) {
                    String textBeforeCaret = getTextBeforeCaret((XmlToken) position);
                    if (isOgnlExpression(textBeforeCaret)) {
                        Optional<? extends PartnerElementFinder> finder = PartnerElementFinder.forFile(file);
                        if (finder.isPresent()) {
                            List<PsiFile> partnerFiles = finder.get().findPartnerFiles();
    
                            for (PsiFile partnerFile : partnerFiles) {
                                OgnlMethodFinder ognlMethodFinder = new OgnlMethodFinder(partnerFile, textBeforeCaret);
                                
                                String expressionPrefix = ognlMethodFinder.getExpressionPrefix();
                                List<String> methodCandidateNames = ognlMethodFinder.findMethodCandidateNames();
                                
                                for (String candidateName : methodCandidateNames) {
                                    LookupElementBuilder element = createLookupElement(partnerFile, expressionPrefix, candidateName);
                                    result.addElement(element);
                                    result.stopHere();
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private LookupElementBuilder createLookupElement(PsiFile partnerFile, String expressionPrefix, String candidateName) {
        return LookupElementBuilder.create(expressionPrefix + candidateName)
                .withIcon(StdFileTypes.JAVA.getIcon())
                .withTailText(" " + partnerFile.getName(), true)
                .withBoldness(true);
    }

    private String getTextBeforeCaret(XmlToken position) {
        String attributeValue = (((XmlToken) position)).getText();
        int indexOf = Math.max(0, attributeValue.indexOf(CompletionInitializationContext.DUMMY_IDENTIFIER));
        return attributeValue.substring(0, indexOf);
    }

    private boolean isXmlAttribute(PsiElement position) {
        if (position instanceof XmlToken) {
            IElementType tokenType = ((XmlToken) position).getTokenType();
            return XML_ATTRIBUTE_VALUE_TOKEN.equals(tokenType.toString());
        }
        return false;
    }
}
