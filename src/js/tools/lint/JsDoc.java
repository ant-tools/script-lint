package js.tools.lint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class JsDoc
{
  private static final Pattern annotationPattern = Pattern.compile("^[^@]+@(constructor|return|type|param|throws)\\s+([^\\s]+)?.*$", Pattern.MULTILINE);
  private static final String CONSTRUCTOR = "constructor";
  private static final String RETURN = "return";
  private static final String TYPE = "type";
  private static final String PARAM = "param";
  private static final String THROWS = "throws";

  private Map<String, List<String>> annotations = new HashMap<String, List<String>>();

  JsDoc(String apidoc)
  {
    Matcher matcher = annotationPattern.matcher(apidoc);
    while(matcher.find()) {
      String annotation = matcher.group(1);
      String type = matcher.group(2);
      List<String> types = this.annotations.get(annotation);
      if(types == null) {
        types = new ArrayList<String>();
        this.annotations.put(annotation, types);
      }
      types.add(type);
    }
  }

  public boolean hasConstructor()
  {
    return this.annotations.get(CONSTRUCTOR) != null;
  }

  public String getReturnType()
  {
    return getType(RETURN);
  }

  public String getTypeType()
  {
    return getType(TYPE);
  }

  public String getParamType(int index)
  {
    return getType(PARAM, index);
  }

  public String getThrowsType(int index)
  {
    return getType(THROWS, index);
  }

  private String getType(String annotation, int... optIndex)
  {
    int index = optIndex.length == 0 ? 0 : optIndex[0];
    List<String> types = this.annotations.get(annotation);
    if(types == null) return null;
    assert index < types.size();
    return types.get(index);
  }
}
