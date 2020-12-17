package js.tools.lint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import js.tools.commons.ast.Log;
import js.tools.commons.ast.Scanner;

import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.NewExpression;
import org.mozilla.javascript.ast.ObjectLiteral;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.SwitchStatement;
import org.mozilla.javascript.ast.VariableInitializer;

/**
 * Parse j(s)-script source file. This parser validate given source file against j(s)-script dialect grammar. It does
 * not produce anything except warnings to configured logger.
 * 
 * @author Iulian Rotaru
 */
final class JsParser
{
  private Log log;

  public void setLogger(Log log)
  {
    this.log = log;
  }

  public void parse(File jsFile) throws FileNotFoundException
  {
    Scanner scanner = new Scanner(this.log);

    JsClass.clear();
    Context context = new Context();
    context.writer = new Writer();
    context.writer.setLog(this.log);

    scanner.bind(Assignment.class, new AssignmentHandler(context));
    scanner.bind(FunctionCall.class, new FunctionCallHandler(context));
    scanner.bind(NewExpression.class, new NewExpressionHandler(context));
    scanner.bind(SwitchStatement.class, new SwitchStatementHandler(context));
    scanner.bind(ReturnStatement.class, new ReturnStatementHandler(context));
    scanner.bind(VariableInitializer.class, new VariableInitializerHandler(context));
    scanner.bind(ObjectLiteral.class, new ObjectLiteralHandler(context));

    try {
      scanner.parse(new FileReader(jsFile), jsFile.getName());
    }
    catch(RhinoException e) {
      context.writer.print(Warn.BAD_SYNTAX);
      return;
    }

    for(JsClass jsClass : JsClass.getClasses()) {
      if(!jsClass.isNative() && jsClass.getSuperClass() == null) {
        // System.out.println(jsClass);
        // context.writer.print(Warn.NO_SUPER);
      }
    }
  }
}
