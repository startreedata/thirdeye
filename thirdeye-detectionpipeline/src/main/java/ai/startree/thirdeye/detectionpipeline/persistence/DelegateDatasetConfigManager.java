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

package ai.startree.thirdeye.detectionpipeline.persistence;

import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import java.util.List;

/**
 * This class is generated using IntelliJ IDEA.
 * - Click where you want to generate the delegate implementation.
 * - Press Alt + Insert (Windows/Linux) or Cmd + N (Mac) to open the "Generate" menu.
 * - Select "Delegate Methods" from the menu.
 * - In the "Select Interface Methods to Delegate" dialog, choose the methods you want to delegate
 *   by checking the checkboxes next to the method names.
 * - Click "OK."
 */
public class DelegateDatasetConfigManager implements DatasetConfigManager {

  private final DatasetConfigManager delegate;

  public DelegateDatasetConfigManager(final DatasetConfigManager delegate) {
    this.delegate = delegate;
  }

  @Override
  public Long save(final DatasetConfigDTO entity) {
    return delegate.save(entity);
  }

  @Override
  public int update(final DatasetConfigDTO entity) {
    return delegate.update(entity);
  }

  @Override
  public int update(final List<DatasetConfigDTO> entities) {
    return delegate.update(entities);
  }

  @Override
  public DatasetConfigDTO findById(final Long id) {
    return delegate.findById(id);
  }

  @Override
  public List<DatasetConfigDTO> findByName(final String name) {
    return delegate.findByName(name);
  }

  @Override
  public List<DatasetConfigDTO> findByIds(final List<Long> id) {
    return delegate.findByIds(id);
  }

  @Override
  public int delete(final DatasetConfigDTO entity) {
    return delegate.delete(entity);
  }

  @Override
  public int deleteById(final Long id) {
    return delegate.deleteById(id);
  }

  @Override
  public int deleteByIds(final List<Long> ids) {
    return delegate.deleteByIds(ids);
  }

  @Override
  public int deleteByPredicate(final Predicate predicate) {
    return delegate.deleteByPredicate(predicate);
  }

  @Override
  public int deleteRecordsOlderThanDays(final int days) {
    return delegate.deleteRecordsOlderThanDays(days);
  }

  @Override
  public List<DatasetConfigDTO> findAll() {
    return delegate.findAll();
  }

  @Override
  public List<DatasetConfigDTO> findByPredicate(final Predicate predicate) {
    return delegate.findByPredicate(predicate);
  }

  @Override
  public List<DatasetConfigDTO> filter(final DaoFilter daoFilter) {
    return delegate.filter(daoFilter);
  }

  @Override
  public int update(final DatasetConfigDTO entity, final Predicate predicate) {
    return delegate.update(entity, predicate);
  }

  @Override
  public long count() {
    return delegate.count();
  }

  @Override
  public long count(final Predicate predicate) {
    return delegate.count(predicate);
  }

  @Override
  public DatasetConfigDTO findByDataset(final String dataset) {
    return delegate.findByDataset(dataset);
  }

  @Override
  public List<DatasetConfigDTO> findActive() {
    return delegate.findActive();
  }

  @Override
  public void updateLastRefreshTime(final String dataset, final long lastRefreshTime,
      final long lastEventTime) {
    delegate.updateLastRefreshTime(dataset, lastRefreshTime, lastEventTime);
  }
}
