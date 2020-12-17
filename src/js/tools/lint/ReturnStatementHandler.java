package js.tools.lint;

import js.tools.commons.ast.AstHandler;

import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ConditionalExpression;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.ReturnStatement;

public class ReturnStatementHandler extends AstHandler
{
  private static final String UNDEFINED = "undefined";

  private Context context;
  private Writer writer;
  private JsClass enclosingClass;
  private JsFunction enclosingFunction;
  private AstNode returnValue;
  private JsType declaredReturnType;
  private JsType returnType;

  public ReturnStatementHandler(Context context)
  {
    this.context = context;
  }

  @Override
  public void handle(Node node)
  {
    this.writer = this.context.writer;
    this.writer.setLog(this.log);

    ReturnStatement returnStatement = (ReturnStatement)node;
    this.enclosingClass = this.context.getEnclosingClass(returnStatement);
    this.enclosingFunction = this.context.getEnclosingFunction(returnStatement);
    if(this.enclosingFunction == null) {
      throw new LintException("Return from global scope is syntax error and should be catched by Rhino parser.");
    }
    this.returnValue = returnStatement.getReturnValue();
    this.declaredReturnType = this.enclosingFunction.getDeclaredReturnType();
    this.returnType = null;

    if(this.returnValue == null) {
      if(processNullValue()) return;
    }
    if(this.returnValue instanceof ConditionalExpression) {
      if(processTernaryOperator()) return;
    }
    if(this.returnValue.getType() == Token.CALL) {
      // do not use instanceof FunctionCall because NewExpression is subclass
      if(processFunctionCall()) return;
    }
    if(this.returnValue instanceof Name) {
      if(processVariable()) return;
    }
    if(this.returnValue instanceof PropertyGet) {
      if(processPropertyGet()) return;
    }
    if(this.returnValue instanceof InfixExpression) {
      // test InfixExpression after PropertyGet because it is superclass
      if(processBinaryOperator()) return;
    }
    if(this.returnValue.getType() == Token.THIS) {
      // return value is this keyword, i.e. 'return this;'
      // force return type to enclosing class
      this.returnType = JsType.getInstance(this.enclosingClass.getName());
    }
    if(this.returnType == null) {
      this.returnType = JsType.getInstance(this.returnValue);
    }

    if(this.enclosingFunction.isConstructor() && !this.returnType.isVoid()) {
      this.writer.print(Warn.CTOR_RETURN);
      return;
    }
    if(this.returnType.isUndefined()) {
      // undefined actual return type is valid for every declared one
      return;
    }
    if(this.returnType.isVoid() ^ this.declaredReturnType.isVoid()) {
      this.writer.print(Warn.BAD_RETURN);
      return;
    }
    if(!this.returnType.isKindOf(this.declaredReturnType)) {
      this.writer.print(Warn.BAD_RETURN);
    }
  }

  private boolean processNullValue()
  {
    if(!this.declaredReturnType.isVoid()) {
      this.writer.print(Warn.BAD_RETURN);
    }
    return true;
  }

  private boolean processBinaryOperator()
  {
    if(this.declaredReturnType.isVoid()) {
      this.writer.print(Warn.BAD_RETURN);
    }
    return true;
  }

  private boolean processPropertyGet()
  {
    // return value is a class field like 'return this.field;'
    // force return type to field type; warn if field declaration is missing
    PropertyGet propertyGet = (PropertyGet)this.returnValue;
    AstNode target = propertyGet.getTarget();
    if(target.getType() == Token.THIS) {
      String fieldName = Utils.getName(propertyGet.getProperty());
      JsField jsField = this.enclosingClass.getField(fieldName);
      if(jsField == null) {
        this.writer.print(Warn.NO_FIELD_DECL);
        return true;
      }
      this.returnType = jsField.getType();
    }
    else {
      // return value is a property of a field or variable, e.g. 'return this.field.name;' or 'return variable.name;'
      // for now this condition is not processed
      return true;
    }
    return false;
  }

  private boolean processVariable()
  {
    // return value is a variable like 'return variable;'
    // force return type to variable type; warn if variable declaration is missing
    String variableName = Utils.getName(this.returnValue);
    if(UNDEFINED.equals(variableName)) return false;
    JsVariable variable = this.enclosingFunction.getVariable(variableName);
    if(variable == null) {
      this.writer.print(Warn.NO_VAR_DECL);
      return true;
    }
    this.returnType = variable.getType();
    return false;
  }

  private boolean processFunctionCall()
  {
    // returning result of a function call is not processed for now
    // sample code: 'return call();'
    if(this.declaredReturnType.isVoid()) {
      this.writer.print(Warn.BAD_RETURN);
    }
    return true;
  }

  private boolean processTernaryOperator()
  {
    if(this.declaredReturnType.isVoid()) {
      this.writer.print(Warn.BAD_RETURN);
    }
    return true;
  }
}
