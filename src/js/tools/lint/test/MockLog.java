package js.tools.lint.test;

import java.util.ArrayList;
import java.util.List;

import js.tools.commons.ast.Log;

import org.mozilla.javascript.Node;

public class MockLog extends Log
{
  List<String> messages = new ArrayList<String>();
  private int warnCount;
  private String source;
  private int lineno;

  @Override
  public void setCurrentSource(String source)
  {
    this.source = source;
  }

  @Override
  public void setCurrentNode(Node node)
  {
    this.lineno = node.getLineno();
  }

  @Override
  public void print(char c)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void print(String message)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void println(String message)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void warn(String format, Object... args)
  {
    System.err.println(String.format("[%d][%s:%d] %s", ++this.warnCount, this.source, this.lineno, String.format(format, args)));
    String message = String.format(format, args);
    this.messages.add(message);
  }
}
