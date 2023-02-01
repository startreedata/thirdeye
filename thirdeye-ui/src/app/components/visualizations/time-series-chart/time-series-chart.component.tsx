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
import { Box, Button, Typography } from "@material-ui/core";
import { ParentSize } from "@visx/responsive";
import { scaleOrdinal, scaleTime } from "@visx/scale";
import { TooltipWithBounds, useTooltip } from "@visx/tooltip";
import { Settings, Zone } from "luxon";
import React, {
    FunctionComponent,
    MouseEvent,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { DAY_IN_MILLISECONDS } from "../../../utils/time/time.util";
import { determineGranularity } from "../../../utils/visualization/visualization.util";
import { ChartBrush } from "./chart-brush/chart-brush.component";
import { ChartCore } from "./chart-core/chart-core.component";
import { ChartZoom } from "./chart-zoom/chart-zoom.component";
import { EventsChart } from "./events-chart/events-chart.component";
import { Legend } from "./legend/legend.component";
import {
    DataPoint,
    EventWithChartState,
    NormalizedSeries,
    Series,
    TimeSeriesChartInternalProps,
    TimeSeriesChartProps,
    ZoomDomain,
} from "./time-series-chart.interfaces";
import {
    COLOR_PALETTE,
    getMinMax,
    normalizeSeries,
    syncEnabledDisabled,
} from "./time-series-chart.utils";
import { TooltipMarkers } from "./tooltip/tooltip-markers.component";
import { TooltipPopover } from "./tooltip/tooltip-popover.component";
import { determineXPointForHover } from "./tooltip/tooltip.utils";

const MIN_DATA_POINTS_TO_DISPLAY = 30;
const CHART_SEPARATION = 30;
const CHART_MARGINS = {
    top: 20,
    left: 50,
    bottom: 20,
    right: 50,
};

/**
 *
 * @example
 * const series = [{
 *  name: 'Series 1',
 *  data: [
 *      {x: 1639353600000, y: 200},
 *      {x: 1639440000000, y: 300},
 *      {x: 1639526400000, y: 123},
 *      {x: 1639612800000, y: 544},
 *      {x: 1639785600000, y: 50},
 *  ]
 *  }, {
 *     name: 'Series 2',
 *     data: [
 *         {x: 1639353600000, y: 100},
 *         {x: 1639440000000, y: 200},
 *         {x: 1639526400000, y: 300},
 *         {x: 1639612800000, y: 400},
 *         {x: 1639785600000, y: 30},
 *     ]
 * }];
 *
 *  const chartOptions = {
 *     yAxis: true,
 *     xAxis: {
 *         enabled: false,
 *         plotBands: [{
 *             name: "Anomaly",
 *             start: 1639353600000,
 *             end: 1639526400000,
 *             color: "#000",
 *             opacity: 0.25
 *         }],
 *         timezone: "utc"
 *     },
 *     series,
 *     legend: false,
 *     brush: false,
 *     height: 250
 * }
 *
 * <TimeSeriesChart {...chartOptions} />
 */
export const TimeSeriesChart: FunctionComponent<TimeSeriesChartProps> = (
    props
) => {
    return (
        <ParentSize>
            {({ width, height }) => (
                <TimeSeriesChartInternal
                    height={props.height || height}
                    margins={CHART_MARGINS}
                    width={width}
                    {...props}
                />
            )}
        </ParentSize>
    );
};

export const TimeSeriesChartInternal: FunctionComponent<TimeSeriesChartInternalProps> =
    ({
        series,
        legend,
        brush,
        zoom,
        height,
        width,
        xAxis,
        yAxis,
        tooltip,
        initialZoom,
        chartEvents,
        events,
        LegendComponent = Legend,
        margins = CHART_MARGINS,
    }) => {
        const { t } = useTranslation();
        const [currentZoom, setCurrentZoom] = useState<ZoomDomain | undefined>(
            initialZoom
        );
        const [processedMainChartSeries, setProcessedMainChartSeries] =
            useState<NormalizedSeries[]>(normalizeSeries(series, currentZoom));
        const [processedBrushChartSeries, setProcessedBrushChartSeries] =
            useState<NormalizedSeries[]>(normalizeSeries(series));
        const [enabledDisabledSeriesMapping, setEnabledDisabledSeriesMapping] =
            useState<boolean[]>(series.map(syncEnabledDisabled));

        const bestGuessGranularity = useMemo(() => {
            const candidateSeries: Series | undefined = series.find(
                (s) => s.data.length > 0
            );

            if (!candidateSeries) {
                return DAY_IN_MILLISECONDS;
            }

            return determineGranularity(
                (candidateSeries.data as DataPoint[]).map((d: DataPoint) => d.x)
            );
        }, [series]);
        const minMaxValues = useMemo(() => {
            return getMinMax(series, (d) => [d.x]);
        }, [series]);

        const [processedEvents, setProcessedEvents] = useState<
            EventWithChartState[]
        >([]);

        const tooltipUtils = useTooltip<{ xValue: number }>();
        const {
            tooltipData,
            tooltipLeft,
            tooltipTop,
            hideTooltip,
            showTooltip,
        } = tooltipUtils;

        // Legend should take on the value of the option if it exists otherwise default to true
        const isLegendEnabled = legend === undefined ? true : legend;
        const isBrushEnabled = brush === undefined ? false : brush;
        const isZoomEnabled = zoom === undefined ? false : zoom;
        const isTooltipEnabled = tooltip === undefined ? true : tooltip;
        const isXAxisEnabled =
            xAxis === undefined
                ? true
                : xAxis.enabled === undefined
                ? true
                : xAxis.enabled;
        const isYAxisEnabled =
            yAxis === undefined
                ? true
                : yAxis.enabled === undefined
                ? true
                : yAxis.enabled;
        let topChartHeight: number;
        let brushChartHeight;
        let topChartBottomMargin;

        const innerHeight = height - margins.top - margins.bottom;

        if (brush) {
            topChartBottomMargin = CHART_SEPARATION;

            if (innerHeight > 400) {
                brushChartHeight = 100;
            } else {
                brushChartHeight = 75;
            }
            topChartHeight =
                innerHeight - brushChartHeight - topChartBottomMargin;
        } else {
            topChartBottomMargin = margins.bottom;
            topChartHeight = innerHeight - topChartBottomMargin;
            brushChartHeight = 0;
        }

        // Bounds
        const xMax = Math.max(width - margins.left - margins.right, 0);
        const yMax = Math.max(topChartHeight, 0);

        const colorScale = useMemo(() => {
            return scaleOrdinal({
                domain: series.map((x) => x.name) as string[],
                range: COLOR_PALETTE,
            });
        }, [series]);

        // If enabledDisabledMapping changes, sync it with the stored series
        useEffect(() => {
            processedMainChartSeries &&
                setProcessedMainChartSeries([
                    ...processedMainChartSeries.map((seriesData, idx) => {
                        seriesData.enabled = enabledDisabledSeriesMapping[idx];

                        return seriesData;
                    }),
                ]);
            processedBrushChartSeries &&
                setProcessedBrushChartSeries([
                    ...processedBrushChartSeries.map((seriesData, idx) => {
                        seriesData.enabled = enabledDisabledSeriesMapping[idx];

                        return seriesData;
                    }),
                ]);
        }, [enabledDisabledSeriesMapping]);

        // If series changed, reset everything
        useEffect(() => {
            setProcessedMainChartSeries(normalizeSeries(series, currentZoom));
            setProcessedBrushChartSeries(normalizeSeries(series));
            setEnabledDisabledSeriesMapping(series.map(syncEnabledDisabled));
        }, [series]);

        // If events change, figure out what was removed and added and sync the processedEvents
        useEffect(() => {
            const eventsToUse = events || [];

            setProcessedEvents((original) => {
                const newProcessedEvents: EventWithChartState[] = [];

                eventsToUse.forEach((event) => {
                    const result = original.find(
                        (candidate) => candidate.id === event.id
                    );

                    if (result) {
                        newProcessedEvents.push(result);
                    } else {
                        newProcessedEvents.push({
                            ...event,
                            enabled: true,
                        });
                    }
                });

                return newProcessedEvents;
            });
        }, [events]);

        /**
         * Flip the enabled flag and force a re-render by creating a new array
         */
        const handleSeriesClickFromLegend = (idx: number): void => {
            const copied = [...enabledDisabledSeriesMapping];
            copied[idx] = !copied[idx];
            setEnabledDisabledSeriesMapping([...copied]);
        };

        const handleBrushChange = (domain: ZoomDomain | null): void => {
            if (!domain) {
                return;
            }

            let { x0, x1 } = domain;
            // Ensure a minimum of 7 data points are in view
            while (
                bestGuessGranularity &&
                x0 > minMaxValues[0] &&
                x1 < minMaxValues[1] &&
                (x1 - x0) / bestGuessGranularity < MIN_DATA_POINTS_TO_DISPLAY
            ) {
                x0 = x0 - bestGuessGranularity;
                x1 = x1 + bestGuessGranularity;
            }

            const seriesDataCopy = series.map((seriesData, idx) => {
                const copied = { ...seriesData, data: [...seriesData.data] };
                copied.data = copied.data.filter((d) => {
                    const x = d.x;

                    return x > x0 && x < x1;
                });
                copied.enabled = enabledDisabledSeriesMapping[idx];

                return copied;
            });
            setProcessedMainChartSeries(normalizeSeries(seriesDataCopy));
            setCurrentZoom({ x0, x1 });

            if (chartEvents && chartEvents.onZoomChange) {
                chartEvents.onZoomChange(domain);
            }
        };

        const handleResetZoom = (): void => {
            const seriesDataCopy = series.map((seriesData, idx) => {
                const copied = { ...seriesData };
                copied.enabled = enabledDisabledSeriesMapping[idx];

                return copied;
            });

            setProcessedMainChartSeries(normalizeSeries(seriesDataCopy));
            setCurrentZoom(undefined);

            if (chartEvents && chartEvents.onZoomChange) {
                chartEvents.onZoomChange(null);
            }
        };

        // Open the tooltip
        const dateScaleForHandleMouseOver = useMemo(() => {
            const minMaxTimestamp = getMinMax(
                processedMainChartSeries.filter((s) => s.enabled),
                (d) => [d.x]
            );

            return scaleTime<number>({
                range: [0, xMax],
                domain: [
                    new Date(minMaxTimestamp[0]),
                    new Date(minMaxTimestamp[1]),
                ] as [Date, Date],
            });
        }, [xMax, processedMainChartSeries]);

        const handleMouseOver = (event: MouseEvent<SVGSVGElement>): void => {
            if (!isTooltipEnabled) {
                return;
            }

            const [xValue, coords] = determineXPointForHover(
                event,
                processedMainChartSeries,
                dateScaleForHandleMouseOver,
                margins.left
            );

            if (xValue === null || coords === null) {
                hideTooltip();

                return;
            }

            showTooltip({
                tooltipLeft: dateScaleForHandleMouseOver(xValue),
                tooltipTop: coords.y - margins.top,
                tooltipData: {
                    xValue: xValue,
                },
            });
        };

        return (
            <div style={{ position: "relative" }}>
                <Box paddingLeft={5} position="absolute">
                    <Typography color="textSecondary" variant="caption">
                        {t("message.times-displayed-in-timezone", {
                            timezone:
                                xAxis?.timezone ??
                                (Settings.defaultZone as Zone).name,
                        })}
                    </Typography>
                </Box>
                {events && events.length > 0 && (
                    <EventsChart
                        events={processedEvents}
                        isTooltipEnabled={isTooltipEnabled}
                        margin={{ ...margins, bottom: topChartBottomMargin }}
                        series={processedMainChartSeries}
                        tooltipUtils={tooltipUtils}
                        width={width}
                        xMax={xMax}
                    />
                )}
                <svg height={height} width={width}>
                    <ChartCore
                        colorScale={colorScale}
                        height={height}
                        margin={{ ...margins, bottom: topChartBottomMargin }}
                        series={processedMainChartSeries}
                        showXAxis={isXAxisEnabled}
                        showYAxis={isYAxisEnabled}
                        width={width}
                        xAxisOptions={xAxis}
                        xMax={xMax}
                        yAxisOptions={yAxis}
                        yMax={yMax}
                        // Handles whether to show the tooltip
                        onMouseLeave={() => {
                            isTooltipEnabled && hideTooltip();
                        }}
                        onMouseMove={(event: MouseEvent<SVGSVGElement>) =>
                            isTooltipEnabled && handleMouseOver(event)
                        }
                    >
                        {(xScale, yScale) => {
                            return (
                                <>
                                    {isZoomEnabled &&
                                        processedMainChartSeries && (
                                            <ChartZoom
                                                colorScale={colorScale}
                                                height={topChartHeight}
                                                margins={margins}
                                                series={
                                                    processedMainChartSeries
                                                }
                                                width={width}
                                                onZoomChange={handleBrushChange}
                                            />
                                        )}
                                    {tooltipData && (
                                        <TooltipMarkers
                                            chartHeight={topChartHeight}
                                            colorScale={colorScale}
                                            series={processedMainChartSeries}
                                            xScale={xScale}
                                            xValue={tooltipData.xValue}
                                            yScale={yScale}
                                        />
                                    )}
                                </>
                            );
                        }}
                    </ChartCore>
                    {isBrushEnabled && (
                        <ChartBrush
                            colorScale={colorScale}
                            currentZoom={currentZoom}
                            height={brushChartHeight}
                            margins={margins}
                            series={processedBrushChartSeries}
                            top={
                                topChartHeight +
                                topChartBottomMargin +
                                margins.top
                            }
                            width={width}
                            xAxisOptions={xAxis}
                            onBrushChange={handleBrushChange}
                            onBrushClick={handleResetZoom}
                            onMouseEnter={() => hideTooltip()}
                        />
                    )}
                </svg>
                {isTooltipEnabled && !!tooltipData && !!tooltipLeft && (
                    <TooltipWithBounds
                        // set this to random so it correctly updates with parent bounds
                        key={Math.random()}
                        left={tooltipLeft + margins.left}
                        top={tooltipTop}
                    >
                        <TooltipPopover
                            colorScale={colorScale}
                            series={processedMainChartSeries}
                            timezoneOverride={xAxis?.timezone}
                            xValue={tooltipData.xValue}
                        />
                    </TooltipWithBounds>
                )}
                {isLegendEnabled && (
                    <LegendComponent
                        colorScale={colorScale}
                        events={processedEvents}
                        series={processedMainChartSeries}
                        onEventsStateChange={setProcessedEvents}
                        onSeriesClick={handleSeriesClickFromLegend}
                    />
                )}

                {isZoomEnabled && currentZoom && (
                    <Box position="absolute" right={margins.right + 10} top={5}>
                        <Button onClick={handleResetZoom}>
                            {t("label.reset-zoom")}
                        </Button>
                    </Box>
                )}
            </div>
        );
    };
