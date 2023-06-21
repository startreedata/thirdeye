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
import { Box } from "@material-ui/core";
import { isEqual } from "lodash";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import {
    SkeletonV1,
    useNotificationProviderV1,
} from "../../../../platform/components";
import { ActionStatus } from "../../../../rest/actions.interfaces";
import {
    useGetAlertInsight,
    useGetEvaluation,
} from "../../../../rest/alerts/alerts.actions";
import {
    AlertEvaluation,
    EditableAlert,
} from "../../../../rest/dto/alert.interfaces";
import { DetectionEvaluation } from "../../../../rest/dto/detection.interfaces";
import {
    createAlertEvaluation,
    determineTimezoneFromAlertInEvaluation,
    extractDetectionEvaluation,
    shouldHideTimeInDatetimeFormat,
} from "../../../../utils/alerts/alerts.util";
import { generateNameForDetectionResult } from "../../../../utils/enumeration-items/enumeration-items.util";
import { notifyIfErrors } from "../../../../utils/notifications/notifications.util";
import { ChartContent } from "../../../alert-wizard-v3/preview-chart/chart-content/chart-content.component";
import { PreviewChartHeader } from "../../../alert-wizard-v3/preview-chart/header/preview-chart-header.component";
import { NoDataIndicator } from "../../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { generateChartOptionsForAlert } from "../../../rca/anomaly-time-series-card/anomaly-time-series-card.utils";
import { TimeRangeQueryStringKey } from "../../../time-range/time-range-provider/time-range-provider.interfaces";
import { TimeSeriesChartProps } from "../../../visualizations/time-series-chart/time-series-chart.interfaces";
import { PreviewChartProps } from "./preview-chart.interfaces";

