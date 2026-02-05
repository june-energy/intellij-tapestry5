package pl.holowko.intellij.tapestry.ognl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import ognl.*;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getLast;

public class OgnlParser {

    private static final String OGNL_PREFIX = "ognl:";
    private static final String LISTENER_PREFIX = "listener:";
    public static final String ACTION_PREFIX = "action:";

    private String expression;
    private String prefix;

    public OgnlParser(String rawExpression) {
        checkState(isOgnlExpression(rawExpression), "Expression %s is not valid OGNL expression", rawExpression);
        
        this.expression = findOgnlExpression(rawExpression);
        this.prefix = findPrefix(rawExpression);
    }

    private String findOgnlExpression(String rawExpression) {
        return rawExpression.replace(OGNL_PREFIX, "").replace(LISTENER_PREFIX, "").replace(ACTION_PREFIX, "");
    }

    private String findPrefix(String rawExpression) {
        if (isOgnl(rawExpression)) {
            return OGNL_PREFIX;
        } else if (isListener(rawExpression)) {
            return LISTENER_PREFIX;
        } else if (isAction(rawExpression)) {
            return ACTION_PREFIX;
        } else {
            throw new IllegalStateException("Unsuported ognl prefix in text" + rawExpression);
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public List<OgnlNode> findNodes() {
        ImmutableList.Builder<OgnlNode> builder = ImmutableList.builder();
        try {
            Node node = (Node) Ognl.parseExpression(expression);
            builder.addAll(createNodes(node));
        } catch (OgnlException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.build();
    }
    
    public Optional<OgnlNode> tryFindLastNode() {
        return Optional.fromNullable(getLast(findNodes()));
    }
    
    public static boolean isOgnlExpression(String text) {
        return isOgnl(text) || isListener(text) || isAction(text);
    }

    private static boolean isAction(String text) {
        return text.startsWith(ACTION_PREFIX);
    }

    private static boolean isListener(String text) {
        return text.startsWith(LISTENER_PREFIX);
    }

    private static boolean isOgnl(String text) {
        return text.startsWith(OGNL_PREFIX);
    }

    private List<OgnlNode> createNodes(Node node) {
        ImmutableList.Builder<OgnlNode> builder = ImmutableList.builder();
        List<Node> leafNodes = getLeafNodes(node);
        for (Node leafNode : leafNodes) {
            if (leafNode instanceof ASTConst) {
                builder.add(new OgnlConstNode(((ASTConst) leafNode)));
            } else if (leafNode instanceof ASTMethod) {
                builder.add(new OgnlMethodNode((ASTMethod) leafNode));
            }
        }
        return builder.build();
    }
    
    private List<Node> getLeafNodes(Node node) {
        ImmutableList.Builder<Node> builder = ImmutableList.builder();
        if (node instanceof ASTChain) {
            //get first node
            builder.addAll(getLeafNodes(node.jjtGetChild(0)));
        } else {
            if (hasChildren(node)) {
                int numChildren = node.jjtGetNumChildren();
                for (int i = 0; i < numChildren; i++) {
                    Node child = node.jjtGetChild(i);
                    List<Node> leafNodes = getLeafNodes(child);
                    builder.addAll(leafNodes);
                }
            } else {
                builder.add(node);
            }
        }
        return builder.build();
    }
    
    private boolean hasChildren(Node node) {
        return node.jjtGetNumChildren() > 0;
    }
}
