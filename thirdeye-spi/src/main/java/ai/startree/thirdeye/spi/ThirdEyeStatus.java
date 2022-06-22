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
package ai.startree.thirdeye.spi;

public enum ThirdEyeStatus {

  ERR_DATASOURCE_NOT_FOUND("Data Source not found! %s"),
  ERR_DATASOURCE_UNREACHABLE("Data source validation failed!"),
  ERR_DATASOURCE_NOT_LOADED("Data source cannot be instantiated! %s"),
  ERR_DATASET_NOT_FOUND("Dataset not found: %s"),
  ERR_MULTIPLE_DATASETS_FOUND(
      "Multiple datasets found based on the dataset's display name %s, candidates: %s"),
  ERR_DATA_UNAVAILABLE("Data not available! %s"),
  ERR_CRON_INVALID("Failed to parse cron expression: %s"),
  ERR_MISSING_CONFIGURATION_FIELD("Missing configuration field in alert: %s"),
  ERR_TEMPLATE_MISSING_PROPERTY("Failed to apply templateProperties to template. Missing property: %s"),
  ERR_DETECTION_INTERVAL_COMPUTATION("Failed to compute detection interval. Error caused by : %s"),
  ERR_ALERT_PIPELINE_EXECUTION("Failed to run alert pipeline. Error caused by : %s"),
  ERR_DUPLICATE_NAME("Name must be unique!"),
  ERR_MISSING_ID("ID is null!"),
  ERR_MISSING_NAME("name is null!"),
  ERR_NOTIFICATION_DISPATCH("%s"),
  ERR_UNEXPECTED_QUERY_PARAM("Unexpected Query Param. Allowed values: %s"),
  ERR_ID_UNEXPECTED_AT_CREATION("ID should be null at creation time."),
  ERR_INVALID_JSON_FORMAT("Invalid json format"),
  ERR_INVALID_QUERY_PARAM_OPERATOR("Invalid operator for query param. Allowed Values:"),
  ERR_OBJECT_UNEXPECTED("Object should be null/empty! %s"),
  ERR_OBJECT_DOES_NOT_EXIST("Object does not exist! %s"),
  ERR_OPERATION_UNSUPPORTED("Operation is not supported!"),
  ERR_CONFIG("Configuration Error! %s"),
  ERR_TIMEOUT("Operation timed out!"),
  ERR_UNKNOWN("%s"),
  HEALTHY("Healthy"),
  ERR_UNKNOWN_RCA_ALGORITHM("Unknown error running the rca algorithm: %s"),
  ERR_AUTH_SERVER_NOT_RESPONDING("Auth server is not responding. Auth Server URL : %s"),
  UNHEALTHY("Unhealthy %s : %s");

  final String message;

  ThirdEyeStatus(final String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
