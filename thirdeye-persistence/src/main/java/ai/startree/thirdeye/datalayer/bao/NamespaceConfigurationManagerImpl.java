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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.datalayer.dao.NamespaceConfigurationDao;
import ai.startree.thirdeye.spi.config.TimeConfiguration;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.NamespaceConfigurationManager;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.NamespaceConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.QuotasConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskQuotasConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TemplateConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TimeConfigurationDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Singleton
public class NamespaceConfigurationManagerImpl implements NamespaceConfigurationManager {

  private final NamespaceConfigurationDao dao;
  private final @Nullable TimeConfiguration timeConfiguration;
  private final NamespaceConfigurationDTO defaultNamespaceConfiguration;

  @Inject
  public NamespaceConfigurationManagerImpl(final NamespaceConfigurationDao dao,
      final @Nullable TimeConfiguration timeConfiguration,
      final NamespaceConfigurationDTO defaultNamespaceConfiguration) {
    this.dao = dao;
    this.timeConfiguration = timeConfiguration;
    this.defaultNamespaceConfiguration = defaultNamespaceConfiguration;
  }

  public @NonNull NamespaceConfigurationDTO getNamespaceConfiguration(final String namespace) {
    final NamespaceConfigurationDTO existingNamespaceConfig = fetchExistingNamespaceConfiguration(
        namespace);

    // namespace config exists, update if components are empty
    if (existingNamespaceConfig != null) {
      final boolean updateIsRequired = updateDefaults(existingNamespaceConfig, false);
      if (updateIsRequired) {
        final Long namespaceConfigurationId = save(existingNamespaceConfig);
        // FIXME CYRIL ANSHUL - THIS STATEMENT IS USELESS ? save will throw already ?  
        checkState(namespaceConfigurationId != null,
            "Failed to update namespace configuration for namespace %s",
            existingNamespaceConfig.namespace());
      }
      return existingNamespaceConfig;
    }

    return createNewNamespaceConfiguration(namespace);
  }

  /*
   * New fields may be added to the NamespaceConfiguration.
   * We need to ensure all NamespaceConfiguration objects have the latest fields.
   * This function takes care of migrating and updating fields.
   * It returns true if the input configuration was mutated.
   * It is the caller responsibility to persist the changes in the database if necessary.
   * */
  private boolean updateDefaults(final NamespaceConfigurationDTO existingNamespaceConfig, final boolean force) {
    boolean updated = false;
    if (force || existingNamespaceConfig.getTimeConfiguration() == null) {
      existingNamespaceConfig.setTimeConfiguration(defaultTimeConfiguration());
      updated = true;
    }
    if (force || existingNamespaceConfig.getTemplateConfiguration() == null) {
      existingNamespaceConfig.setTemplateConfiguration(defaultTemplateConfiguration());
      updated = true;
    }
    if (force || existingNamespaceConfig.getQuotasConfiguration() == null) {
      existingNamespaceConfig.setQuotasConfiguration(defaultQuotasConfiguration());
      updated = true;
    }
    if (force || (existingNamespaceConfig.getQuotasConfiguration() != null &&
        existingNamespaceConfig.getQuotasConfiguration().getTaskQuotasConfiguration() == null)) {
      existingNamespaceConfig.getQuotasConfiguration()
          .setTaskQuotasConfiguration(defaultTaskQuotasConfiguration());
    }
    return updated;
  }

  public @NonNull NamespaceConfigurationDTO updateNamespaceConfiguration(
      final NamespaceConfigurationDTO updatedNamespaceConfiguration) {
    final String namespace = updatedNamespaceConfiguration.namespace();
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
      updateDefaults(existingNamespaceConfig, true);
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

  private @NonNull NamespaceConfigurationDTO createNewNamespaceConfiguration(
      final String namespace) {
    final NamespaceConfigurationDTO namespaceConfigurationDTO = defaultNamespaceConfiguration(
        namespace);

    final Long namespaceConfigurationId = save(namespaceConfigurationDTO);
    checkState(namespaceConfigurationId != null,
        "Failed to create namespace configuration for namespace %s",
        namespaceConfigurationDTO.namespace());
    return namespaceConfigurationDTO;
  }

  private NamespaceConfigurationDTO defaultNamespaceConfiguration(final String namespace) {
    final NamespaceConfigurationDTO namespaceConfigurationDTO = new NamespaceConfigurationDTO();
    namespaceConfigurationDTO
        .setTimeConfiguration(defaultTimeConfiguration())
        .setTemplateConfiguration(defaultTemplateConfiguration())
        .setQuotasConfiguration(defaultQuotasConfiguration())
        .setAuth(new AuthorizationConfigurationDTO().setNamespace(namespace));
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

  private TimeConfigurationDTO defaultTimeConfiguration() {
    if (timeConfiguration != null) {
      // timeConfiguration is deprecated - users should use the defaultNamespaceConfiguration 
      return new TimeConfigurationDTO()
          .setDateTimePattern(timeConfiguration.getDateTimePattern())
          .setTimezone(timeConfiguration.getTimezone())
          .setMinimumOnboardingStartTime(timeConfiguration.getMinimumOnboardingStartTime());  
    } else {
      return optional(defaultNamespaceConfiguration.getTimeConfiguration())
          .orElse(new TimeConfigurationDTO());
    }
  }

  private TemplateConfigurationDTO defaultTemplateConfiguration() {
    return optional(defaultNamespaceConfiguration.getTemplateConfiguration())
        .orElse(new TemplateConfigurationDTO());
  }

  private QuotasConfigurationDTO defaultQuotasConfiguration() {
    return optional(defaultNamespaceConfiguration.getQuotasConfiguration())
        .orElse(new QuotasConfigurationDTO()
            .setTaskQuotasConfiguration(new TaskQuotasConfigurationDTO()));
  }

  private TaskQuotasConfigurationDTO defaultTaskQuotasConfiguration() {
    return optional(defaultNamespaceConfiguration.getQuotasConfiguration())
        .map(QuotasConfigurationDTO::getTaskQuotasConfiguration)
        .orElse(new TaskQuotasConfigurationDTO());
  }
}
