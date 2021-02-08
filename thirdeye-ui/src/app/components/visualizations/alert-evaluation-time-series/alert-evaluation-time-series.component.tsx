import BaseBrush from "@visx/brush/lib/BaseBrush";
import { Bounds } from "@visx/brush/lib/types";
import {
    Bar,
    Brush,
    Circle,
    Group,
    Line,
    localPoint,
    ParentSize,
    Point,
    scaleLinear,
    scaleTime,
    useTooltip,
} from "@visx/visx";
import bounds from "binary-search-bounds";
import { debounce, isEmpty } from "lodash";
import React, {
    createRef,
    FunctionComponent,
    MouseEvent,
    useCallback,
    useEffect,
    useMemo,
    useReducer,
} from "react";
import { useTranslation } from "react-i18next";
import { Dimension } from "../../../utils/material-ui/dimension.util";
import { Palette } from "../../../utils/material-ui/palette.util";
import {
    filterAlertEvaluationAnomalyPointsByTime,
    filterAlertEvaluationTimeSeriesPointsByTime,
    getAlertEvaluationAnomalyPoints,
    getAlertEvaluationTimeSeriesPoints,
    getAlertEvaluationTimeSeriesPointsMaxTimestamp,
    getAlertEvaluationTimeSeriesPointsMaxValue,
    getAlertEvaluationTimeSeriesPointsMinTimestamp,
} from "../../../utils/visualization/visualization.util";
import { LoadingIndicator } from "../../loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LinearAxisLeft } from "../linear-axis-left/linear-axis-left.component";
import { TimeAxisBottom } from "../time-axis-bottom/time-axis-bottom.component";
import { AlertEvaluationTimeSeriesAnomaliesPlot } from "./alert-evaluation-time-series-anomalies-plot/alert-evaluation-time-series-anomalies-plot.component";
import { AlertEvaluationTimeSeriesBaselinePlot } from "./alert-evaluation-time-series-baseline-plot/alert-evaluation-time-series-baseline-plot.component";
import { AlertEvaluationTimeSeriesCurrentPlot } from "./alert-evaluation-time-series-current-plot/alert-evaluation-time-series-current-plot.component";
import { AlertEvaluationTimeSeriesLegend } from "./alert-evaluation-time-series-legend/alert-evaluation-time-series-legend.component";
import { AlertEvaluationTimeSeriesUpperAndLowerBoundPlot } from "./alert-evaluation-time-series-upper-and-lower-bound-plot/alert-evaluation-time-series-upper-and-lower-bound-plot.component";
import {
    AlertEvaluationTimeSeriesInternalProps,
    AlertEvaluationTimeSeriesInternalStateAction,
    AlertEvaluationTimeSeriesPlot,
    AlertEvaluationTimeSeriesPoint,
    AlertEvaluationTimeSeriesProps,
    AlertEvaluationTimeSeriesTooltipPoint,
} from "./alert-evaluation-time-series.interfaces";
import { alertEvaluationTimeSeriesInternalReducer } from "./alert-evaluation-time-series.reducer";

const HEIGHT_CONTAINER_MIN = 310;
const WIDTH_CONTAINER_MIN = 520;
const PADDING_SVG_TOP = 10;
const PADDING_SVG_BOTTOM = 30;
const PADDING_SVG_LEFT = 50;
const PADDING_SVG_RIGHT = 50;
const HEIGHT_SEPARATOR_TIME_SERIES_BRUSH = 60;
const HEIGHT_BRUSH = 100;
const HEIGHT_LEGEND = 30;

// Simple wrapper around AlertEvaluationTimeSeriesInternal to capture parent container dimensions
export const AlertEvaluationTimeSeries: FunctionComponent<AlertEvaluationTimeSeriesProps> = (
    props: AlertEvaluationTimeSeriesProps
) => {
    return (
        <ParentSize>
            {(parent) => (
                <AlertEvaluationTimeSeriesInternal
                    alertEvaluation={props.alertEvaluation}
                    height={parent.height}
                    width={parent.width}
                />
            )}
        </ParentSize>
    );
};

