package js.tools.lint.test;

import js.tools.commons.util.Classes;
import junit.framework.TestCase;

public class JsFunctionUnitTests extends TestCase
{
  public void testConstructor() throws Throwable
  {
    String apiDoc = "/**\r\n * Method.\r\n * @return Number\r\n */";
    Object jsFunction = getJsFunction("comp.prj.Class", apiDoc);
    assertEquals("comp.prj.Class", Classes.getFieldValue(jsFunction, "qualifiedName"));
    Object declaredReturnType = Classes.invoke(jsFunction, "getDeclaredReturnType");
    assertEquals(6, Classes.getFieldValue(declaredReturnType, "value"));
  }

  public void testConstructorWithoutApiDoc() throws Throwable
  {
    Object jsFunction = getJsFunction("comp.prj.Class");
    assertEquals("comp.prj.Class", Classes.getFieldValue(jsFunction, "qualifiedName"));
    Object declaredReturnType = Classes.invoke(jsFunction, "getDeclaredReturnType");
    assertTrue((Boolean)Classes.invoke(declaredReturnType, "isVoid"));
  }

  public void testIsConstructor() throws Throwable
  {
    Object f = getJsFunction("comp.prj.Class");
    assertTrue((Boolean)Classes.invoke(f, "isConstructor"));

    f = getJsFunction("comp.prj.Class.InnerClass");
    assertTrue((Boolean)Classes.invoke(f, "isConstructor"));

    f = getJsFunction("comp.prj.CLASS");
    assertTrue((Boolean)Classes.invoke(f, "isConstructor"));

    f = getJsFunction("comp.prj.Class.staticMethod");
    assertFalse((Boolean)Classes.invoke(f, "isConstructor"));

    f = getJsFunction("comp.prj.Class.InnerClass.staticMethod");
    assertFalse((Boolean)Classes.invoke(f, "isConstructor"));
  }

  private static Object getJsFunction(String qualifiedName) throws Exception
  {
    return Classes.newInstance("js.tools.lint.JsFunction", qualifiedName);
  }

  private static Object getJsFunction(String qualifiedName, String apiDoc) throws Exception
  {
    return Classes.newInstance("js.tools.lint.JsFunction", qualifiedName, apiDoc);
  }
}
