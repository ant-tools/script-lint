package js.tools.lint.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import js.tools.commons.util.Classes;
import junit.framework.TestCase;

public class JsDocUnitTests extends TestCase
{
  private Matcher parse(String apidoc) throws Exception
  {
    Class<?> utilsClass = Class.forName("js.tools.lint.JsDoc");
    Pattern p = Classes.getFieldValue(utilsClass, "annotationPattern");
    return p.matcher(apidoc);
  }

  public void testTypeAnnotationPattern() throws Exception
  {
    String apidoc = "/**\r\n * Field.\r\n * @type comp.prj.Class.InnerClass\r\n */";
    Matcher m = parse(apidoc);
    assertTrue(m.find());
    assertEquals("type", m.group(1));
    assertEquals("comp.prj.Class.InnerClass", m.group(2));
  }

  public void testReturnAnnotationPattern() throws Exception
  {
    String apidoc = "/**\r\n * Class.\r\n * @return Number\r\n */";
    Matcher m = parse(apidoc);
    assertTrue(m.find());
    assertEquals("return", m.group(1));
    assertEquals("Number", m.group(2));
  }

  public void testParamAnnotationPattern() throws Exception
  {
    String apidoc = "/**\r\n * Method.\r\n * @param Number firstParameter first parameter,\r\n * @param String secondParameter second parameter.\r\n */";
    Matcher m = parse(apidoc);
    assertTrue(m.find());
    assertEquals(2, m.groupCount());
    assertEquals("param", m.group(1));
    assertEquals("Number", m.group(2));
    assertTrue(m.find());
    assertEquals(2, m.groupCount());
    assertEquals("param", m.group(1));
    assertEquals("String", m.group(2));
  }

  public void testMethodApidoc() throws Throwable
  {
    String apidoc = "/**\r\n" + //
        " * Method.\r\n" + //
        " *\r\n" + //
        " * @param Number firstParameter first parameter,\r\n" + //
        " * @param String secondParameter second parameter.\r\n" + //
        " * @return comp.prj.Class created class.\r\n" + //
        " * @throws js.lang.IllegalState if state is illegal.\r\n" + //
        " * @throws js.lang.IllegalArgument if argument is illegal.\r\n" + //
        " */";
    Object jsDoc = Classes.newInstance("js.tools.lint.JsDoc", apidoc);
    assertEquals("Number", Classes.invoke(jsDoc, "getParamType", 0));
    assertEquals("String", Classes.invoke(jsDoc, "getParamType", 1));
    assertEquals("comp.prj.Class", Classes.invoke(jsDoc, "getReturnType"));
    assertEquals("js.lang.IllegalState", Classes.invoke(jsDoc, "getThrowsType", 0));
    assertEquals("js.lang.IllegalArgument", Classes.invoke(jsDoc, "getThrowsType", 1));
  }
}
