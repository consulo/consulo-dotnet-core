package consulo.dotnet.core.newProject;

import consulo.util.lang.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class NewParser {
  public static NewParser parse(String text)  {
    String[] lines = StringUtil.convertLineSeparators(text).split("\n");

    List<Integer> lenghts = null;

    List<String> header = null;
    List<List<String>> data = new ArrayList<>();

    String prevLine = null;
    for (String line : lines) {
      if (line.isBlank()) {
        continue;
      }

      try {
        if (lenghts == null) {
          if (line.charAt(0) == '-') {
            String[] groups = line.split(" ");
            lenghts = new ArrayList<>();
            for (String group : groups) {
              if (group.isBlank()) {
                continue;
              }

              lenghts.add(group.length());
            }

            // parse header
            header = parseData(prevLine, lenghts);
          }
        }
        else {
          data.add(parseData(line, lenghts));
        }
      }
      finally {
        prevLine = line;
      }
    }

    if (header == null || data.isEmpty()) {
      throw new IllegalArgumentException("no data");
    }
    return new NewParser(header, data);
  }

  private final List<String> header;
  private final List<List<String>> data;

  public NewParser(List<String> header, List<List<String>> data) {
    this.header = header;
    this.data = data;
  }

  public List<String> getHeader() {
    return header;
  }

  public List<List<String>> getData() {
    return data;
  }

  private static List<String> parseData(String line, List<Integer> lenghts) {
    List<String> data = new ArrayList<>();

    String targetLine = line;
    for (Integer lenght : lenghts) {
      // if target line is empty we don't need substring
      String infoData = targetLine.isEmpty() ? targetLine : targetLine.substring(0, lenght).trim();

      data.add(infoData);

      targetLine = targetLine.isEmpty() ? targetLine : targetLine.substring(lenght + 1, targetLine.length());
    }

    return data;
  }
}
