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
package ai.startree.thirdeye.rca;

import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EventContextDto;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.joda.time.Chronology;
import org.joda.time.Period;

public record RcaInfo(
    // todo cyril consider not exposing all objects - once refactored, only expose what is really used
    @NonNull AnomalyDTO anomaly,
    @NonNull AlertDTO alert,
    @NonNull MetricConfigDTO metric,
    @NonNull DatasetConfigDTO dataset,
    @NonNull DataSourceDTO dataSourceDto,
    // avoid passing the whole AlertMetadataDTO
    @NonNull Chronology chronology,
    @NonNull Period granularity,
    @NonNull EventContextDto eventContext) {}
  
