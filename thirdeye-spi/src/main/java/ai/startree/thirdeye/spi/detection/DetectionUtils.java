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

package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalySubscriptionGroupNotificationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.DetectionPipelineResult;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetectionUtils {

  // Extracts the component type from the component key
  // e.g., "myRule:ALGORITHM" -> "ALGORITHM"
  public static String getComponentType(final String componentKey) {
    if (componentKey != null && componentKey.contains(":")) {
      return componentKey.substring(componentKey.lastIndexOf(":") + 1);
    }
    throw new IllegalArgumentException(
        "componentKey is invalid; must be of type componentName:type");
  }

  // get the spec class name for a component class
  public static String getSpecClassName(final Class<BaseComponent> componentClass) {
    final ParameterizedType genericSuperclass = (ParameterizedType) componentClass
        .getGenericInterfaces()[0];
    return (genericSuperclass.getActualTypeArguments()[0].getTypeName());
  }

  public static void setEntityChildMapping(final MergedAnomalyResultDTO parent,
      final MergedAnomalyResultDTO child1) {
    if (child1 != null) {
      parent.getChildren().add(child1);
      child1.setChild(true);
    }

    parent.setChild(false);
  }

  public static MergedAnomalyResultDTO makeEntityAnomaly() {
    final MergedAnomalyResultDTO entityAnomaly = new MergedAnomalyResultDTO();
    // TODO: define anomaly type
    //entityAnomaly.setType();
    entityAnomaly.setChild(false);

    return entityAnomaly;
  }

  public static MergedAnomalyResultDTO makeParentEntityAnomaly(
      final MergedAnomalyResultDTO childAnomaly) {
    final MergedAnomalyResultDTO newEntityAnomaly = makeEntityAnomaly();
    newEntityAnomaly.setStartTime(childAnomaly.getStartTime());
    newEntityAnomaly.setEndTime(childAnomaly.getEndTime());
    setEntityChildMapping(newEntityAnomaly, childAnomaly);
    return newEntityAnomaly;
  }

  public static List<MergedAnomalyResultDTO> mergeAndSortAnomalies(
      final List<MergedAnomalyResultDTO> anomalyListA,
      final List<MergedAnomalyResultDTO> anomalyListB) {
    final List<MergedAnomalyResultDTO> anomalies = new ArrayList<>();
    if (anomalyListA != null) {
      anomalies.addAll(anomalyListA);
    }
    if (anomalyListB != null) {
      anomalies.addAll(anomalyListB);
    }

    // Sort by increasing order of anomaly start time
    anomalies.sort(Comparator.comparingLong(MergedAnomalyResultDTO::getStartTime));
    return anomalies;
  }

  public static Predicate AND(final Collection<Predicate> predicates) {
    return Predicate.AND(predicates.toArray(new Predicate[predicates.size()]));
  }

  public static List<Predicate> buildPredicatesOnTime(final long start, final long end) {
    final List<Predicate> predicates = new ArrayList<>();
    if (end >= 0) {
      predicates.add(Predicate.LT("startTime", end));
    }
    if (start >= 0) {
      predicates.add(Predicate.GT("endTime", start));
    }

    return predicates;
  }

  /**
   * Renotify the anomaly by creating or updating the record in the subscription group notification
   * table
   *
   * @param anomaly the anomaly to be notified.
   */
  public static void renotifyAnomaly(final MergedAnomalyResultDTO anomaly,
      final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager) {
    final List<AnomalySubscriptionGroupNotificationDTO> subscriptionGroupNotificationDTOs =
        anomalySubscriptionGroupNotificationManager
            .findByPredicate(Predicate.EQ("anomalyId", anomaly.getId()));
    final AnomalySubscriptionGroupNotificationDTO anomalyNotificationDTO;
    if (subscriptionGroupNotificationDTOs.isEmpty()) {
      // create a new record if it is not existed yet.
      anomalyNotificationDTO = new AnomalySubscriptionGroupNotificationDTO();
      new AnomalySubscriptionGroupNotificationDTO();
      anomalyNotificationDTO.setAnomalyId(anomaly.getId());
      anomalyNotificationDTO.setDetectionConfigId(anomaly.getDetectionConfigId());
    } else {
      // update the existing record if the anomaly needs to be re-notified
      anomalyNotificationDTO = subscriptionGroupNotificationDTOs.get(0);
      anomalyNotificationDTO.setNotifiedSubscriptionGroupIds(Collections.emptyList());
    }
    anomalySubscriptionGroupNotificationManager.save(anomalyNotificationDTO);
  }

  public static Map<String, DataTable> getTimeSeriesMap(
      final Map<String, DetectionPipelineResult> inputMap) {
    final Map<String, DataTable> timeSeriesMap = new HashMap<>();
    for (final String key : inputMap.keySet()) {
      final DetectionPipelineResult input = inputMap.get(key);
      if (input instanceof DataTable) {
        timeSeriesMap.put(key, (DataTable) input);
      }
    }
    return timeSeriesMap;
  }
}
