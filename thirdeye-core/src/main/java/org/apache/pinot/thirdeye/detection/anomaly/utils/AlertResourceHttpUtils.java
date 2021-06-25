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

package org.apache.pinot.thirdeye.detection.anomaly.utils;

import java.io.IOException;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;

/**
 * Utility classes for calling detector endpoints to execute/schedule jobs
 */
public class AlertResourceHttpUtils extends AbstractResourceHttpUtils {

  private static final String ALERT_JOB_ENDPOINT = "/api/alert-job/";
  private static final String ADHOC = "/ad-hoc";

  public AlertResourceHttpUtils(String alertHost, int alertPort, String authToken) {
    super(new HttpHost(alertHost, alertPort));
    addAuthenticationCookie(authToken);
  }

  public String enableEmailConfiguration(String id) throws IOException {
    HttpPost req = new HttpPost(ALERT_JOB_ENDPOINT + id);
    return callJobEndpoint(req);
  }

  public String disableEmailConfiguration(String id) throws IOException {
    HttpDelete req = new HttpDelete(ALERT_JOB_ENDPOINT + id);
    return callJobEndpoint(req);
  }

  public String runAdhocEmailConfiguration(String id, String startTimeIso, String endTimeIso)
      throws IOException {
    HttpPost req = new HttpPost(
        ALERT_JOB_ENDPOINT + id + ADHOC + "?start=" + startTimeIso + "&end=" + endTimeIso);
    return callJobEndpoint(req);
  }
}
