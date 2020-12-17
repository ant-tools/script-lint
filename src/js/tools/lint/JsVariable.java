package js.tools.lint;

class JsVariable
{
  protected String name;
  protected JsType type;

  public JsVariable(String name)
  {
    this.name = name;
    this.type = new JsType(JsType.UNKNOWN);
  }

  public JsVariable(String name, String type)
  {
    this.name = name;
    this.type = JsType.getInstance(type);
  }

  public JsVariable(String name, JsType type)
  {
    this.name = name;
    this.type = type;
  }

  public String getName()
  {
    return this.name;
  }

  public JsType getType()
  {
    return this.type;
  }
}
