/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.spi.util;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.joda.time.Chronology;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.chrono.ISOChronology;

public class AlertMetadataUtils {

  @NonNull
  public static Chronology getDateTimeZone(final AlertMetadataDTO metadata) {
    return optional(metadata)
        .map(AlertMetadataDTO::getTimezone)
        // templates can have an empty string as default property
        .filter(StringUtils::isNotEmpty)
        .map(DateTimeZone::forID)
        .map(ISOChronology::getInstance)
        .map( c -> (Chronology) c)
        .orElse(Constants.DEFAULT_CHRONOLOGY);
  }

  @NonNull
  public static Period getDelay(final AlertMetadataDTO metadata) {
    return optional(metadata)
        .map(AlertMetadataDTO::getDataset)
        .map(DatasetConfigDTO::getCompletenessDelay)
        .map(TimeUtils::isoPeriod)
        .orElse(Period.ZERO);
  }

  @NonNull
  public static Period getGranularity(final AlertMetadataDTO metadata) {
    return optional(metadata)
        .map(AlertMetadataDTO::getGranularity)
        .map(TimeUtils::isoPeriod)
        .orElseThrow(() -> new IllegalArgumentException(
            "Granularity is null. Granularity must be set in metadata$granularity."));
  }
}