const AlertEvaluationTimeSeriesInternal: FunctionComponent<AlertEvaluationTimeSeriesInternalProps> = (
    props: AlertEvaluationTimeSeriesInternalProps
) => {
    const [
        {
            loading,
            noData,
            alertEvaluationTimeSeriesPoints,
            filteredAlertEvaluationTimeSeriesPoints,
            alertEvaluationAnomalyPoints,
            filteredAlertEvaluationAnomalyPoints,
            currentPlotVisible,
            baselinePlotVisible,
            upperAndLowerBoundPlotVisible,
            anomaliesPlotVisible,
        },
        dispatch,
    ] = useReducer(alertEvaluationTimeSeriesInternalReducer, {
        loading: true,
        noData: false,
        alertEvaluationTimeSeriesPoints: [],
        filteredAlertEvaluationTimeSeriesPoints: [],
        alertEvaluationAnomalyPoints: [],
        filteredAlertEvaluationAnomalyPoints: [],
        currentPlotVisible: true,
        baselinePlotVisible: true,
        upperAndLowerBoundPlotVisible: true,
        anomaliesPlotVisible: true,
    });
    const {
        tooltipData,
        showTooltip,
        hideTooltip,
    } = useTooltip<AlertEvaluationTimeSeriesTooltipPoint>();
    const brushRef = createRef<BaseBrush>();
    const { t } = useTranslation();

    // SVG bounds
    const svgWidth = props.width; // Container width
    const svgHeight = props.height - HEIGHT_LEGEND; // Container height - space for legend

    // Time series bounds
    const timeSeriesHeight =
        svgHeight -
        PADDING_SVG_TOP -
        HEIGHT_SEPARATOR_TIME_SERIES_BRUSH -
        HEIGHT_BRUSH; // Available SVG height - top SVG padding - separator height between time series and brush - space for brush
    const timeSeriesXMax = svgWidth - PADDING_SVG_LEFT - PADDING_SVG_RIGHT; // Available SVG width - left and right SVG padding
    const timeSeriesYMax = timeSeriesHeight;

    // Brush bounds
    const brushHeight = HEIGHT_BRUSH - PADDING_SVG_BOTTOM; // Brush height - bottom SVG padding
    const brushXMax = svgWidth - PADDING_SVG_LEFT - PADDING_SVG_RIGHT; // Available SVG width - left and right SVG padding
    const brushYMax = brushHeight;

    // Time series scales
    const timeSeriesXScale = useMemo(() => {
        return scaleTime<number>({
            range: [0, timeSeriesXMax],
            domain: [
                getAlertEvaluationTimeSeriesPointsMinTimestamp(
                    filteredAlertEvaluationTimeSeriesPoints
                ),
                getAlertEvaluationTimeSeriesPointsMaxTimestamp(
                    filteredAlertEvaluationTimeSeriesPoints
                ),
            ],
            clamp: true,
        });
    }, [props.width, filteredAlertEvaluationTimeSeriesPoints]);
    const timeSeriesYScale = useMemo(() => {
        return scaleLinear<number>({
            range: [timeSeriesYMax, 0],
            domain: [
                0,
                getAlertEvaluationTimeSeriesPointsMaxValue(
                    filteredAlertEvaluationTimeSeriesPoints
                ),
            ],
            nice: true,
            clamp: true,
        });
    }, [props.height, filteredAlertEvaluationTimeSeriesPoints]);

    // Brush scales
    const brushXScale = useMemo(() => {
        return scaleTime<number>({
            range: [0, brushXMax],
            domain: [
                getAlertEvaluationTimeSeriesPointsMinTimestamp(
                    alertEvaluationTimeSeriesPoints
                ),
                getAlertEvaluationTimeSeriesPointsMaxTimestamp(
                    alertEvaluationTimeSeriesPoints
                ),
            ],
            clamp: true,
        });
    }, [props.width, alertEvaluationTimeSeriesPoints]);
    const brushYScale = useMemo(() => {
        return scaleLinear<number>({
            range: [brushYMax, 0],
            domain: [
                0,
                getAlertEvaluationTimeSeriesPointsMaxValue(
                    alertEvaluationTimeSeriesPoints
                ),
            ],
            nice: true,
            clamp: true,
        });
    }, [props.height, alertEvaluationTimeSeriesPoints]);

    useEffect(() => {
        // Input changed, reset
        dispatch({
            type: AlertEvaluationTimeSeriesInternalStateAction.UPDATE,
            payload: {
                loading: true,
                noData: false,
                alertEvaluationTimeSeriesPoints: [],
                filteredAlertEvaluationTimeSeriesPoints: [],
                alertEvaluationAnomalyPoints: [],
                filteredAlertEvaluationAnomalyPoints: [],
            },
        });
        // Reset brush
        brushRef && brushRef.current && brushRef.current.reset();

        if (!props.alertEvaluation) {
            return;
        }

        const newAlertEvaluationTimeSeriesPoints = getAlertEvaluationTimeSeriesPoints(
            props.alertEvaluation
        );
        const newAlertEvaluationAnomalyPoints = getAlertEvaluationAnomalyPoints(
            props.alertEvaluation
        );
        if (isEmpty(newAlertEvaluationTimeSeriesPoints)) {
            dispatch({
                type: AlertEvaluationTimeSeriesInternalStateAction.UPDATE,
                payload: {
                    loading: false,
                    noData: true,
                },
            });

            return;
        }

        dispatch({
            type: AlertEvaluationTimeSeriesInternalStateAction.UPDATE,
            payload: {
                loading: false,
                noData: false,
                alertEvaluationTimeSeriesPoints: newAlertEvaluationTimeSeriesPoints,
                filteredAlertEvaluationTimeSeriesPoints: newAlertEvaluationTimeSeriesPoints,
                alertEvaluationAnomalyPoints: newAlertEvaluationAnomalyPoints,
                filteredAlertEvaluationAnomalyPoints: newAlertEvaluationAnomalyPoints,
            },
        });
    }, [props.alertEvaluation]);

    const onTimeSeriesMouseMove = (event: MouseEvent<SVGRectElement>): void => {
        onTimeSeriesMouseMoveDebounced(
            localPoint(event) as Point // Event coordinates to SVG coordinates
        );
    };

    const onTimeSeriesMouseMoveDebounced = useCallback(
        debounce((svgPoint: Point): void => {
            if (!svgPoint) {
                hideTooltip();

                return;
            }

            // Determine time series time scale value from SVG coordinate, accounting for SVG
            // padding
            const xValue = timeSeriesXScale.invert(
                svgPoint.x - PADDING_SVG_LEFT
            );

            if (!xValue) {
                hideTooltip();

                return;
            }

            // Search first alert evaluation anomaly point with timestamp less than or equal to SVG
            // coordinate timestamp
            const index = bounds.le(
                filteredAlertEvaluationTimeSeriesPoints,
                {
                    timestamp: xValue.getTime(),
                } as AlertEvaluationTimeSeriesPoint,
                (
                    alertEvaluationTimeSeriesPointA,
                    alertEvaluationTimeSeriesPointB
                ) =>
                    alertEvaluationTimeSeriesPointA.timestamp -
                    alertEvaluationTimeSeriesPointB.timestamp
            );

            if (index === -1) {
                // Not found
                hideTooltip();

                return;
            }

            showTooltip({
                tooltipTop: 0,
                tooltipLeft: 0,
                tooltipData: {
                    timestamp:
                        filteredAlertEvaluationTimeSeriesPoints[index]
                            .timestamp,
                    current:
                        filteredAlertEvaluationTimeSeriesPoints[index].current,
                    expected:
                        filteredAlertEvaluationTimeSeriesPoints[index].expected,
                    upperBound:
                        filteredAlertEvaluationTimeSeriesPoints[index]
                            .upperBound,
                    lowerBound:
                        filteredAlertEvaluationTimeSeriesPoints[index]
                            .lowerBound,
                    anomalies: [],
                },
            });
        }, 1),
        [props.width, filteredAlertEvaluationTimeSeriesPoints]
    );

    const onTimeSeriesMouseLeave = (): void => {
        hideTooltip();
    };

    const onBrushChangeDebounced = useCallback(
        debounce((domain: Bounds | null): void => {
            if (!domain || domain.x1 - domain.x0 === 0) {
                // Reset brush selection
                dispatch({
                    type: AlertEvaluationTimeSeriesInternalStateAction.UPDATE,
                    payload: {
                        filteredAlertEvaluationTimeSeriesPoints: alertEvaluationTimeSeriesPoints,
                        filteredAlertEvaluationAnomalyPoints: alertEvaluationAnomalyPoints,
                    },
                });

                return;
            }

            // Filter time series based on brush selection
            const newFilteredAlertEvaluationTimeSeriesPoints = filterAlertEvaluationTimeSeriesPointsByTime(
                alertEvaluationTimeSeriesPoints,
                domain.x0,
                domain.x1
            );
            // Filter anomalies based on brush selection
            const newFilteredAlertEvaluationAnomalyPoints = filterAlertEvaluationAnomalyPointsByTime(
                alertEvaluationAnomalyPoints,
                domain.x0,
                domain.x1
            );

            dispatch({
                type: AlertEvaluationTimeSeriesInternalStateAction.UPDATE,
                payload: {
                    filteredAlertEvaluationTimeSeriesPoints: newFilteredAlertEvaluationTimeSeriesPoints,
                    filteredAlertEvaluationAnomalyPoints: newFilteredAlertEvaluationAnomalyPoints,
                },
            });
        }, 1),
        [alertEvaluationTimeSeriesPoints, alertEvaluationAnomalyPoints]
    );

    const onLegendChange = (
        alertEvaluationTimeSeriesPlot: AlertEvaluationTimeSeriesPlot
    ): void => {
        switch (alertEvaluationTimeSeriesPlot) {
            case AlertEvaluationTimeSeriesPlot.CURRENT: {
                dispatch({
                    type:
                        AlertEvaluationTimeSeriesInternalStateAction.TOGGLE_CURRENT_PLOT_VISIBLE,
                });

                break;
            }
            case AlertEvaluationTimeSeriesPlot.BASELINE: {
                dispatch({
                    type:
                        AlertEvaluationTimeSeriesInternalStateAction.TOGGLE_BASELINE_PLOT_VISIBLE,
                });

                break;
            }
            case AlertEvaluationTimeSeriesPlot.UPPER_AND_LOWER_BOUND: {
                dispatch({
                    type:
                        AlertEvaluationTimeSeriesInternalStateAction.TOGGLE_UPPER_AND_LOWER_BOUND_PLOT_VISIBLE,
                });

                break;
            }
            case AlertEvaluationTimeSeriesPlot.ANOMALIES: {
                dispatch({
                    type:
                        AlertEvaluationTimeSeriesInternalStateAction.TOGGLE_ANOMALIES_PLOT_VISIBLE,
                });

                break;
            }
        }
    };

    if (loading) {
        return <LoadingIndicator />;
    }

    if (noData) {
        return <NoDataIndicator />;
    }

    if (
        props.height < HEIGHT_CONTAINER_MIN ||
        props.width < WIDTH_CONTAINER_MIN
    ) {
        return (
            <NoDataIndicator text={t("message.visualization-render-error")} />
        );
    }

    return (
        <>
            {/* SVG container with parent dimensions */}
            <svg height={svgHeight} width={svgWidth}>
                {/* Time series */}
                <Group left={PADDING_SVG_LEFT} top={PADDING_SVG_TOP}>
                    {/* Anomalies */}
                    {anomaliesPlotVisible && (
                        <AlertEvaluationTimeSeriesAnomaliesPlot
                            alertEvaluationAnomalyPoints={
                                filteredAlertEvaluationAnomalyPoints
                            }
                            xScale={timeSeriesXScale}
                            yScale={timeSeriesYScale}
                        />
                    )}

                    {/* Upper and lower bound */}
                    {upperAndLowerBoundPlotVisible && (
                        <AlertEvaluationTimeSeriesUpperAndLowerBoundPlot
                            alertEvaluationTimeSeriesPoints={
                                filteredAlertEvaluationTimeSeriesPoints
                            }
                            xScale={timeSeriesXScale}
                            yScale={timeSeriesYScale}
                        />
                    )}

                    {/* Baseline */}
                    {baselinePlotVisible && (
                        <AlertEvaluationTimeSeriesBaselinePlot
                            alertEvaluationTimeSeriesPoints={
                                filteredAlertEvaluationTimeSeriesPoints
                            }
                            xScale={timeSeriesXScale}
                            yScale={timeSeriesYScale}
                        />
                    )}

                    {/* Current */}
                    {currentPlotVisible && (
                        <AlertEvaluationTimeSeriesCurrentPlot
                            alertEvaluationTimeSeriesPoints={
                                filteredAlertEvaluationTimeSeriesPoints
                            }
                            xScale={timeSeriesXScale}
                            yScale={timeSeriesYScale}
                        />
                    )}

                    {/* Mouse hover region  */}
                    <Bar
                        height={timeSeriesYMax}
                        opacity={0}
                        width={timeSeriesXMax}
                        onMouseLeave={onTimeSeriesMouseLeave}
                        onMouseMove={onTimeSeriesMouseMove}
                    />

                    {/* Mouse hover marker */}
                    {tooltipData && (
                        <>
                            <Line
                                from={{
                                    x: timeSeriesXScale(0),
                                    y: timeSeriesYScale(tooltipData.current),
                                }}
                                opacity={0.2}
                                stroke={
                                    Palette.COLOR_VISUALIZATION_STROKE_HOVER_MARKER
                                }
                                strokeDasharray={
                                    Dimension.DASHARRAY_VISUALIZATION_HOVER_MARKER
                                }
                                strokeWidth={
                                    Dimension.WIDTH_VISUALIZATION_STROKE_HOVER_MARKER
                                }
                                to={{
                                    x: timeSeriesXScale(tooltipData.timestamp),
                                    y: timeSeriesYScale(tooltipData.current),
                                }}
                            />

                            <Line
                                from={{
                                    x: timeSeriesXScale(tooltipData.timestamp),
                                    y: timeSeriesYScale(tooltipData.current),
                                }}
                                opacity={0.2}
                                stroke={
                                    Palette.COLOR_VISUALIZATION_STROKE_HOVER_MARKER
                                }
                                strokeDasharray={
                                    Dimension.DASHARRAY_VISUALIZATION_HOVER_MARKER
                                }
                                strokeWidth={
                                    Dimension.WIDTH_VISUALIZATION_STROKE_HOVER_MARKER
                                }
                                to={{
                                    x: timeSeriesXScale(tooltipData.timestamp),
                                    y: timeSeriesYScale(0),
                                }}
                            />

                            <Circle
                                cx={timeSeriesXScale(tooltipData.timestamp)}
                                cy={timeSeriesYScale(tooltipData.current)}
                                fill={
                                    Palette.COLOR_VISUALIZATION_FILL_HOVER_MARKER
                                }
                                opacity={0.7}
                                r={Dimension.RADIUS_VISUALIZATION_HOVER_MARKER}
                                stroke={
                                    Palette.COLOR_VISUALIZATION_STROKE_HOVER_MARKER
                                }
                                strokeWidth={
                                    Dimension.WIDTH_VISUALIZATION_STROKE_HOVER_MARKER
                                }
                            />
                        </>
                    )}

                    {/* X axis */}
                    <TimeAxisBottom
                        scale={timeSeriesXScale}
                        top={timeSeriesYMax}
                    />

                    {/* Y axis */}
                    <LinearAxisLeft scale={timeSeriesYScale} />
                </Group>

                {/* Brush */}
                <Group
                    left={PADDING_SVG_LEFT}
                    top={timeSeriesHeight + HEIGHT_SEPARATOR_TIME_SERIES_BRUSH}
                >
                    {/* Time series in the brush to be always visible and slightly transparent */}
                    <Group opacity={0.6}>
                        {/* Anomalies */}
                        <AlertEvaluationTimeSeriesAnomaliesPlot
                            alertEvaluationAnomalyPoints={
                                alertEvaluationAnomalyPoints
                            }
                            xScale={brushXScale}
                            yScale={brushYScale}
                        />

                        {/* Upper and lower bound */}
                        <AlertEvaluationTimeSeriesUpperAndLowerBoundPlot
                            alertEvaluationTimeSeriesPoints={
                                alertEvaluationTimeSeriesPoints
                            }
                            xScale={brushXScale}
                            yScale={brushYScale}
                        />

                        {/* Baseline */}
                        <AlertEvaluationTimeSeriesBaselinePlot
                            alertEvaluationTimeSeriesPoints={
                                alertEvaluationTimeSeriesPoints
                            }
                            xScale={brushXScale}
                            yScale={brushYScale}
                        />

                        {/* Current */}
                        <AlertEvaluationTimeSeriesCurrentPlot
                            alertEvaluationTimeSeriesPoints={
                                alertEvaluationTimeSeriesPoints
                            }
                            xScale={brushXScale}
                            yScale={brushYScale}
                        />
                    </Group>

                    {/* Brush */}
                    <Brush
                        height={brushYMax}
                        innerRef={brushRef}
                        margin={{
                            top: 0,
                            left: PADDING_SVG_LEFT,
                            right: PADDING_SVG_RIGHT,
                            bottom: 0,
                        }}
                        selectedBoxStyle={{
                            fill: Palette.COLOR_VISUALIZATION_STROKE_BRUSH,
                            fillOpacity: 0.4,
                            strokeOpacity: 1,
                            stroke: Palette.COLOR_VISUALIZATION_STROKE_BRUSH,
                            strokeWidth:
                                Dimension.WIDTH_VISUALIZATION_STROKE_DEFAULT,
                        }}
                        width={brushXMax}
                        xScale={brushXScale}
                        yScale={brushYScale}
                        onChange={onBrushChangeDebounced}
                    />

                    {/* X axis */}
                    <TimeAxisBottom scale={brushXScale} top={brushYMax} />
                </Group>
            </svg>

            {/* Legend */}
            <AlertEvaluationTimeSeriesLegend
                anomalies={anomaliesPlotVisible}
                baseline={baselinePlotVisible}
                current={currentPlotVisible}
                upperAndLowerBound={upperAndLowerBoundPlotVisible}
                onChange={onLegendChange}
            />
        </>
    );
};
