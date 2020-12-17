package js.tools.lint;

import java.util.HashMap;
import java.util.Map;

enum Warn
{
  BAD_SYNTAX, GLOBAL_ASSIGN, CONST_ASSIGN, NOT_SUPPORTED, BAD_ARGS_COUNT, BAD_CLASS_PACKAGE, BAD_CLASS_NAME, BAD_ARG_TYPE, BAD_RETURN, BAD_BODY_DEF, BAD_FUNC_DEF, BAD_RVALUE, BAD_THIS, CTOR_RETURN, CTOR_STATIC, CTOR_ANNOTATION, BAD_APIDOC, NO_APIDOC, NO_PACKAGE_DEF, NO_CLASS_DEF, NO_FIELD_DECL, NO_VAR_DECL, NO_SUPER, OVERLOAD;

  public String getMessage()
  {
    return messages.get(this);
  }

  private static Map<Warn, String> messages = new HashMap<Warn, String>();
  static {
    messages.put(BAD_SYNTAX, "Bad syntax");
    messages.put(GLOBAL_ASSIGN, "Global assignment");
    messages.put(CONST_ASSIGN, "Assignment to constant");
    messages.put(NOT_SUPPORTED, "Operation not supported");
    messages.put(BAD_ARGS_COUNT, "Bad arguments count");
    messages.put(BAD_CLASS_PACKAGE, "Bad class package");
    messages.put(BAD_CLASS_NAME, "Bad class name");
    messages.put(BAD_ARG_TYPE, "Bad argument type");
    messages.put(BAD_RETURN, "Return type does not match declaration");
    messages.put(BAD_BODY_DEF, "Bad class body definition");
    messages.put(BAD_FUNC_DEF, "Bad function definition");
    messages.put(BAD_RVALUE, "Bad right value");
    messages.put(BAD_THIS, "Invalid this pointer usage");
    messages.put(CTOR_ANNOTATION, "Missing constructor annotation");
    messages.put(CTOR_RETURN, "Return value from constructor");
    messages.put(CTOR_STATIC, "Can't access static member from constructor");
    messages.put(NO_APIDOC, "Missing API documentation");
    messages.put(BAD_APIDOC, "Unexpected API documentation");
    messages.put(NO_PACKAGE_DEF, "Missing package definition");
    messages.put(NO_CLASS_DEF, "Missing class definition");
    messages.put(NO_FIELD_DECL, "Missing field declaration");
    messages.put(NO_VAR_DECL, "Missing variable declaration");
    messages.put(NO_SUPER, "Missing super class");
    messages.put(OVERLOAD, "Member overloading");
  }
}
