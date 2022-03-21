/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.alert.fetcher;

import ai.startree.thirdeye.detection.anomaly.utils.AnomalyUtils;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertSnapshotDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFetcherConfig;
import ai.startree.thirdeye.spi.util.SpiUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.Properties;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseAnomalyFetcher implements AnomalyFetcher {

  private static final Logger LOG = LoggerFactory.getLogger(BaseAnomalyFetcher.class);
  protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static final String ANOMALY_SOURCE_TYPE = "anomalySourceType";
  public static final String ANOMALY_SOURCE = "anomalySource";

  protected Properties properties;
  protected AnomalyFetcherConfig anomalyFetcherConfig;
  protected MergedAnomalyResultManager mergedAnomalyResultManager;
  protected boolean active = true;

  public BaseAnomalyFetcher(final MergedAnomalyResultManager mergedAnomalyResultManager) {
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
  }

  public static String getSnapshotKey(MergedAnomalyResultDTO anomaly) {
    return anomaly.getMetric() + "::" + AnomalyUtils.generateFilterSetForTimeSeriesQuery(anomaly);
  }

  @Override
  public void init(AnomalyFetcherConfig anomalyFetcherConfig) {
    this.anomalyFetcherConfig = anomalyFetcherConfig;
    this.properties = SpiUtils
        .decodeCompactedProperties(anomalyFetcherConfig.getProperties());
  }

  @Override
  public abstract Collection<MergedAnomalyResultDTO> getAlertCandidates(DateTime current,
      AlertSnapshotDTO alertSnapShot);

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
