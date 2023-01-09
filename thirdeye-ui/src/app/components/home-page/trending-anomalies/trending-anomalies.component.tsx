/*
 * Copyright 2022 StarTree Inc
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
import { Box, Link, Typography } from "@material-ui/core";
import { Orientation } from "@visx/axis";
import { scaleLinear } from "d3-scale";
import { capitalize } from "lodash";
import { DateTime } from "luxon";
import React, {
    FunctionComponent,
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { Link as RouterLink } from "react-router-dom";
import { SkeletonV1 } from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetAnomalies } from "../../../rest/anomalies/anomaly.actions";
import { getAnomaliesAllRangePath } from "../../../utils/routes/routes.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    TimeRange,
    TimeRangeQueryStringKey,
} from "../../time-range/time-range-provider/time-range-provider.interfaces";
import { TimeSeriesChart } from "../../visualizations/time-series-chart/time-series-chart.component";
import {
    DataPoint,
    SeriesType,
    TimeSeriesChartProps,
    ZoomDomain,
} from "../../visualizations/time-series-chart/time-series-chart.interfaces";
import { getMinMax } from "../../visualizations/time-series-chart/time-series-chart.utils";
import { TrendingAnomaliesProps } from "./trending-anomalies.interface";

export const TrendingAnomalies: FunctionComponent<TrendingAnomaliesProps> = ({
    startTime,
}) => {
    const { t } = useTranslation();
    const [timeseriesOptions, setTimeseriesOptions] =
        useState<TimeSeriesChartProps>();
    const [currentChartZoom, setCurrentChartZoom] =
        useState<ZoomDomain | null>();
    const { getAnomalies, status } = useGetAnomalies();

    const handleZoomChange = useCallback(
        (domain: ZoomDomain | null) => {
            setCurrentChartZoom(domain);
        },
        [setCurrentChartZoom]
    );

    const allAnomaliesLinkSearchParams = useMemo(() => {
        if (!currentChartZoom) {
            return new URLSearchParams();
        }

        return new URLSearchParams([
            [
                TimeRangeQueryStringKey.START_TIME,
                currentChartZoom.x0.toString(),
            ],
            [TimeRangeQueryStringKey.END_TIME, currentChartZoom.x1.toString()],
            [TimeRangeQueryStringKey.TIME_RANGE, TimeRange.CUSTOM],
        ]);
    }, [currentChartZoom]);

    useEffect(() => {
        getAnomalies({
            startTime,
        }).then((anomalies) => {
            if (!anomalies) {
                return;
            }

            const trendingData: { [key: string]: number } = {};
            anomalies.forEach((anomaly) => {
                if (anomaly.startTime) {
                    const dt = DateTime.fromMillis(anomaly.startTime);
                    const dtStartOfDay = dt.startOf("day");
                    const count =
                        trendingData[dtStartOfDay.toMillis().toString()] || 0;
                    trendingData[dtStartOfDay.toMillis().toString()] =
                        count + 1;
                }
            });

            const dataset = Object.keys(trendingData)
                .sort()
                .map((timestamp: string) => {
                    return {
                        x: Number(timestamp),
                        y: trendingData[timestamp],
                    };
                });
            const minMaxValue = getMinMax(
                [
                    {
                        data: dataset,
                    },
                ],
                (d: DataPoint) => d.y
            );
            const colorScale = scaleLinear(minMaxValue, ["#DAE8FF", "#1850A6"]);

            dataset.forEach((d: DataPoint) => {
                d.color = colorScale(d.y);
            });

            setTimeseriesOptions({
                series: [
                    {
                        name: t("label.anomaly-count"),
                        type: SeriesType.BAR,
                        data: dataset,
                        color: "#1850A6",
                    },
                ],
                margins: {
                    left: 0,
                    right: 25,
                    top: 10,
                    bottom: 10,
                },
                legend: false,
                brush: false,
                zoom: true,
                tooltip: true,
                yAxis: {
                    position: Orientation.right,
                },
                chartEvents: {
                    onZoomChange: handleZoomChange,
                },
            });
        });
    }, [startTime]);

    const isTimeSeriesEmpty =
        status === ActionStatus.Done && timeseriesOptions?.series
            ? timeseriesOptions.series.length === 0 || // Either there are no series
              !timeseriesOptions.series.some(({ data }) => data.length > 0) // Or none of the series have data to render
            : false;

    return (
        <LoadingErrorStateSwitch
            errorState={
                <Box
                    alignItems="center"
                    display="flex"
                    height="100%"
                    justifyContent="center"
                >
                    <Box>
                        <NoDataIndicator>
                            {t("message.experienced-issues-fetching-data")}
                        </NoDataIndicator>
                    </Box>
                </Box>
            }
            isError={status === ActionStatus.Error}
            isLoading={status === ActionStatus.Working}
            loadingState={
                <Box minHeight={195}>
                    <SkeletonV1 animation="pulse" />
                    <SkeletonV1 animation="pulse" />
                    <SkeletonV1 animation="pulse" />
                    <SkeletonV1 animation="pulse" />
                    <SkeletonV1 animation="pulse" />
                </Box>
            }
        >
            {timeseriesOptions && (
                <>
                    {isTimeSeriesEmpty ? (
                        <Box
                            alignItems="center"
                            display="flex"
                            justifyContent="center"
                            minHeight={195}
                            position="relative"
                        >
                            <Typography variant="h6">
                                {capitalize(
                                    t(
                                        "message.there-have-been-no-entity-in-the-timePeriod",
                                        {
                                            entity: t("label.anomalies"),
                                            timePeriod: t(
                                                "label.selected-time-range"
                                            ),
                                        }
                                    )
                                )}
                            </Typography>
                        </Box>
                    ) : (
                        <Box position="relative">
                            <Box position="absolute" zIndex={500}>
                                <Typography variant="h6">
                                    {t("label.daily-anomalies")}
                                </Typography>
                                {currentChartZoom && (
                                    <Link
                                        component={RouterLink}
                                        to={
                                            getAnomaliesAllRangePath() +
                                            allAnomaliesLinkSearchParams.toString()
                                        }
                                    >
                                        {t(
                                            "label.view-anomalies-for-zoom-range"
                                        )}
                                    </Link>
                                )}
                            </Box>
                            <Box height={195}>
                                <TimeSeriesChart {...timeseriesOptions} />
                            </Box>
                        </Box>
                    )}
                </>
            )}
        </LoadingErrorStateSwitch>
    );
};
