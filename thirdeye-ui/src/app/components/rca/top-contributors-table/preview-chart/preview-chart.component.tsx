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
import { Box, Button, CircularProgress, Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    SkeletonV1,
    useNotificationProviderV1,
} from "../../../../platform/components";
import { ActionStatus } from "../../../../rest/actions.interfaces";
import { useGetEvaluation } from "../../../../rest/alerts/alerts.actions";
import { getAlertEvaluation } from "../../../../rest/alerts/alerts.rest";
import { AlertEvaluation } from "../../../../rest/dto/alert.interfaces";
import { DetectionEvaluation } from "../../../../rest/dto/detection.interfaces";
import {
    determineTimezoneFromAlertInEvaluation,
    extractDetectionEvaluation,
    shouldHideTimeInDatetimeFormat,
} from "../../../../utils/alerts/alerts.util";
import { createAlertEvaluation } from "../../../../utils/anomalies/anomalies.util";
import { baselineOffsetToMilliseconds } from "../../../../utils/anomaly-breakdown/anomaly-breakdown.util";
import { notifyIfErrors } from "../../../../utils/notifications/notifications.util";
import { concatKeyValueWithEqual } from "../../../../utils/params/params.util";
import { getErrorMessages } from "../../../../utils/rest/rest.util";
import { NoDataIndicator } from "../../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { TimeSeriesChart } from "../../../visualizations/time-series-chart/time-series-chart.component";
import { TimeSeriesChartProps } from "../../../visualizations/time-series-chart/time-series-chart.interfaces";
import { AnomalyFilterOption } from "../../anomaly-dimension-analysis/anomaly-dimension-analysis.interfaces";
import { generateChartOptions } from "../../anomaly-time-series-card/anomaly-time-series-card.utils";
import { PreviewChartProps } from "./preview-chart.interface";

export const PreviewChart: FunctionComponent<PreviewChartProps> = ({
    anomaly,
    dimensionCombinations,
    alertInsight,
    events,
    children,
}) => {
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const {
        evaluation: evaluationForAnomaly,
        getEvaluation,
        errorMessages,
        status: getEvaluationRequestStatus,
    } = useGetEvaluation();

    const [timeSeriesOptions, setTimeSeriesOptions] =
        useState<TimeSeriesChartProps>();
    const [filteredAlertEvaluation, setFilteredAlertEvaluation] = useState<
        [AlertEvaluation, AnomalyFilterOption[]][]
    >([]);
    const [isFilteredDataFetching, setIsFilteredDataFetching] = useState(false);
    const [hideTime, timezone, granularity] = useMemo(() => {
        return [
            shouldHideTimeInDatetimeFormat(
                alertInsight?.templateWithProperties
            ),
            determineTimezoneFromAlertInEvaluation(
                alertInsight?.templateWithProperties
            ),
            alertInsight?.templateWithProperties?.metadata?.granularity,
        ];
    }, [alertInsight]);

    const [startTime, endTime] = useMemo(() => {
        if (!anomaly) {
            return [0, 0];
        }

        const padding = granularity
            ? baselineOffsetToMilliseconds(granularity) * 14
            : 0;

        return [
            Number(anomaly.startTime) - padding,
            Number(anomaly.endTime) + padding,
        ];
    }, [anomaly, granularity]);

    const fetchAlertEvaluation = (): void => {
        if (
            !anomaly ||
            !anomaly.alert ||
            !startTime ||
            !endTime ||
            !alertInsight
        ) {
            return;
        }

        getEvaluation(
            createAlertEvaluation(anomaly.alert.id, startTime, endTime),
            undefined,
            anomaly?.enumerationItem
        );
    };

    const fetchFilteredAlertEvaluations = (): void => {
        if (
            !anomaly ||
            !anomaly.alert ||
            !startTime ||
            !endTime ||
            !alertInsight
        ) {
            return;
        }

        const dataRequests = dimensionCombinations.map((filterSet) => {
            const filters = filterSet.map((item) =>
                concatKeyValueWithEqual(item, false)
            );

            return getAlertEvaluation(
                createAlertEvaluation(anomaly.alert.id, startTime, endTime),
                filters,
                anomaly?.enumerationItem
            );
        });
        setIsFilteredDataFetching(true);
        Promise.all(dataRequests)
            .then((dataFromRequests) => {
                setFilteredAlertEvaluation(
                    dataFromRequests.map((alertEval, idx) => {
                        return [alertEval, dimensionCombinations[idx]];
                    })
                );
            })
            .catch((error) => {
                setFilteredAlertEvaluation([]);
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t("message.error-while-fetching", {
                        entity: t("label.chart-data"),
                    })
                );
            })
            .finally(() => {
                setIsFilteredDataFetching(false);
            });
    };

    useEffect(() => {
        if (!evaluationForAnomaly) {
            return;
        }
        const detectionsEvaluations =
            extractDetectionEvaluation(evaluationForAnomaly);
        const detectionEvalForAnomaly: DetectionEvaluation | undefined =
            detectionsEvaluations[0];

        setTimeSeriesOptions(
            generateChartOptions(
                detectionEvalForAnomaly,
                anomaly,
                filteredAlertEvaluation,
                t,
                timezone,
                hideTime
            )
        );
    }, [evaluationForAnomaly, filteredAlertEvaluation]);

    useEffect(() => {
        fetchAlertEvaluation();
    }, [anomaly, startTime, endTime]);

    useEffect(() => {
        fetchFilteredAlertEvaluations();
    }, [anomaly, dimensionCombinations]);

    return (
        <Grid container>
            <Grid item xs={12}>
                <Grid
                    container
                    alignItems="center"
                    justifyContent="space-between"
                >
                    <Grid item>{children}</Grid>
                    <Grid item>
                        {isFilteredDataFetching ||
                        getEvaluationRequestStatus === ActionStatus.Working ? (
                            <CircularProgress size={20} />
                        ) : (
                            <Button
                                color="primary"
                                onClick={() => {
                                    fetchAlertEvaluation();
                                    fetchFilteredAlertEvaluations();
                                }}
                            >
                                {t("label.reload-data")}
                            </Button>
                        )}
                    </Grid>
                </Grid>
            </Grid>
            <Grid item xs={12}>
                <LoadingErrorStateSwitch
                    errorState={
                        <Box pb={20} pt={20}>
                            <NoDataIndicator text={errorMessages?.[0]} />
                        </Box>
                    }
                    isError={getEvaluationRequestStatus === ActionStatus.Error}
                    isLoading={
                        getEvaluationRequestStatus === ActionStatus.Working ||
                        getEvaluationRequestStatus === ActionStatus.Initial
                    }
                    loadingState={
                        <Box pb={2} pt={2}>
                            <SkeletonV1
                                animation="pulse"
                                height={300}
                                variant="rect"
                            />
                        </Box>
                    }
                >
                    {!!timeSeriesOptions && (
                        <TimeSeriesChart
                            events={events}
                            height={300}
                            {...timeSeriesOptions}
                            brush={false}
                            zoom={false}
                        />
                    )}
                </LoadingErrorStateSwitch>
            </Grid>
        </Grid>
    );
};
