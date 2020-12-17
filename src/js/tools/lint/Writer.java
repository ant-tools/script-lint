package js.tools.lint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import js.tools.commons.ast.Log;

final class Writer
{
  private Log log;
  private List<Warn> suppress = new ArrayList<Warn>();

  public Writer()
  {
  }

  public void setLog(Log log)
  {
    this.log = log;
  }

  public void addSuppress(String warn)
  {
    try {
      this.suppress.add(Warn.valueOf(warn));
    }
    catch(IllegalArgumentException ignore) {}
  }

  public void print(Warn warn, Object... args)
  {
    if(this.suppress.contains(warn)) return;
    String message = warn.getMessage();
    if(args.length == 0) {
      this.log.warn(message + ".");
    }
    else {
      this.log.warn(message + ". " + args[0], Arrays.copyOfRange(args, 1, args.length));
    }
  }
}
