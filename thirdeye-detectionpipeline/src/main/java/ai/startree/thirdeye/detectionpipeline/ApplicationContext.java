/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.detectionpipeline;

import ai.startree.thirdeye.datalayer.core.EnumerationItemMaintainer;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import java.util.concurrent.ExecutorService;

public record ApplicationContext(DataSourceCache dataSourceCache,
                                 DetectionRegistry detectionRegistry,
                                 PostProcessorRegistry postProcessorRegistry,
                                 EventManager eventManager, DataSourceManager dataSourceDao,
                                 DatasetConfigManager datasetConfigManager,
                                 ExecutorService subTaskExecutor,
                                 DetectionPipelineConfiguration configuration,
                                 EnumerationItemMaintainer enumerationItemMaintainer) {}
