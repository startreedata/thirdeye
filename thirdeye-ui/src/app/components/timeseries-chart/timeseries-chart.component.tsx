import { Grid } from "@material-ui/core";
import AxisBottom from "@visx/axis/lib/axis/AxisBottom";
import AxisLeft from "@visx/axis/lib/axis/AxisLeft";
import { Brush } from "@visx/brush";
import { Bounds } from "@visx/brush/lib/types";
import localPoint from "@visx/event/lib/localPoint";
import Group from "@visx/group/lib/Group";
import { Legend } from "@visx/legend";
import { PatternLines } from "@visx/pattern";
import { scaleLinear, scaleOrdinal, scaleTime } from "@visx/scale";
import AreaClosed from "@visx/shape/lib/shapes/AreaClosed";
import Bar from "@visx/shape/lib/shapes/Bar";
import Line from "@visx/shape/lib/shapes/Line";
import LinePath from "@visx/shape/lib/shapes/LinePath";
import {
    defaultStyles,
    Tooltip,
    TooltipWithBounds,
    withTooltip,
} from "@visx/tooltip";
import { bisector, extent, format as d3Format, max, timeFormat } from "d3";
import React, { useCallback, useMemo, useState } from "react";
import {
    TimeSeriesChartProps,
    TimeSeriesProps,
} from "./timeseries-chart.interfaces";
import {
    selectedBrushStyle,
    useTimeseriesChartStyles,
} from "./timeseries-chart.styles";

const chartSeparation = 30;

// Utils functions
const format = timeFormat("%b %d");
const formatDateDetailed = timeFormat("%b %d, %H:%M %p");
const formatDate = (
    date: Date | number | { valueOf(): number },
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    _i: number
): string => format(date as Date);

const formatValue = (d: number | { valueOf(): number }): string =>
    d3Format("~s")(d);
const bisectDate = bisector<TimeSeriesProps, Date>((d) => new Date(d.timestamp))
    .left;

// Constants
const COLORS = {
    current: "#1B1B1E",
    expacted: "#FF9505",
    shaded: "#1CAFED",
};
// Margin for bursh
const brushMargin = { top: 10, bottom: 15, left: 50, right: 20 };

const ordinalColor2Scale = scaleOrdinal({
    domain: ["Current", "Baseline", "UpperBound", "LowerBound"],
    range: [COLORS.current, COLORS.expacted, COLORS.shaded, COLORS.expacted],
});

// accessors
const getDate = (d: TimeSeriesProps): Date => d.timestamp;
const getValue = (d: TimeSeriesProps): number =>
    isNaN(d.current) ? 0 : d.current;
const getBaseline = (d: TimeSeriesProps): number =>
    isNaN(d.expacted) ? 0 : d.expacted;
const getLowerBound = (d: TimeSeriesProps): number =>
    isNaN(d.lowerBound) ? d.current : d.lowerBound;
const getUpperBound = (d: TimeSeriesProps): number =>
    isNaN(d.current) ? 0 : d.current;

export const TimeSeriesChart = withTooltip<
    TimeSeriesChartProps,
    TimeSeriesProps
