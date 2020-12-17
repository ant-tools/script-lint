package js.tools.lint;

import js.tools.commons.ast.AstHandler;
import js.tools.commons.ast.Names;

import org.mozilla.javascript.Node;
import org.mozilla.javascript.ast.NewExpression;

final class NewExpressionHandler extends AstHandler
{
  private Context context;

  public NewExpressionHandler(Context context)
  {
    this.context = context;
  }

  @Override
  public void handle(Node node)
  {
    Writer writer = this.context.writer;
    writer.setLog(this.log);

    NewExpression newExpression = (NewExpression)node;
    String targetName = Names.getName(newExpression.getTarget());
    if(Utils.isStaticMemberName(targetName)) {
      writer.print(Warn.NOT_SUPPORTED, "Can't use new operator on member [%s].", targetName);
      return;
    }
  }
}
