package js.tools.lint;

import js.tools.commons.ast.Names;
import js.tools.commons.util.Strings;

import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.ObjectLiteral;
import org.mozilla.javascript.ast.ObjectProperty;

final class Context
{
  Writer writer;
  boolean legacy;
  String currentPackage;

  public JsFunction getEnclosingFunction(AstNode node)
  {
    FunctionNode enclosingFunction = node.getEnclosingFunction();
    if(enclosingFunction == null) return null;
    String functionName = enclosingFunction.getName();
    String apidoc = enclosingFunction.getJsDoc();

    if(functionName.isEmpty()) {
      AstNode parent = enclosingFunction.getParent();
      if(parent == null) return null;
      if(parent instanceof Assignment) {
        Assignment assignment = (Assignment)parent;
        functionName = Names.getName(assignment.getLeft());
        apidoc = assignment.getJsDoc();
      }
      else if(parent instanceof ObjectProperty) {
        ObjectProperty property = (ObjectProperty)parent;
        AstNode left = property.getLeft();
        String methodName = Names.getName(left);
        apidoc = left.getJsDoc();

        node = property.getParent();
        assert node != null;
        ObjectLiteral objectLiteral = (ObjectLiteral)node;

        node = objectLiteral.getParent();
        assert node != null;
        Assignment assignment = (Assignment)node;
        assert assignment != null;
        String className = Names.getName(assignment.getLeft());
        functionName = Strings.concat(className, '.', methodName);
      }
    }
    JsFunction function = JsFunction.forName(functionName);
    if(apidoc != null) {
      function.setApiDoc(apidoc);
    }
    return function;
  }

  @SuppressWarnings("unused")
  public JsClass getEnclosingClass(AstNode node)
  {
    FunctionNode enclosingFunction = node.getEnclosingFunction();
    if(enclosingFunction == null) {
      // we are in global space
      if(!(node instanceof Assignment)) return null;
      Assignment assignment = (Assignment)node;
      String leftName = Names.getName(assignment.getLeft());
      if(Utils.isStaticMemberName(leftName)) {
        return JsClass.forName(Utils.getStaticMemberClassName(leftName));
      }
      return null;
    }

    String functionName = enclosingFunction.getName();
    if(functionName.isEmpty()) {
      AstNode parent = enclosingFunction.getParent();
      if(parent == null) return null;
      if(parent instanceof Assignment) {
        Assignment assignment = (Assignment)parent;
        functionName = Names.getName(assignment.getLeft());
      }
      else if(parent instanceof ObjectProperty) {
        ObjectProperty property = (ObjectProperty)parent;
        parent = property.getParent();
        if(parent == null) return null;
        ObjectLiteral objectLiteral = (ObjectLiteral)parent;
        parent = objectLiteral.getParent();
        if(parent == null) return null;
        Assignment assignment = (Assignment)parent;
        if(assignment == null) return null;
        functionName = Names.getName(assignment.getLeft());
      }
    }

    if(Utils.isStaticMemberName(functionName)) {
      functionName = Utils.getStaticMemberClassName(functionName);
    }
    else if(Utils.isPrototype(functionName)) {
      functionName = Utils.getPrototypeClassName(functionName);
    }
    else if(Utils.isPrototypeMember(functionName)) {
      functionName = Utils.getPrototypeMemberClassName(functionName);
    }
    return JsClass.forName(functionName);
  }
}
