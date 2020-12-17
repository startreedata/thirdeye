import { Grid, Typography } from "@material-ui/core";
import AxisBottom from "@visx/axis/lib/axis/AxisBottom";
import AxisLeft from "@visx/axis/lib/axis/AxisLeft";
import { Brush } from "@visx/brush";
import BaseBrush from "@visx/brush/lib/BaseBrush";
import { Bounds } from "@visx/brush/lib/types";
import localPoint from "@visx/event/lib/localPoint";
import Group from "@visx/group/lib/Group";
import { Legend, LegendItem, LegendLabel } from "@visx/legend";
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
import { extent, max } from "d3";
import React, {
    createRef,
    ReactElement,
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import {
    bisectDate,
    CHART_SEPRATION_HEIGHT,
    formatDate,
    formatDateDetailed,
    formatValue,
    getBaseline,
    getDate,
    getLowerBound,
    getUpperBound,
    getValue,
} from "../../utils/chart/chart-util";
import { AnomalyChart } from "../anomaly-chart/anomaly-chart.component";
import {
    TimeSeriesChartProps,
    TimeSeriesProps,
} from "./timeseries-chart.interfaces";
import {
    selectedBrushStyle,
    useTimeseriesChartStyles,
} from "./timeseries-chart.styles";

// Constants
const COLORS = {
    current: "#1B1B1E",
    expacted: "#FF9505",
    shaded: "#1CAFED",
    anomaly: "#FF0000",
};

enum CHARTS_TYPES {
    CURRENT_LINE,
    EXPECTED_LINE,
    SHADED_AREA,
    ANOMALIES,
}

const ordinalColor2Scale = scaleOrdinal({
    domain: ["Current", "Baseline", "Upper and Lower bound", "Anomaly"],
    range: [
        <rect
            fill={COLORS.current}
            height={5}
            key="current"
            stroke={COLORS.current}
            width={15}
            y={5}
        />,
        <line
            key="baseline"
            stroke={COLORS.expacted}
            strokeDasharray="5, 5"
            strokeWidth={5}
            x1={0}
            x2={15}
            y1={5}
            y2={5}
        />,
        <rect
            fill={COLORS.shaded}
            height={15}
            key="upper-lower-bound"
            width={15}
        />,
        <rect fill={COLORS.anomaly} height={15} key="anomaly" width={15} />,
    ],
});

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
        showLegend,
        anomalies,
    } = props;

    const [filteredData, setFilteredData] = useState(data);
    const timeseriesChartClasses = useTimeseriesChartStyles();
    const brushRef = createRef<BaseBrush>();
    const [activeChartsList, setActiveChartList] = useState<Set<number>>(
        new Set([
            CHARTS_TYPES.CURRENT_LINE,
            CHARTS_TYPES.EXPECTED_LINE,
            CHARTS_TYPES.SHADED_AREA,
            CHARTS_TYPES.ANOMALIES,
        ])
    );
    const { t } = useTranslation();

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
        ? CHART_SEPRATION_HEIGHT / 2
        : CHART_SEPRATION_HEIGHT + 10;
    const topChartHeight = 0.8 * innerHeight - topChartBottomMargin;
    const bottomChartHeight =
        innerHeight - topChartHeight - CHART_SEPRATION_HEIGHT;

    // bounds
    const xMax = Math.max(width - margin.left - margin.right, 0);
    const yMax = Math.max(topChartHeight, 0);
    const xBrushMax = Math.max(width - margin.left - margin.right, 0);
    const yBrushMax = Math.max(
        bottomChartHeight - margin.top - margin.bottom,
        0
    );

    useEffect(() => {
        // Reset chart when data changes
        brushRef.current?.reset();
        setFilteredData(data);
    }, [data]);

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
                domain: [0, max(filteredData, getValue) || 100],
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
            d &&
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
                {/* Main Graph Region */}
                <Group left={margin.left} top={margin.top}>
                    {/* Shaded region */}
                    {activeChartsList?.has(CHARTS_TYPES.SHADED_AREA) && (
                        <AreaClosed<TimeSeriesProps> {...areaProps} />
                    )}
                    {/* Line graph with Current value */}
                    {activeChartsList?.has(CHARTS_TYPES.CURRENT_LINE) && (
                        <LinePath<TimeSeriesProps>
                            {...commonGraphProps}
                            {...currentLineProps}
                        />
                    )}
                    {/* Line graph with expacted value */}
                    {activeChartsList?.has(CHARTS_TYPES.EXPECTED_LINE) && (
                        <LinePath<TimeSeriesProps>
                            {...commonGraphProps}
                            {...expectedLineProps}
                        />
                    )}
                    {/* X-Axis */}
                    <AxisBottom
                        scale={dateScale}
                        tickFormat={formatDate}
                        tickStroke={"rgba(0,0,0,0.85)"}
                        top={yMax}
                    />
                    {/* Y-Axis */}
                    <AxisLeft
                        scale={valueScale}
                        tickFormat={formatValue}
                        tickStroke={"rgba(0,0,0,0.85)"}
                    />
                    {/* Show Anomaly with Line & Dots */}
                    {activeChartsList?.has(CHARTS_TYPES.ANOMALIES) && (
                        <AnomalyChart
                            anomalies={anomalies}
                            xScale={dateScale}
                            yScale={valueScale}
                        />
                    )}
                    {/* Transparent Layer for Tooltip */}
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
                    {/* Visuals for Tooltip */}
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

                {/* Brush Region */}
                <Group
                    left={margin.left}
                    top={topChartHeight + topChartBottomMargin + margin.top}
                >
                    {/* Shaded region */}
                    {activeChartsList?.has(CHARTS_TYPES.SHADED_AREA) && (
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
                    )}
                    {/* Line graph with Current value */}
                    {activeChartsList?.has(CHARTS_TYPES.CURRENT_LINE) && (
                        <LinePath<TimeSeriesProps>
                            {...commonGraphProps}
                            {...currentLineProps}
                            data={data}
                            x={(d): number => brushDateScale(getDate(d)) ?? 0}
                            y={(d): number => brushValueScale(getValue(d)) ?? 0}
                        />
                    )}
                    {/* Line graph with expacted value */}
                    {activeChartsList?.has(CHARTS_TYPES.EXPECTED_LINE) && (
                        <LinePath<TimeSeriesProps>
                            {...commonGraphProps}
                            {...expectedLineProps}
                            data={data}
                            x={(d): number => brushDateScale(getDate(d)) ?? 0}
                            y={(d): number =>
                                brushValueScale(getBaseline(d)) ?? 0
                            }
                        />
                    )}
                    {/* Show Anomaly Circless */}
                    {activeChartsList?.has(CHARTS_TYPES.ANOMALIES) && (
                        <AnomalyChart
                            anomalies={anomalies}
                            dotRadius={3}
                            xScale={brushDateScale}
                            yScale={brushValueScale}
                        />
                    )}
                    {/* X-Axis for brush area */}
                    <AxisBottom
                        scale={brushDateScale}
                        tickFormat={formatDate}
                        tickStroke={"rgba(0,0,0,0.85)"}
                        top={yBrushMax}
                    />
                    {/* Visual for selected region on brush */}
                    <PatternLines
                        height={8}
                        id={"brush_pattern"}
                        orientation={["diagonal"]}
                        stroke={"black"}
                        strokeWidth={1}
                        width={8}
                    />
                    {/* Brush component */}
                    <Brush
                        brushDirection="horizontal"
                        handleSize={8}
                        height={yBrushMax}
                        innerRef={brushRef}
                        // initialBrushPosition={initialBrushPosition}
                        margin={margin}
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

            {/* Legends for graph with interactions */}
            {showLegend && (
                <Legend scale={ordinalColor2Scale}>
                    {(labels): ReactElement => (
                        <div className={timeseriesChartClasses.legends}>
                            {labels.map((label, i) => {
                                const shape = ordinalColor2Scale(label.datum);
                                const isValidElement = React.isValidElement(
                                    shape
                                );

                                return (
                                    <LegendItem
                                        alignItems=""
                                        className={`${
                                            !activeChartsList.has(label.index)
                                                ? timeseriesChartClasses.grayout
                                                : ""
                                        } ${timeseriesChartClasses.clickable}`}
                                        flexDirection="row"
                                        key={`legend-quantile-${i}`}
                                        onClick={(): void => {
                                            console.log(label);
                                            const { index } = label;
                                            const newArray = new Set(
                                                activeChartsList
                                            );
                                            if (newArray.has(index)) {
                                                newArray.delete(index);
                                            } else {
                                                newArray?.add(index);
                                            }
                                            if (newArray.size === 0) {
                                                newArray.add(
                                                    CHARTS_TYPES.CURRENT_LINE
                                                );
                                            }
                                            setActiveChartList(newArray);
                                        }}
                                    >
                                        <div
                                            className={`visx-legend-shape ${timeseriesChartClasses.legendsShape}`}
                                        >
                                            <svg height={15} width={15}>
                                                {isValidElement
                                                    ? React.cloneElement(
                                                          shape as React.ReactElement
                                                      )
                                                    : null}
                                            </svg>
                                        </div>
                                        <LegendLabel
                                            align="left"
                                            flex="0 0 100%"
                                            margin="0 4px"
                                            wrap="wrap"
                                        >
                                            {label.text}
                                        </LegendLabel>
                                    </LegendItem>
                                );
                            })}
                        </div>
                    )}
                </Legend>
            )}

            {/* Tooltip for hovered region */}
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
            {!data.length ? (
                <Typography
                    className={timeseriesChartClasses.noDataLabel}
                    variant="h6"
                >
                    {t("label.no-chart-data-available")}
                </Typography>
            ) : null}
        </div>
    );
});
