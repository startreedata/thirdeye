package ai.startree.thirdeye.testutils;

public class SqlUtils {

  public static String cleanSql(String sql) {
    return sql
        .trim()
        .replaceAll("[\\n\\t\\r]+", " ")
        .replaceAll(" +", " ");
  }
}
