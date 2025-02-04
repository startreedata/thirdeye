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
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
// external
import React, { useEffect, useMemo } from "react";
import { Autocomplete } from "@material-ui/lab";
import {
    Box,
    CircularProgress,
    TextField,
    Typography,
} from "@material-ui/core";
import { useTranslation } from "react-i18next";
import { cloneDeep, isEmpty, toLower } from "lodash";
import { Cancel, CheckCircle } from "@material-ui/icons";

// app components
import { InputSectionV2 } from "../../../../../components/form-basics/input-section-v2/input-section-v2.component";
import { AvailableAlgorithmOption } from "../../../../../components/alert-wizard-v3/alert-type-selection/alert-type-selection.interfaces";
import { useNotificationProviderV1 } from "../../../../../platform/components";

// utils
import {
    generateAvailableAlgorithmOptions,
    generateAvailableAlgorithmOptionsForRecommendations,
} from "../../../../../components/alert-wizard-v3/alert-type-selection/alert-type-selection.utils";
import { getWorkingAlert } from "../../../utils";
import { notifyIfErrors } from "../../../../../utils/notifications/notifications.util";

// styles
import { graphOptionsStyles } from "./styles";

// state
import { useCreateAlertStore } from "../../../hooks/state";

// types
import { AlertTemplate } from "../../../../../rest/dto/alert-template.interfaces";
import {
    AnomalyDetectionOptions,
    SelectDimensionsOptions,
} from "../../../../../rest/dto/metric.interfaces";
import { EditableAlert } from "../../../../../rest/dto/alert.interfaces";

// apis
import { useGetEvaluation } from "../../../../../rest/alerts/alerts.actions";

