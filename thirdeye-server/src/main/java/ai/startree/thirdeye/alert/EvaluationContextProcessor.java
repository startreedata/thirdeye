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

package ai.startree.thirdeye.alert;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.detectionpipeline.PlanNodeContext;
import ai.startree.thirdeye.detectionpipeline.plan.EnumeratorPlanNode;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.EnumerationItemApi;
import ai.startree.thirdeye.spi.api.EvaluationContextApi;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class EvaluationContextProcessor {

  private final EnumerationItemManager enumerationItemManager;

  @Inject
  public EvaluationContextProcessor(final EnumerationItemManager enumerationItemManager) {
    this.enumerationItemManager = enumerationItemManager;
  }

  public void process(final PlanNodeContext context,
      final EvaluationContextApi evaluationContext,
      final AlertTemplateDTO templateWithProperties) {
    if (evaluationContext == null) {
      return;
    }
    // add predicates
    addPredicates(context, evaluationContext);
    addEnumerationItem(context, evaluationContext, templateWithProperties);
  }

  void addEnumerationItem(final PlanNodeContext context,
      final EvaluationContextApi evaluationContext,
      final AlertTemplateDTO templateWithProperties) {
    if (templateWithProperties != null) {
      final long count = templateWithProperties.getNodes()
          .stream()
          .filter(p -> EnumeratorPlanNode.TYPE.equals(p.getType()))
          .count();
      ensure(count <= 1, "Max 1 enumerator node supported at this time. found: " + count);
    }
    optional(evaluationContext.getEnumerationItem())
        .map(this::map)
        .ifPresent(context::setEnumerationItem);
  }

  private EnumerationItemDTO map(final EnumerationItemApi api) {
    if (api.getId() != null) {
      return ensureExists(enumerationItemManager.findById(api.getId()),
          "enumeration item id: " + api.getId());
    }
    if (api.getName() != null) {
      final List<EnumerationItemDTO> byName = enumerationItemManager.findByName(api.getName());
      ensure(byName.size() == 1, "Exactly 1 name should exist. found: " + byName.size());
      return byName.get(0);
    }
    return ApiBeanMapper.toEnumerationItemDTO(api);
  }

  @VisibleForTesting
  void addPredicates(final PlanNodeContext context,
      final EvaluationContextApi evaluationContext) {
    optional(evaluationContext.getFilters())
        .map(Predicate::parseAndCombinePredicates)
        .ifPresent(context::setPredicates);
  }
}
