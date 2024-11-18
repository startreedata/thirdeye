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
import React, { useEffect, useState } from "react";
import { Box, CircularProgress, Grid, Typography } from "@material-ui/core";
import { useTranslation } from "react-i18next";

// app components
import { RadioSection } from "../../../../../components/form-basics/radio-section-v2/radio-section.component";
import { RadioSectionOptions } from "../../../../../components/form-basics/radio-section-v2/radio-section.interfaces";
import {
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../../../../platform/components";

// types
import { AnomalyDetectionOptions } from "../../../../../rest/dto/metric.interfaces";
import {
    AlertInsight,
    EditableAlert,
} from "../../../../../rest/dto/alert.interfaces";

// state
import { useCreateAlertStore } from "../../../hooks/state";

// apis
import {
    getAlertInsight,
    getAlertRecommendation,
} from "../../../../../rest/alerts/alerts.rest";
import { useGetEvaluation } from "../../../../../rest/alerts/alerts.actions";

// sections
import { MultipleDimensionView } from "./multiple-dimension";

// utils
import { defaultStartingAlert, getWorkingAlert } from "../../../utils";
import { notifyIfErrors } from "../../../../../utils/notifications/notifications.util";

export const SelectDetection = (): JSX.Element => {
    const { t } = useTranslation();
    const {
        selectedMetric,
        selectedDataset,
        editedDatasourceFieldValue,
        aggregationFunction,
        granularity,
        queryFilters,
        anomalyDetectionType,
        setAnomalyDetectionType,
        workingAlert,
        setWorkingAlert,
        alertInsight,
        setAlertInsight,
        setSelectedTimeRange,
        setWorkingAlertEvaluation,
        setAlertRecommendations,
        apiState,
        setApiState,
    } = useCreateAlertStore();
    const { notify } = useNotificationProviderV1();
    const [alertInsightLoading] = useState(false);

    const { getEvaluation, evaluation, status, errorMessages } =
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

    const handleAnomalyDetectionChange = async (
        item: string
    ): Promise<void> => {
        setAnomalyDetectionType(item);
        let updatedAlert = workingAlert;
        if (item === AnomalyDetectionOptions.SINGLE) {
            let updatedAlertInsight: AlertInsight | null = alertInsight;
            if (anomalyDetectionType && item !== anomalyDetectionType) {
                let isCustomMetrics = false;
                if (selectedMetric === t("label.custom-metric-aggregation")) {
                    isCustomMetrics = true;
                }
                updatedAlert = getWorkingAlert({
                    templateName: defaultStartingAlert.template?.name,
                    metric: isCustomMetrics
                        ? editedDatasourceFieldValue
                        : (selectedMetric as string),
                    dataset: selectedDataset!.dataset!,
                    aggregationFunction: aggregationFunction || "",
                    granularity: granularity!,
                    isMultiDimensionAlert: false,
                    queryFilters,
                    min: 0,
                    max: 1,
                });
                setWorkingAlert(updatedAlert);
                updatedAlertInsight = await getAlertInsight({
                    alert: updatedAlert as EditableAlert,
                });
                if (updatedAlertInsight) {
                    setAlertInsight(updatedAlertInsight);
                    setSelectedTimeRange({
                        startTime: updatedAlertInsight.datasetStartTime,
                        endTime: updatedAlertInsight.datasetEndTime,
                    });
                }
            }
            getAlertRecommendation(workingAlert as EditableAlert)
                .then((recommendations) => {
                    setAlertRecommendations(recommendations);
                })
                .catch(() => {
                    notify(
                        NotificationTypeV1.Error,
                        t("errors.could-not-compute-detection-recommendations")
                    );
                });
            const start = updatedAlertInsight?.defaultStartTime;
            const end = updatedAlertInsight?.defaultEndTime;
            if (start && end) {
                getEvaluation({
                    start,
                    end,
                    alert: updatedAlert as EditableAlert,
                });
            }
        }
    };

    useEffect(() => {
        evaluation && setWorkingAlertEvaluation(evaluation);
    }, [evaluation]);

    const getAnomalyDetectionOptions = (
        values: Array<string>
    ): RadioSectionOptions[] => {
        const options: RadioSectionOptions[] = [];
        values.map((item) =>
            options.push({
                value: item,
                label: item,
                disabled: !alertInsight,
                onClick: () =>
                    alertInsight && handleAnomalyDetectionChange(item),
                tooltipText: item,
            })
        );

        return options;
    };

    return (
        <>
            <Grid item xs={12}>
                <RadioSection
                    preventDoubleTrigger
                    label={t("label.detection-type")}
                    options={getAnomalyDetectionOptions([
                        AnomalyDetectionOptions.SINGLE,
                        AnomalyDetectionOptions.COMPOSITE,
                    ])}
                    value={anomalyDetectionType || undefined}
                />
                {alertInsightLoading && (
                    <Box alignItems="center" display="flex">
                        <CircularProgress color="primary" size={12} />
                        <Typography
                            style={{ marginLeft: "4px" }}
                            variant="caption"
                        >
                            {t("label.loading-insights")}
                        </Typography>
                    </Box>
                )}
            </Grid>
            {anomalyDetectionType === AnomalyDetectionOptions.COMPOSITE && (
                <MultipleDimensionView />
            )}
        </>
    );
};
