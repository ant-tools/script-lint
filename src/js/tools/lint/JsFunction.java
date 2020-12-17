package js.tools.lint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class JsFunction
{
  private static Map<String, JsFunction> jsFunctions = new HashMap<String, JsFunction>();

  public static JsFunction forName(String qualifiedName)
  {
    JsFunction jsFunction = jsFunctions.get(qualifiedName);
    if(jsFunction == null) {
      jsFunction = new JsFunction(qualifiedName);
      jsFunctions.put(qualifiedName, jsFunction);
    }
    return jsFunction;
  }

  private String qualifiedName;
  private JsType declaredReturnType;
  private List<JsVariable> variables = new ArrayList<JsVariable>();

  public JsFunction(String qualifiedName)
  {
    this.qualifiedName = qualifiedName;
    this.declaredReturnType = new JsType();
  }

  public String getName()
  {
    return this.qualifiedName;
  }

  public void setApiDoc(String apidoc)
  {
    this.declaredReturnType = JsType.getInstance(new JsDoc(apidoc).getReturnType());
  }

  public void addVariable(String name, JsType jsType)
  {
    this.variables.add(new JsVariable(name, jsType));
  }

  public JsVariable getVariable(String name)
  {
    for(JsVariable variable : this.variables) {
      if(variable.getName().equals(name)) return variable;
    }
    return null;
  }

  public boolean hasVariable(String name)
  {
    return getVariable(name) != null;
  }

  public boolean isAnonymous()
  {
    return this.qualifiedName.isEmpty();
  }

  public boolean isConstructor()
  {
    return Utils.isClassName(this.qualifiedName);
  }

  public boolean isStatic()
  {
    return Utils.isStaticMemberName(this.qualifiedName);
  }

  /**
   * Check if this function is an instance method.
   * 
   */
  public boolean isInstance()
  {
    return Utils.isPrototypeMember(this.qualifiedName);
  }

  public JsType getDeclaredReturnType()
  {
    return this.declaredReturnType;
  }
}
