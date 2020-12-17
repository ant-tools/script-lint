package js.tools.lint;

import js.tools.commons.ast.AstHandler;

import org.mozilla.javascript.Node;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.NewExpression;
import org.mozilla.javascript.ast.ObjectLiteral;
import org.mozilla.javascript.ast.PropertyGet;

final class AssignmentHandler extends AstHandler
{
  private Context context;
  private Writer writer;

  private Assignment assignment;
  private JsClass enclosingClass;
  private JsFunction enclosingFunction;
  private AstNode leftNode;
  private AstNode rightNode;
  private String leftName;

  public AssignmentHandler(Context context)
  {
    this.context = context;
  }

  @Override
  public void handle(Node node)
  {
    this.writer = this.context.writer;
    this.writer.setLog(this.log);

    this.assignment = (Assignment)node;
    this.enclosingClass = this.context.getEnclosingClass(this.assignment);
    this.enclosingFunction = this.context.getEnclosingFunction(this.assignment);
    this.leftNode = this.assignment.getLeft();
    this.leftName = Utils.getName(this.leftNode);
    this.rightNode = this.assignment.getRight();

    if(Utils.isClassName(this.leftName) && this.rightNode instanceof FunctionNode) {
      processClass();
      return;
    }
    if(Utils.isPrototype(this.leftName)) {
      processPrototype();
      return;
    }
    if(Utils.isInstanceMember(this.leftNode)) {
      if(this.rightNode instanceof FunctionNode) {
        processInstanceMethodAsLValue();
      }
      else {
        processInstanceFieldAsLValue();
      }
      return;
    }
    if(Utils.isStaticMemberName(this.leftName)) {
      if(this.rightNode instanceof FunctionNode) {
        processStaticMethodAsLValue();
      }
      else {
        processStaticFieldAsLValue();
      }
      return;
    }
  }

  private void processClass()
  {
    if(this.context.currentPackage == null) {
      this.writer.print(Warn.NO_PACKAGE_DEF);
      return;
    }
    if(!this.leftName.startsWith(this.context.currentPackage + '.')) {
      this.writer.print(Warn.BAD_CLASS_PACKAGE);
    }
    String apidoc = this.assignment.getJsDoc();
    if(apidoc == null) {
      this.writer.print(Warn.NO_APIDOC);
      return;
    }
    JsDoc jsDoc = new JsDoc(apidoc);
    if(this.rightNode instanceof FunctionNode && !jsDoc.hasConstructor()) {
      this.writer.print(Warn.CTOR_ANNOTATION);
    }
  }

  private void processStaticFieldAsLValue()
  {
    String staticFieldName = Utils.getStaticMemberName(this.leftName);
    if(this.enclosingFunction == null) {
      if(this.rightNode instanceof NewExpression) {
        this.writer.print(Warn.BAD_RVALUE, "Can't initialize static field using new operator.");
        return;
      }
      if(this.rightNode instanceof FunctionCall) {
        this.writer.print(Warn.BAD_RVALUE, "Can't initialize static field from function call.");
        return;
      }
      if(!this.enclosingClass.hasStaticField(staticFieldName)) {
        String apidoc = this.assignment.getJsDoc();
        if(apidoc == null) {
          this.writer.print(Warn.NO_APIDOC, "Missing type annotation for field declaration.");
          return;
        }
        JsDoc jsDoc = new JsDoc(apidoc);
        this.enclosingClass.addStaticField(staticFieldName, jsDoc.getTypeType());
      }
      return;
    }

    if(this.enclosingFunction.isConstructor()) {
      this.writer.print(Warn.CTOR_STATIC);
    }

    // if static field belongs to enclosing class it should be already declared
    if(this.enclosingClass.is(Utils.getStaticMemberClassName(this.leftName))) {
      if(!this.enclosingClass.hasStaticField(staticFieldName)) {
        this.writer.print(Warn.NO_FIELD_DECL);
      }
    }
    if(Utils.isConstantName(this.leftName)) {
      this.writer.print(Warn.CONST_ASSIGN);
    }
  }

  private void processStaticMethodAsLValue()
  {
    if(this.enclosingFunction == null) {}
  }

  private void processPrototype()
  {
    if(!(this.rightNode instanceof ObjectLiteral)) {
      this.writer.print(Warn.BAD_RVALUE);
    }
    if(!this.context.legacy && this.enclosingFunction != null) {
      this.writer.print(Warn.BAD_BODY_DEF);
    }
  }

  private void processInstanceMethodAsLValue()
  {

  }

  /**
   * Instance field is a construct of form 'this.field'. Here is used as left value of assignment.
   */
  private void processInstanceFieldAsLValue()
  {
    if(this.enclosingFunction == null) {
      // can't assign to this.field from global space
      // j(s)-script dialect does use this pointer only inside constructor and class methods
      this.writer.print(Warn.BAD_THIS, "Attempt to use this pointer as lvalue in global space.");
      return;
    }

    PropertyGet propertyGet = (PropertyGet)this.leftNode;
    String propertyName = Utils.getName(propertyGet.getProperty());

    if(this.enclosingFunction.isConstructor()) {
      // instance field declaration is allowed only on constructor and only if not already declared
      // subsequent assignments are considered instance field value updates
      String apidoc = this.assignment.getJsDoc();
      if(!this.enclosingClass.hasField(propertyName)) {
        if(apidoc == null) {
          this.writer.print(Warn.NO_APIDOC, "Missing type annotation for field declaration.");
          return;
        }
        JsDoc jsDoc = new JsDoc(apidoc);
        this.enclosingClass.addField(propertyName, jsDoc.getTypeType());
        return;
      }
      if(apidoc != null) {
        this.writer.print(Warn.BAD_APIDOC);
      }
    }

    if(this.enclosingFunction.isStatic()) {
      this.writer.print(Warn.BAD_THIS, "Assignment to static field using this pointer.");
      return;
    }
    if(this.enclosingFunction.isAnonymous()) {
      this.writer.print(Warn.BAD_THIS, "Attempt to use this pointer as lvalue in anonymous function.");
      return;
    }
    if(!this.enclosingClass.hasField(propertyName)) {
      this.writer.print(Warn.NO_FIELD_DECL, "Instance field must be declared in constructor.");
      return;
    }
    if(Utils.isConstantName(propertyName)) {
      this.writer.print(Warn.CONST_ASSIGN);
    }
  }
}
