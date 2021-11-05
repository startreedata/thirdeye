package org.apache.pinot.thirdeye.spi.datasource.macro;

import java.util.List;

public interface MacroFunction {

  String expandMacro(List<String> macroParams);
}
