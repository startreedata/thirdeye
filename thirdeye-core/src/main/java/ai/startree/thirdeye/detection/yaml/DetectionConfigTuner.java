/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.yaml;

import static ai.startree.thirdeye.spi.detection.DetectionUtils.getSpecClassName;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.detection.DefaultInputDataFetcher;
import ai.startree.thirdeye.detection.annotation.registry.DetectionRegistry;
import ai.startree.thirdeye.detection.spi.components.Tunable;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.ConfigUtils;
import ai.startree.thirdeye.spi.detection.DataProvider;
import ai.startree.thirdeye.spi.detection.DetectionUtils;
import ai.startree.thirdeye.spi.detection.InputDataFetcher;
import ai.startree.thirdeye.spi.rootcause.impl.MetricEntity;
import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class focuses on tuning the detection config for the provided tuning window
 *
 * This wraps and calls the appropriate tunable implementation
 */
// todo cyril cyril work on this
public class DetectionConfigTuner {

  protected static final Logger LOG = LoggerFactory.getLogger(DetectionConfigTuner.class);

  private static final String PROP_CLASS_NAME = "className";
  private static final String PROP_METRIC_URN = "metricUrn";
  private static final String DEFAULT_TIMEZONE = "America/Los_Angeles";

  private static final DetectionRegistry DETECTION_REGISTRY = new DetectionRegistry();
  public static final String PROP_YAML_PARAMS = "yamlParams";

  private final AlertDTO detectionConfig;
  private final DataProvider dataProvider;

  public DetectionConfigTuner(AlertDTO config, DataProvider dataProvider) {
    Preconditions.checkNotNull(config);
    this.detectionConfig = config;
    this.dataProvider = dataProvider;
  }

  /**
   * Returns the default(for new alert) or tuned configuration for the specified component
   *
   * @param componentProps previously tuned configuration along with user supplied yaml config
   * @param startTime start time of the tuning window
   * @param endTime end time of the tuning window
   */
  private Map<String, Object> getTunedSpecs(Map<String, Object> componentProps, long startTime,
      long endTime)
      throws Exception {
    Map<String, Object> tunedSpec = new HashMap<>();

    // Instantiate tunable component
    long configId = detectionConfig.getId() == null ? 0 : detectionConfig.getId();
    String componentClassName = componentProps.get(PROP_CLASS_NAME).toString();
    Map<String, Object> yamlParams = ConfigUtils.getMap(componentProps.get(PROP_YAML_PARAMS));
    InputDataFetcher dataFetcher = new DefaultInputDataFetcher(dataProvider, configId);
    Tunable tunable = instantiateTunable(componentClassName, yamlParams, dataFetcher);

    // round to daily boundary
    Preconditions.checkNotNull(componentProps.get(PROP_METRIC_URN));
    String metricUrn = componentProps.get(PROP_METRIC_URN).toString();
    MetricEntity metricEntity = MetricEntity.fromURN(metricUrn);
    Map<Long, MetricConfigDTO> metricConfigMap = dataProvider
        .fetchMetrics(Collections.singleton(metricEntity.getId()));
    Preconditions.checkArgument(metricConfigMap.size() == 1, "Unable to find the metric to tune");

    MetricConfigDTO metricConfig = metricConfigMap.values().iterator().next();
    DatasetConfigDTO datasetConfig = dataProvider
        .fetchDatasets(Collections.singletonList(metricConfig.getDataset()))
        .get(metricConfig.getDataset());
    DateTimeZone timezone = DateTimeZone.forID(
        datasetConfig.getTimezone() == null ? DEFAULT_TIMEZONE : datasetConfig.getTimezone());
    DateTime start = new DateTime(startTime, timezone).withTimeAtStartOfDay();
    DateTime end = new DateTime(endTime, timezone).withTimeAtStartOfDay();
    Interval window = new Interval(start, end);

    // TODO: if dimension drill down applied, pass in the metric urn of top dimension
    tunedSpec.putAll(tunable.tune(componentProps, window, metricUrn));

    return tunedSpec;
  }

  private Tunable instantiateTunable(String componentClassName, Map<String, Object> yamlParams,
      InputDataFetcher dataFetcher)
      throws Exception {
    String tunableClassName = DETECTION_REGISTRY.lookupTunable(componentClassName);
    Class clazz = Class.forName(tunableClassName);
    Class<AbstractSpec> specClazz = (Class<AbstractSpec>) Class.forName(getSpecClassName(clazz));
    AbstractSpec spec = AbstractSpec.fromProperties(yamlParams, specClazz);
    Tunable tunable = (Tunable) clazz.newInstance();
    tunable.init(spec, dataFetcher);
    return tunable;
  }

  /**
   * Scans through all the tunable components and injects tuned model params into the detection
   * config
   */
  public AlertDTO tune(long tuningWindowStart, long tuningWindowEnd) {
    Map<String, Object> tunedComponentSpecs = new HashMap<>();

    // Tune each tunable component in the detection componentSpecs
    Map<String, Object> allComponentSpecs = this.detectionConfig.getComponentSpecs();

    // Keep it idempotent
    if (allComponentSpecs == null) {
      return detectionConfig;
    }

    for (Map.Entry<String, Object> componentSpec : allComponentSpecs.entrySet()) {
      Map<String, Object> tunedComponentProps = new HashMap<>();

      String componentKey = componentSpec.getKey();
      Map<String, Object> existingComponentProps = ConfigUtils.getMap(componentSpec.getValue());

      // For tunable components, the model params are computed from user supplied yaml params and previous model params.
      String componentClassName = optional(existingComponentProps.get(PROP_CLASS_NAME))
          .map(Objects::toString)
          .orElse(null);
      String type = DetectionUtils.getComponentType(componentKey);
      if (componentClassName != null && DETECTION_REGISTRY.isTunable(componentClassName)) {
        try {
          tunedComponentProps
              .putAll(getTunedSpecs(existingComponentProps, tuningWindowStart, tuningWindowEnd));
        } catch (Exception e) {
          LOG.error("Tuning failed for component " + type, e);
        }
      }

      tunedComponentProps.putAll(existingComponentProps);
      tunedComponentSpecs.put(componentKey, tunedComponentProps);
    }

    detectionConfig.setComponentSpecs(tunedComponentSpecs);
    return detectionConfig;
  }
}
