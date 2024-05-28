/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { AxiosError } from "axios";
import i18next from "i18next";
import { get } from "lodash";
import { ErrorMessage } from "../../platform/components/notification-provider-v1/notification-provider-v1/notification-provider-v1.interfaces";

export enum ErrorCode {
    ERR_UNAUTHENTICATED = "ERR_UNAUTHENTICATED",
    ERR_ALERT_PIPELINE_EXECUTION = "ERR_ALERT_PIPELINE_EXECUTION",
    ERR_AUTH_SERVER_NOT_RESPONDING = "ERR_AUTH_SERVER_NOT_RESPONDING",
    ERR_CONFIG = "ERR_CONFIG",
    ERR_CRON_INVALID = "ERR_CRON_INVALID",
    ERR_CRON_FREQUENCY_TOO_HIGH = "ERR_CRON_FREQUENCY_TOO_HIGH",
    ERR_DATASET_NOT_FOUND = "ERR_DATASET_NOT_FOUND",
    ERR_DATASET_NOT_FOUND_IN_NAMESPACE = "ERR_DATASET_NOT_FOUND_IN_NAMESPACE",
    ERR_DATASOURCE_NOT_FOUND = "ERR_DATASOURCE_NOT_FOUND",
    ERR_DATASOURCE_NOT_FOUND_IN_NAMESPACE = "ERR_DATASOURCE_NOT_FOUND_IN_NAMESPACE",
    ERR_DATASOURCE_NOT_LOADED = "ERR_DATASOURCE_NOT_LOADED",
    ERR_DATASOURCE_VALIDATION_FAILED = "ERR_DATASOURCE_VALIDATION_FAILED",
    ERR_DATA_UNAVAILABLE = "ERR_DATA_UNAVAILABLE",
    ERR_DETECTION_INTERVAL_COMPUTATION = "ERR_DETECTION_INTERVAL_COMPUTATION",
    ERR_DUPLICATE_NAME = "ERR_DUPLICATE_NAME",
    ERR_DUPLICATE_ENTITY = "ERR_DUPLICATE_ENTITY",
    ERR_ID_UNEXPECTED_AT_CREATION = "ERR_ID_UNEXPECTED_AT_CREATION",
    ERR_INVALID_JSON_FORMAT = "ERR_INVALID_JSON_FORMAT",
    ERR_INVALID_QUERY_PARAM_OPERATOR = "ERR_INVALID_QUERY_PARAM_OPERATOR",
    ERR_MISSING_CONFIGURATION_FIELD = "ERR_MISSING_CONFIGURATION_FIELD",
    ERR_MISSING_ID = "ERR_MISSING_ID",
    ERR_MISSING_NAME = "ERR_MISSING_NAME",
    ERR_MULTIPLE_DATASETS_FOUND = "ERR_MULTIPLE_DATASETS_FOUND",
    ERR_MULTIPLE_DATASOURCES_FOUND = "ERR_MULTIPLE_DATASOURCES_FOUND",
    ERR_NOTIFICATION_DISPATCH = "ERR_NOTIFICATION_DISPATCH",
    ERR_NOT_ENOUGH_DATA_FOR_RCA = "ERR_NOT_ENOUGH_DATA_FOR_RCA",
    ERR_OBJECT_DOES_NOT_EXIST = "ERR_OBJECT_DOES_NOT_EXIST",
    ERR_OBJECT_UNEXPECTED = "ERR_OBJECT_UNEXPECTED",
    ERR_OPERATION_UNSUPPORTED = "ERR_OPERATION_UNSUPPORTED",
    ERR_TEMPLATE_MISSING_PROPERTY = "ERR_TEMPLATE_MISSING_PROPERTY",
    ERR_TIMEOUT = "ERR_TIMEOUT",
    ERR_UNEXPECTED_QUERY_PARAM = "ERR_UNEXPECTED_QUERY_PARAM",
    ERR_UNKNOWN = "ERR_UNKNOWN",
    ERR_UNKNOWN_RCA_ALGORITHM = "ERR_UNKNOWN_RCA_ALGORITHM",
    ERR_CALCITE_FILTERING = "ERR_CALCITE_FILTERING",
    ERR_INVALID_PARAMS_COMPONENTS = "ERR_INVALID_PARAMS_COMPONENTS",
    ERR_INVALID_DETECTION_REGRESSORS = "ERR_INVALID_DETECTION_REGRESSORS",
    ERR_INVALID_SQL = "ERR_INVALID_SQL",
    ERR_NEGATIVE_LIMIT_VALUE = "ERR_NEGATIVE_LIMIT_VALUE",
    ERR_NEGATIVE_OFFSET_VALUE = "ERR_NEGATIVE_OFFSET_VALUE",
    ERR_OFFSET_WITHOUT_LIMIT = "ERR_OFFSET_WITHOUT_LIMIT",
}

