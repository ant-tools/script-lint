package js.tools.lint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class JsClass
{
  private static Map<String, JsClass> jsClasses = new HashMap<String, JsClass>();

  public static void clear()
  {
    jsClasses.clear();
  }

  public static JsClass forName(String qualifiedName)
  {
    JsClass jsClass = jsClasses.get(qualifiedName);
    if(jsClass == null) {
      jsClass = new JsClass(qualifiedName);
      jsClasses.put(qualifiedName, jsClass);
    }
    return jsClass;
  }

  public static Collection<JsClass> getClasses()
  {
    return jsClasses.values();
  }

  private String name;
  private JsClass superClass;
  List<JsField> fields = new ArrayList<JsField>();
  List<JsField> staticFields = new ArrayList<JsField>();

  private JsClass(String name)
  {
    assert name != null;
    this.name = name;
  }

  public String getName()
  {
    return this.name;
  }

  public void setSuperClass(JsClass superClass)
  {
    this.superClass = superClass;
  }

  public JsClass getSuperClass()
  {
    return this.superClass;
  }

  public boolean is(String qualifiedName)
  {
    return this.name.equals(qualifiedName);
  }

  public void addField(String name, String type)
  {
    this.fields.add(new JsField(name, type));
  }

  public JsField getField(String name)
  {
    for(JsField field : this.fields) {
      if(field.getName().equals(name)) return field;
    }
    return null;
  }

  public boolean hasField(String name)
  {
    return getField(name) != null;
  }

  public void addStaticField(String name, String type)
  {
    this.staticFields.add(new JsField(name, type));
  }

  public JsField getStaticField(String name)
  {
    for(JsField field : this.staticFields) {
      if(field.getName().equals(name)) return field;
    }
    return null;
  }

  public boolean hasStaticField(String name)
  {
    return getStaticField(name) != null;
  }

  private static final String OBJECT_ROOT = "js.lang.Object";

  public boolean isNative()
  {
    return Utils.isNativeClassName(this.name) || OBJECT_ROOT.equals(this.name);
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if(this == obj) return true;
    if(obj == null) return false;
    if(getClass() != obj.getClass()) return false;
    JsClass other = (JsClass)obj;
    if(this.name == null) {
      if(other.name != null) return false;
    }
    else if(!this.name.equals(other.name)) return false;
    return true;
  }
}
