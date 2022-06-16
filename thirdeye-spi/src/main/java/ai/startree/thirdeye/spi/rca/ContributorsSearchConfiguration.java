package ai.startree.thirdeye.spi.rca;

import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import java.util.List;
import org.joda.time.Interval;

public class ContributorsSearchConfiguration {

  private final MetricConfigDTO metricConfigDTO;
  private final DatasetConfigDTO datasetConfigDTO;
  private final Interval currentInterval;
  private final Interval currentBaseline;
  private final int summarySize;
  private final int depth;
  private final boolean doOneSideError;
  private final List<Predicate> filters;
  private final List<List<String>> hierarchies;

  public ContributorsSearchConfiguration(final MetricConfigDTO metricConfigDTO, final DatasetConfigDTO datasetConfigDTO,
      final Interval currentInterval, final Interval currentBaseline, final int summarySize,
      final int depth, final boolean doOneSideError, final List<Predicate> filters,
      final List<List<String>> hierarchies) {
    this.metricConfigDTO = metricConfigDTO;
    this.datasetConfigDTO = datasetConfigDTO;
    this.currentInterval = currentInterval;
    this.currentBaseline = currentBaseline;
    this.summarySize = summarySize;
    this.depth = depth;
    this.doOneSideError = doOneSideError;
    this.filters = filters;
    this.hierarchies = hierarchies;
  }

  public MetricConfigDTO getMetricConfigDTO() {
    return metricConfigDTO;
  }

  public DatasetConfigDTO getDatasetConfigDTO() {
    return datasetConfigDTO;
  }

  public Interval getCurrentInterval() {
    return currentInterval;
  }

  public Interval getCurrentBaseline() {
    return currentBaseline;
  }

  public int getSummarySize() {
    return summarySize;
  }

  public int getDepth() {
    return depth;
  }

  public boolean isDoOneSideError() {
    return doOneSideError;
  }

  public List<Predicate> getFilters() {
    return filters;
  }

  public List<List<String>> getHierarchies() {
    return hierarchies;
  }
}
