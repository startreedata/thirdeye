package org.apache.pinot.thirdeye.detection.v2.macro;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.detection.v2.macro.function.TimeFilterFunctionFactory;
import org.apache.pinot.thirdeye.detection.v2.macro.function.TimeGroupFunctionFactory;
import org.apache.pinot.thirdeye.spi.datasource.macro.MacroFunction;
import org.apache.pinot.thirdeye.spi.datasource.macro.MacroFunctionContext;
import org.apache.pinot.thirdeye.spi.datasource.macro.MacroFunctionFactory;

public class MacroFunctionRegistry {

  // this is where you add Macros
  private static final List<MacroFunctionFactory> MACRO_FUNCTION_FACTORIES = ImmutableList.of(
      new TimeFilterFunctionFactory(),
      new TimeGroupFunctionFactory()
  );

  private static final Map<String, MacroFunctionFactory> MACRO_FUNCTION_FACTORY_MAP = Maps
      .uniqueIndex(MACRO_FUNCTION_FACTORIES, MacroFunctionFactory::name);

  public static MacroFunction getMacroFunction(String operatorName, MacroFunctionContext context) {
    MacroFunctionFactory macroFunctionFactory = MACRO_FUNCTION_FACTORY_MAP.get(operatorName);

    return macroFunctionFactory == null ? null : macroFunctionFactory.build(context);
  }
}
