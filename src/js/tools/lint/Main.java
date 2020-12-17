package js.tools.lint;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class Main
{
  private static final String VERBOSE = "-verbose";
  private static final String SOURCEPATH = "-sourcepath";
  private static final String EXCLUDES = "-excludes";

  public static void main(String[] args) throws FileNotFoundException
  {
    try {
      Lint lint = new Lint(getConfig(args));
      lint.scan();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private static Config getConfig(String[] args)
  {
    List<List<String>> optionsList = new ArrayList<List<String>>();
    List<String> optionValues = null;
    for(int i = 0; i < args.length; ++i) {
      if(args[i].charAt(0) == '-') {
        optionValues = new ArrayList<String>();
        optionsList.add(optionValues);
      }
      optionValues.add(args[i]);
    }

    Config config = new Config();
    for(List<String> option : optionsList) {
      String optionName = option.get(0);
      if(VERBOSE.equals(optionName)) {
        config.verbose = true;
        continue;
      }
      if(SOURCEPATH.equals(optionName)) {
        config.sourcepath = new File(option.get(1));
        continue;
      }
      if(EXCLUDES.equals(optionName)) {
        config.excludes = option.subList(1, option.size());
        continue;
      }
    }
    return config;
  }
}
