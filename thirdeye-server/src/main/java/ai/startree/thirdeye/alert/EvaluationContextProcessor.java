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

import ai.startree.thirdeye.detectionpipeline.PlanNodeContext;
import ai.startree.thirdeye.spi.api.EvaluationContextApi;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import org.checkerframework.checker.nullness.qual.Nullable;

public class EvaluationContextProcessor {

  public PlanNodeContext getContext(final @Nullable EvaluationContextApi evaluationContext) {

    final PlanNodeContext context = new PlanNodeContext();

    // add predicates
    optional(evaluationContext)
        .map(EvaluationContextApi::getFilters)
        .map(Predicate::parseAndCombinePredicates)
        .ifPresent(context::setPredicates);

    return context;
  }
}
