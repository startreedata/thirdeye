/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.dto;

public class ApplicationDTO extends AbstractDTO {

  String application;
  String recipients;

  public String getApplication() {
    return application;
  }

  public ApplicationDTO setApplication(final String application) {
    this.application = application;
    return this;
  }

  public String getRecipients() {
    return recipients;
  }

  public ApplicationDTO setRecipients(final String recipients) {
    this.recipients = recipients;
    return this;
  }
}
