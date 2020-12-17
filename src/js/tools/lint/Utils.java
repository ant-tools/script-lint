package js.tools.lint;

import java.util.regex.Pattern;

import js.tools.commons.ast.Names;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.PropertyGet;

final class Utils
{
  private static final Pattern QUALIFIED_NAME_PATTERN = Pattern
      .compile("^[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]+)*\\.[A-Z][a-zA-Z0-9_]*(?:\\.(?:[a-z_][a-zA-Z0-9_]*)|(?:[A-Z0-9_]+))?$");
  private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]+)*(\\.[A-Z][a-zA-Z0-9_]*)+$");
  private static final Pattern NATIVE_CLASS_NAME_PATTERN = Pattern.compile("^[A-Z][a-zA-Z]*$");
  private static final Pattern PROTOTYPE_PATTERN = Pattern.compile("^[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]+)*(\\.[A-Z][a-zA-Z0-9_]*)+\\.prototype$");
  private static final Pattern PROTOTYPE_MEMBER_PATTERN = Pattern
      .compile("^[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]+)*(\\.[A-Z][a-zA-Z0-9_]*)+\\.prototype\\.[a-z_][a-zA-Z0-9_]*$");
  private static final Pattern CONSTANT_NAME_PATTERN = Pattern.compile("^(?:[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]+)*(?:\\.[A-Z][a-zA-Z0-9_]*)+\\.)?[A-Z0-9_]+$");
  private static final Pattern STATIC_MEMBER_NAME_PATTERN = Pattern
      .compile("^[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]+)*(?:\\.[A-Z][a-zA-Z0-9_]*)+(?:\\.(?:(?:[a-z_][a-zA-Z0-9_]*)|(?:[A-Z0-9_]+)))$");
  private static final String PROTOTYPE = ".prototype";

  public static String getName(AstNode node)
  {
    return Names.getName(node);
  }

  public static boolean isQualifiedName(String name)
  {
    return QUALIFIED_NAME_PATTERN.matcher(name).find();
  }

  public static boolean isClassName(String name)
  {
    return CLASS_NAME_PATTERN.matcher(name).find();
  }

  public static boolean isNativeClassName(String name)
  {
    return NATIVE_CLASS_NAME_PATTERN.matcher(name).find();
  }

  /**
   * <pre>
   * comp.prj.Class.InnerClass.prototype
   * </pre>
   * 
   * @param name
   */
  public static boolean isPrototype(String name)
  {
    return PROTOTYPE_PATTERN.matcher(name).find();
  }

  /**
   * <pre>
   *    comp.prj.Class.InnerClass.prototype -> comp.prj.Class.InnerClass
   * </pre>
   * 
   * @param name
   */
  public static String getPrototypeClassName(String name)
  {
    assert isPrototype(name);
    int i = name.lastIndexOf(PROTOTYPE);
    if(i == -1) return name;
    return name.substring(0, i);
  }

  public static boolean isPrototypeMember(String name)
  {
    return PROTOTYPE_MEMBER_PATTERN.matcher(name).find();
  }

  public static String getPrototypeMemberClassName(String name)
  {
    assert isPrototypeMember(name);
    int i = name.lastIndexOf(PROTOTYPE);
    if(i == -1) return name;
    return name.substring(0, i);
  }

  /**
   * <pre>
   *    comp.prj.Class.InnerClass.staticMember
   *    comp.prj.Class.InnerClass.CONSTANT
   * </pre>
   * 
   * @param name
   */
  public static boolean isStaticMemberName(String name)
  {
    return STATIC_MEMBER_NAME_PATTERN.matcher(name).find();
  }

  /**
   * <pre>
   *    comp.prj.Class.InnerClass.staticMember -> comp.prj.Class.InnerClass
   *    comp.prj.Class.InnerClass.CONSTANT -> comp.prj.Class.InnerClass
   * </pre>
   * 
   * @param name
   */
  public static String getStaticMemberClassName(String name)
  {
    assert isStaticMemberName(name);
    int i = name.lastIndexOf('.');
    if(i == -1) return name;
    return name.substring(0, i);
  }

  /**
   * <pre>
   *    comp.prj.Class.InnerClass.staticMember -> staticMember
   *    comp.prj.Class.InnerClass.CONSTANT -> CONSTANT
   * </pre>
   * 
   * @param name
   */
  public static String getStaticMemberName(String name)
  {
    assert isStaticMemberName(name);
    int i = name.lastIndexOf('.');
    if(i == -1) return name;
    return name.substring(i + 1);
  }

  public static boolean isConstantName(String name)
  {
    return CONSTANT_NAME_PATTERN.matcher(name).find();
  }

  public static boolean isInstanceMember(AstNode node)
  {
    if(!(node instanceof PropertyGet)) return false;
    return ((PropertyGet)node).getTarget().getType() == Token.THIS;
  }
}