export const PreviewChart: FunctionComponent<PreviewChartProps> = ({
    alert,
    onChartDataLoadSuccess,
    hideCallToActionPrompt,
    onAlertPropertyChange,
}) => {
    const { t } = useTranslation();
    const [searchParams, setSearchParams] = useSearchParams();
    const [startTime, endTime] = useMemo(
        () => [
            Number(searchParams.get(TimeRangeQueryStringKey.START_TIME)),
            Number(searchParams.get(TimeRangeQueryStringKey.END_TIME)),
        ],
        [searchParams]
    );
    const { notify } = useNotificationProviderV1();
    const [timeSeriesOptions, setTimeSeriesOptions] =
        useState<TimeSeriesChartProps>();
    const [detectionEvaluations, setDetectionEvaluations] =
        useState<DetectionEvaluation[]>();
    const [selectedEvaluationToDisplay, setSelectedEvaluationToDisplay] =
        useState<string>("");
    const [alertForCurrentEvaluation, setAlertForCurrentEvaluation] =
        useState<EditableAlert>();

    const {
        evaluation,
        getEvaluation,
        errorMessages: getEvaluationRequestErrors,
        status: getEvaluationStatus,
    } = useGetEvaluation();

    const {
        alertInsight,
        getAlertInsight,
        status: getAlertInsightStatus,
    } = useGetAlertInsight();

    const fetchAlertEvaluation = async (
        start: number,
        end: number
    ): Promise<void> => {
        const copiedAlert = { ...alert };
        delete copiedAlert.id;
        const fetchedAlertEvaluation = await getEvaluation(
            createAlertEvaluation(copiedAlert, start, end)
        );

        setAlertForCurrentEvaluation(alert);

        if (fetchedAlertEvaluation === undefined) {
            setDetectionEvaluations(undefined);

            return;
        }

        const evaluations = extractDetectionEvaluation(
            fetchedAlertEvaluation as AlertEvaluation
        );

        // Call the callback function if its passed
        onChartDataLoadSuccess && onChartDataLoadSuccess();

        if (evaluations.length === 1) {
            setSelectedEvaluationToDisplay(
                generateNameForDetectionResult(evaluations[0])
            );
        } else if (evaluations.length > 1) {
            // Reset what's chosen if the current selected is not in the data
            if (
                evaluations.find(
                    (evaluation) =>
                        selectedEvaluationToDisplay ===
                        generateNameForDetectionResult(evaluation)
                ) === undefined
            ) {
                setSelectedEvaluationToDisplay(
                    generateNameForDetectionResult(evaluations[0])
                );
            }
        }

        setDetectionEvaluations(evaluations);
    };

    useEffect(() => {
        notifyIfErrors(
            getEvaluationStatus,
            getEvaluationRequestErrors,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.chart-data"),
            })
        );
    }, [getEvaluationStatus]);

    useEffect(() => {
        if (detectionEvaluations) {
            const detectionEvaluation = detectionEvaluations.find(
                (evaluation) =>
                    generateNameForDetectionResult(evaluation) ===
                    selectedEvaluationToDisplay
            );

            if (!detectionEvaluation) {
                return;
            }

            const timeseriesConfiguration = generateChartOptionsForAlert(
                detectionEvaluation,
                detectionEvaluation.anomalies,
                t,
                undefined,
                determineTimezoneFromAlertInEvaluation(
                    evaluation?.alert.template
                ),
                shouldHideTimeInDatetimeFormat(evaluation?.alert.template)
            );

            timeseriesConfiguration.brush = false;
            timeseriesConfiguration.zoom = true;

            setTimeSeriesOptions(timeseriesConfiguration);
        }
    }, [detectionEvaluations, selectedEvaluationToDisplay]);

    useEffect(() => {
        // If alert changes, reset the evaluation data
        setDetectionEvaluations(undefined);
    }, [alert]);

    const handleAutoRangeClick = (): void => {
        getAlertInsight({ alert }).then(
            (insights) => {
                if (insights) {
                    searchParams.set(
                        TimeRangeQueryStringKey.START_TIME,
                        insights.defaultStartTime.toString()
                    );
                    searchParams.set(
                        TimeRangeQueryStringKey.END_TIME,
                        insights.defaultEndTime.toString()
                    );
                    setSearchParams(searchParams, { replace: true });
                    fetchAlertEvaluation(
                        insights.defaultStartTime,
                        insights.defaultEndTime
                    );
                } else {
                    fetchAlertEvaluation(startTime, endTime);
                }
            },
            () => {
                // If API fails use current start and end
                fetchAlertEvaluation(startTime, endTime);
            }
        );
    };

    return (
        <>
            {/** Header Section **/}
            <PreviewChartHeader
                alertInsight={alertInsight}
                getEvaluationStatus={getEvaluationStatus}
                showConfigurationNotReflective={
                    !isEqual(alertForCurrentEvaluation, alert) &&
                    !!timeSeriesOptions
                }
                onReloadClick={handleAutoRangeClick}
                onStartEndChange={(newStart, newEnd) => {
                    fetchAlertEvaluation(newStart, newEnd);
                }}
            />
            <LoadingErrorStateSwitch
                errorState={
                    <Box pb={20} pt={20}>
                        <NoDataIndicator>
                            {t(
                                "message.experienced-issues-loading-chart-data-try"
                            )}
                        </NoDataIndicator>
                    </Box>
                }
                isError={getEvaluationStatus === ActionStatus.Error}
                isLoading={
                    getEvaluationStatus === ActionStatus.Working ||
                    getAlertInsightStatus === ActionStatus.Working
                }
                loadingState={
                    <Box paddingTop={1}>
                        <SkeletonV1
                            animation="pulse"
                            delayInMS={0}
                            height={300}
                            variant="rect"
                        />
                    </Box>
                }
            >
                <ChartContent
                    showLoadButton
                    alert={alert}
                    alertEvaluation={evaluation}
                    hideCallToActionPrompt={hideCallToActionPrompt}
                    onAlertPropertyChange={onAlertPropertyChange}
                    onReloadClick={handleAutoRangeClick}
                />
            </LoadingErrorStateSwitch>
        </>
    );
};
