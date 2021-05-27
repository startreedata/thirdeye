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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.pinot.thirdeye.datalayer.dao.GenericPojoDao;
import org.apache.pinot.thirdeye.spi.datalayer.DaoFilter;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AbstractManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AbstractDTO;
import org.apache.pinot.thirdeye.spi.datalayer.util.Predicate;
import org.joda.time.DateTime;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

public abstract class AbstractManagerImpl<E extends AbstractDTO> implements AbstractManager<E> {

  protected static final ModelMapper MODEL_MAPPER = new ModelMapper();

  static {
    MODEL_MAPPER.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
  }

  private final Class<? extends AbstractDTO> dtoClass;
  private final Class<? extends AbstractDTO> beanClass;
  protected final GenericPojoDao genericPojoDao;

  protected AbstractManagerImpl(Class<? extends AbstractDTO> dtoClass,
      Class<? extends AbstractDTO> beanClass,
      final GenericPojoDao genericPojoDao) {
    this.dtoClass = dtoClass;
    this.beanClass = beanClass;
    this.genericPojoDao = genericPojoDao;
  }

  @Override
  public Long save(E entity) {
    if (entity.getId() != null) {
      //TODO: throw exception and force the caller to call update instead
      update(entity);
      return entity.getId();
    }
    AbstractDTO bean = convertDTO2Bean(entity, beanClass);
    Long id = genericPojoDao.put(bean);
    entity.setId(id);
    return id;
  }

  @Override
  public int update(E entity, Predicate predicate) {
    AbstractDTO bean = convertDTO2Bean(entity, beanClass);
    return genericPojoDao.update(bean, predicate);
  }

  @Override
  public int update(E entity) {
    AbstractDTO bean = convertDTO2Bean(entity, beanClass);
    return genericPojoDao.update(bean);
  }

  // Test is located at TestAlertConfigManager.testBatchUpdate()
  @Override
  public int update(List<E> entities) {
    ArrayList<AbstractDTO> beans = new ArrayList<>();
    for (E entity : entities) {
      beans.add(convertDTO2Bean(entity, beanClass));
    }
    return genericPojoDao.update(beans);
  }

  public E findById(Long id) {
    AbstractDTO abstractBean = genericPojoDao.get(id, beanClass);
    if (abstractBean != null) {
      AbstractDTO abstractDTO = MODEL_MAPPER.map(abstractBean, dtoClass);
      return (E) abstractDTO;
    } else {
      return null;
    }
  }

  @Override
  public List<E> findByIds(List<Long> ids) {
    List<? extends AbstractDTO> abstractBeans = genericPojoDao.get(ids, beanClass);
    List<E> abstractDTOs = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(abstractBeans)) {
      for (AbstractDTO abstractBean : abstractBeans) {
        E abstractDTO = (E) MODEL_MAPPER.map(abstractBean, dtoClass);
        abstractDTOs.add(abstractDTO);
      }
    }
    return abstractDTOs;
  }

  @Override
  public int delete(E entity) {
    return genericPojoDao.delete(entity.getId(), beanClass);
  }

  // Test is located at TestAlertConfigManager.testBatchDeletion()
  @Override
  public int deleteById(Long id) {
    return genericPojoDao.delete(id, beanClass);
  }

  @Override
  public int deleteByIds(List<Long> ids) {
    return genericPojoDao.delete(ids, beanClass);
  }

  @Override
  public int deleteByPredicate(Predicate predicate) {
    return genericPojoDao.deleteByPredicate(predicate, beanClass);
  }

  @Override
  @Transactional
  public int deleteRecordsOlderThanDays(int days) {
    DateTime expireDate = new DateTime().minusDays(days);
    Timestamp expireTimestamp = new Timestamp(expireDate.getMillis());
    Predicate timestampPredicate = Predicate.LT("createTime", expireTimestamp);
    return deleteByPredicate(timestampPredicate);
  }

  @Override
  public List<E> findAll() {
    List<? extends AbstractDTO> list = genericPojoDao.getAll(beanClass);
    List<E> result = new ArrayList<>();
    for (AbstractDTO bean : list) {
      AbstractDTO dto = MODEL_MAPPER.map(bean, dtoClass);
      result.add((E) dto);
    }
    return result;
  }

  @Override
  public List<E> findByParams(Map<String, Object> filters) {
    List<? extends AbstractDTO> list = genericPojoDao.get(filters, beanClass);
    return convertBeanListToDTOList(list);
  }

  @Override
  public List<E> findByPredicate(Predicate predicate) {
    List<? extends AbstractDTO> list = genericPojoDao.get(predicate, beanClass);
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

  protected List<E> convertBeanListToDTOList(List<? extends AbstractDTO> beans) {
    List<E> result = new ArrayList<>();
    for (AbstractDTO bean : beans) {
      result.add((E) convertBean2DTO(bean, dtoClass));
    }
    return result;
  }

  protected <T extends AbstractDTO> T convertBean2DTO(AbstractDTO entity, Class<T> dtoClass) {
    try {
      AbstractDTO dto = dtoClass.newInstance();
      MODEL_MAPPER.map(entity, dto);
      return (T) dto;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected <T extends AbstractDTO> T convertDTO2Bean(AbstractDTO entity, Class<T> beanClass) {
    try {
      AbstractDTO bean = beanClass.newInstance();
      MODEL_MAPPER.map(entity, bean);
      return (T) bean;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