export const DetectionAlgorithms = (): JSX.Element => {
    const { t } = useTranslation();
    const componentStyles = graphOptionsStyles();
    const {
        alertTemplates,
        alertRecommendations,
        anomalyDetectionType,
        workingAlert,
        setWorkingAlert,
        selectedMetric,
        selectedDataset,
        aggregationFunction,
        granularity,
        queryFilters,
        enumeratorQuery,
        selectedTimeRange,
        selectedEnumerationItems,
        editedDatasourceFieldValue,
        setWorkingAlertEvaluation,
        multipleDimensionEnumeratorType,
        selectedDetectionAlgorithm,
        setSelectedDetectionAlgorithm,
        apiState,
        setApiState,
    } = useCreateAlertStore();
    const isGetAlertRecommendationLoading = false;
    const { evaluation, getEvaluation, status, errorMessages } =
        useGetEvaluation();
    useEffect(() => {
        setApiState({
            ...apiState,
            evaluationState: {
                ...apiState.evaluationState,
                status,
            },
        });
    }, [status]);

    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        evaluation && setWorkingAlertEvaluation(evaluation);
    }, [evaluation]);

    useEffect(() => {
        notifyIfErrors(
            status,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.alert-evaluation"),
            })
        );
    }, [status]);

    const alertTemplateOptions = useMemo(() => {
        if (!isEmpty(alertTemplates)) {
            const isMultiDimensionAlert =
                anomalyDetectionType === AnomalyDetectionOptions.COMPOSITE;
            const availableAlgorithmOptions = generateAvailableAlgorithmOptions(
                alertTemplates!.map((a: AlertTemplate) => a.name)
            ).filter((option) =>
                isMultiDimensionAlert
                    ? option.hasMultidimension
                    : option.hasAlertTemplate
            );
            const recommendedAlgorithmOptions =
                generateAvailableAlgorithmOptionsForRecommendations(
                    alertRecommendations ?? [],
                    isMultiDimensionAlert
                );

            return [
                ...recommendedAlgorithmOptions,
                ...availableAlgorithmOptions,
            ];
        }

        return [];
    }, [alertTemplates, alertRecommendations]);

    const handleAlgorithmChange = (
        algorithm: AvailableAlgorithmOption
    ): void => {
        setSelectedDetectionAlgorithm(algorithm);
        const isMultiDimensionAlert =
            anomalyDetectionType === AnomalyDetectionOptions.COMPOSITE;
        const recommendedTemplate = alertRecommendations?.find(
            (rec, index) =>
                `${rec.alert.template?.name}-${index}` ===
                algorithm?.recommendationId
        );
        const isEnumeratorQuery =
            multipleDimensionEnumeratorType ===
            SelectDimensionsOptions.ENUMERATORS;
        let templateName = algorithm?.algorithmOption?.alertTemplate;
        if (isMultiDimensionAlert) {
            if (isEnumeratorQuery) {
                templateName =
                    algorithm?.algorithmOption?.alertTemplateForMultidimension?.replace(
                        "-dx",
                        "-query-dx"
                    );
            } else {
                templateName =
                    algorithm?.algorithmOption?.alertTemplateForMultidimension;
            }
        }
        const clonedAlert = cloneDeep(workingAlert) as Partial<EditableAlert>;
        clonedAlert.template!.name = templateName;
        if (recommendedTemplate) {
            clonedAlert.templateProperties = {
                ...recommendedTemplate.alert.templateProperties,
            };
            const start = selectedTimeRange?.startTime;
            const end = selectedTimeRange?.endTime;
            if (start && end) {
                getEvaluation({
                    start,
                    end,
                    alert: clonedAlert as EditableAlert,
                });
            }
        } else {
            let isCustomMetrics = false;
            if (selectedMetric === t("label.custom-metric-aggregation")) {
                isCustomMetrics = true;
            }
            const workingAlertUpdated = getWorkingAlert({
                templateName,
                metric: isCustomMetrics
                    ? editedDatasourceFieldValue
                    : (selectedMetric as string),
                dataset: selectedDataset!.dataset!,
                aggregationFunction: aggregationFunction || "",
                granularity: granularity!,
                queryFilters,
                dxAlertProps: {
                    queryFilters: queryFilters,
                    enumerationItems: selectedEnumerationItems,
                    enumeratorQuery: enumeratorQuery,
                },
                isMultiDimensionAlert,
            });
            clonedAlert.templateProperties = {
                ...workingAlertUpdated.templateProperties,
            };
        }
        setWorkingAlert(clonedAlert);
    };

    return (
        <InputSectionV2
            inputComponent={
                <Autocomplete<AvailableAlgorithmOption>
                    fullWidth
                    className={
                        isGetAlertRecommendationLoading
                            ? componentStyles.animatedBorder
                            : ""
                    }
                    data-testId="datasource-select"
                    getOptionLabel={(option) =>
                        option.algorithmOption.title as string
                    }
                    groupBy={(option) => option.recommendationLabel || ""}
                    noOptionsText={t("message.no-options-available-entity", {
                        entity: t("label.dataset"),
                    })}
                    options={alertTemplateOptions || []}
                    renderInput={(params) => (
                        <TextField
                            {...params}
                            InputProps={{ ...params.InputProps }}
                            placeholder={t("message.select-algorithm")}
                            variant="outlined"
                        />
                    )}
                    renderOption={(
                        option: AvailableAlgorithmOption
                    ): JSX.Element => {
                        return (
                            <Box
                                data-testId={`${toLower(
                                    option.algorithmOption.title
                                )}-datasource-option`}
                            >
                                <Typography variant="h6">
                                    {option.algorithmOption.title}
                                </Typography>
                            </Box>
                        );
                    }}
                    value={selectedDetectionAlgorithm}
                    onChange={(_, value) => {
                        if (!value) {
                            return;
                        }
                        handleAlgorithmChange(value);
                    }}
                />
            }
            labelComponent={
                <Box className={componentStyles.recommendedAlgorithmContainer}>
                    <Typography
                        className={componentStyles.recommendedAlgorithmText}
                        variant="caption"
                    >
                        {t("label.detection-algorithm")}
                    </Typography>
                    <Box
                        className={
                            componentStyles.detectionRecommendationsContainer
                        }
                    >
                        {isGetAlertRecommendationLoading ? (
                            <Box display="flex">
                                <CircularProgress color="primary" size={15} />
                                <Typography
                                    style={{ marginLeft: "4px" }}
                                    variant="caption"
                                >
                                    {t(
                                        "label.computing-detection-recommendations"
                                    )}
                                </Typography>
                            </Box>
                        ) : !isEmpty(alertRecommendations) ? (
                            <Box alignItems="center" display="flex">
                                <CheckCircle
                                    className={componentStyles.checkCircleIcon}
                                />
                                <Typography
                                    className={
                                        componentStyles.detectionRecommendationsReadyText
                                    }
                                    variant="caption"
                                >
                                    {t("label.detection-recommendations-ready")}
                                </Typography>
                            </Box>
                        ) : (
                            <Box alignItems="center" display="flex">
                                <Cancel
                                    className={componentStyles.cancelIcon}
                                />
                                <Typography
                                    className={
                                        componentStyles.detectionRecommendationsFailedText
                                    }
                                    variant="caption"
                                >
                                    {t(
                                        "errors.could-not-compute-detection-recommendations"
                                    )}
                                </Typography>
                            </Box>
                        )}
                    </Box>
                </Box>
            }
        />
    );
};
