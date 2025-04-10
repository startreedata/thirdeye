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
import { Box, Button } from "@material-ui/core";
import { Bounds } from "@visx/brush/lib/types";
import { ParentSize } from "@visx/responsive";
import { scaleOrdinal, scaleTime } from "@visx/scale";
import { TooltipWithBounds, useTooltip } from "@visx/tooltip";
import React, {
    FunctionComponent,
    MouseEvent,
    ReactNode,
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
import { LegendPlacement } from "../../alert-wizard-v2/alert-template/preview-chart/preview-chart.interfaces";

const MIN_DATA_POINTS_TO_DISPLAY = 14;
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
        svgContainerUseAuto,
        // Zoom override takes precedence
        zoomOverride,
        tooltipPositionOverride,
        legendsPlacement = LegendPlacement.BOTTOM,
        colorPalette,
    }) => {
        const { t } = useTranslation();
        const [currentZoom, setCurrentZoom] = useState<ZoomDomain | undefined>(
            initialZoom || zoomOverride
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
                range: colorPalette || COLOR_PALETTE,
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
        const handleSeriesClickFromLegend = (
            idx: number,
            flagToSet: boolean
        ): void => {
            setEnabledDisabledSeriesMapping((current) => {
                const copied = [...current];
                copied[idx] = flagToSet;

                return copied;
            });
        };

        const handleBrushChange = (domain: ZoomDomain | null): void => {
            const seriesDataCopy = series.map((seriesData) => {
                return {
                    ...seriesData,
                    data: [...seriesData.data],
                };
            });

            if (domain) {
                let { x0, x1 } = domain;
                // Ensure a minimum of 14 data points are in view
                while (
                    bestGuessGranularity &&
                    x0 > minMaxValues[0] &&
                    x1 < minMaxValues[1] &&
                    (x1 - x0) / bestGuessGranularity <
                        MIN_DATA_POINTS_TO_DISPLAY
                ) {
                    x0 = x0 - bestGuessGranularity;
                    x1 = x1 + bestGuessGranularity;
                }

                seriesDataCopy.forEach((seriesData, idx) => {
                    seriesData.data = seriesData.data.filter((d) => {
                        const x = d.x;

                        return x > x0 && x < x1;
                    });
                    seriesData.enabled = enabledDisabledSeriesMapping[idx];
                });
            }

            setProcessedMainChartSeries(normalizeSeries(seriesDataCopy));
            setCurrentZoom(
                domain ? { x0: domain.x0, x1: domain.x1 } : undefined
            );

            if (chartEvents && chartEvents.onZoomChange) {
                chartEvents.onZoomChange(domain);
            }
        };

        // If zoomOverride changes, sync it with current
        useEffect(() => {
            handleBrushChange(zoomOverride ? zoomOverride : null);
        }, [zoomOverride]);

        const handleResetZoom = (): void => {
            let shouldContinue: boolean | undefined = true;
            /**
             * If chart `onZoomReset` exists, determine whether to continue
             * changing the zoom window by the value returned by `onZoomReset`
             *
             * Falsey will prevent the zoom window change and truthy will
             * continue the window change
             */
            if (chartEvents?.onZoomReset) {
                shouldContinue = chartEvents.onZoomReset();
            }

            if (!shouldContinue) {
                return;
            }

            handleBrushChange(null);
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

            let shouldContinue: boolean | undefined = true;

            if (chartEvents?.onPositionTooltipChange) {
                shouldContinue = chartEvents.onPositionTooltipChange([
                    xValue,
                    coords.y,
                ]);
            }

            shouldContinue && handleTooltipPositionChange([xValue, coords.y]);
        };

        const handleTooltipPositionChange = (
            xValueYPosition: [number, number] | undefined
        ): void => {
            if (!xValueYPosition) {
                hideTooltip();

                return;
            }

            const [xValue, yPosition] = xValueYPosition;

            showTooltip({
                tooltipLeft: dateScaleForHandleMouseOver(xValue),
                tooltipTop: yPosition - margins.top,
                tooltipData: {
                    xValue,
                },
            });
        };

        // If tooltipPositionOverride changes, sync it with current
        useEffect(() => {
            handleTooltipPositionChange(tooltipPositionOverride);
        }, [tooltipPositionOverride]);

        const shouldRenderSelectionComponent =
            isZoomEnabled || chartEvents?.onRangeSelection;

        const handleZoomChange = (domain: Bounds | null): void => {
            let shouldZoom: boolean | undefined = true;

            /**
             * If chart `onRangeSelection` exists, determine whether to continue
             * changing the zoom window by the value returned by `onRangeSelection`
             *
             * Falsey will prevent the zoom window change and truthy will
             * continue the window change
             */
            if (chartEvents?.onRangeSelection) {
                shouldZoom = chartEvents.onRangeSelection(domain);
            }

            shouldZoom && isZoomEnabled && handleBrushChange(domain);
        };

        const renderLegends = (): ReactNode => (
            <LegendComponent
                colorScale={colorScale}
                events={processedEvents}
                series={processedMainChartSeries}
                onEventsStateChange={setProcessedEvents}
                onSeriesClick={handleSeriesClickFromLegend}
            />
        );

        return (
            <div style={{ position: "relative" }}>
                {isLegendEnabled && legendsPlacement === LegendPlacement.TOP && (
                    <Box marginLeft="auto" mb={2} width="max-content">
                        {renderLegends()}
                    </Box>
                )}

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
                <svg
                    height={height}
                    width={svgContainerUseAuto ? "auto" : width}
                >
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
                                    {shouldRenderSelectionComponent &&
                                        processedMainChartSeries && (
                                            <ChartZoom
                                                colorScale={colorScale}
                                                height={topChartHeight}
                                                key={`${currentZoom?.toString()}`}
                                                margins={margins}
                                                series={
                                                    processedMainChartSeries
                                                }
                                                width={width}
                                                onZoomChange={handleZoomChange}
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
                            hideTime={xAxis?.hideTime}
                            series={processedMainChartSeries}
                            timezoneOverride={xAxis?.timezone}
                            xValue={tooltipData.xValue}
                        />
                    </TooltipWithBounds>
                )}
                {isLegendEnabled &&
                    legendsPlacement === LegendPlacement.BOTTOM &&
                    renderLegends()}

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
