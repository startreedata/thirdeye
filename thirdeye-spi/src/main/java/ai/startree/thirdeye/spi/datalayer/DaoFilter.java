/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.spi.datalayer;

import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;

public class DaoFilter {

  private Class<? extends AbstractDTO> beanClass;
  private Predicate predicate;
  private Integer limit;
  private Integer offset;
  private String orderByKey;
  private boolean isDesc = false;

  public Predicate getPredicate() {
    return predicate;
  }

  public DaoFilter setPredicate(final Predicate predicate) {
    this.predicate = predicate;
    return this;
  }

  public Integer getLimit() {
    return limit;
  }

  public DaoFilter setLimit(final Integer limit) {
    this.limit = limit;
    return this;
  }

  public Integer getOffset() {
    return offset;
  }

  public DaoFilter setOffset(final Integer offset) {
    this.offset = offset;
    return this;
  }

  public Class<? extends AbstractDTO> getBeanClass() {
    return beanClass;
  }

  public DaoFilter setBeanClass(
      final Class<? extends AbstractDTO> beanClass) {
    this.beanClass = beanClass;
    return this;
  }

  public String getOrderByKey() {
    return orderByKey;
  }

  public DaoFilter setOrderByKey(final String orderByKey) {
    this.orderByKey = orderByKey;
    return this;
  }

  public boolean isDesc() {
    return isDesc;
  }

  public DaoFilter setDesc(final boolean desc) {
    isDesc = desc;
    return this;
  }
}
