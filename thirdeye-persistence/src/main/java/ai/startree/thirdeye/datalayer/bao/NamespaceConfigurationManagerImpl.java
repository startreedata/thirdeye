/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.datalayer.bao;

import static ai.startree.thirdeye.spi.Constants.DEFAULT_CHRONOLOGY;
import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.datalayer.dao.NamespaceConfigurationDao;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.NamespaceConfigurationManager;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.NamespaceConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TimeConfigurationDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

@Singleton
public class NamespaceConfigurationManagerImpl implements NamespaceConfigurationManager {

  private final NamespaceConfigurationDao dao;

  @Inject
  public NamespaceConfigurationManagerImpl(final NamespaceConfigurationDao dao) {
    this.dao = dao;
  }

  public @NonNull NamespaceConfigurationDTO getNamespaceConfiguration(final String namespace) {
    final DaoFilter daoFilter = new DaoFilter().setPredicate(Predicate.EQ("namespace", namespace));
    final List<NamespaceConfigurationDTO> results = filter(daoFilter);

    // namespace config exists, update if components are empty
    if (results != null && !results.isEmpty()) {
      NamespaceConfigurationDTO existingNamespaceConfigurationDTO = results.get(0);
      if (existingNamespaceConfigurationDTO.getTimeConfiguration() == null) {
        existingNamespaceConfigurationDTO.setTimeConfiguration(getDefaultTimeConfiguration());
        final Long namespaceConfigurationId = save(existingNamespaceConfigurationDTO);
        checkState(namespaceConfigurationId != null,
            "Failed to update namespace configuration for namespace %s",
            existingNamespaceConfigurationDTO.namespace());
      }
      return existingNamespaceConfigurationDTO;
    }

    // create new namespace configuration
    NamespaceConfigurationDTO namespaceConfigurationDTO = new NamespaceConfigurationDTO();
    namespaceConfigurationDTO.setTimeConfiguration(getDefaultTimeConfiguration())
        .setAuth(new AuthorizationConfigurationDTO().setNamespace(namespace));
    final Long namespaceConfigurationId = save(namespaceConfigurationDTO);
    checkState(namespaceConfigurationId != null,
        "Failed to create namespace configuration for namespace %s",
        namespaceConfigurationDTO.namespace());

    return namespaceConfigurationDTO;
  }

  @Override
  public Long save(final NamespaceConfigurationDTO entity) {
    if (entity.getId() != null) {
      final int id = update(entity);
      checkState(id != 0, "failed to update namespace configuration entity");
      return entity.getId();
    }
    final Long id = dao.put(entity);
    checkState(id != null, "failed to save namespace configuration entity");
    entity.setId(id);
    return id;
  }

  @Override
  public int update(final NamespaceConfigurationDTO entity) {
    return dao.update(entity);
  }

  @Override
  public int update(final List<NamespaceConfigurationDTO> entities) {
    return dao.update(entities);
  }

  @Override
  public NamespaceConfigurationDTO findById(final Long id) {
    return dao.get(id);
  }

  @Override
  public @Nullable NamespaceConfigurationDTO findUniqueByNameAndNamespace(
      final @NonNull String name, final @Nullable String namespace) {
    final List<NamespaceConfigurationDTO> list = findByPredicate(
        Predicate.AND(
            Predicate.OR(
                Predicate.EQ("namespace", namespace)
            )
        )
    ).stream().filter(e -> Objects.equals(e.namespace(), namespace)).toList();

    if (list.isEmpty()) {
      return null;
    } else if (list.size() == 1) {
      return list.iterator().next();
    } else {
      throw new IllegalStateException(String.format(
          "Found multiple entities with and namespace %s. This should not happen. Please reach out to support.",
          namespace));
    }
  }

  @Override
  public List<NamespaceConfigurationDTO> findByIds(final List<Long> ids) {
    return dao.get(ids);
  }

  @Override
  public int delete(final NamespaceConfigurationDTO entity) {
    return dao.delete(entity.getId());
  }

  @Override
  public int deleteById(final Long id) {
    return dao.delete(id);
  }

  @Override
  public int deleteByIds(final List<Long> ids) {
    return dao.delete(ids);
  }

  @Override
  public int deleteByPredicate(final Predicate predicate) {
    return dao.deleteByPredicate(predicate);
  }

  @Override
  public int deleteRecordsOlderThanDays(final int days) {
    final DateTime expireDate = new DateTime(DateTimeZone.UTC).minusDays(days);
    final Timestamp expireTimestamp = new Timestamp(expireDate.getMillis());
    final Predicate timestampPredicate = Predicate.LT("createTime", expireTimestamp);
    return deleteByPredicate(timestampPredicate);
  }

  @Override
  public List<NamespaceConfigurationDTO> findAll() {
    return dao.getAll();
  }

  @Override
  public List<NamespaceConfigurationDTO> findByPredicate(final Predicate predicate) {
    return dao.get(predicate);
  }

  @Override
  public List<NamespaceConfigurationDTO> filter(final DaoFilter daoFilter) {
    return dao.filter(daoFilter);
  }

  @Override
  public int update(final NamespaceConfigurationDTO entity, final Predicate predicate) {
    return dao.update(entity, predicate);
  }

  @Override
  public long count() {
    return dao.count();
  }

  @Override
  public long count(final Predicate predicate) {
    return dao.count(predicate);
  }

  private TimeConfigurationDTO getDefaultTimeConfiguration() {
    return new TimeConfigurationDTO()
        .setDateTimePattern(Constants.NOTIFICATIONS_DEFAULT_DATE_PATTERN)
        .setTimezone(DEFAULT_CHRONOLOGY.getZone())
        .setMinimumOnboardingStartTime(946684800000L);
  }
}
