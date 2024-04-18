/*
 * Copyright 2024 StarTree Inc
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

  ERR_UNAUTHENTICATED("User authentication failed!"),
  ERR_ALERT_PIPELINE_EXECUTION("Pipeline Failed! Error: %s"),
  ERR_AUTH_SERVER_NOT_RESPONDING("Auth server is not responding. Auth Server URL : %s"),
  ERR_CONFIG("Configuration Error! %s"),
  ERR_CRON_INVALID("Failed to parse cron expression: %s"),
  ERR_CRON_FREQUENCY_TOO_HIGH("Invalid cron: %s. This cron can trigger up to %s times per minute. This is not allowed. Please use a cron that is triggered at most %s times per minute."),
  ERR_DATASET_NOT_FOUND("Dataset not found: %s. Dataset is not onboarded?"),
  ERR_DATASET_NOT_FOUND_IN_NAMESPACE("Dataset not found: %s in namespace: %s. Dataset is not onboarded?"),
  ERR_DATASOURCE_NOT_FOUND("Data Source not found: %s. Data Source is not created?"),
  ERR_DATASOURCE_NOT_LOADED("Data source cannot be instantiated! %s"),
  ERR_DATASOURCE_VALIDATION_FAILED("Data source validation failed! name: %s. %s"),
  ERR_DATA_UNAVAILABLE("Data not available! %s"),
  ERR_DETECTION_INTERVAL_COMPUTATION("Failed to compute detection interval. Error caused by : %s"),
  ERR_DUPLICATE_NAME("Please provide a unique name. '%s' already exists."),
  ERR_DUPLICATE_ENTITY("Entity already exists. %s"),
  ERR_ID_UNEXPECTED_AT_CREATION("ID should be null at creation time."),
  ERR_INVALID_JSON_FORMAT("Invalid json format"),
  ERR_INVALID_QUERY_PARAM_OPERATOR("Invalid operator for query param. Allowed Values:"),
  ERR_MISSING_CONFIGURATION_FIELD("Missing configuration field in alert: %s"),
  ERR_MISSING_ID("ID is null!"),
  ERR_MISSING_NAME("name is null!"),
  ERR_MULTIPLE_DATASETS_FOUND(
      "Multiple datasets found based on the dataset's display name %s, candidates: %s"),
  ERR_MULTIPLE_DATASOURCES_FOUND(
      "Multiple data sources found with the same name: %s"),
  ERR_NOTIFICATION_DISPATCH("%s"),
  ERR_NOT_ENOUGH_DATA_FOR_RCA("Not enough data for RCA algorithm: %s"),
  ERR_OBJECT_DOES_NOT_EXIST("Object does not exist! %s"),
  ERR_OBJECT_UNEXPECTED("Object should be null/empty! %s"),
  ERR_OPERATION_UNSUPPORTED("Operation not supported. %s"),
  ERR_TEMPLATE_MISSING_PROPERTY(
      "Failed to apply templateProperties to template. Missing property: %s"),
  ERR_TIMEOUT("Operation timed out!"),
  ERR_UNEXPECTED_QUERY_PARAM("Unexpected Query Param. Allowed values: %s"),
  ERR_UNKNOWN("%s"),
  ERR_UNKNOWN_RCA_ALGORITHM("Unknown error running the rca algorithm: %s"),
  ERR_CALCITE_FILTERING("Failed running Calcite filtering query with filter: %s"),
  ERR_INVALID_PARAMS_COMPONENTS("Invalid param components: %s for Class %s"),
  ERR_INVALID_DETECTION_REGRESSORS("Invalid regressors configuration. Too few or too many regressors?"),
  ERR_INVALID_SQL("Invalid SQL:\n%s"),
  ERR_NEGATIVE_LIMIT_VALUE("Negative 'limit' value provided."),
  ERR_NEGATIVE_OFFSET_VALUE("Negative 'offset' value provided."),
  ERR_OFFSET_WITHOUT_LIMIT("'offset' value provided without 'limit' value."),

  OK("OK"),
  ;

  final String message;

  ThirdEyeStatus(final String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
