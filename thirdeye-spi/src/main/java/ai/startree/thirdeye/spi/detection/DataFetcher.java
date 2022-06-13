/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.detection.v2.DataTable;
import org.joda.time.Interval;

public interface DataFetcher<T extends AbstractSpec> extends BaseComponent<T> {

  DataTable getDataTable(Interval detectionInterval) throws Exception;
}
