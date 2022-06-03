package ai.startree.thirdeye.spi.rca;

public interface ContributorsFinder {

  /**
   * Find top contributors of an anomaly.
   */
  ContributorsFinderResult search(final ContributorsSearchConfiguration searchConfiguration) throws Exception;
}
