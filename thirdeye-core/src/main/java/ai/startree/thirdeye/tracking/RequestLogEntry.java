/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.tracking;

/**
 * Data source request performance log entry
 */
final class RequestLogEntry {

  final String datasource;
  final String dataset;
  final String metric;
  final String principal;
  final boolean success;
  final long start;
  final long end;
  final Exception exception;

  public RequestLogEntry(String datasource, String dataset, String metric, String principal,
      boolean success, long start, long end,
      Exception exception) {
    this.datasource = datasource;
    this.dataset = dataset;
    this.metric = metric;
    this.principal = principal;
    this.success = success;
    this.start = start;
    this.end = end;
    this.exception = exception;
  }

  public String getDatasource() {
    return datasource;
  }

  public String getDataset() {
    return dataset;
  }

  public String getMetric() {
    return metric;
  }

  public String getPrincipal() {
    return principal;
  }

  public boolean isSuccess() {
    return success;
  }

  public long getStart() {
    return start;
  }

  public long getEnd() {
    return end;
  }

  public Exception getException() {
    return exception;
  }
}
