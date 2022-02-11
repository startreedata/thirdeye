/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause;

import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import com.google.common.collect.Collections2;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class MockMetricConfigManager extends AbstractMockManager<MetricConfigDTO> implements
    MetricConfigManager {

  private final Collection<MetricConfigDTO> metrics;

  public MockMetricConfigManager(Collection<MetricConfigDTO> metrics) {
    this.metrics = metrics;
  }

  @Override
  public MetricConfigDTO findById(final Long id) {
    Collection<MetricConfigDTO> output = Collections2
        .filter(this.metrics, dto -> dto.getId().equals(id));

    if (output.isEmpty()) {
      return null;
    }
    return output.iterator().next();
  }

  @Override
  public List<MetricConfigDTO> findByName(String name) {
    return findByPredicate(Predicate.EQ("name", name));
  }

  @Override
  public List<MetricConfigDTO> findAll() {
    return new ArrayList<>(this.metrics);
  }

  @Override
  public List<MetricConfigDTO> findByDataset(final String dataset) {
    return new ArrayList<>(Collections2.filter(this.metrics,
        dto -> dto.getDataset().equals(dataset)));
  }

  @Override
  public MetricConfigDTO findByMetricAndDataset(String metricName, String dataset) {
    throw new AssertionError("not implemented");
  }

  @Override
  public List<MetricConfigDTO> findActiveByDataset(String dataset) {
    throw new AssertionError("not implemented");
  }

  @Override
  public List<MetricConfigDTO> findWhereNameOrAliasLikeAndActive(String name) {
    throw new AssertionError("not implemented");
  }

  @Override
  public List<MetricConfigDTO> findWhereAliasLikeAndActive(Set<String> aliasParts) {
    throw new AssertionError("not implemented");
  }

  @Override
  public List<MetricConfigDTO> findByMetricName(String metricName) {
    throw new AssertionError("not implemented");
  }
}
