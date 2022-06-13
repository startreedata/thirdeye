/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.detection.components.triggers;

import ai.startree.thirdeye.spi.detection.EventTrigger;
import ai.startree.thirdeye.spi.detection.EventTriggerException;
import ai.startree.thirdeye.spi.detection.v2.ColumnType;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import java.util.List;

/**
 * Absolute change rule detection
 */
public class ConsoleOutputTrigger implements EventTrigger<ConsoleOutputTriggerSpec> {

  private ConsoleOutputTriggerSpec spec;

  @Override
  public void init(final ConsoleOutputTriggerSpec spec) {
    this.spec = spec;
  }

  @Override
  public void trigger(final List<String> columnNames, final List<ColumnType> columnTypes, final Object[] event) throws EventTriggerException {
    System.out.println(String.format(spec.getFormat(), DataTable.getRecord(columnNames, event)));
  }

  @Override
  public void close() {
  }
}
