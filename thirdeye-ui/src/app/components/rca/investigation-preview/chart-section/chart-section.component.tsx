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
import { Box, Card, CardContent, IconButton } from "@material-ui/core";
import RefreshIcon from "@material-ui/icons/Refresh";
import { Orientation } from "@visx/axis";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useInView } from "react-intersection-observer";
import { useSearchParams } from "react-router-dom";
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
import { notifyIfErrors } from "../../../../utils/notifications/notifications.util";
import {
    concatKeyValueWithEqual,
    serializeKeyValuePair,
} from "../../../../utils/params/params.util";
import { getErrorMessages } from "../../../../utils/rest/rest.util";
import { LoadingErrorStateSwitch } from "../../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { TimeRangeQueryStringKey } from "../../../time-range/time-range-provider/time-range-provider.interfaces";
import { TimeRangeSelectorButtonWithSearchParamsContext } from "../../../time-range/v2/time-range-selector-button-with-search-params-context/time-range-selector-btn.component";
import { TimeSeriesChart } from "../../../visualizations/time-series-chart/time-series-chart.component";
import {
    Series,
    TimeSeriesChartProps,
} from "../../../visualizations/time-series-chart/time-series-chart.interfaces";
import { COLOR_PALETTE } from "../../../visualizations/time-series-chart/time-series-chart.utils";
import { AnomalyFilterOption } from "../../anomaly-dimension-analysis/anomaly-dimension-analysis.interfaces";
import {
    generateSeriesDataForDetectionEvaluation,
    generateSeriesForAnomalies,
    generateSeriesForFilteredEvaluations,
} from "../../anomaly-time-series-card/anomaly-time-series-card.utils";
import { ChartType } from "../investigation-preview.interfaces";
import { getColorForStr } from "../investigation-preview.utils";
import { ChartSectionProps } from "./chart-section.interfaces";
import { MultiChart } from "./multi-chart/multi-chart.component";

