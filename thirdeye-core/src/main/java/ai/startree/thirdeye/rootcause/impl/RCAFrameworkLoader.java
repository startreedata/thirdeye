/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package ai.startree.thirdeye.rootcause.impl;

import ai.startree.thirdeye.datasource.ThirdEyeCacheRegistry;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.rootcause.Pipeline;
import ai.startree.thirdeye.rootcause.PipelineInitContext;
import ai.startree.thirdeye.rootcause.RCAFramework;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.EntityToEntityMappingManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RCAFrameworkLoader creates Pipeline instances based on a YAML config file. It expects the
 * Pipeline
 * implementation to support a constructor that takes (name, inputs, properties map) as arguments.
 * It further augments certain properties with additional information, e.g. the {@code PROP_PATH}
 * property with absolute path information.
 */
@Singleton
public class RCAFrameworkLoader {

  private static final Logger LOG = LoggerFactory.getLogger(RCAFrameworkLoader.class);

  private final RCAConfiguration configuration;
  private final MetricConfigManager metricConfigManager;
  private final DatasetConfigManager datasetConfigManager;
  private final DataSourceCache dataSourceCache;
  private final ThirdEyeCacheRegistry thirdEyeCacheRegistry;
  private final EntityToEntityMappingManager entityToEntityMappingManager;
  private final EventManager eventManager;
  private final MergedAnomalyResultManager mergedAnomalyResultManager;

  @Inject
  public RCAFrameworkLoader(
      final RCAConfiguration configuration,
      final MetricConfigManager metricConfigManager,
      final DatasetConfigManager datasetConfigManager,
      final DataSourceCache dataSourceCache,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry,
      final EntityToEntityMappingManager entityToEntityMappingManager,
      final EventManager eventManager,
      final MergedAnomalyResultManager mergedAnomalyResultManager) {
    this.configuration = configuration;
    this.metricConfigManager = metricConfigManager;
    this.datasetConfigManager = datasetConfigManager;
    this.dataSourceCache = dataSourceCache;
    this.thirdEyeCacheRegistry = thirdEyeCacheRegistry;
    this.entityToEntityMappingManager = entityToEntityMappingManager;
    this.eventManager = eventManager;
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
  }

  public Map<String, RCAFramework> getFrameworksFromConfig() {
    final ExecutorService executor = Executors.newFixedThreadPool(configuration.getParallelism());

    final Map<String, RCAFramework> frameworks = new HashMap<>();
    for (String frameworkName : configuration.getFrameworks().keySet()) {
      List<Pipeline> pipelines = getPipelinesFromConfig(frameworkName);
      frameworks.put(frameworkName, new RCAFramework(pipelines, executor));
    }

    return frameworks;
  }

  public List<Pipeline> getPipelinesFromConfig(String frameworkName) {
    return getPipelines(frameworkName);
  }

  private List<Pipeline> getPipelines(
      String frameworkName) {
    List<Pipeline> pipelines = new ArrayList<>();
    Map<String, List<PipelineConfiguration>> rcaPipelinesConfiguration =
        configuration.getFrameworks();

    if (!MapUtils.isEmpty(rcaPipelinesConfiguration)) {
      if (!rcaPipelinesConfiguration.containsKey(frameworkName)) {
        throw new IllegalArgumentException(
            String.format("Framework '%s' does not exist", frameworkName));
      }

      for (PipelineConfiguration pipelineConfig : rcaPipelinesConfiguration.get(frameworkName)) {
        String outputName = pipelineConfig.getOutputName();
        Set<String> inputNames = new HashSet<>(pipelineConfig.getInputNames());
        String className = pipelineConfig.getClassName();
        Map<String, Object> properties = pipelineConfig.getProperties();
        if (properties == null) {
          properties = new HashMap<>();
        }

        LOG.info("Creating pipeline '{}' [{}] with inputs '{}'", outputName, className, inputNames);
        final PipelineInitContext initContext = new PipelineInitContext()
            .setInputNames(inputNames)
            .setOutputName(outputName)
            .setProperties(properties)
            .setDatasetConfigManager(datasetConfigManager)
            .setMetricConfigManager(metricConfigManager)
            .setDataSourceCache(dataSourceCache)
            .setThirdEyeCacheRegistry(thirdEyeCacheRegistry)
            .setEntityToEntityMappingManager(entityToEntityMappingManager)
            .setEventManager(eventManager)
            .setMergedAnomalyResultManager(mergedAnomalyResultManager);
        ;

        final Constructor<?> constructor;
        try {
          constructor = Class.forName(className).getConstructor();
          final Pipeline pipeline = (Pipeline) constructor.newInstance();
          pipeline.init(initContext);
          pipelines.add(pipeline);
        } catch (RuntimeException e) {
          // Already a runtime exception. Just escalate.
          throw e;
        } catch (Exception e) {
          // wrap in RuntimeException and escalate.
          throw new RuntimeException("Failed to load: " + className, e);
        }
      }
    }

    return pipelines;
  }
}
