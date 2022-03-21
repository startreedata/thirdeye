/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.alert;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import javax.mail.internet.InternetAddress;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

public class AlertUtils {

  private AlertUtils() {
    //left blank
  }

  /**
   * Helper to determine presence of user-feedback for an anomaly
   *
   * @param anomaly anomaly
   * @return {@code true} if feedback exists and is TRUE or FALSE, {@code false} otherwise
   */
  public static boolean hasFeedback(MergedAnomalyResultDTO anomaly) {
    return anomaly.getFeedback() != null
        && !anomaly.getFeedback().getFeedbackType().isUnresolved();
  }

  /**
   * Helper to convert a collection of email strings into {@code InternetAddress} instances,
   * filtering
   * out invalid addresses and nulls.
   *
   * @param emailCollection collection of email address strings
   * @return filtered collection of InternetAddress objects
   */
  public static Collection<InternetAddress> toAddress(Collection<String> emailCollection) {
    if (CollectionUtils.isEmpty(emailCollection)) {
      return Collections.emptySet();
    }
    return Collections2.filter(Collections2.transform(emailCollection,
        AlertUtils::toInternetAddress),
        Objects::nonNull);
  }

  private static InternetAddress toInternetAddress(final String s) {
    try {
      return new InternetAddress(s);
    } catch (Exception e) {
      return null;
    }
  }

  private static long getLastTimeStamp(Collection<MergedAnomalyResultDTO> anomalies,
      long startTime) {
    long lastTimeStamp = startTime;
    for (MergedAnomalyResultDTO anomaly : anomalies) {
      lastTimeStamp = Math.max(anomaly.getCreatedTime(), lastTimeStamp);
    }
    return lastTimeStamp;
  }

  public static Map<Long, Long> makeVectorClock(Collection<MergedAnomalyResultDTO> anomalies) {
    Multimap<Long, MergedAnomalyResultDTO> grouped = Multimaps
        .index(anomalies, new Function<MergedAnomalyResultDTO, Long>() {
          @Nullable
          @Override
          public Long apply(@Nullable MergedAnomalyResultDTO mergedAnomalyResultDTO) {
            // Return functionId to support alerting of legacy anomalies
            if (mergedAnomalyResultDTO.getDetectionConfigId() == null) {
              return mergedAnomalyResultDTO.getFunctionId();
            }

            return mergedAnomalyResultDTO.getDetectionConfigId();
          }
        });
    Map<Long, Long> detection2max = new HashMap<>();
    for (Map.Entry<Long, Collection<MergedAnomalyResultDTO>> entry : grouped.asMap().entrySet()) {
      detection2max.put(entry.getKey(), getLastTimeStamp(entry.getValue(), -1));
    }
    return detection2max;
  }

  public static Map<Long, Long> mergeVectorClock(Map<Long, Long> a, Map<Long, Long> b) {
    Set<Long> keySet = new HashSet<>();
    if (a != null) {
      keySet.addAll(a.keySet());
    }
    if (b != null) {
      keySet.addAll(b.keySet());
    }

    Map<Long, Long> result = new HashMap<>();
    for (Long detectionId : keySet) {
      long valA = MapUtils.getLongValue(a, detectionId, -1);
      long valB = MapUtils.getLongValue(b, detectionId, -1);
      result.put(detectionId, Math.max(valA, valB));
    }

    return result;
  }
}
