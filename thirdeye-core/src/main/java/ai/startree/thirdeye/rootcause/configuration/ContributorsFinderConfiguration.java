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
package ai.startree.thirdeye.rootcause.configuration;

import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ContributorsFinderConfiguration {

  private static final String DEFAULT_CONTRIBUTORS_ALGORITHM = "simple";
  private static final Map<String, Object> DEFAULT_ALGORITHM_PARAMS = Map.of();

  private @NonNull String algorithm = DEFAULT_CONTRIBUTORS_ALGORITHM;
  private @NonNull Map<String, Object> params = DEFAULT_ALGORITHM_PARAMS;

  public @NonNull String getAlgorithm() {
    return algorithm;
  }

  public ContributorsFinderConfiguration setAlgorithm(final @NonNull String algorithm) {
    Objects.requireNonNull(algorithm);
    this.algorithm = algorithm;
    return this;
  }

  public @NonNull Map<String, Object> getParams() {
    return params;
  }

  public ContributorsFinderConfiguration setParams(final Map<String, Object> params) {
    Objects.requireNonNull(params);
    this.params = params;
    return this;
  }
}
