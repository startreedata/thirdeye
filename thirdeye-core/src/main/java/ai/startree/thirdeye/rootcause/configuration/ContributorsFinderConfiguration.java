package ai.startree.thirdeye.rootcause.configuration;

import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ContributorsFinderConfiguration {

  // todo cyril change this default value to a source available algorithm
  private static final String DEFAULT_CONTRIBUTORS_ALGORITHM = "cube";
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
