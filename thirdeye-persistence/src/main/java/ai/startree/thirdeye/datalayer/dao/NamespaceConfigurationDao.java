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
package ai.startree.thirdeye.datalayer.dao;

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.datalayer.DatabaseClient;
import ai.startree.thirdeye.datalayer.DatabaseOrm;
import ai.startree.thirdeye.datalayer.entity.NamespaceConfigurationEntity;
import ai.startree.thirdeye.datalayer.mapper.NamespaceConfigurationEntityMapper;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.NamespaceConfigurationDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NamespaceConfigurationDao {

  private static final Logger LOG = LoggerFactory.getLogger(NamespaceConfigurationDao.class);
  private static final boolean IS_DEBUG = LOG.isDebugEnabled();

  private final DatabaseOrm databaseOrm;
  private final DatabaseClient databaseClient;

  @Inject
  public NamespaceConfigurationDao(final DatabaseOrm databaseOrm,
      final DatabaseClient DatabaseClient) {
    this.databaseOrm = databaseOrm;
    this.databaseClient = DatabaseClient;
  }

  private NamespaceConfigurationEntity toEntity(final NamespaceConfigurationDTO dto)
      throws JsonProcessingException {
    NamespaceConfigurationEntity entity = NamespaceConfigurationEntityMapper.INSTANCE
        .toNamespaceConfigurationEntity(dto);

    final String jsonVal = toJsonString(dto);
    entity.setJsonVal(jsonVal);

    final Timestamp currentTime = new java.sql.Timestamp(System.currentTimeMillis());
    entity.setCreateTime(currentTime);
    entity.setUpdateTime(currentTime);

    return entity;
  }

  private NamespaceConfigurationDTO toDto(
      final NamespaceConfigurationEntity entity) throws JsonProcessingException {
    NamespaceConfigurationDTO dto = Constants.TEMPLATABLE_OBJECT_MAPPER.readValue(
        entity.getJsonVal(), NamespaceConfigurationDTO.class);

    dto.setId(entity.getId());
    dto.setCreateTime(entity.getCreateTime());
    dto.setUpdateTime(entity.getUpdateTime());

    return dto;
  }

  private List<NamespaceConfigurationDTO> toDto(
      final List<NamespaceConfigurationEntity> entities) throws JsonProcessingException {
    final List<NamespaceConfigurationDTO> ret = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(entities)) {
      for (final NamespaceConfigurationEntity entity : entities) {
        final NamespaceConfigurationDTO dto = toDto(entity);
        ret.add(dto);
      }
    }
    return ret;
  }

  private String toJsonString(final NamespaceConfigurationDTO dto) throws JsonProcessingException {
    return Constants.TEMPLATABLE_OBJECT_MAPPER.writeValueAsString(dto);
  }

  public Long put(final NamespaceConfigurationDTO pojo) {
    if(pojo.getId() != null) {
      return null;
    }
    try {
      final NamespaceConfigurationEntity entity = toEntity(pojo);
      return databaseClient.executeTransaction(
          (connection) -> databaseOrm.save(entity, connection),
          null);
    } catch (JsonProcessingException | SQLException e) {
      LOG.error(e.getMessage(), e);
      return null;
    }
  }

  /**
   * Update the list of pojos in transaction mode. Every transaction contains MAX_BATCH_SIZE of
   * entries. By default,
   * this method update the entries in one transaction. If any entries cause an exception, this
   * method will
   * update the entries one-by-one (i.e., in separated transactions) and skip the one that causes
   * exceptions.
   *
   * @param pojos the pojo to be updated, whose ID cannot be null; otherwise, it will be
   *     ignored.
   * @return the number of rows that are affected.
   */
  public int update(final List<NamespaceConfigurationDTO> pojos) {
    if (CollectionUtils.isEmpty(pojos)) {
      return 0;
    }
    int updateCounter = 0;
    for (final NamespaceConfigurationDTO pojo : pojos) {
      Preconditions.checkNotNull(pojo.getId());
      try {
        updateCounter += update(pojo);
      } catch (final Exception e) {
        LOG.error("Could not update namespace configuration entity : {}", pojo, e);
      }
    }
    return updateCounter;
  }

  public int update(final NamespaceConfigurationDTO pojo) {
    if (pojo.getId() == null) {
      throw new IllegalArgumentException(String.format(
          "Need an ID to update the Namespace Configuration entity: %s", pojo));
    }
    return update(pojo, null);
  }

  public int update(final NamespaceConfigurationDTO pojo, final Predicate predicate) {
    try {
      final NamespaceConfigurationEntity entity = toEntity(pojo);
      return databaseClient.executeTransaction(
          (connection) -> databaseOrm.update(entity, predicate, connection),
          0);
    } catch (JsonProcessingException | SQLException e) {
      LOG.error(e.getMessage(), e);
      return 0;
    }
  }

  public List<NamespaceConfigurationDTO> getAll() {
    try {
      final List<NamespaceConfigurationEntity> entities = databaseClient.executeTransaction(
          (connection) -> databaseOrm.findAll(null,
              null, null, NamespaceConfigurationEntity.class, connection),
          Collections.emptyList());
      return toDto(entities);
    } catch (final JsonProcessingException | SQLException e) {
      LOG.error(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  public List<NamespaceConfigurationDTO> list(final long limit, final long offset) {
    try {
      final List<NamespaceConfigurationEntity> entities = databaseClient.executeTransaction(
          (connection) -> databaseOrm.findAll(null,
              limit, offset, NamespaceConfigurationEntity.class, connection),
          Collections.emptyList());
      return toDto(entities);
    } catch (final JsonProcessingException | SQLException e) {
      LOG.error(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  public NamespaceConfigurationDTO get(final Long id) {
    try {
      final NamespaceConfigurationEntity entity = databaseClient.executeTransaction(
          (connection) -> databaseOrm.find(id, NamespaceConfigurationEntity.class, connection),
          null);
      if (entity == null) {
        return null;
      }
      return toDto(entity);
    } catch (final JsonProcessingException | SQLException e) {
      LOG.error(e.getMessage(), e);
      return null;
    }
  }

  public List<NamespaceConfigurationDTO> get(final List<Long> idList) {
    return get(
        Predicate.IN("id", idList.toArray())
    );
  }

  public List<NamespaceConfigurationDTO> filter(final DaoFilter daoFilter) {
    requireNonNull(daoFilter.getPredicate(),
        "If the predicate is null, you can just do "
            + "getAll() which doesn't need to fetch IDs first");
    return get(daoFilter.getPredicate(), daoFilter.getLimit());
  }

  public List<NamespaceConfigurationDTO> get(final Predicate predicate) {
    return get(predicate, null);
  }

  public List<NamespaceConfigurationDTO> get(final Predicate predicate, @Nullable Long limit) {
    try {
      final List<NamespaceConfigurationEntity> entities = databaseClient.executeTransaction(
          (connection) -> databaseOrm.findAll(
              predicate, limit, null, NamespaceConfigurationEntity.class, connection),
          Collections.emptyList());
      return toDto(entities);
    } catch (final JsonProcessingException | SQLException e) {
      LOG.error(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  public long count() {
    try {
      return databaseClient.executeTransaction(
          (connection) -> databaseOrm.count(null, NamespaceConfigurationEntity.class,
              connection),
          0L);
    } catch (SQLException e) {
      LOG.error(e.getMessage(), e);
      return 0;
    }
  }

  public long count(final Predicate predicate) {
    try {
      return databaseClient.executeTransaction(
          (connection) -> databaseOrm.count(predicate, NamespaceConfigurationEntity.class,
              connection),
          0L);
    } catch (SQLException e) {
      LOG.error(e.getMessage(), e);
      return 0;
    }
  }

  /**
   * @param parameterizedSQL second part of the sql (omit select from table section)
   */
  public List<NamespaceConfigurationDTO> executeParameterizedSQL(final String parameterizedSQL,
      final Map<String, Object> parameterMap) {
    try {
      final List<NamespaceConfigurationEntity> entities = databaseClient.executeTransaction(
          (connection) -> databaseOrm.runSQL(
              parameterizedSQL,
              parameterMap,
              NamespaceConfigurationEntity.class,
              connection), Collections.emptyList());
      return toDto(entities);
    } catch (JsonProcessingException | SQLException e) {
      LOG.error(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  public int delete(final Long id) {
    return delete(List.of(id));
  }

  public int delete(final List<Long> idsToDelete) {
    return deleteByPredicate(Predicate.IN("id", idsToDelete.toArray()));
  }

  public int deleteByPredicate(final Predicate predicate) {
    try {
      return databaseClient.executeTransaction(
          (connection) -> databaseOrm.delete(predicate, NamespaceConfigurationEntity.class,
              connection),
          0);
    } catch (SQLException e) {
      LOG.error(e.getMessage(), e);
      return 0;
    }
  }
}
