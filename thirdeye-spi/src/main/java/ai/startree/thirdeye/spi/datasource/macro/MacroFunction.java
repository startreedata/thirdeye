/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datasource.macro;

import java.util.List;

public interface MacroFunction {

  String name();

  String expandMacro(List<String> macroParams, MacroFunctionContext context);
}
