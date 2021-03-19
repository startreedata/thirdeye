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

package org.apache.pinot.thirdeye.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import java.util.Map;

public class ThirdEyeConfiguration extends Configuration {

  /**
   * Root directory for all other configuration
   */
  private String rootDir = "";
  private String dashboardHost;

  @JsonProperty("swagger")
  public SwaggerBundleConfiguration swaggerBundleConfiguration;

  private Map<String, Map<String, Object>> alerterConfigurations;

  private String phantomJsPath = "";
  private String failureFromAddress;
  private String failureToAddress;

  public String getRootDir() {
    return rootDir;
  }

  public ThirdEyeConfiguration setRootDir(final String rootDir) {
    this.rootDir = rootDir;
    return this;
  }

  public String getPhantomJsPath() {
    return phantomJsPath;
  }

  public void setPhantomJsPath(String phantomJsPath) {
    this.phantomJsPath = phantomJsPath;
  }

  public String getDashboardHost() {
    return dashboardHost;
  }

  public void setDashboardHost(String dashboardHost) {
    this.dashboardHost = dashboardHost;
  }

  public String getFailureFromAddress() {
    return failureFromAddress;
  }

  public void setFailureFromAddress(String failureFromAddress) {
    this.failureFromAddress = failureFromAddress;
  }

  public String getFailureToAddress() {
    return failureToAddress;
  }

  public void setFailureToAddress(String failureToAddress) {
    this.failureToAddress = failureToAddress;
  }

  public Map<String, Map<String, Object>> getAlerterConfiguration() {
    return alerterConfigurations;
  }

  public void setAlerterConfiguration(Map<String, Map<String, Object>> alerterConfigurations) {
    this.alerterConfigurations = alerterConfigurations;
  }
}
