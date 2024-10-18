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
import {
    createAlertEvaluation,
    extractDetectionEvaluation,
} from "../../../../utils/alerts/alerts.util";
import { generateNameForDetectionResult } from "../../../../utils/enumeration-items/enumeration-items.util";
import { notifyIfErrors } from "../../../../utils/notifications/notifications.util";
import { ChartContentV2 } from "../../../alert-wizard-v3/preview-chart/chart-content-v2/chart-content-v2.component";
import { PreviewChartHeader } from "../../../alert-wizard-v3/preview-chart/header/preview-chart-header-v2.component";
import { NoDataIndicator } from "../../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { TimeRangeQueryStringKey } from "../../../time-range/time-range-provider/time-range-provider.interfaces";
import { TimeSeriesChartProps } from "../../../visualizations/time-series-chart/time-series-chart.interfaces";
import { PreviewChartProps } from "./preview-chart.interfaces";

export const PreviewChart: FunctionComponent<PreviewChartProps> = ({
    alert,
    onChartDataLoadSuccess,
    hideCallToActionPrompt,
    disableReload,
    showTimeRange = true,
    children,
    legendsPlacement,
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
    useState<TimeSeriesChartProps>();

    const [selectedEvaluationToDisplay, setSelectedEvaluationToDisplay] =
        useState<string>("");
    const [alertForCurrentEvaluation, setAlertForCurrentEvaluation] =
        useState<EditableAlert>();
    const [evaluationTimeRange, setEvaluationTimeRange] = useState({
        startTime: startTime,
        endTime: endTime,
    });
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
        /* On the Preview Page we have to defer fetching the data for enumeration items till they
        are in view.
        We only fetch the list of enumeration items without data and anomalies
        by passing {listEnumerationItemsOnly: true} as fetching all the data at once introduces
        significant latency because of the request size.
        Hence we first fetch the evaluations with enumeration items without anomalies and data.
        And then enumerationRow component fetches anomalies and data progresivelly */
        const hasEnumerationItems =
            !!alert.templateProperties?.enumeratorQuery ||
            !!alert.templateProperties?.enumerationItems;
        const fetchedAlertEvaluation = await getEvaluation(
            createAlertEvaluation(copiedAlert, start, end, {
                listEnumerationItemsOnly: hasEnumerationItems,
            })
        );

        setAlertForCurrentEvaluation(alert);
        setEvaluationTimeRange({ startTime: start, endTime: end });
        if (fetchedAlertEvaluation === undefined) {
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

    const handleAutoRangeClick = (): void => {
        if (
            getAlertInsightStatus === ActionStatus.Initial ||
            getAlertInsightStatus === ActionStatus.Error
        ) {
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
        } else if ((!startTime || !endTime) && alertInsight) {
            // If start or end is missing and there exists an alert insight
            fetchAlertEvaluation(
                alertInsight.defaultStartTime,
                alertInsight.defaultEndTime
            );
        } else {
            fetchAlertEvaluation(startTime, endTime);
        }
    };

    return (
        <>
            {/** Header Section **/}
            <PreviewChartHeader
                alertInsight={alertInsight}
                disableReload={disableReload}
                getEvaluationStatus={getEvaluationStatus}
                showConfigurationNotReflective={
                    !isEqual(alertForCurrentEvaluation, alert)
                }
                showTimeRange={showTimeRange}
                onReloadClick={handleAutoRangeClick}
                onStartEndChange={(newStart, newEnd) => {
                    fetchAlertEvaluation(newStart, newEnd);
                }}
            >
                {children}
            </PreviewChartHeader>
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
                <ChartContentV2
                    showLoadButton
                    alert={alert}
                    alertEvaluation={evaluation}
                    evaluationTimeRange={evaluationTimeRange}
                    hideCallToActionPrompt={hideCallToActionPrompt}
                    legendsPlacement={legendsPlacement}
                    onReloadClick={handleAutoRangeClick}
                />
            </LoadingErrorStateSwitch>
        </>
    );
};