export const ChartSection: FunctionComponent<ChartSectionProps> = ({
    alertInsight,
    anomaly,
    events,
    availableDimensionCombinations,
    selectedDimensionCombinations,
    chartType,
}) => {
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const { ref, inView } = useInView({
        triggerOnce: true,
        delay: 200,
        threshold: 0,
    });
    const [searchParams] = useSearchParams();

    // Chart data for alert unfiltered
    const {
        evaluation: alertEvaluation,
        getEvaluation,
        errorMessages,
        status: getEvaluationRequestStatus,
    } = useGetEvaluation();

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

    const [baseTimeSeriesOptions, setBaseTimeSeriesOptions] =
        useState<TimeSeriesChartProps>();

    const [chartSeriesForAlert, setChartSeriesForAlert] = useState<Series[]>(
        []
    );
    const [multiChartData, setMultiChartData] = useState<
        [Series, AnomalyFilterOption[], AlertEvaluation][]
    >([]);
    const [chartSeriesForAnomaly, setChartSeriesForAnomaly] =
        useState<Series>();

    // Chart data for alert evaluation filtered on dimension combinations
    const [filteredAlertEvaluations, setFilteredAlertEvaluations] = useState<
        [AlertEvaluation, AnomalyFilterOption[]][]
    >([]);

    const hideTime = useMemo(() => {
        return shouldHideTimeInDatetimeFormat(
            alertInsight?.templateWithProperties
        );
    }, [alertInsight]);
    const timezone = useMemo(() => {
        return determineTimezoneFromAlertInEvaluation(
            alertInsight?.templateWithProperties
        );
    }, [alertInsight]);

    const [startTime, endTime] = useMemo(
        () => [
            Number(searchParams.get(TimeRangeQueryStringKey.START_TIME)),
            Number(searchParams.get(TimeRangeQueryStringKey.END_TIME)),
        ],
        [searchParams]
    );

    const fetchAlertEvaluation = (): void => {
        if (!anomaly || !anomaly.alert || !startTime || !endTime) {
            return;
        }

        getEvaluation(
            createAlertEvaluation(anomaly.alert.id, startTime, endTime),
            undefined,
            anomaly?.enumerationItem
        );
    };

    const fetchFilteredAlertEvaluations = (): void => {
        setFilteredAlertEvaluations([]);

        if (!anomaly || !anomaly.alert || !startTime || !endTime) {
            return;
        }

        const dataRequests = availableDimensionCombinations.map((filterSet) => {
            const filters = filterSet.map((item) =>
                concatKeyValueWithEqual(item, false)
            );

            return getAlertEvaluation(
                createAlertEvaluation(anomaly.alert.id, startTime, endTime),
                filters,
                anomaly?.enumerationItem
            );
        });

        Promise.all(dataRequests)
            .then((dataFromRequests) => {
                setFilteredAlertEvaluations(
                    dataFromRequests.map((alertEval, idx) => {
                        return [alertEval, availableDimensionCombinations[idx]];
                    })
                );
            })
            .catch((error) => {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t("message.error-while-fetching", {
                        entity: t("label.chart-data"),
                    })
                );
            });
    };

    useEffect(() => {
        inView && fetchAlertEvaluation();
    }, [anomaly, startTime, endTime, inView]);

    useEffect(() => {
        inView && fetchFilteredAlertEvaluations();
    }, [anomaly, startTime, endTime, availableDimensionCombinations, inView]);

    useEffect(() => {
        if (!alertEvaluation || !anomaly) {
            return;
        }

        const detectionsEvaluations =
            extractDetectionEvaluation(alertEvaluation);
        const detectionEvalForAnomaly: DetectionEvaluation | undefined =
            detectionsEvaluations[0];

        setChartSeriesForAnomaly(
            generateSeriesForAnomalies(
                [anomaly],
                t,
                detectionEvalForAnomaly.data.timestamp,
                detectionEvalForAnomaly.data.current,
                undefined,
                timezone,
                hideTime
            )
        );
        setChartSeriesForAlert(
            generateSeriesDataForDetectionEvaluation(
                detectionEvalForAnomaly,
                [],
                t
            )
        );

        const multiChartData = generateSeriesForFilteredEvaluations(
            filteredAlertEvaluations
        )
            .map(
                (
                    series,
                    idx
                ): [Series, AnomalyFilterOption[], AlertEvaluation] => {
                    const dimensionCombinationForSeries =
                        filteredAlertEvaluations[idx][1];
                    const strId = serializeKeyValuePair(
                        dimensionCombinationForSeries
                    );
                    series.color = getColorForStr(strId, COLOR_PALETTE);
                    series.hideInLegend = true;

                    return [
                        series,
                        filteredAlertEvaluations[idx][1],
                        filteredAlertEvaluations[idx][0],
                    ];
                }
            )
            .filter(([, anomalyFilterOption]) => {
                return selectedDimensionCombinations.has(
                    serializeKeyValuePair(anomalyFilterOption)
                );
            });

        setMultiChartData(multiChartData);

        const chartOptions: TimeSeriesChartProps = {
            series: [],
            yAxis: {
                position: Orientation.right,
            },
            xAxis: {
                hideTime: hideTime,
                timezone,
            },
            legend: true,
            brush: true,
            zoom: true,
            tooltip: true,
        };

        setBaseTimeSeriesOptions(chartOptions);
    }, [
        selectedDimensionCombinations,
        filteredAlertEvaluations,
        alertEvaluation,
        timezone,
        hideTime,
    ]);

    return (
        <Card innerRef={ref}>
            <CardContent>
                <Box textAlign="right">
                    <TimeRangeSelectorButtonWithSearchParamsContext
                        btnGroupColor="primary"
                        btnVariant="text"
                        timezone={timezone}
                    />
                    <IconButton
                        color="primary"
                        size="small"
                        onClick={() => {
                            fetchAlertEvaluation();
                            fetchFilteredAlertEvaluations();
                        }}
                    >
                        <RefreshIcon />
                    </IconButton>
                </Box>
            </CardContent>
            <CardContent>
                <LoadingErrorStateSwitch
                    isError={getEvaluationRequestStatus === ActionStatus.Error}
                    isLoading={
                        getEvaluationRequestStatus === ActionStatus.Initial ||
                        getEvaluationRequestStatus === ActionStatus.Working
                    }
                    loadingState={<SkeletonV1 height={500} variant="rect" />}
                >
                    {baseTimeSeriesOptions &&
                        chartSeriesForAnomaly &&
                        chartType === ChartType.ONE && (
                            <TimeSeriesChart
                                events={events}
                                height={500}
                                {...baseTimeSeriesOptions}
                                series={[
                                    ...chartSeriesForAlert,
                                    chartSeriesForAnomaly,
                                    ...multiChartData.map((c) => c[0]),
                                ]}
                            />
                        )}
                    {baseTimeSeriesOptions &&
                        chartSeriesForAnomaly &&
                        chartType === ChartType.MULTI && (
                            <MultiChart
                                alertInsight={alertInsight}
                                anomaly={anomaly}
                                baseTimeSeriesOptions={baseTimeSeriesOptions}
                                chartSeriesForAlert={chartSeriesForAlert}
                                chartSeriesForAnomaly={chartSeriesForAnomaly}
                                chartSeriesForFiltersWithEvaluationAndDimensionCombo={
                                    multiChartData
                                }
                                events={events}
                            />
                        )}
                </LoadingErrorStateSwitch>
            </CardContent>
        </Card>
    );
};
