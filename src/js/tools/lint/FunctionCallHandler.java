package js.tools.lint;

import java.util.List;

import js.tools.commons.ast.AstHandler;
import js.tools.commons.ast.Names;

import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;

final class FunctionCallHandler extends AstHandler
{
  private static final String PACKAGE = "$package";
  private static final String SUPPRESS = "$suppress";
  private static final String EXTENDS = "$extends";
  private static final String LEGACY = "$legacy";

  private Context context;

  public FunctionCallHandler(Context context)
  {
    this.context = context;
  }

  @Override
  public void handle(Node node)
  {
    Writer writer = this.context.writer;
    writer.setLog(this.log);

    FunctionCall functionCall = (FunctionCall)node;
    AstNode target = functionCall.getTarget();
    String functionName = Names.getName(target);
    List<AstNode> arguments = functionCall.getArguments();

    if(PACKAGE.equals(functionName)) {
      if(arguments.size() != 1) {
        writer.print(Warn.BAD_ARGS_COUNT, "$package pseudo-operator needs exactly one argument.");
        return;
      }
      if(arguments.get(0).getType() != Token.STRING) {
        writer.print(Warn.BAD_ARG_TYPE);
        return;
      }
      this.context.currentPackage = Utils.getName(arguments.get(0));
      return;
    }

    if(SUPPRESS.equals(functionName)) {
      if(arguments.size() != 1) {
        writer.print(Warn.BAD_ARGS_COUNT, "$suppress pseudo-operator needs exactly one argument.");
        return;
      }
      if(arguments.get(0).getType() != Token.STRING) {
        writer.print(Warn.BAD_ARG_TYPE);
        return;
      }
      writer.addSuppress(Utils.getName(arguments.get(0)));
      return;
    }

    if(EXTENDS.equals(functionName)) {
      if(arguments.size() != 2) {
        writer.print(Warn.BAD_ARGS_COUNT, "$extends pseudo-operator needs exactly 2 arguments.");
        return;
      }

      String subClassName = Utils.getName(arguments.get(0));
      if(!Utils.isClassName(subClassName)) {
        writer.print(Warn.BAD_CLASS_NAME, "Bad subclass name format [%s].", subClassName);
        return;
      }
      if(this.context == null) {
        writer.print(Warn.NO_CLASS_DEF);
        return;
      }
      JsClass subClass = JsClass.forName(subClassName);
      if(subClass == null) {
        writer.print(Warn.BAD_ARG_TYPE);
        return;
      }

      String superClassName = Utils.getName(arguments.get(1));
      if(!Utils.isClassName(superClassName)) {
        writer.print(Warn.BAD_CLASS_NAME, "Bad superclass name format [%s].", superClassName);
        return;
      }
      JsClass superClass = JsClass.forName(superClassName);
      subClass.setSuperClass(superClass);
      return;
    }

    if(LEGACY.equals(functionName)) {
      this.context.legacy = true;
    }
  }
}
