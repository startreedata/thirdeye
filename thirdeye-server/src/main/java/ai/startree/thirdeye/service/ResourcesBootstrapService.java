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
package ai.startree.thirdeye.service;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class ResourcesBootstrapService {

  private final AlertTemplateService alertTemplateService;
  private final DatasetConfigManager datasetDAO;

  @Inject
  public ResourcesBootstrapService(final AlertTemplateService alertTemplateService,
      final DatasetConfigManager datasetDAO) {
    this.alertTemplateService = alertTemplateService;
    this.datasetDAO = datasetDAO;
  }

  public void bootstrap() {
    // MIGRATION CODE: START
    // TODO CYRIL - can be removed around November 2023/December 2024
    // force migration of dataset configuration to the new timeFormat - migration is made inside DatasetConfigDto#getTimeFormat
    final List<DatasetConfigDTO> datasets = datasetDAO.findAll();
    datasetDAO.update(datasets);
    // MIGRATION CODE: END

    // FIXME CYRIL - next iteration once templates are namespaced - bootstrap should happen for each namespace
    alertTemplateService.loadRecommendedTemplates(AuthorizationManager.getInternalValidPrincipal(),
        true);
  }
}
