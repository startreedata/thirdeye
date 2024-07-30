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
package ai.startree.thirdeye.notification;

import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.joda.time.Period;

public class AlertUtils {

  public static final String NODE_TYPE_POST_PROCESSOR = "PostProcessor";
  public static final String NODE_SUB_TYPE_ANOMALY_MERGER = "ANOMALY_MERGER";

  /**
   * Get the merge max gap from the alert
   * --------------------------------------------
   * The alert needs to be rendered with the template. The merge max gap is a property of the
   * anomaly merger post processor. By default, it is 1 second.
   *
   * @param renderedTemplate the alert template rendered 
   * @return the merge max gap
   */
  @NonNull
  public static Period getMergeMaxGap(final AlertTemplateDTO renderedTemplate) {
    for (final PlanNodeBean n : renderedTemplate.getNodes()) {
      final TemplatableMap<String, Object> params = n.getParams();
      if (NODE_TYPE_POST_PROCESSOR.equals(n.getType())
          && params.containsKey("type")
          && NODE_SUB_TYPE_ANOMALY_MERGER.equals(params.get("type").getValue())) {
        if (params.containsKey("component.mergeMaxGap")) {
          final String mergeMaxGap = requireNonNull(params.get("component.mergeMaxGap").getValue())
              .toString()
              .trim();
          if (!mergeMaxGap.isEmpty()) {
            return isoPeriod(mergeMaxGap);
          }
        }
      }
    }
    return Period.seconds(1);
  }
}