>((props) => {
    const {
        height,
        width,
        margin,
        data,
        showTooltip,
        hideTooltip,
        tooltipData,
        tooltipTop = 0,
        tooltipLeft = 0,
        compact = false,
    } = props;

    const [filteredData, setFilteredData] = useState(data);
    const timeseriesChartClasses = useTimeseriesChartStyles();

    // brush handler
    const onBrushChange = (domain: Bounds | null): void => {
        if (!domain) {
            return;
        }
        const { x0, x1, y0, y1 } = domain;
        const dataCopy = data.filter((s) => {
            const x = getDate(s).getTime();
            const y = getValue(s);

            return x > x0 && x < x1 && y > y0 && y < y1;
        });
        // If length is 0 graph will shows blank
        if (dataCopy && dataCopy.length) {
            setFilteredData(dataCopy);
        }
    };

    const innerHeight = height - margin.top - margin.bottom;
    const topChartBottomMargin = compact
        ? chartSeparation / 2
        : chartSeparation + 10;
    const topChartHeight = 0.8 * innerHeight - topChartBottomMargin;
    const bottomChartHeight = innerHeight - topChartHeight - chartSeparation;

    // bounds
    const xMax = Math.max(width - margin.left - margin.right, 0);
    const yMax = Math.max(topChartHeight, 0);
    const xBrushMax = Math.max(width - brushMargin.left - brushMargin.right, 0);
    const yBrushMax = Math.max(
        bottomChartHeight - brushMargin.top - brushMargin.bottom,
        0
    );

    // scales
    const dateScale = useMemo(
        () =>
            scaleTime<number>({
                range: [0, xMax],
                domain: extent(filteredData, getDate) as [Date, Date],
            }),
        [xMax, filteredData]
    );
    const valueScale = useMemo(
        () =>
            scaleLinear<number>({
                range: [yMax, 0],
                domain: [0, max(filteredData, getValue) || 0],
                nice: true,
            }),
        [yMax, filteredData]
    );
    const brushDateScale = useMemo(
        () =>
            scaleTime<number>({
                range: [0, xBrushMax],
                domain: extent(data, getDate) as [Date, Date],
            }),
        [xBrushMax, data]
    );
    const brushValueScale = useMemo(
        () =>
            scaleLinear({
                range: [yBrushMax, 0],
                domain: [0, max(data, getValue) || 0],
                nice: true,
            }),
        [yBrushMax, data]
    );

    // Initial position for brush
    const initialBrushPosition = useMemo(
        () => ({
            start: { x: brushDateScale(getDate(data[25])) },
            end: { x: brushDateScale(getDate(data[25])) },
        }),
        [brushDateScale, data]
    );

    // tooltip handler
    const handleTooltip = useCallback(
        (
            event:
                | React.TouchEvent<SVGRectElement>
                | React.MouseEvent<SVGRectElement>
        ) => {
            const { x } = localPoint(event) || { x: 0 };
            const x0 = dateScale.invert(x - margin.left);
            const index = bisectDate(filteredData, x0, 1);
            const d0 = filteredData[index - 1];
            const d1 = filteredData[index];
            let d = d0;
            if (d1 && getDate(d1)) {
                d =
                    x0.valueOf() - getDate(d0).valueOf() >
                    getDate(d1).valueOf() - x0.valueOf()
                        ? d1
                        : d0;
            }
            showTooltip({
                tooltipData: d,
                tooltipLeft: x - margin.left,
                tooltipTop: valueScale(getValue(d)),
            });
        },
        [showTooltip, valueScale, dateScale, filteredData, margin]
    );

    const areaProps = {
        data: filteredData,
        fill: "#1CAFED",
        opacity: 0.5,
        stroke: "#1CAFED",
        strokeWidth: 1,
        x: (d: TimeSeriesProps): number => dateScale(getDate(d)) ?? 0,
        y0: (d: TimeSeriesProps): number => valueScale(getLowerBound(d)) ?? 0,
        y1: (d: TimeSeriesProps): number => valueScale(getUpperBound(d)) ?? 0,
        yScale: valueScale,
    };

    const commonGraphProps = {
        data: filteredData,
        shapeRendering: "geometricPrecision",
        strokeWidth: 2,
        x: (d: TimeSeriesProps): number => dateScale(getDate(d)) ?? 0,
    };

    const currentLineProps = {
        stroke: COLORS.current,
        y: (d: TimeSeriesProps): number => valueScale(getValue(d)) ?? 0,
    };

    const expectedLineProps = {
        stroke: COLORS.expacted,
        strokeDasharray: "5, 5",
        y: (d: TimeSeriesProps): number => valueScale(getBaseline(d)) ?? 0,
    };

    return width < 10 ? null : (
        <div>
            <svg height={height} width={width}>
                <Group left={margin.left} top={margin.top}>
                    <AreaClosed<TimeSeriesProps> {...areaProps} />
                    <LinePath<TimeSeriesProps>
                        {...commonGraphProps}
                        {...currentLineProps}
                    />
                    <LinePath<TimeSeriesProps>
                        {...commonGraphProps}
                        {...expectedLineProps}
                    />
                    <AxisBottom
                        scale={dateScale}
                        tickFormat={formatDate}
                        tickStroke={"rgba(0,0,0,0.85)"}
                        top={yMax}
                    />

                    <AxisLeft
                        scale={valueScale}
                        tickFormat={formatValue}
                        tickStroke={"rgba(0,0,0,0.85)"}
                    />
                    <Bar
                        fill="transparent"
                        height={yMax}
                        rx={14}
                        width={xMax}
                        onMouseLeave={(): void => hideTooltip()}
                        onMouseMove={handleTooltip}
                        onTouchMove={handleTooltip}
                        onTouchStart={handleTooltip}
                    />
                    {tooltipData && (
                        <g>
                            <Line
                                from={{ x: tooltipLeft, y: margin.top }}
                                pointerEvents="none"
                                stroke={"#75daad"}
                                strokeDasharray="5,2"
                                strokeWidth={2}
                                to={{ x: tooltipLeft, y: yMax + margin.top }}
                            />
                            <circle
                                cx={tooltipLeft}
                                cy={tooltipTop + 1}
                                fill="black"
                                fillOpacity={0.1}
                                pointerEvents="none"
                                r={4}
                                stroke="black"
                                strokeOpacity={0.1}
                                strokeWidth={2}
                            />
                            <circle
                                cx={tooltipLeft}
                                cy={tooltipTop}
                                fill={"stealblue"}
                                pointerEvents="none"
                                r={4}
                                stroke="white"
                                strokeWidth={2}
                            />
                        </g>
                    )}
                </Group>
                <Group
                    left={brushMargin.left}
                    top={
                        topChartHeight + topChartBottomMargin + brushMargin.top
                    }
                >
                    <AreaClosed<TimeSeriesProps>
                        {...areaProps}
                        data={data}
                        x={(d): number => brushDateScale(getDate(d)) ?? 0}
                        y0={(d): number =>
                            brushValueScale(getLowerBound(d)) ?? 0
                        }
                        y1={(d): number =>
                            brushValueScale(getUpperBound(d)) ?? 0
                        }
                        yScale={brushValueScale}
                    />
                    <LinePath<TimeSeriesProps>
                        {...commonGraphProps}
                        {...currentLineProps}
                        data={data}
                        x={(d): number => brushDateScale(getDate(d)) ?? 0}
                        y={(d): number => brushValueScale(getValue(d)) ?? 0}
                    />
                    <LinePath<TimeSeriesProps>
                        {...commonGraphProps}
                        {...expectedLineProps}
                        data={data}
                        x={(d): number => brushDateScale(getDate(d)) ?? 0}
                        y={(d): number => brushValueScale(getBaseline(d)) ?? 0}
                    />
                    <AxisBottom
                        scale={brushDateScale}
                        tickFormat={formatDate}
                        tickStroke={"rgba(0,0,0,0.85)"}
                        top={yBrushMax}
                    />
                    <PatternLines
                        height={8}
                        id={"brush_pattern"}
                        orientation={["diagonal"]}
                        stroke={"black"}
                        strokeWidth={1}
                        width={8}
                    />
                    <Brush
                        brushDirection="horizontal"
                        handleSize={8}
                        height={yBrushMax}
                        initialBrushPosition={initialBrushPosition}
                        margin={brushMargin}
                        resizeTriggerAreas={["left", "right"]}
                        selectedBoxStyle={selectedBrushStyle}
                        width={xBrushMax}
                        xScale={brushDateScale}
                        yScale={brushValueScale}
                        onChange={onBrushChange}
                        onClick={(): void => setFilteredData(data)}
                    />
                </Group>
            </svg>
            {tooltipData && (
                <div>
                    <TooltipWithBounds
                        className={timeseriesChartClasses.tooltipWithBounds}
                        key={getDate(tooltipData).getTime()}
                        left={tooltipLeft + margin.left + 12}
                        style={defaultStyles}
                        top={tooltipTop + margin.top - 12}
                    >
                        <Grid container direction="column">
                            <Grid item>Current: {getValue(tooltipData)}</Grid>
                            <Grid item>
                                Baseline: {getBaseline(tooltipData)}
                            </Grid>
                            <Grid item>
                                UpperBound: {getUpperBound(tooltipData)}
                            </Grid>
                            <Grid item>
                                LowerBound: {getLowerBound(tooltipData)}
                            </Grid>
                        </Grid>
                    </TooltipWithBounds>
                    <Tooltip
                        className={timeseriesChartClasses.tooltip}
                        left={tooltipLeft + margin.left}
                        style={defaultStyles}
                        top={topChartHeight + margin.top - 14}
                    >
                        {formatDateDetailed(getDate(tooltipData))}
                    </Tooltip>
                </div>
            )}
            <Legend scale={ordinalColor2Scale} />
        </div>
    );
});
