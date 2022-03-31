/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.slack;

public class SlackConfiguration {

  private String webhookUrl;

  public String getWebhookUrl() {
    return webhookUrl;
  }

  public SlackConfiguration setWebhookUrl(final String webhookUrl) {
    this.webhookUrl = webhookUrl;
    return this;
  }
}
