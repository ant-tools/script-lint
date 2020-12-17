package js.tools.lint.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import js.tools.commons.util.Classes;
import junit.framework.TestCase;

public class UtilsUnitTests extends TestCase
{
  private static boolean execPattern(String patternName, String input) throws Exception
  {
    Class<?> utilsClass = Class.forName("js.tools.lint.Utils");
    Pattern p = Classes.getFieldValue(utilsClass, patternName);
    Matcher m = p.matcher(input);
    return m.find();
  }

  public void testConstantNamePattern() throws Exception
  {
    assertTrue(execPattern("constantNamePattern", "CONSTANT"));
    assertTrue(execPattern("constantNamePattern", "CONSTANT2"));
    assertTrue(execPattern("constantNamePattern", "_PRIVATE_CONSTANT"));
    assertFalse(execPattern("constantNamePattern", "field"));
  }

  public void testClassMemberNamePattern() throws Exception
  {
    assertTrue(execPattern("classMemberNamePattern", "comp.prj.Class.field"));
    assertTrue(execPattern("classMemberNamePattern", "comp.prj.Class._privateField"));
    assertTrue(execPattern("classMemberNamePattern", "comp.prj.Class.InnerClass.field"));
    assertTrue(execPattern("classMemberNamePattern", "comp.prj.Class.InnerClass._privateField"));
    assertTrue(execPattern("classMemberNamePattern", "comp.prj.Class.CONSTANT"));
    assertTrue(execPattern("classMemberNamePattern", "comp.prj.Class._PRIVATE_CONSTANT"));
    assertTrue(execPattern("classMemberNamePattern", "comp.prj.Class.InnerClass.CONSTANT"));
    assertTrue(execPattern("classMemberNamePattern", "comp.prj.Class.InnerClass._PRIVATE_CONSTANT"));

    assertFalse(execPattern("classMemberNamePattern", "comp.prj"));
    assertFalse(execPattern("classMemberNamePattern", "comp.prj.Class"));
    assertFalse(execPattern("classMemberNamePattern", "comp.prj.CLASS"));
    assertFalse(execPattern("classMemberNamePattern", "comp.prj.Class.InnerClass"));
  }

  public void testGetName()
  {
    fail("Not yet implemented");
  }

  public void testIsQualifiedName()
  {
    fail("Not yet implemented");
  }

  public void testIsClassName()
  {
    fail("Not yet implemented");
  }

  public void testIsClassPrototype()
  {
    fail("Not yet implemented");
  }

  public void testIsPrototypeMethod()
  {
    fail("Not yet implemented");
  }

  public void testIsClassMemberName()
  {
    fail("Not yet implemented");
  }

  public void testIsConstantName()
  {
    fail("Not yet implemented");
  }

  public void testGetScope()
  {
    fail("Not yet implemented");
  }

  public void testGetClassName()
  {
    fail("Not yet implemented");
  }

  public void testGetTypeAstNode()
  {
    fail("Not yet implemented");
  }

  public void testGetTypeString()
  {
    fail("Not yet implemented");
  }

  public void testGetJsClassName()
  {
    fail("Not yet implemented");
  }

  public void testGetEnclosingFunction()
  {
    fail("Not yet implemented");
  }
}
