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
package ai.startree.thirdeye.plugins.rca.contributors.simple;


import ai.startree.thirdeye.spi.rca.ContributorsFinder;
import ai.startree.thirdeye.spi.rca.ContributorsFinderContext;
import ai.startree.thirdeye.spi.rca.ContributorsFinderFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SimpleContributorsFinderPluginFactory implements ContributorsFinderFactory {

  @Override
  public String name() {
    return "simple";
  }

  @Override
  public @NonNull ContributorsFinder build(final ContributorsFinderContext context) {
    final SimpleConfiguration configuration = new ObjectMapper().convertValue(context.getParams(), SimpleConfiguration.class);

    return new SimpleContributorsFinder(
        context.getAggregationLoader(),
        configuration);
  }
}
