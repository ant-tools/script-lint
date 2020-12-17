package js.tools.lint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import js.tools.commons.util.Files;
import js.tools.lint.test.MockLog;

public class Lint
{
  private static final String PACKAGE_INFO = "package-info.js";
  private static final String JS_EXT = ".js";

  private File sourcepath;
  private List<File> excludes = new ArrayList<File>();
  private JsParser jsParser = new JsParser();

  public Lint(Config config)
  {
    this.sourcepath = config.sourcepath;
    for(String excludePackage : config.excludes) {
      this.excludes.add(new File(this.sourcepath, Files.dot2path(excludePackage)));
    }
    this.jsParser.setLogger(new MockLog());
  }

  public void scan() throws IOException
  {
    scan(this.sourcepath);
  }

  private void scan(File file) throws IOException
  {
    assert file.isDirectory();
    if(isExcluded(file)) return;
    for(File f : file.listFiles()) {
      if(isHidden(f)) {
        continue;
      }
      if(f.isDirectory()) {
        scan(f);
        continue;
      }
      if(isPackageInfo(f)) {
        continue;
      }
      if(isSource(f)) {
        this.jsParser.parse(f);
      }
    }
  }

  private boolean isExcluded(File file)
  {
    for(File excluded : this.excludes) {
      if(excluded.equals(file)) {
        return true;
      }
    }
    return false;
  }

  private boolean isHidden(File file)
  {
    return file.getName().charAt(0) == '.';
  }

  private boolean isPackageInfo(File file)
  {
    return file.getName().endsWith(PACKAGE_INFO);
  }

  private boolean isSource(File file)
  {
    return file.exists() && file.getName().endsWith(JS_EXT);
  }
}
