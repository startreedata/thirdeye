/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.detection.components.detectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class DataFrameApi {

  private Map<String, List<Serializable>> seriesMap;

  public Map<String, List<Serializable>> getSeriesMap() {
    return seriesMap;
  }

  public DataFrameApi setSeriesMap(
      final Map<String, List<Serializable>> seriesMap) {
    this.seriesMap = seriesMap;
    return this;
  }
}
