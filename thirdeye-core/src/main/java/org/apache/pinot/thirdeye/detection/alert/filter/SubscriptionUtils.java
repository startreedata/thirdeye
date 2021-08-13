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

package org.apache.pinot.thirdeye.detection.alert.filter;

import java.util.Map;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EmailSchemeDto;
import org.apache.pinot.thirdeye.spi.datalayer.dto.NotificationSchemesDto;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.detection.ConfigUtils;

public class SubscriptionUtils {

  public static final String PROP_RECIPIENTS = "recipients";
  public static final String PROP_TO = "to";

  /**
   * Make a new(child) subscription group from given(parent) subscription group.
   *
   * This is used   preparing the notification for each dimensional recipients.
   */
  public static SubscriptionGroupDTO makeChildSubscriptionConfig(
      SubscriptionGroupDTO parentConfig,
      NotificationSchemesDto notificationSchemes,
      Map<String, String> refLinks) {
    // TODO: clone object using serialization rather than manual copy
    SubscriptionGroupDTO subsConfig = new SubscriptionGroupDTO();
    subsConfig.setId(parentConfig.getId());
    subsConfig.setFrom(parentConfig.getFrom());
    subsConfig.setActive(parentConfig.isActive());
    subsConfig.setOwners(parentConfig.getOwners());
    subsConfig.setYaml(parentConfig.getYaml());
    subsConfig.setApplication(parentConfig.getApplication());
    subsConfig.setName(parentConfig.getName());
    subsConfig.setSubjectType(parentConfig.getSubjectType());
    subsConfig.setProperties(parentConfig.getProperties());
    subsConfig.setVectorClocks(parentConfig.getVectorClocks());

    subsConfig.setNotificationSchemes(notificationSchemes);
    subsConfig.setRefLinks(refLinks);

    return subsConfig;
  }

  /**
   * Validates if the subscription config has email recipients configured or not.
   */
  public static boolean isEmptyEmailRecipients(SubscriptionGroupDTO subscriptionGroupDTO) {
    EmailSchemeDto emailProps = subscriptionGroupDTO.getNotificationSchemes().getEmailScheme();
    return emailProps==null || emailProps.getTo() == null || emailProps.getTo().isEmpty();
  }
}
