package js.tools.lint;

import java.util.List;

import js.tools.commons.ast.AstHandler;

import org.mozilla.javascript.Node;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.BreakStatement;
import org.mozilla.javascript.ast.SwitchCase;
import org.mozilla.javascript.ast.SwitchStatement;

final class SwitchStatementHandler extends AstHandler
{
  public SwitchStatementHandler(Context context)
  {
    // TODO Auto-generated constructor stub
  }

  @Override
  public void handle(Node node)
  {
    SwitchStatement switchStatement = (SwitchStatement)node;

    boolean defaultPresent = false;
    for(SwitchCase switchCase : switchStatement.getCases()) {
      if(switchCase.isDefault()) {
        defaultPresent = true;
        continue;
      }

      boolean breakPresent = false;
      List<AstNode> statements = switchCase.getStatements();
      if(statements != null) {
        for(AstNode statement : statements) {
          if(statement instanceof BreakStatement) {
            breakPresent = true;
            break;
          }
        }
      }

      if(!breakPresent) {
        this.log.warn("Switch clause with no break statement.");
      }
    }

    if(!defaultPresent) {
      this.log.warn("Switch statement with missing default clause.");
    }
  }
}
