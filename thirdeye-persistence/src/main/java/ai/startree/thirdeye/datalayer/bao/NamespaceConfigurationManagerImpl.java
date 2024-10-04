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

import ai.startree.thirdeye.datalayer.dao.NamespaceConfigurationDao;
import ai.startree.thirdeye.spi.config.TimeConfiguration;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.NamespaceConfigurationManager;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.NamespaceConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TimeConfigurationDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public class NamespaceConfigurationManagerImpl implements NamespaceConfigurationManager {

  private final NamespaceConfigurationDao dao;
  private final TimeConfiguration timeConfiguration;

  @Inject
  public NamespaceConfigurationManagerImpl(final NamespaceConfigurationDao dao,
      final TimeConfiguration timeConfiguration) {
    this.dao = dao;
    this.timeConfiguration = timeConfiguration;
  }

  public @NonNull NamespaceConfigurationDTO getNamespaceConfiguration(final String namespace) {
    final NamespaceConfigurationDTO existingNamespaceConfig = fetchExistingNamespaceConfiguration(
        namespace);

    // namespace config exists, update if components are empty
    if (existingNamespaceConfig != null) {
      if (existingNamespaceConfig.getTimeConfiguration() == null) {
        existingNamespaceConfig.setTimeConfiguration(
            getDefaultTimeConfigurationFromServerConfig());
        final Long namespaceConfigurationId = save(existingNamespaceConfig);
        checkState(namespaceConfigurationId != null,
            "Failed to update namespace configuration for namespace %s",
            existingNamespaceConfig.namespace());
      }
      return existingNamespaceConfig;
    }

    return createNewNamespaceConfiguration(namespace);
  }

  public @NonNull NamespaceConfigurationDTO updateNamespaceConfiguration(
      NamespaceConfigurationDTO updatedNamespaceConfiguration) {
    String namespace = updatedNamespaceConfiguration.namespace();
    final NamespaceConfigurationDTO existingNamespaceConfig = fetchExistingNamespaceConfiguration(
        namespace);
    checkState(existingNamespaceConfig != null,
        "Trying to update non-existent namespace configuration for namespace %s",
        namespace);

    final Long namespaceConfigurationId = save(updatedNamespaceConfiguration);
    checkState(namespaceConfigurationId != null,
        "Failed to update namespace configuration for namespace %s",
        namespace);

    return updatedNamespaceConfiguration;
  }

  public @NonNull NamespaceConfigurationDTO resetNamespaceConfiguration(final String namespace) {
    final NamespaceConfigurationDTO existingNamespaceConfig = fetchExistingNamespaceConfiguration(
        namespace);

    // namespace config exists, update values to default
    if (existingNamespaceConfig != null) {
      existingNamespaceConfig.setTimeConfiguration(
          getDefaultTimeConfigurationFromServerConfig());
      final Long namespaceConfigurationId = save(existingNamespaceConfig);
      checkState(namespaceConfigurationId != null,
          "Failed to rollback namespace configuration for namespace %s",
          existingNamespaceConfig.namespace());
      return existingNamespaceConfig;
    }

    return createNewNamespaceConfiguration(namespace);
  }

  private NamespaceConfigurationDTO fetchExistingNamespaceConfiguration(String namespace) {
    final DaoFilter daoFilter = new DaoFilter().setPredicate(Predicate.EQ(
        "namespace", namespace));
    final List<NamespaceConfigurationDTO> results = filter(daoFilter);
    if (results != null && !results.isEmpty()) {
      checkState(results.size() == 1,
          "Invalid state. Multiple namespace configuration exist for namespace %s. Contact support",
          namespace);
      return results.get(0);
    }
    return null;
  }

  private @NonNull NamespaceConfigurationDTO createNewNamespaceConfiguration(String namespace) {
    NamespaceConfigurationDTO namespaceConfigurationDTO = new NamespaceConfigurationDTO();
    namespaceConfigurationDTO.setTimeConfiguration(getDefaultTimeConfigurationFromServerConfig())
        .setAuth(new AuthorizationConfigurationDTO().setNamespace(namespace));
    final Long namespaceConfigurationId = save(namespaceConfigurationDTO);
    checkState(namespaceConfigurationId != null,
        "Failed to create namespace configuration for namespace %s",
        namespaceConfigurationDTO.namespace());
    return namespaceConfigurationDTO;
  }

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

  public int update(final NamespaceConfigurationDTO entity) {
    return dao.update(entity);
  }

  public NamespaceConfigurationDTO findById(final Long id) {
    return dao.get(id);
  }

  public int delete(final NamespaceConfigurationDTO entity) {
    return dao.delete(entity.getId());
  }

  public int deleteById(final Long id) {
    return dao.delete(id);
  }

  public List<NamespaceConfigurationDTO> findAll() {
    return dao.getAll();
  }

  public List<NamespaceConfigurationDTO> filter(final DaoFilter daoFilter) {
    return dao.filter(daoFilter);
  }

  private TimeConfigurationDTO getDefaultTimeConfigurationFromServerConfig() {
    return new TimeConfigurationDTO()
        .setDateTimePattern(timeConfiguration.getDateTimePattern())
        .setTimezone(timeConfiguration.getTimezone())
        .setMinimumOnboardingStartTime(timeConfiguration.getMinimumOnboardingStartTime());
  }
}
