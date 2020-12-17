package js.tools.lint.test;

import java.io.File;

import js.tools.commons.util.Classes;
import junit.framework.TestCase;

public class JsParserUnitTests extends TestCase
{
  private MockLog log;

  @Override
  protected void setUp() throws Exception
  {
    this.log = new MockLog();
  }

  public void _testValidAssignment()
  {
    run("assignment");
  }

  public void testXHR()
  {
    run("XHR");
  }

  public void testBadSyntax()
  {
    run("bad-syntax");
    assertEquals(1, this.log.messages.size());
    assertEquals("Bad syntax.", this.log.messages.get(0));
  }

  public void testValidClassDefinition()
  {
    run("valid-class-definition");
  }

  public void testValidInnerClassDefinition()
  {
    run("valid-inner-class-definition");
  }

  public void testMissingPackage()
  {
    run("missing-package");
    assertEquals(1, this.log.messages.size());
    assertEquals("Missing package definition.", this.log.messages.get(0));
  }

  public void testBadClassPackage()
  {
    run("bad-class-package");
    assertEquals(1, this.log.messages.size());
    assertEquals("Bad class package.", this.log.messages.get(0));
  }

  public void testMissingFieldDeclaration()
  {
    run("missing-field-declaration");
    assertEquals(1, this.log.messages.size());
    assertEquals("Missing field declaration. Instance field must be declared in constructor.", this.log.messages.get(0));
  }

  public void testAssignmentToConstant()
  {
    run("assignment-to-constant");
    assertEquals(3, this.log.messages.size());
    assertEquals("Assignment to constant.", this.log.messages.get(0));
    assertEquals("Assignment to constant.", this.log.messages.get(1));
  }

  public void testBadReturnScope()
  {
    run("bad-return-scope");
    assertEquals(7, this.log.messages.size());
    assertEquals("Return value from constructor.", this.log.messages.get(0));
    assertEquals("Return type does not match declaration.", this.log.messages.get(1));
    assertEquals("Return type does not match declaration.", this.log.messages.get(2));
    assertEquals("Return value from constructor.", this.log.messages.get(3));
    assertEquals("Return type does not match declaration.", this.log.messages.get(4));
    assertEquals("Return type does not match declaration.", this.log.messages.get(5));
    assertEquals("Return type does not match declaration.", this.log.messages.get(6));
  }

  public void testValidReturn()
  {
    run("valid-return");
    assertEquals(0, this.log.messages.size());
  }

  public void testBadReturnValue()
  {
    run("bad-return-value");
    assertEquals(8, this.log.messages.size());
    for(int i = 0; i < 8; ++i) {
      assertEquals("Return type does not match declaration.", this.log.messages.get(i));
    }
  }

  public void testMissingCtorAnnotation()
  {
    run("missing-ctor-annotation");
    assertEquals(1, this.log.messages.size());
    assertEquals("Missing constructor annotation.", this.log.messages.get(0));
  }

  public void testMemeberOverloading()
  {
    run("member-overloading");
    assertEquals(5, this.log.messages.size());
    for(int i = 0; i < 5; ++i) {
      assertEquals("Member overloading.", this.log.messages.get(i));
    }
  }

  public void testBadFunctionDefinition()
  {
    run("bad-function-definition");
    assertEquals(1, this.log.messages.size());
    assertEquals("Bad function definition.", this.log.messages.get(0));
  }

  private void run(String testName)
  {
    try {
      Object jsParser = Classes.newInstance("js.tools.lint.JsParser");
      Classes.invoke(jsParser, "setLogger", this.log);
      Classes.invoke(jsParser, "parse", new File("src/js/tools/lint/test/" + testName + ".js"));
    }
    catch(Throwable e) {
      throw new RuntimeException(e);
    }
  }
}
