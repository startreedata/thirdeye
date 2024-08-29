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

import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.bao.AlertTemplateManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
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

  //todo cyril authz - deprecate the unset namespace fallback logic once envs are migrated
  @Override
  public @Nullable AlertTemplateDTO findMatchInNamespaceOrUnsetNamespace(final @NonNull AlertTemplateDTO alertTemplateDTO, final @Nullable String namespace) {
    AlertTemplateDTO match = null;
    final Long id = alertTemplateDTO.getId();
    final String name = alertTemplateDTO.getName();
    if (id != null) {
      match = findById(id);
      if (namespace != null) { // todo cyril authz - this if condition will be always true once unset namespace backward compatibility logic is removed
        checkState(Objects.equals(match.namespace(), namespace), "Could not find template with id  %s in namespace %s", id, namespace); // fixme cyril - can leak existence of entities of other namespaces? 
      }
    } else if (name != null) {
      match = findUniqueByNameAndNamespace(name, namespace);
      if (match == null && namespace != null) { // todo cyril authz - this if condition will be removed once unset namespace backward compatibility logic is removed
        match = findUniqueByNameAndNamespace(name, null);
        if (match != null) {
          LOG.warn( // todo cyril authz - make it error level once we start migrating
              "Could not find template with name {} in namespace {}, but found a template with this name with an unset namespace. "
                  + "Using this template. This behaviour will change. Please migrate your templates to a namespace.",
              name, namespace);
        }
      }
    }
    if (match != null) {
      return match;
    } else if (alertTemplateDTO.getNodes() != null) {
      return alertTemplateDTO;
    } else {
      LOG.error("Template not found. Name: {}. Namespace: {}. Id: {}. ", name, namespace, id);
      return null;
    }
  }
}
