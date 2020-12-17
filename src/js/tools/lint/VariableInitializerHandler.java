package js.tools.lint;

import js.tools.commons.ast.AstHandler;

import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.VariableInitializer;

public class VariableInitializerHandler extends AstHandler
{
  private Context context;

  public VariableInitializerHandler(Context context)
  {
    this.context = context;
  }

  @Override
  public void handle(Node node)
  {
    Writer writer = this.context.writer;
    writer.setLog(this.log);
    VariableInitializer variableInitializer = (VariableInitializer)node;
    AstNode target = variableInitializer.getTarget();
    AstNode initializer = variableInitializer.getInitializer();

    // bad function definition: var fn = function(){}
    // use instead: function fn(){}
    if(initializer instanceof FunctionNode) {
      writer.print(Warn.BAD_FUNC_DEF);
      return;
    }

    JsClass jsClass = this.context.getEnclosingClass(variableInitializer);
    JsType jsType = null;

    if(initializer instanceof PropertyGet) {
      PropertyGet propertyGet = (PropertyGet)initializer;
      if(propertyGet.getTarget().getType() == Token.THIS) {
        String fieldName = Utils.getName(propertyGet.getProperty());
        JsField jsField = jsClass.getField(fieldName);
        if(jsField == null) {
          writer.print(Warn.NO_FIELD_DECL);
          return;
        }
        jsType = jsField.getType();
      }
    }

    if(jsType == null) jsType = JsType.getInstance(initializer);
    JsFunction function = this.context.getEnclosingFunction(variableInitializer);
    function.addVariable(Utils.getName(target), jsType);
  }
}
