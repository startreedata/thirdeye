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

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DataSourceManagerImpl extends AbstractManagerImpl<DataSourceDTO>
    implements DataSourceManager {
  
  private static final Logger LOG = LoggerFactory.getLogger(DataSourceManagerImpl.class);

  @Inject
  public DataSourceManagerImpl(GenericPojoDao genericPojoDao) {
    super(DataSourceDTO.class, genericPojoDao);
  }

  @Override
  public @Nullable DataSourceDTO findByNameAndNamespaceOrUnsetNamespace(final String name,
      final String namespace) {
    DataSourceDTO dataSourceDTO = findUniqueByNameAndNamespace(name, namespace);
    if (dataSourceDTO == null && namespace != null) {
      // look in the unset namespace
      dataSourceDTO = findUniqueByNameAndNamespace(name, null);
      if (dataSourceDTO != null) {
        LOG.warn( // fixme cyril authz - make it error level once we start migrating
            "Could not find datasource {} in namespace {}, but found a datasource with this name with an unset namespace. "
                + "Using this datasource. This behaviour will change. Please migrate your datasource to a namespace.",
            name, namespace);
      }
    }
    return dataSourceDTO;
  }
}
