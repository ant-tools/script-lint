package js.tools.lint.test;

import java.util.Map;

import js.tools.commons.util.Classes;
import js.tools.lint.JsType;
import junit.framework.TestCase;

public class JsTypeUnitTests extends TestCase
{
  private String[] types = new String[]
  {
      "Unknown", "Void", "Undefined", "Null", "String", "Number", "Boolean", "Object", "Array", "RegExp", "Function"
  };

  public void testHashCode()
  {
    JsType t1 = new JsType();
    JsType t2 = new JsType();
    assertEquals(t1.hashCode(), t2.hashCode());

    for(String t : this.types) {
      t1 = new JsType(t);
      t2 = new JsType(t);
      assertEquals(t1.hashCode(), t2.hashCode());
    }

    t1 = new JsType("comp.net.Class");
    t2 = new JsType("comp.net.Class");
    assertEquals(t1.hashCode(), t2.hashCode());

    t1 = new JsType("Boolean");
    t2 = new JsType("Number");
    assertTrue(t1.hashCode() != t2.hashCode());

    t1 = new JsType("comp.net.Class1");
    t2 = new JsType("comp.net.Class2");
    assertTrue(t1.hashCode() != t2.hashCode());
  }

  public void testJsType() throws Exception
  {
    JsType jsType = new JsType();
    assertEquals("Void", Classes.getFieldValue(jsType, "value"));

    jsType = new JsType("Boolean");
    assertEquals("Boolean", Classes.getFieldValue(jsType, "value"));

    jsType = new JsType("comp.prj.Class");
    assertEquals("comp.prj.Class", Classes.getFieldValue(jsType, "value"));

    try {
      jsType = new JsType("Object");
      fail("Trying to create object type should assert.");
    }
    catch(AssertionError e) {}
  }

  public void testIsKindOf() throws Throwable
  {
    JsType t1 = new JsType();
    JsType t2 = new JsType();
    assertTrue(t1.isKindOf(t2));

    for(String t : this.types) {
      assertIsKindOf(t, t);
    }

    assertIsKindOf("Null", "Boolean");
    assertIsKindOf("Undefined", "Boolean");
    assertIsKindOf("String", "Boolean");
    assertIsKindOf("Number", "Boolean");

    Class<?> jsClassClass = Class.forName("js.tools.lint.JsClass");
    Map<?, ?> jsClasses = Classes.getFieldValue(jsClassClass, "jsClasses");
    jsClasses.clear();

    Object gradFather = Classes.invoke(jsClassClass, "forName", "comp.prj.GrandFather");
    Object father = Classes.invoke(jsClassClass, "forName", "comp.prj.Father");
    Object son = Classes.invoke(jsClassClass, "forName", "comp.prj.Son");
    Classes.invoke(father, "setSuperClass", gradFather);
    Classes.invoke(son, "setSuperClass", father);

    t1 = new JsType("comp.prj.GrandFather");
    t2 = new JsType("comp.prj.Father");
    JsType t3 = new JsType("comp.prj.Son");
    assertTrue(t3.isKindOf(t1));
    assertTrue(t3.isKindOf(t2));
    assertTrue(t2.isKindOf(t1));
  }

  private static void assertIsKindOf(String value1, String value2)
  {
    JsType type1 = new JsType(value1);
    JsType type2 = new JsType(value2);
    assertTrue(type1.isKindOf(type2));
  }

  public void testIsVoid()
  {
    JsType jsType = new JsType();
    assertTrue(jsType.isVoid());
    jsType = new JsType("Boolean");
    assertFalse(jsType.isVoid());
    jsType = new JsType("comp.prj.Class");
    assertFalse(jsType.isVoid());
  }

  public void testCanCastTo() throws Throwable
  {
    JsType t1 = new JsType();
    assertTrue(t1.canCastTo(t1));
    JsType t2 = new JsType();
    assertTrue(t1.equals(t2));

    for(String t : this.types) {
      t1 = new JsType(t);
      t2 = new JsType(t);
      assertTrue(t1.canCastTo(t2));
    }
    assertCanCastTo("Null", "Boolean");
    assertCanCastTo("String", "Boolean");
    assertCanCastTo("Number", "Boolean");

    Class<?> jsClassClass = Class.forName("js.tools.lint.JsClass");
    Map<?, ?> jsClasses = Classes.getFieldValue(jsClassClass, "jsClasses");
    jsClasses.clear();

    Object gradFather = Classes.invoke(jsClassClass, "forName", "comp.prj.GrandFather");
    Object father = Classes.invoke(jsClassClass, "forName", "comp.prj.Father");
    Object son = Classes.invoke(jsClassClass, "forName", "comp.prj.Son");
    t1 = new JsType("comp.prj.GrandFather");
    t2 = new JsType("comp.prj.Father");
    JsType t3 = new JsType("comp.prj.Son");
    assertFalse(t3.canCastTo(t1));
    assertFalse(t2.canCastTo(t1));

    Classes.invoke(father, "setSuperClass", gradFather);
    Classes.invoke(son, "setSuperClass", father);
    assertTrue(t3.canCastTo(t1));
    assertTrue(t3.canCastTo(t2));
    assertTrue(t2.canCastTo(t1));
    assertFalse(t1.canCastTo(t2));
    assertFalse(t1.canCastTo(t3));
    assertFalse(t2.canCastTo(t3));
  }

  private static void assertCanCastTo(String value1, String value2)
  {
    JsType type1 = new JsType(value1);
    JsType type2 = new JsType(value2);
    assertTrue(type1.canCastTo(type2));
  }

  public void testEqualsObject()
  {
    JsType t1 = new JsType();
    JsType t2 = new JsType();
    assertTrue(t1.equals(t2));

    for(String t : this.types) {
      t1 = new JsType(t);
      t2 = new JsType(t);
      assertTrue(t1.equals(t2));
    }

    t1 = new JsType("comp.net.Class");
    t2 = new JsType("comp.net.Class");
    assertTrue(t1.equals(t2));

    t1 = new JsType("Boolean");
    t2 = new JsType("Number");
    assertFalse(t1.equals(t2));

    t1 = new JsType("comp.net.Class1");
    t2 = new JsType("comp.net.Class2");
    assertFalse(t1.equals(t2));
  }
}
