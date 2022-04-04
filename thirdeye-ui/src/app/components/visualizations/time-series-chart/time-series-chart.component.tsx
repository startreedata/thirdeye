import { ParentSize } from "@visx/responsive";
import { scaleOrdinal } from "@visx/scale";
import React, { FunctionComponent, useEffect, useState } from "react";
import { ChartBrush } from "./chart-brush/chart-brush.component";
import { ChartCore } from "./chart-core/chart-core.component";
import { Legend } from "./legend/legend.component";
import {
    Series,
    TimeSeriesChartInternalProps,
    TimeSeriesChartProps,
} from "./time-series-chart.interfaces";
import { normalizeSeries } from "./time-series-chart.utils";

const TOP_CHART_HEIGHT_RATIO = 0.9;
const CHART_SEPARATION = 50;
const CHART_MARGINS = {
    top: 20,
    left: 50,
    bottom: 20,
    right: 20,
};
const COLOR_PALETTE = [
    "#fd7f6f",
    "#7eb0d5",
    "#b2e061",
    "#bd7ebe",
    "#ffb55a",
    "#ffee65",
    "#beb9db",
    "#fdcce5",
    "#8bd3c7",
];

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
 *         enabled: false
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
> = ({ series, legend, brush, height, width, xAxis, yAxis }) => {
    const [processedMainChartSeries, setProcessedMainChartSeries] =
        useState<Series[]>(series);
    const [processedBrushChartSeries, setProcessedBrushChartSeries] =
        useState<Series[]>(series);
    const [enabledDisabledMapping, setEnabledDisabledMapping] = useState<
        boolean[]
    >(series.map(() => true));

    // Legend should take on the value of the option if it exists otherwise default to true
    const isLegendEnabled = legend === undefined ? true : legend;
    const isBrushEnabled = brush === undefined ? true : brush;
    const isXAxisEnabled =
        xAxis === undefined
            ? true
            : xAxis.enabled === undefined
            ? true
            : xAxis.enabled;
    const isYAxisEnabled = yAxis === undefined ? true : yAxis;
    let topChartHeight;
    let brushChartHeight;
    let topChartBottomMargin;

    const innerHeight = height - CHART_MARGINS.top - CHART_MARGINS.bottom;

    if (brush) {
        topChartBottomMargin = isXAxisEnabled
            ? CHART_SEPARATION / 2
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
                    seriesData.enabled = enabledDisabledMapping[idx];

                    return seriesData;
                }),
            ]);
        processedBrushChartSeries &&
            setProcessedBrushChartSeries([
                ...processedBrushChartSeries.map((seriesData, idx) => {
                    seriesData.enabled = enabledDisabledMapping[idx];

                    return seriesData;
                }),
            ]);
    }, [enabledDisabledMapping]);

    // If series changed, reset everything
    useEffect(() => {
        setProcessedMainChartSeries(normalizeSeries(series));
        setProcessedBrushChartSeries(normalizeSeries(series));
        setEnabledDisabledMapping(series.map(() => true));
    }, [series]);

    /**
     * Flip the enabled flag and force a re-render by creating a new array
     */
    const handleSeriesClickFromLegend = (idx: number): void => {
        const copied = [...enabledDisabledMapping];
        copied[idx] = !copied[idx];
        setEnabledDisabledMapping([...copied]);
    };

    const handleBrushChange = (
        domain: { x0: number; x1: number } | null
    ): void => {
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
            copied.enabled = enabledDisabledMapping[idx];

            return copied;
        });
        setProcessedMainChartSeries(seriesDataCopy);
    };

    const handleBrushClick = (): void => {
        const seriesDataCopy = series.map((seriesData, idx) => {
            const copied = { ...seriesData };
            copied.enabled = enabledDisabledMapping[idx];

            return copied;
        });
        setProcessedMainChartSeries(seriesDataCopy);
    };

    return (
        <>
            <svg height={height} width={width}>
                <ChartCore
                    colorScale={colorScale}
                    margin={{ ...CHART_MARGINS, bottom: topChartBottomMargin }}
                    series={processedMainChartSeries}
                    showXAxis={isXAxisEnabled}
                    showYAxis={isYAxisEnabled}
                    width={width}
                    xMax={xMax}
                    yMax={yMax}
                />
                {isBrushEnabled && (
                    <ChartBrush
                        colorScale={colorScale}
                        height={brushChartHeight}
                        series={processedBrushChartSeries}
                        top={
                            topChartHeight +
                            topChartBottomMargin +
                            CHART_MARGINS.top
                        }
                        width={width}
                        onBrushChange={handleBrushChange}
                        onBrushClick={handleBrushClick}
                    />
                )}
            </svg>
            {isLegendEnabled && (
                <Legend
                    colorScale={colorScale}
                    series={processedMainChartSeries}
                    onSeriesClick={handleSeriesClickFromLegend}
                />
            )}
        </>
    );
};
