package ai.startree.thirdeye.testutils;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlUtils {

  public static void assertThatQueriesAreTheSame(final String output, final String expected) {
    assertThat(output
        .trim()
        .replaceAll("[\\n\\t\\r]+", " ")
        .replaceAll("  +", " ")
    ).isEqualTo(expected);
  }
}
