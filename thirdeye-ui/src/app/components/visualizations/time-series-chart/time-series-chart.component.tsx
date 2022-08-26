/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { ParentSize } from "@visx/responsive";
import { scaleOrdinal } from "@visx/scale";
import { TooltipWithBounds, useTooltip } from "@visx/tooltip";
import React, { FunctionComponent, useEffect, useState } from "react";
import { ChartBrush } from "./chart-brush/chart-brush.component";
import { ChartCore } from "./chart-core/chart-core.component";
import { EventsChart } from "./events-chart/events-chart.component";
import { Legend } from "./legend/legend.component";
import {
    EventWithChartState,
    NormalizedSeries,
    TimeSeriesChartInternalProps,
    TimeSeriesChartProps,
    ZoomDomain,
} from "./time-series-chart.interfaces";
import {
    COLOR_PALETTE,
    normalizeSeries,
    syncEnabledDisabled,
} from "./time-series-chart.utils";
import { TooltipMarkers } from "./tooltip/tooltip-markers.component";
import { TooltipPopover } from "./tooltip/tooltip-popover.component";

const TOP_CHART_HEIGHT_RATIO = 0.85;
const CHART_SEPARATION = 50;
const CHART_MARGINS = {
    top: 20,
    left: 50,
    bottom: 20,
    right: 20,
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
 *         }]
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
                    width={width}
                    {...props}
                />
            )}
        </ParentSize>
    );
};

export const TimeSeriesChartInternal: FunctionComponent<
    TimeSeriesChartInternalProps
> = ({
    series,
    legend,
    brush,
    height,
    width,
    xAxis,
    yAxis,
    tooltip,
    initialZoom,
    chartEvents,
    events,
    LegendComponent = Legend,
}) => {
    const [instanceId] = useState(Math.random());
    const [currentZoom, setCurrentZoom] = useState<ZoomDomain | undefined>(
        initialZoom
    );
    const [processedMainChartSeries, setProcessedMainChartSeries] = useState<
        NormalizedSeries[]
    >(normalizeSeries(series, currentZoom));
    const [processedBrushChartSeries, setProcessedBrushChartSeries] = useState<
        NormalizedSeries[]
    >(normalizeSeries(series));
    const [enabledDisabledSeriesMapping, setEnabledDisabledSeriesMapping] =
        useState<boolean[]>(series.map(syncEnabledDisabled));

    const [processedEvents, setProcessedEvents] = useState<
        EventWithChartState[]
    >([]);

    const tooltipUtils = useTooltip<{ xValue: number }>();
    const { tooltipData, tooltipLeft, tooltipTop } = tooltipUtils;

    // Legend should take on the value of the option if it exists otherwise default to true
    const isLegendEnabled = legend === undefined ? true : legend;
    const isBrushEnabled = brush === undefined ? false : brush;
    const isTooltipEnabled = tooltip === undefined ? true : tooltip;
    const isXAxisEnabled =
        xAxis === undefined
            ? true
            : xAxis.enabled === undefined
            ? true
            : xAxis.enabled;
    const isYAxisEnabled = yAxis === undefined ? true : yAxis;
    let topChartHeight: number;
    let brushChartHeight;
    let topChartBottomMargin;

    const innerHeight = height - CHART_MARGINS.top - CHART_MARGINS.bottom;

    if (brush) {
        topChartBottomMargin = isXAxisEnabled
            ? CHART_SEPARATION
            : CHART_SEPARATION + 10;
        topChartHeight =
            TOP_CHART_HEIGHT_RATIO * innerHeight - topChartBottomMargin;
        brushChartHeight = innerHeight - topChartHeight - CHART_SEPARATION;
    } else {
        topChartBottomMargin = CHART_MARGINS.top;
        topChartHeight = innerHeight - topChartBottomMargin;
        brushChartHeight = 0;
    }

    // Bounds
    const xMax = Math.max(width - CHART_MARGINS.left - CHART_MARGINS.right, 0);
    const yMax = Math.max(topChartHeight, 0);

    const colorScale = scaleOrdinal({
        domain: series.map((x) => x.name) as string[],
        range: COLOR_PALETTE,
    });

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
        console.log(instanceId, "Series changed");
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
        console.log(instanceId, "handleBrushChange");
        if (!domain) {
            return;
        }
        const { x0, x1 } = domain;

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

    const handleBrushClick = (): void => {
        console.log(instanceId, "handleBrushClick");
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

    return (
        <div style={{ position: "relative" }}>
            {events && events.length > 0 && (
                <EventsChart
                    events={processedEvents}
                    isTooltipEnabled={isTooltipEnabled}
                    margin={{ ...CHART_MARGINS, bottom: topChartBottomMargin }}
                    series={processedMainChartSeries}
                    tooltipUtils={tooltipUtils}
                    width={width}
                    xMax={xMax}
                />
            )}
            <svg height={height} width={width}>
                <ChartCore
                    colorScale={colorScale}
                    margin={{ ...CHART_MARGINS, bottom: topChartBottomMargin }}
                    series={processedMainChartSeries}
                    showXAxis={isXAxisEnabled}
                    showYAxis={isYAxisEnabled}
                    tooltipUtils={tooltipUtils}
                    width={width}
                    xAxisOptions={xAxis}
                    xMax={xMax}
                    yMax={yMax}
                >
                    {(xScale, yScale) => {
                        if (tooltipData) {
                            return (
                                <TooltipMarkers
                                    chartHeight={topChartHeight}
                                    colorScale={colorScale}
                                    series={processedMainChartSeries}
                                    xScale={xScale}
                                    xValue={tooltipData.xValue}
                                    yScale={yScale}
                                />
                            );
                        }

                        return undefined;
                    }}
                </ChartCore>
                {isBrushEnabled && (
                    <ChartBrush
                        colorScale={colorScale}
                        height={brushChartHeight}
                        initialZoom={currentZoom}
                        margins={CHART_MARGINS}
                        series={processedBrushChartSeries}
                        top={
                            topChartHeight +
                            topChartBottomMargin +
                            CHART_MARGINS.top
                        }
                        width={width}
                        xAxisOptions={xAxis}
                        onBrushChange={handleBrushChange}
                        onBrushClick={handleBrushClick}
                    />
                )}
            </svg>
            {isTooltipEnabled && !!tooltipData && !!tooltipLeft && (
                <TooltipWithBounds
                    // set this to random so it correctly updates with parent bounds
                    key={Math.random()}
                    left={tooltipLeft + CHART_MARGINS.left}
                    top={tooltipTop}
                >
                    <TooltipPopover
                        colorScale={colorScale}
                        series={processedMainChartSeries}
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
        </div>
    );
};
