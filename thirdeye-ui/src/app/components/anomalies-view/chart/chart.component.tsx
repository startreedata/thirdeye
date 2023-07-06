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
import {
    Box,
    Card,
    CardContent,
    Divider,
    Grid,
    IconButton,
    useTheme,
} from "@material-ui/core";
import DeleteOutlineIcon from "@material-ui/icons/DeleteOutline";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    PageContentsCardV1,
    SkeletonV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetEvaluation } from "../../../rest/alerts/alerts.actions";
import { DetectionEvaluation } from "../../../rest/dto/detection.interfaces";
import { extractDetectionEvaluation } from "../../../utils/alerts/alerts.util";
import { createAlertEvaluation } from "../../../utils/anomalies/anomalies.util";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import { iso8601ToMilliseconds } from "../../../utils/time/time.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { EmptyStateSwitch } from "../../page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { generateChartOptionsForAlert } from "../../rca/anomaly-time-series-card/anomaly-time-series-card.utils";
import { PastDurationPicker } from "../../time-range/past-duration-picker/past-duration-picker.component";
import { TimeRangeButtonWithContext } from "../../time-range/time-range-button-with-context/time-range-button.component";
import { TimeSeriesChart } from "../../visualizations/time-series-chart/time-series-chart.component";
import { TimeSeriesChartProps } from "../../visualizations/time-series-chart/time-series-chart.interfaces";
import { ChartProps } from "./chart.interfaces";

const DEFAULT_CHART_HEIGHT = 300;
const DEFAULT_SKELETON_HEIGHT = 400;

export const Chart: FunctionComponent<ChartProps> = ({
    onDateChange,
    onDeleteClick,
    onPastPeriodChange,
    anomaly,
    start,
    end,
    // ISO-8601 format
    startEndShift = "P0D",
    timezone,
    hideTime,
    hideChartBrush,
    chartHeight,
}) => {
    const offset = iso8601ToMilliseconds(startEndShift);

    const theme = useTheme();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    const {
        evaluation,
        getEvaluation,
        errorMessages,
        status: getEvaluationRequestStatus,
    } = useGetEvaluation();

    const [detectionEvaluation, setDetectionEvaluation] =
        useState<DetectionEvaluation | null>(null);

    useEffect(() => {
        notifyIfErrors(
            getEvaluationRequestStatus,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.chart-data"),
            })
        );
    }, [errorMessages, getEvaluationRequestStatus]);

    useEffect(() => {
        if (!evaluation) {
            return;
        }

        const detectionEvalForAnomaly =
            extractDetectionEvaluation(evaluation)[0];

        detectionEvalForAnomaly.anomalies = [anomaly];
        setDetectionEvaluation(detectionEvalForAnomaly);
    }, [evaluation]);

    useEffect(() => {
        fetchAlertEvaluation();
    }, [anomaly, start, end, startEndShift]);

    const chartOptions = useMemo(() => {
        if (!detectionEvaluation) {
            return;
        }
        const anomalyToDisplay = [];

        // If anomaly is between time period, include it
        if (
            (anomaly.startTime >= start - offset &&
                anomaly.startTime <= end - offset) ||
            (anomaly.endTime >= start - offset &&
                anomaly.endTime <= end - offset)
        ) {
            anomalyToDisplay.push(anomaly);
        }

        const chartOptions = generateChartOptionsForAlert(
            detectionEvaluation,
            anomalyToDisplay,
            t,
            undefined,
            timezone,
            hideTime
        );

        if (hideChartBrush) {
            chartOptions.brush = false;
        }

        return chartOptions;
    }, [detectionEvaluation, timezone, anomaly, hideChartBrush]);

    const fetchAlertEvaluation = (): void => {
        if (!anomaly || !anomaly.alert || !start || !end) {
            setDetectionEvaluation(null);

            return;
        }

        getEvaluation(
            createAlertEvaluation(
                anomaly.alert.id,
                start - offset,
                end - offset
            ),
            undefined,
            anomaly.enumerationItem
        );
    };

    return (
        <LoadingErrorStateSwitch
            isError={false}
            isLoading={
                getEvaluationRequestStatus === ActionStatus.Working ||
                getEvaluationRequestStatus === ActionStatus.Initial
            }
            loadingState={
                <PageContentsCardV1>
                    <SkeletonV1 animation="pulse" variant="rect" />
                    <SkeletonV1
                        animation="pulse"
                        height={DEFAULT_SKELETON_HEIGHT}
                        variant="rect"
                    />
                </PageContentsCardV1>
            }
        >
            <Card variant="outlined">
                <CardContent style={{ paddingBottom: 0 }}>
                    <Grid
                        container
                        alignItems="center"
                        justifyContent="flex-end"
                    >
                        {onDateChange && (
                            <Grid item>
                                <TimeRangeButtonWithContext
                                    btnGroupColor="primary"
                                    timezone={timezone}
                                    onTimeRangeChange={(
                                        start: number,
                                        end: number
                                    ) => onDateChange(start, end)}
                                />
                            </Grid>
                        )}
                        {onPastPeriodChange && (
                            <Grid item>
                                <PastDurationPicker
                                    selected={startEndShift}
                                    onSelectedChange={onPastPeriodChange}
                                >
                                    Compare to the previous
                                </PastDurationPicker>
                            </Grid>
                        )}

                        {onDeleteClick && (
                            <Grid item>
                                <Grid container justifyContent="flex-end">
                                    <Grid item>
                                        <Divider orientation="vertical" />
                                    </Grid>
                                    <Grid item>
                                        <IconButton
                                            size="small"
                                            onClick={onDeleteClick}
                                        >
                                            <DeleteOutlineIcon
                                                htmlColor={
                                                    theme.palette.error.main
                                                }
                                            />
                                        </IconButton>
                                    </Grid>
                                </Grid>
                            </Grid>
                        )}
                    </Grid>
                </CardContent>
                <CardContent style={{ paddingTop: 0 }}>
                    <EmptyStateSwitch
                        emptyState={
                            <Box pb={20} pt={20}>
                                <NoDataIndicator
                                    text={t(
                                        "message.experienced-an-issue-fetching-chart-data"
                                    )}
                                />
                            </Box>
                        }
                        isEmpty={
                            getEvaluationRequestStatus === ActionStatus.Error ||
                            !chartOptions
                        }
                    >
                        <TimeSeriesChart
                            height={chartHeight ?? DEFAULT_CHART_HEIGHT}
                            {...(chartOptions as TimeSeriesChartProps)}
                        />
                    </EmptyStateSwitch>
                </CardContent>
            </Card>
        </LoadingErrorStateSwitch>
    );
};
