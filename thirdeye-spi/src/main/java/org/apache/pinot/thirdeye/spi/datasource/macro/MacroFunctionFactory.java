package org.apache.pinot.thirdeye.spi.datasource.macro;

public interface MacroFunctionFactory {

  String name();

  MacroFunction build(MacroFunctionContext context);
}
