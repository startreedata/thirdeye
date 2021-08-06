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

package org.apache.pinot.thirdeye.datasource.pinot;

import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.List;
import org.apache.pinot.thirdeye.datasource.RelationalQuery;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.apache.pinot.thirdeye.spi.detection.TimeSpec;

public class PinotQuery extends RelationalQuery {

  private String tableName;
  private String metric;
  private List<String> groupByKeys;
  private TimeGranularity granularity;
  private TimeSpec timeSpec;

  public PinotQuery(String query, String tableName, String metric, List<String> groupByKeys, TimeGranularity granularity, TimeSpec timeSpec) {
    super(query);
    this.tableName = tableName;
    this.metric = metric;
    this.groupByKeys = groupByKeys;
    this.granularity = granularity;
    this.timeSpec = timeSpec;
  }

  public PinotQuery(final String query, final String tableName) {
    super(query);
    this.tableName = tableName;
    this.metric = "";
    this.groupByKeys = new ArrayList<>();
    this.granularity = new TimeGranularity();
    this.timeSpec = new TimeSpec();
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getMetric() {
    return metric;
  }

  public PinotQuery setMetric(final String metric) {
    this.metric = metric;
    return this;
  }

  public List<String> getGroupByKeys() {
    return groupByKeys;
  }

  public PinotQuery setGroupByKeys(final List<String> groupByKeys) {
    this.groupByKeys = groupByKeys;
    return this;
  }

  public TimeGranularity getGranularity() {
    return granularity;
  }

  public PinotQuery setGranularity(
      final TimeGranularity granularity) {
    this.granularity = granularity;
    return this;
  }

  public TimeSpec getTimeSpec() {
    return timeSpec;
  }

  public PinotQuery setTimeSpec(final TimeSpec timeSpec) {
    this.timeSpec = timeSpec;
    return this;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("query", query)
        .add("tableName", tableName)
        .add("metric", metric)
        .add("groupByKeys", groupByKeys)
        .add("granularity", granularity)
        .add("timeSpec", timeSpec)
        .toString();
  }
}