export const getErrorMessages = (error: AxiosError): ErrorMessage[] => {
    const errorCodeMessageMap: Record<ErrorCode, string> = {
        [ErrorCode.ERR_UNAUTHENTICATED]: i18next.t(
            "errors.authentication-failed"
        ),
        [ErrorCode.ERR_ALERT_PIPELINE_EXECUTION]: i18next.t(
            "errors.pipeline-execution-failed"
        ),
        [ErrorCode.ERR_AUTH_SERVER_NOT_RESPONDING]: i18next.t(
            "errors.auth-server-not-responding"
        ),
        [ErrorCode.ERR_CONFIG]: i18next.t("errors.configuration-error"),
        [ErrorCode.ERR_CRON_INVALID]: i18next.t("errors.cron-invalid"),
        [ErrorCode.ERR_CRON_FREQUENCY_TOO_HIGH]: i18next.t(
            "errors.cron-frequency-too-high"
        ),
        [ErrorCode.ERR_DATASET_NOT_FOUND]: i18next.t(
            "errors.dataset-not-found"
        ),
        [ErrorCode.ERR_DATASET_NOT_FOUND_IN_NAMESPACE]: i18next.t(
            "errors.dataset-not-found-in-namespace"
        ),
        [ErrorCode.ERR_DATASOURCE_NOT_FOUND]: i18next.t(
            "errors.datasource-not-found"
        ),
        [ErrorCode.ERR_DATASOURCE_NOT_FOUND_IN_NAMESPACE]: i18next.t(
            "errors.datasource-not-found-in-namespace"
        ),
        [ErrorCode.ERR_DATASOURCE_NOT_LOADED]: i18next.t(
            "errors.datasource-not-loaded"
        ),
        [ErrorCode.ERR_DATASOURCE_VALIDATION_FAILED]: i18next.t(
            "errors.datasource-validation-failed"
        ),
        [ErrorCode.ERR_DATA_UNAVAILABLE]: i18next.t("errors.data-unavailable"),
        [ErrorCode.ERR_DETECTION_INTERVAL_COMPUTATION]: i18next.t(
            "errors.detection-interval-computation"
        ),
        [ErrorCode.ERR_DUPLICATE_NAME]: i18next.t("errors.duplicate-name"),
        [ErrorCode.ERR_DUPLICATE_ENTITY]: i18next.t("errors.duplicate-entity"),
        [ErrorCode.ERR_ID_UNEXPECTED_AT_CREATION]: i18next.t(
            "errors.id-unexpected-at-creation"
        ),
        [ErrorCode.ERR_INVALID_JSON_FORMAT]: i18next.t(
            "errors.invalid-json-format"
        ),
        [ErrorCode.ERR_INVALID_QUERY_PARAM_OPERATOR]: i18next.t(
            "errors.invalid-query-param-operator"
        ),
        [ErrorCode.ERR_MISSING_CONFIGURATION_FIELD]: i18next.t(
            "errors.missing-configuration-field"
        ),
        [ErrorCode.ERR_MISSING_ID]: i18next.t("errors.missing-id"),
        [ErrorCode.ERR_MISSING_NAME]: i18next.t("errors.missing-name"),
        [ErrorCode.ERR_MULTIPLE_DATASETS_FOUND]: i18next.t(
            "errors.multiple-datasets-found"
        ),
        [ErrorCode.ERR_MULTIPLE_DATASOURCES_FOUND]: i18next.t(
            "errors.multiple-datasources-found"
        ),
        [ErrorCode.ERR_NOTIFICATION_DISPATCH]: i18next.t(
            "errors.notification-dispatch"
        ),
        [ErrorCode.ERR_NOT_ENOUGH_DATA_FOR_RCA]: i18next.t(
            "errors.not-enough-data-for-rca"
        ),
        [ErrorCode.ERR_OBJECT_DOES_NOT_EXIST]: i18next.t(
            "errors.object-does-not-exist"
        ),
        [ErrorCode.ERR_OBJECT_UNEXPECTED]: i18next.t(
            "errors.object-unexpected"
        ),
        [ErrorCode.ERR_OPERATION_UNSUPPORTED]: i18next.t(
            "errors.operation-unsupported"
        ),
        [ErrorCode.ERR_TEMPLATE_MISSING_PROPERTY]: i18next.t(
            "errors.template-missing-property"
        ),
        [ErrorCode.ERR_TIMEOUT]: i18next.t("errors.timeout"),
        [ErrorCode.ERR_UNKNOWN]: i18next.t("errors.unknown"),
        [ErrorCode.ERR_UNKNOWN_RCA_ALGORITHM]: i18next.t(
            "errors.unknown-rca-algorithm"
        ),
        [ErrorCode.ERR_CALCITE_FILTERING]: i18next.t(
            "errors.calcite-filtering"
        ),
        [ErrorCode.ERR_INVALID_PARAMS_COMPONENTS]: i18next.t(
            "errors.invalid-params-components"
        ),
        [ErrorCode.ERR_INVALID_DETECTION_REGRESSORS]: i18next.t(
            "errors.invalid-detection-regressors"
        ),
        [ErrorCode.ERR_INVALID_SQL]: i18next.t("errors.invalid-sql"),
        [ErrorCode.ERR_NEGATIVE_LIMIT_VALUE]: i18next.t(
            "errors.negative-limit-value"
        ),
        [ErrorCode.ERR_NEGATIVE_OFFSET_VALUE]: i18next.t(
            "errors.negative-offset-value"
        ),
        [ErrorCode.ERR_OFFSET_WITHOUT_LIMIT]: i18next.t(
            "errors.offset-without-limit"
        ),
        [ErrorCode.ERR_UNEXPECTED_QUERY_PARAM]: i18next.t(
            "errors.unexpected-query-param"
        ),
    };
    const errorMessages: ErrorMessage[] = [];

    // Check if it's 403
    if (error.response?.status === 403) {
        const errorMessage = i18next.t("errors.authorization-error");
        errorMessages.push({
            message: errorMessage,
        });

        return errorMessages;
    }

    const errList = get(error, "response.data.list", []);

    if (Array.isArray(errList)) {
        errList.forEach((err: { code: string; msg: string }) => {
            if (!err.code && !err.msg) {
                return;
            }
            const errorMessage: ErrorMessage = {
                message: i18next.t("errors.error-has-occured", {
                    code: err.code,
                }),
                details: err.msg,
            };

            if (errorCodeMessageMap[err.code as ErrorCode]) {
                errorMessage.message =
                    errorCodeMessageMap[err.code as ErrorCode];
                errorMessage.details = err.msg;
            }

            errorMessages.push(errorMessage);
        });
    }

    return errorMessages;
};
