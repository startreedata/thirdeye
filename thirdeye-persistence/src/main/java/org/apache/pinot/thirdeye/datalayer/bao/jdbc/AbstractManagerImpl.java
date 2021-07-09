/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.datalayer.bao.jdbc;

import com.google.inject.persist.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.pinot.thirdeye.datalayer.dao.GenericPojoDao;
import org.apache.pinot.thirdeye.spi.datalayer.DaoFilter;
import org.apache.pinot.thirdeye.spi.datalayer.Predicate;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AbstractManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AbstractDTO;
import org.joda.time.DateTime;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

public abstract class AbstractManagerImpl<E extends AbstractDTO> implements AbstractManager<E> {

  protected static final ModelMapper MODEL_MAPPER = new ModelMapper();

  static {
    MODEL_MAPPER.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
  }

  protected final GenericPojoDao genericPojoDao;
  private final Class<? extends AbstractDTO> dtoClass;
  private final Class<? extends AbstractDTO> beanClass;

  protected AbstractManagerImpl(final Class<? extends AbstractDTO> dtoClass,
      final Class<? extends AbstractDTO> beanClass,
      final GenericPojoDao genericPojoDao) {
    this.dtoClass = dtoClass;
    this.beanClass = beanClass;
    this.genericPojoDao = genericPojoDao;
  }

  @Override
  public Long save(final E entity) {
    if (entity.getId() != null) {
      //TODO: throw exception and force the caller to call update instead
      update(entity);
      return entity.getId();
    }
    final AbstractDTO bean = toBean(entity);
    final Long id = genericPojoDao.put(bean);
    entity.setId(id);
    return id;
  }

  @Override
  public int update(final E entity, final Predicate predicate) {
    final AbstractDTO bean = toBean(entity);
    return genericPojoDao.update(bean, predicate);
  }

  @Override
  public int update(final E entity) {
    final AbstractDTO bean = toBean(entity);
    return genericPojoDao.update(bean);
  }

  // Test is located at TestAlertConfigManager.testBatchUpdate()
  @Override
  public int update(List<E> entities) {
    List<AbstractDTO> beans = entities.stream()
        .map(entity -> convertDTO2Bean(entity, beanClass))
        .collect(Collectors.toList());
    return genericPojoDao.update(beans);
  }

  @Override
  public E findById(final Long id) {
    final AbstractDTO abstractBean = genericPojoDao.get(id, beanClass);
    return abstractBean != null ? toDto(abstractBean) : null;
  }

  private E toDto(final AbstractDTO o) {
    if (o.getClass() != dtoClass) {
      return (E) MODEL_MAPPER.map(o, dtoClass);
    }
    return (E) o;
  }

  protected <T extends AbstractDTO> T toBean(final AbstractDTO o) {
    return (T) convertDTO2Bean(o, beanClass);
  }

  @Override
  public List<E> findByIds(final List<Long> ids) {
    final List<? extends AbstractDTO> abstractBeans = genericPojoDao.get(ids, beanClass);
    final List<E> abstractDTOs = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(abstractBeans)) {
      for (final AbstractDTO abstractBean : abstractBeans) {
        abstractDTOs.add(toDto(abstractBean));
      }
    }
    return abstractDTOs;
  }

  @Override
  public int delete(final E entity) {
    return genericPojoDao.delete(entity.getId(), beanClass);
  }

  // Test is located at TestAlertConfigManager.testBatchDeletion()
  @Override
  public int deleteById(final Long id) {
    return genericPojoDao.delete(id, beanClass);
  }

  @Override
  public int deleteByIds(final List<Long> ids) {
    return genericPojoDao.delete(ids, beanClass);
  }

  @Override
  public int deleteByPredicate(final Predicate predicate) {
    return genericPojoDao.deleteByPredicate(predicate, beanClass);
  }

  @Override
  @Transactional
  public int deleteRecordsOlderThanDays(final int days) {
    final DateTime expireDate = new DateTime().minusDays(days);
    final Timestamp expireTimestamp = new Timestamp(expireDate.getMillis());
    final Predicate timestampPredicate = Predicate.LT("createTime", expireTimestamp);
    return deleteByPredicate(timestampPredicate);
  }

  @Override
  public List<E> findAll() {
    return genericPojoDao.getAll(beanClass).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  @Override
  public List<E> findByParams(final Map<String, Object> filters) {
    final List<? extends AbstractDTO> list = genericPojoDao.get(filters, beanClass);
    return convertBeanListToDTOList(list);
  }

  @Override
  public List<E> findByPredicate(final Predicate predicate) {
    final List<? extends AbstractDTO> list = genericPojoDao.get(predicate, beanClass);
    return convertBeanListToDTOList(list);
  }

  @Override
  public List<E> filter(final DaoFilter daoFilter) {
    return convertBeanListToDTOList(genericPojoDao.filter(daoFilter.setBeanClass(beanClass)));
  }

  @Override
  public long count() {
    return genericPojoDao.count(beanClass);
  }

  protected List<E> convertBeanListToDTOList(final List<? extends AbstractDTO> beans) {
    return beans.stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  protected <T extends AbstractDTO> T convertDTO2Bean(final AbstractDTO entity,
      final Class<T> beanClass) {
    if (entity.getClass() == beanClass) {
      return (T) entity;
    }
    try {
      final AbstractDTO bean = beanClass.newInstance();
      MODEL_MAPPER.map(entity, bean);
      return (T) bean;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
