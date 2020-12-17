package js.tools.lint.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import js.tools.commons.util.Classes;
import js.tools.lint.Config;
import js.tools.lint.Lint;
import junit.framework.TestCase;

public class JsLintUnitTests extends TestCase
{
  public void testScanTestRepository() throws Exception
  {
    List<String> excludes = new ArrayList<String>();
    excludes.add("ecma.lang");
    excludes.add("w3c.dom");
    excludes.add("w3c.event");

    Config config = Classes.newInstance("js.tools.lint.Config");
    Classes.setFieldValue(config, "verbose", true);
    Classes.setFieldValue(config, "sourcepath", new File("res"));
    Classes.setFieldValue(config, "excludes", excludes);

    Lint lint = new Lint(config);
    lint.scan();
  }
}
