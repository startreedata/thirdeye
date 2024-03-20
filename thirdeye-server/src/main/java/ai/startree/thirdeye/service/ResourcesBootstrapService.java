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

import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ResourcesBootstrapService {

  private final AlertTemplateService alertTemplateService;

  @Inject
  public ResourcesBootstrapService(final AlertTemplateService alertTemplateService) {
    this.alertTemplateService = alertTemplateService;
  }

  public void bootstrap(final ThirdEyeServerPrincipal principal) {
    // FIXME CYRIL - the template loading should be performed with a principal that 
    //  corresponds to a shared namespace? or in all namespaces? or without namespace? 
    alertTemplateService.loadRecommendedTemplates(principal, true);
  }
}
