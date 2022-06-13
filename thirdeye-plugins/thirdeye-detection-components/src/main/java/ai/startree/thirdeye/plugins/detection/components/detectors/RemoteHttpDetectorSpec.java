/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.detection.components.detectors;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteHttpDetectorSpec extends AbstractSpec {

  private String url;
  private Map<String, Object> params;

  public String getUrl() {
    return url;
  }

  public RemoteHttpDetectorSpec setUrl(final String url) {
    this.url = url;
    return this;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public RemoteHttpDetectorSpec setParams(final Map<String, Object> params) {
    this.params = params;
    return this;
  }
}
