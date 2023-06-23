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
import { Box, Chip, Grid } from "@material-ui/core";
import React, { FunctionComponent, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Anomaly } from "../../../../../rest/dto/anomaly.interfaces";
import {
    determineTimezoneFromAlertInEvaluation,
    extractDetectionEvaluation,
    shouldHideTimeInDatetimeFormat,
} from "../../../../../utils/alerts/alerts.util";
import { TimeSeriesChart } from "../../../../visualizations/time-series-chart/time-series-chart.component";
import { ZoomDomain } from "../../../../visualizations/time-series-chart/time-series-chart.interfaces";
import { generateSeriesForAnomalies } from "../../../anomaly-time-series-card/anomaly-time-series-card.utils";
import { getColorForDimensionCombo } from "../../investigation-preview.utils";
import { MultiChartProps } from "./multi-chart.interfaces";

export const MultiChart: FunctionComponent<MultiChartProps> = ({
    alertInsight,
    anomaly,
    events,
    chartSeriesForFiltersWithEvaluationAndDimensionCombo,
    baseTimeSeriesOptions,
    chartSeriesForAlert,
    chartSeriesForAnomaly,
}) => {
    const { t } = useTranslation();

    // Have all them multi-charts use the same zoom and tooltip positions
    const [currentZoom, setCurrentZoom] = useState<ZoomDomain | undefined>();
    const [tooltipPosition, setTooltipPosition] = useState<
        [number, number] | undefined
    >();

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

    const filteredDimensionSeriesForMultiChart = useMemo(() => {
        if (chartSeriesForFiltersWithEvaluationAndDimensionCombo) {
            return chartSeriesForFiltersWithEvaluationAndDimensionCombo.map(
                ([s, , evaluation]) => {
                    const detectionEvaluations =
                        extractDetectionEvaluation(evaluation);
                    const timestamps = detectionEvaluations[0].data.timestamp;
                    const trendingData = detectionEvaluations[0].data.current;

                    return [
                        s,
                        generateSeriesForAnomalies(
                            [anomaly as Anomaly],
                            t,
                            timestamps,
                            trendingData,
                            undefined,
                            timezone,
                            hideTime,
                            true
                        ),
                    ];
                }
            );
        }

        return [];
    }, [chartSeriesForFiltersWithEvaluationAndDimensionCombo]);

    const seriesForAlert = useMemo(() => {
        if (chartSeriesForAlert) {
            return [...chartSeriesForAlert, chartSeriesForAnomaly];
        }

        return [];
    }, [chartSeriesForAlert, chartSeriesForAnomaly]);

    const handleZoomChange = (domain: ZoomDomain | null): boolean => {
        setCurrentZoom(() => {
            return domain ? { ...domain } : undefined;
        });

        return false;
    };

    const handleZoomReset = (): boolean => {
        setCurrentZoom(undefined);

        return false;
    };

    const handleTooltipPositionChange = (
        newPosition: [number, number] | undefined
    ): boolean => {
        setTooltipPosition(newPosition);

        return false;
    };

    return (
        <>
            <TimeSeriesChart
                chartEvents={{
                    onRangeSelection: handleZoomChange,
                    onZoomReset: handleZoomReset,
                    onPositionTooltipChange: handleTooltipPositionChange,
                }}
                events={events}
                height={300}
                zoomOverride={currentZoom ? { ...currentZoom } : undefined}
                {...baseTimeSeriesOptions}
                brush={false}
                series={seriesForAlert}
                tooltipPositionOverride={tooltipPosition}
            />
            {filteredDimensionSeriesForMultiChart?.map(
                (seriesForCombo, idx) => {
                    const dimensionCombinationForSeries =
                        chartSeriesForFiltersWithEvaluationAndDimensionCombo[
                            idx
                        ][1];
                    const color = getColorForDimensionCombo(
                        dimensionCombinationForSeries
                    );
                    seriesForCombo[0].color = color;

                    return (
                        <>
                            <Box pb={1} pr={1} pt={1}>
                                <Grid container spacing={1}>
                                    {dimensionCombinationForSeries.map((kv) => {
                                        return (
                                            <Grid
                                                item
                                                key={`${kv.key}=${kv.value}`}
                                            >
                                                <Chip
                                                    label={`${kv.key}=${kv.value}`}
                                                    size="small"
                                                    style={{
                                                        borderColor: color,
                                                        color: color,
                                                    }}
                                                />
                                            </Grid>
                                        );
                                    })}
                                </Grid>
                            </Box>
                            <TimeSeriesChart
                                chartEvents={{
                                    onRangeSelection: handleZoomChange,
                                    onZoomReset: handleZoomReset,
                                    onPositionTooltipChange:
                                        handleTooltipPositionChange,
                                }}
                                events={events}
                                height={200}
                                key={seriesForCombo[0].name}
                                tooltipPositionOverride={tooltipPosition}
                                zoomOverride={
                                    currentZoom ? { ...currentZoom } : undefined
                                }
                                {...baseTimeSeriesOptions}
                                brush={false}
                                legend={false}
                                series={seriesForCombo}
                            />
                        </>
                    );
                }
            )}
        </>
    );
};
