package ai.startree.thirdeye.spi.datasource.macro;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.annotations.Test;

public class MacroFunctionTest {

  private static final MacroFunction TEST_MACRO_FUNCTION = new MacroFunction() {
    @Override
    public String name() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String expandMacro(final List<String> macroParams, final MacroFunctionContext context) {
      throw new UnsupportedOperationException();
    }
  };

  @Test
  public void testIsAutoTimeConfiguration() {
    for (final String autoString : List.of("AUTO", "\"AUTO\"", "'AUTO'")) {
      final boolean res = TEST_MACRO_FUNCTION.isAutoTimeConfiguration(autoString);
      assertThat(res).isTrue();
    }
    // case sensitive
    final boolean res = TEST_MACRO_FUNCTION.isAutoTimeConfiguration("auto");
    assertThat(res).isFalse();
  }
}
