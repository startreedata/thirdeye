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

  ERR_UNAUTHENTICATED(401,"User authentication failed!"),
  ERR_ALERT_PIPELINE_EXECUTION(null,"Pipeline Failed! Error: %s"),
  ERR_ALERT_INSIGHTS(500,"Failed to get insights from alert configuration. Error: %s"),
  ERR_AUTH_SERVER_NOT_RESPONDING(500,"Auth server is not responding. Auth Server URL : %s"),
  ERR_CONFIG(null,"Configuration Error! %s"),
  ERR_CRON_INVALID(null,"Failed to parse cron expression: %s"),
  ERR_CRON_FREQUENCY_TOO_HIGH(
      null,"Invalid cron: %s. This cron can trigger up to %s times per minute. This is not allowed. Please use a cron that is triggered at most %s times per minute."),
  ERR_DATASET_NOT_FOUND(null,"Dataset not found: %s. Dataset is not onboarded?"),
  ERR_DATASET_NOT_FOUND_IN_NAMESPACE(
      null,"Dataset not found: %s in namespace: %s. Dataset is not onboarded?"),
  ERR_DATASOURCE_NOT_FOUND(null,"Data Source not found: %s. Data Source is not created?"),
  ERR_DATASOURCE_NOT_FOUND_IN_NAMESPACE(
      null,"Data Source not found: %s in namespace %s. Data Source is not created?"),
  ERR_DATASOURCE_NOT_LOADED(null,"Data source cannot be instantiated! %s"),
  ERR_DATASOURCE_VALIDATION_FAILED(null,"Data source validation failed! name or id: %s. %s"),
  ERR_DATA_UNAVAILABLE(null,"Data not available! %s"),
  ERR_DETECTION_INTERVAL_COMPUTATION(null,"Failed to compute detection interval. Error caused by : %s"),
  ERR_DUPLICATE_NAME(null,"Please provide a unique name. '%s' already exists."),
  ERR_DUPLICATE_ENTITY(null,"Entity already exists. %s"),
  ERR_ID_UNEXPECTED_AT_CREATION(null,"ID should be null at creation time."),
  ERR_INVALID_JSON_FORMAT(null,"Invalid json format"),
  ERR_INVALID_QUERY_PARAM_OPERATOR(null,"Invalid operator for query param. Allowed Values:"),
  ERR_MISSING_CONFIGURATION_FIELD(null,"Missing configuration field in alert: %s"),
  ERR_MISSING_ID(null,"ID is null!"),
  ERR_MISSING_NAME(null,"name is null!"),
  ERR_MULTIPLE_DATASETS_FOUND(
      null,"Multiple datasets found based on the dataset's display name %s, candidates: %s"),
  ERR_MULTIPLE_DATASOURCES_FOUND(
      null,"Multiple data sources found with the same name: %s"),
  ERR_NAMESPACE_CONFIGURATION_VALIDATION_FAILED(
      null,"Namespace Configuration validation failed! namespace: %s. %s"),
  ERR_NOTIFICATION_DISPATCH(null,"%s"),
  ERR_NOT_ENOUGH_DATA_FOR_RCA(null,"Not enough data for RCA algorithm: %s"),
  ERR_OBJECT_DOES_NOT_EXIST(null,"Object does not exist! %s"),
  ERR_OBJECT_UNEXPECTED(null,"Object should be null/empty! %s"),
  ERR_OPERATION_UNSUPPORTED(null,"Operation not supported. %s"),
  ERR_TEMPLATE_MISSING_PROPERTY(
      null,"Failed to apply templateProperties to template. Missing property: %s"),
  ERR_TIMEOUT(500,"Operation timed out."),
  ERR_UNEXPECTED_QUERY_PARAM(400,"Unexpected Query Param. Allowed values: %s"),
  ERR_UNKNOWN(500, "%s"),
  ERR_EXECUTION_RCA_ALGORITHM(null,"RCA Algorithm Execution Failed. Error: %s"),
  ERR_UNKNOWN_RCA_ALGORITHM(null,"Unknown error running the rca algorithm: %s"),
  ERR_CALCITE_FILTERING(null,"Failed running Calcite filtering query with filter: %s"),
  ERR_INVALID_PARAMS_COMPONENTS(null,"Invalid param components: %s for Class %s"),
  ERR_INVALID_DETECTION_REGRESSORS(
      null,"Invalid regressors configuration. Too few or too many regressors?"),
  ERR_INVALID_SQL(null,"Invalid SQL:\n%s"),
  ERR_NEGATIVE_LIMIT_VALUE(null,"Negative 'limit' value provided."),
  ERR_NEGATIVE_OFFSET_VALUE(null,"Negative 'offset' value provided."),
  ERR_OFFSET_WITHOUT_LIMIT(null,"'offset' value provided without 'limit' value."),
  ERR_PINOT_QUERY_QUOTA_EXCEEDED(null,"Pinot Query Quota Exceeded. Error: %s. SQL: %s"),
  ERR_PINOT_QUERY_EXECUTION(null,"Pinot Query Execution Failed. Error: %s. SQL: %s"),
  ERR_DATASOURCE_DEMO_TABLE_CREATION_CONFLICT_ERROR(409,"Failed to create demo table %s. Error: %s"),
  ERR_DATASOURCE_DEMO_TABLE_CREATION_UNKNOWN_ERROR(500,"Failed to create demo table %s. Error: %s"),

  OK(null,"OK"),
  ;

  /**
   * Plugins and module don't have to be aware of the server context when they return exception with particular ThirdEyeStatus.  
   * It is the responsibility of the server to translate the ThirdEyeExceptions in HTTP exception based on the ThirdEyeStatus.  
   * This is optional and the server may decide to return another type of exception/status code based on its context.
   */
  final Integer recommendedStatusCode;
  final String message;

  ThirdEyeStatus(final Integer recommendedStatusCode, final String message) {
    this.recommendedStatusCode = recommendedStatusCode;
    this.message = message;
  }

  public Integer getRecommendedStatusCode() {
    return recommendedStatusCode;
  }

  public String getMessage() {
    return message;
  }
}
