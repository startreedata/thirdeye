/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
