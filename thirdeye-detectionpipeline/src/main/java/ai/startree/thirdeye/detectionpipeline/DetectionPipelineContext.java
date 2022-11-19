/*
 * Copyright 2022 StarTree Inc
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

import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import java.util.List;
import org.joda.time.Interval;

public class DetectionPipelineContext {

  private ApplicationContext applicationContext;
  private Interval detectionInterval;
  private List<Predicate> predicates;
  private EnumerationItemDTO enumerationItem;
  private boolean preserveOutputDataFrames = false;

  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  public DetectionPipelineContext setApplicationContext(
      final ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
    return this;
  }

  public Interval getDetectionInterval() {
    return detectionInterval;
  }

  public DetectionPipelineContext setDetectionInterval(final Interval detectionInterval) {
    this.detectionInterval = detectionInterval;
    return this;
  }

  public List<Predicate> getPredicates() {
    return predicates;
  }

  public DetectionPipelineContext setPredicates(final List<Predicate> predicates) {
    this.predicates = predicates;
    return this;
  }

  public EnumerationItemDTO getEnumerationItem() {
    return enumerationItem;
  }

  public DetectionPipelineContext setEnumerationItem(
      final EnumerationItemDTO enumerationItem) {
    this.enumerationItem = enumerationItem;
    return this;
  }

  public boolean isPreserveOutputDataFrames() {
    return preserveOutputDataFrames;
  }

  public DetectionPipelineContext setPreserveOutputDataFrames(
      final boolean preserveOutputDataFrames) {
    this.preserveOutputDataFrames = preserveOutputDataFrames;
    return this;
  }
}
