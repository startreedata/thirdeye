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

package org.apache.pinot.thirdeye.spi.api;

import org.apache.pinot.thirdeye.spi.detection.alert.DetectionAlertFilterRecipients;

public class EmailEntityApi {

  private String from;
  private DetectionAlertFilterRecipients to;
  private String subject;
  private String htmlContent;
  private String snapshotPath;

  public String getFrom() {
    return from;
  }

  public EmailEntityApi setFrom(final String from) {
    this.from = from;
    return this;
  }

  public DetectionAlertFilterRecipients getTo() {
    return to;
  }

  public EmailEntityApi setTo(
      final DetectionAlertFilterRecipients to) {
    this.to = to;
    return this;
  }

  public String getSubject() {
    return subject;
  }

  public EmailEntityApi setSubject(final String subject) {
    this.subject = subject;
    return this;
  }

  public String getHtmlContent() {
    return htmlContent;
  }

  public EmailEntityApi setHtmlContent(final String htmlContent) {
    this.htmlContent = htmlContent;
    return this;
  }

  public String getSnapshotPath() {
    return snapshotPath;
  }

  public EmailEntityApi setSnapshotPath(final String snapshotPath) {
    this.snapshotPath = snapshotPath;
    return this;
  }
}
