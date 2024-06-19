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

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_OBJECT_DOES_NOT_EXIST;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.datalayer.bao.AlertTemplateManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertTemplateManagerImpl extends AbstractManagerImpl<AlertTemplateDTO>
    implements AlertTemplateManager {

  private static final Logger LOG = LoggerFactory.getLogger(AlertTemplateManagerImpl.class);

  @Inject
  public AlertTemplateManagerImpl(GenericPojoDao genericPojoDao) {
    super(AlertTemplateDTO.class, genericPojoDao);
  }

  @Override
  public AlertTemplateDTO findMatch(final @NonNull AlertTemplateDTO alertTemplateDTO) {
    AlertTemplateDTO match = null;
    final Long id = alertTemplateDTO.getId();
    final String name = alertTemplateDTO.getName();
    if (id != null) {
      match = findById(id);
    } else if (name != null) {
      // fixme cyril authz pass namespace 
      match = findUniqueByNameAndNamespace(name, null);
    }
    if (match != null) {
      return match;
    } else if (alertTemplateDTO.getNodes() != null) {
      return alertTemplateDTO;
    } else {
      // todo cyril authz add namespace info
      throw new ThirdEyeException(ERR_OBJECT_DOES_NOT_EXIST,
          "Template not found. Name: %s. Id: %s. ".formatted(name, id));
    }
  }
}
