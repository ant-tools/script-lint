package js.tools.lint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import js.tools.commons.ast.Names;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NewExpression;
import org.mozilla.javascript.ast.ObjectLiteral;

public class JsType
{
  static final String UNKNOWN = "Unknown";

  private static final String VOID = "Void";
  private static final String UNDEFINED = "Undefined";
  private static final String NULL = "Null";
  private static final String STRING = "String";
  private static final String NUMBER = "Number";
  private static final String BOOLEAN = "Boolean";
  private static final String OBJECT = "Object";
  private static final String ARRAY = "Array";
  private static final String REGEXP = "RegExp";
  private static final String FUNCTION = "Function";

  public static JsType getInstance(String typeName)
  {
    if(typeName == null) return new JsType();
    return new JsType(typeName);
  }

  public static JsType getInstance(AstNode node)
  {
    if(node == null) {
      return new JsType();
    }

    if(node instanceof ObjectLiteral) {
      AstNode parent = node.getParent();
      if(parent instanceof Assignment) {
        Assignment assignment = new Assignment();
        return new JsType(Names.getName(assignment.getLeft()));
      }
    }

    if(node instanceof NewExpression) {
      NewExpression newExpression = (NewExpression)node;
      return new JsType(Names.getName(newExpression.getTarget()));
    }

    if(node instanceof FunctionCall) {
      FunctionCall functionCall = (FunctionCall)node;
      String functionName = Names.getName(functionCall.getTarget());
      // here we have two variants:
      // 1. static method invocation: js.lang.LogFactory.getLogger(...);
      // 2. constructor as a function: js.util.Timer(...)
      int i = functionName.lastIndexOf('.');
      if(i != -1) {
        return new JsType(Character.isLowerCase(functionName.charAt(i + 1)) ? functionName.substring(0, i) : functionName);
      }
    }

    if(node instanceof Name) {
      Name name = (Name)node;
      if(UNDEFINED.equalsIgnoreCase(name.getIdentifier())) {
        return new JsType(UNDEFINED);
      }
    }

    switch(node.getType()) {
    case Token.GETPROP:
      String name = Names.getName(node);
      if(Utils.isStaticMemberName(name)) return new JsType(Utils.getStaticMemberClassName(name));
      break;

    case Token.STRING:
      return new JsType(STRING);

    case Token.NUMBER:
      return new JsType(NUMBER);

    case Token.FALSE:
    case Token.TRUE:
      return new JsType(BOOLEAN);

    case Token.NULL:
      return new JsType(NULL);

    case Token.OBJECTLIT:
      return new JsType(OBJECT);

    case Token.FUNCTION:
      return new JsType(FUNCTION);

    case Token.ARRAYLIT:
      return new JsType(ARRAY);

    case Token.REGEXP:
      return new JsType(REGEXP);
    }
    return new JsType(UNKNOWN);
  }

  private String value;

  public JsType()
  {
    this.value = VOID;
  }

  public JsType(String value)
  {
    this.value = value;
  }

  public boolean isKindOf(JsType jsType)
  {
    return equals(jsType) || canCastTo(jsType);
  }

  public boolean isVoid()
  {
    return this.value.equals(VOID);
  }

  public boolean isUndefined()
  {
    return this.value.equals(UNDEFINED);
  }

  public boolean isObject()
  {
    // TODO improve for speed
    if(this.value.equals(VOID)) return false;
    if(this.value.equals(UNDEFINED)) return false;
    if(this.value.equals(NULL)) return false;
    if(this.value.equals(STRING)) return false;
    if(this.value.equals(NUMBER)) return false;
    if(this.value.equals(BOOLEAN)) return false;
    if(this.value.equals(FUNCTION)) return false;
    if(this.value.equals(ARRAY)) return false;
    if(this.value.equals(REGEXP)) return false;
    return true;
  }

  public boolean isFunction()
  {
    return this.value.equals(FUNCTION);
  }

  public boolean canCastTo(JsType jsType)
  {
    // null can cast to everything but void
    if(this.value.equals(NULL) && !jsType.value.equals(VOID)) return true;
    if(this.value.equals(jsType.value)) return true;

    if(isObject() && jsType.isObject()) {
      if(jsType.value.equals(OBJECT)) return true;
      JsClass c = JsClass.forName(this.value);
      while(c != null) {
        if(c.getName().equals(jsType.value)) return true;
        c = c.getSuperClass();
      }
      return false;
    }
    Set<String> canCastTo = casts.get(jsType.value);
    if(canCastTo != null) {
      return canCastTo.contains(this.value);
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if(this == obj) return true;
    if(obj == null) return false;
    if(getClass() != obj.getClass()) return false;
    JsType other = (JsType)obj;
    if(this.value == null) {
      if(other.value != null) return false;
    }
    else if(!this.value.equals(other.value)) return false;
    return true;
  }

  private static Map<String, Set<String>> casts = new HashMap<String, Set<String>>();
  static {
    Set<String> castableToBoolean = new HashSet<String>();
    castableToBoolean.add(NULL);
    castableToBoolean.add(UNDEFINED);
    castableToBoolean.add(STRING);
    castableToBoolean.add(NUMBER);
    casts.put(BOOLEAN, castableToBoolean);
  }
}
