/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.spi;

import ai.startree.thirdeye.spi.accessControl.AccessControlFactory;
import ai.startree.thirdeye.spi.auth.Authenticator.OauthAuthenticatorFactory;
import ai.startree.thirdeye.spi.auth.OpenIdConfigurationProvider;
import ai.startree.thirdeye.spi.bootstrap.BootstrapResourcesProviderFactory;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorFactory;
import ai.startree.thirdeye.spi.detection.EnumeratorFactory;
import ai.startree.thirdeye.spi.detection.EventTriggerFactory;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessorFactory;
import ai.startree.thirdeye.spi.notification.NotificationServiceFactory;
import ai.startree.thirdeye.spi.rca.ContributorsFinderFactory;
import java.util.Collections;

public interface Plugin {

  default Iterable<OpenIdConfigurationProvider.Factory> getOpenIdConfigurationProviderFactories() {
    return Collections.emptyList();
  }

  default Iterable<OauthAuthenticatorFactory> getOAuthAuthenticatorFactories() {
    return Collections.emptyList();
  }

  default Iterable<ThirdEyeDataSourceFactory> getDataSourceFactories() {
    return Collections.emptyList();
  }

  default Iterable<AnomalyDetectorFactory> getAnomalyDetectorFactories() {
    return Collections.emptyList();
  }

  default Iterable<EventTriggerFactory> getEventTriggerFactories() {
    return Collections.emptyList();
  }

  default Iterable<NotificationServiceFactory> getNotificationServiceFactories() {
    return Collections.emptyList();
  }

  default Iterable<ContributorsFinderFactory> getContributorsFinderFactories() {
    return Collections.emptyList();
  }

  default Iterable<BootstrapResourcesProviderFactory> getBootstrapResourcesProviderFactories() {
    return Collections.emptyList();
  }

  default Iterable<AnomalyPostProcessorFactory> getAnomalyPostProcessorFactories() {
    return Collections.emptyList();
  }

  default Iterable<EnumeratorFactory> getEnumeratorFactories() {
    return Collections.emptyList();
  }

  default Iterable<AccessControlFactory> getAccessControlFactories() {
    return Collections.emptyList();
  }
}
