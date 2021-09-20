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

package org.apache.pinot.thirdeye.rootcause.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Config class for RCA's yml config
 * Maintain a list of configs for each external event data provider
 * Maintain a list of configs for each external pipeline using this config
 */
public class RCAConfiguration {

  private Map<String, List<PipelineConfiguration>> frameworks = new HashMap<>();
  private List<String> formatters = Collections.emptyList();
  private int parallelism = 1;

  public Map<String, List<PipelineConfiguration>> getFrameworks() {
    return frameworks;
  }

  public RCAConfiguration setFrameworks(
      final Map<String, List<PipelineConfiguration>> frameworks) {
    this.frameworks = frameworks;
    return this;
  }

  public int getParallelism() {
    return parallelism;
  }

  public RCAConfiguration setParallelism(final int parallelism) {
    this.parallelism = parallelism;
    return this;
  }

  public List<String> getFormatters() {
    return formatters;
  }

  public RCAConfiguration setFormatters(final List<String> formatters) {
    this.formatters = formatters;
    return this;
  }
}
