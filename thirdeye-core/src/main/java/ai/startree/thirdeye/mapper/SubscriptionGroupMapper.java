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
package ai.startree.thirdeye.mapper;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.SubscriptionGroupApi;
import ai.startree.thirdeye.spi.api.TimeWindowSuppressorApi;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {
    NotificationSpecMapper.class,
    NotificationSchemeMapper.class,
    AlertAssociationMapper.class
})
public interface SubscriptionGroupMapper {

  SubscriptionGroupMapper INSTANCE = Mappers.getMapper(SubscriptionGroupMapper.class);
  String DEFAULT_ALERTER_PIPELINE = "DEFAULT_ALERTER_PIPELINE";

  private static List<Long> getAlertIds(final List<AlertApi> alerts) {
    return optional(alerts)
        .orElse(Collections.emptyList())
        .stream()
        .map(AlertApi::getId)
        .collect(Collectors.toList());
  }

  @Mapping(source = "alertSuppressors", target = "alertSuppressors", qualifiedByName = "alertSuppressors")
  @Mapping(source = "alerts", target = "properties", qualifiedByName = "properties")
  @Mapping(source = "alerts", target = "vectorClocks", qualifiedByName = "vectorClocks")
  @Mapping(source = "type", target = "type", defaultValue = DEFAULT_ALERTER_PIPELINE)
  @Mapping(source = "active", target = "active", defaultValue = "true")
  @Mapping(source = "cron", target = "cronExpression")
  SubscriptionGroupDTO toDto(SubscriptionGroupApi api);

  @Mapping(target = "type", ignore = true)
  @Mapping(target = "active", ignore = true)
  @Mapping(source = "cronExpression", target = "cron")
  @Mapping(source = "properties", target = "alerts", qualifiedByName = "toAlerts")
  @Mapping(target = "alertSuppressors", ignore = true)
  SubscriptionGroupApi toApi(SubscriptionGroupDTO dto);

  @SuppressWarnings("unchecked")
  @Named("alertSuppressors")
  default Map<String, Object> mapAlertSuppressors(final TimeWindowSuppressorApi value) {
    Map<String, Object> alertSuppressors = new HashMap<>();
    if (value != null) {
      alertSuppressors = new ObjectMapper().convertValue(value, Map.class);
    }
    return alertSuppressors;
  }

  @Named("properties")
  default Map<String, Object> mapProperties(final List<AlertApi> alerts) {
    final List<Long> alertIds = getAlertIds(alerts);
    return new HashMap<>(Map.of("detectionConfigIds", alertIds));
  }

  @Named("vectorClocks")
  default Map<Long, Long> mapVectorClocks(final List<AlertApi> alerts) {
    final List<Long> alertIds = getAlertIds(alerts);
    long currentTimestamp = 0L;
    Map<Long, Long> vectorClocks = new HashMap<>();
    for (long detectionConfigId : alertIds) {
      vectorClocks.put(detectionConfigId, currentTimestamp);
    }
    return vectorClocks;
  }

  @SuppressWarnings("unchecked")
  @Named("toAlerts")
  default List<AlertApi> fromProperties(final Map<String, Object> properties) {
    return optional(properties)
        .map(o1 -> o1.get("detectionConfigIds"))
        .map(l -> ((List<Number>) l).stream()
            .map(Number::longValue)
            .map(o -> new AlertApi().setId(o))
            .collect(Collectors.toList()))
        .orElse(null);
  }
}
