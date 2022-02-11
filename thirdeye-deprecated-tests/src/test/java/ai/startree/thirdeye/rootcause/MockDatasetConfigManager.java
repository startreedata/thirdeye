/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause;

import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MockDatasetConfigManager extends AbstractMockManager<DatasetConfigDTO> implements
    DatasetConfigManager {

  private final Collection<DatasetConfigDTO> datasets;

  public MockDatasetConfigManager(Collection<DatasetConfigDTO> datasets) {
    this.datasets = datasets;
  }

  @Override
  public DatasetConfigDTO findById(final Long id) {
    Collection<DatasetConfigDTO> output = Collections2
        .filter(this.datasets, new Predicate<DatasetConfigDTO>() {
          @Override
          public boolean apply(DatasetConfigDTO dto) {
            return dto.getId().equals(id);
          }
        });

    if (output.isEmpty()) {
      return null;
    }
    return output.iterator().next();
  }

  @Override
  public List<DatasetConfigDTO> findByName(String name) {
    return findByPredicate(ai.startree.thirdeye.spi.datalayer.Predicate.EQ("name", name));
  }

  @Override
  public int deleteRecordsOlderThanDays(int days) {
    return 0;
  }

  @Override
  public List<DatasetConfigDTO> findAll() {
    return new ArrayList<>(this.datasets);
  }

  @Override
  public DatasetConfigDTO findByDataset(final String dataset) {
    Collection<DatasetConfigDTO> output = new ArrayList<>(
        Collections2.filter(this.datasets, new Predicate<DatasetConfigDTO>() {
          @Override
          public boolean apply(DatasetConfigDTO dto) {
            return dto.getDataset().equals(dataset);
          }
        }));

    if (output.isEmpty()) {
      return null;
    }
    return output.iterator().next();
  }

  @Override
  public List<DatasetConfigDTO> findActive() {
    throw new AssertionError("not implemented");
  }

  @Override
  public void updateLastRefreshTime(String dataset, long lastRefreshTime, long lastEventTime) {
    throw new AssertionError("not implemented");
  }
}
