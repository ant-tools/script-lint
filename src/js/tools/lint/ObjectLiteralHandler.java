package js.tools.lint;

import java.util.HashSet;
import java.util.Set;

import js.tools.commons.ast.AstHandler;

import org.mozilla.javascript.Node;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ObjectLiteral;
import org.mozilla.javascript.ast.ObjectProperty;

public class ObjectLiteralHandler extends AstHandler
{
  private Context context;

  public ObjectLiteralHandler(Context context)
  {
    this.context = context;
  }

  @Override
  public void handle(Node node)
  {
    Writer writer = this.context.writer;
    writer.setLog(this.log);
    ObjectLiteral object = (ObjectLiteral)node;
    boolean apidocRequired = isApidocRequired(object);

    Set<String> members = new HashSet<String>();
    for(ObjectProperty property : object.getElements()) {
      AstNode left = property.getLeft();
      this.log.setCurrentNode(left);
      if(apidocRequired && left.getJsDoc() == null) {
        writer.print(Warn.NO_APIDOC);
      }
      String member = Utils.getName(left);
      if(!members.add(member)) {
        writer.print(Warn.OVERLOAD);
      }
    }
  }

  /**
   * Determine if API doc is required. API doc is mandatory, only if strict mode active, for assignment to class body, utility
   * and enumeration.
   * 
   * @param object
   * @return true if API doc is required.
   */
  private boolean isApidocRequired(ObjectLiteral object)
  {
    AstNode parent = object.getParent();
    if(!(parent instanceof Assignment)) return false;
    Assignment assignment = (Assignment)parent;
    String leftName = Utils.getName(assignment.getLeft());
    return Utils.isPrototype(leftName) || Utils.isClassName(leftName);
  }
}
