import BaseBrush, { BaseBrushState } from "@visx/brush/lib/BaseBrush";
import { Bounds } from "@visx/brush/lib/types";
import {
    Brush,
    Group,
    localPoint,
    ParentSize,
    Point,
    scaleLinear,
    scaleTime,
    useTooltip,
} from "@visx/visx";
import { cloneDeep, debounce, isEmpty } from "lodash";
import React, {
    FunctionComponent,
    MouseEvent,
    useCallback,
    useEffect,
    useMemo,
    useReducer,
    useRef,
} from "react";
import { useTranslation } from "react-i18next";
import { Dimension } from "../../../utils/material-ui/dimension.util";
import { Palette } from "../../../utils/material-ui/palette.util";
import {
    filterAlertEvaluationAnomalyPointsByTime,
    filterAlertEvaluationTimeSeriesPointsByTime,
    getAlertEvaluationAnomalyPoints,
    getAlertEvaluationTimeSeriesPointAtTime,
    getAlertEvaluationTimeSeriesPoints,
    getAlertEvaluationTimeSeriesPointsMaxTimestamp,
    getAlertEvaluationTimeSeriesPointsMaxValue,
    getAlertEvaluationTimeSeriesPointsMinTimestamp,
} from "../../../utils/visualization/visualization.util";
import { LoadingIndicator } from "../../loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LinearAxisLeft } from "../linear-axis-left/linear-axis-left.component";
import { TimeAxisBottom } from "../time-axis-bottom/time-axis-bottom.component";
import {
    AlertEvaluationTimeSeriesInternalProps,
    AlertEvaluationTimeSeriesInternalStateAction,
    AlertEvaluationTimeSeriesPlot,
    AlertEvaluationTimeSeriesProps,
    AlertEvaluationTimeSeriesTooltipPoint,
} from "./alert-evaluation-time-series.interfaces";
import { alertEvaluationTimeSeriesInternalReducer } from "./alert-evaluation-time-series.reducer";
import { AnomaliesPlot } from "./anomalies-plot/anomalies-plot.component";
import { BaselinePlot } from "./baseline-plot/baseline-plot.component";
import { CurrentPlot } from "./current-plot/current-plot.component";
import { Legend } from "./legend/legend.component";
import { MouseHoverMarker } from "./mouse-hover-marker/mouse-hover-marker.component";
import { UpperAndLowerBoundPlot } from "./upper-and-lower-bound-plot/upper-and-lower-bound-plot.component";

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
    const brushRef = useRef<BaseBrush>(null);
    const svgRef = useRef<SVGSVGElement>(null);
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
        resetTimeSeries();
    }, [props.alertEvaluation]);

    useEffect(() => {
        // Width changed, update brush selection, if any
        brushRef &&
            brushRef.current &&
            brushRef.current.updateBrush(brushUpdater);
    }, [props.width]);

    const onTimeSeriesMouseMove = (event: MouseEvent<SVGRectElement>): void => {
        onTimeSeriesMouseMoveDebounced(
            localPoint(event) as Point // Event coordinates to SVG coordinates
        );
    };

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

    const resetTimeSeries = (): void => {
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
    };

    const brushUpdater = (previousState: BaseBrushState): BaseBrushState => {
        if (
            !brushRef ||
            !brushRef.current ||
            brushRef.current.getBrushWidth() === 0 ||
            isEmpty(filteredAlertEvaluationTimeSeriesPoints)
        ) {
            return previousState;
        }

        const newExtent = brushRef.current.getExtent(
            {
                x: brushXScale(
                    filteredAlertEvaluationTimeSeriesPoints[0].timestamp
                ),
            },
            {
                x: brushXScale(
                    filteredAlertEvaluationTimeSeriesPoints[
                        filteredAlertEvaluationTimeSeriesPoints.length - 1
                    ].timestamp
                ),
            }
        );
        const newState = cloneDeep(previousState);
        newState.start = {
            x: newExtent.x0,
            y: newExtent.y0,
        };
        newState.end = {
            x: newExtent.x1,
            y: newExtent.y1,
        };
        newState.extent = newExtent;

        return newState;
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

            // Get data at this point
            const alertEvaluationTimeSeriesPoint = getAlertEvaluationTimeSeriesPointAtTime(
                filteredAlertEvaluationTimeSeriesPoints,
                xValue.getTime()
            );
            if (!alertEvaluationTimeSeriesPoint) {
                // Not found
                hideTooltip();

                return;
            }

            showTooltip({
                tooltipData: {
                    timestamp: alertEvaluationTimeSeriesPoint.timestamp,
                    current: alertEvaluationTimeSeriesPoint.current,
                    expected: alertEvaluationTimeSeriesPoint.expected,
                    upperBound: alertEvaluationTimeSeriesPoint.upperBound,
                    lowerBound: alertEvaluationTimeSeriesPoint.lowerBound,
                    anomalies: [],
                },
            });
        }, 1),
        [props.width, svgRef, filteredAlertEvaluationTimeSeriesPoints]
    );

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
            <svg height={svgHeight} ref={svgRef} width={svgWidth}>
                {/* Time series */}
                <Group left={PADDING_SVG_LEFT} top={PADDING_SVG_TOP}>
                    {/* Anomalies */}
                    {anomaliesPlotVisible && (
                        <AnomaliesPlot
                            alertEvaluationAnomalyPoints={
                                filteredAlertEvaluationAnomalyPoints
                            }
                            xScale={timeSeriesXScale}
                            yScale={timeSeriesYScale}
                        />
                    )}

                    {/* Upper and lower bound */}
                    {upperAndLowerBoundPlotVisible && (
                        <UpperAndLowerBoundPlot
                            alertEvaluationTimeSeriesPoints={
                                filteredAlertEvaluationTimeSeriesPoints
                            }
                            xScale={timeSeriesXScale}
                            yScale={timeSeriesYScale}
                        />
                    )}

                    {/* Baseline */}
                    {baselinePlotVisible && (
                        <BaselinePlot
                            alertEvaluationTimeSeriesPoints={
                                filteredAlertEvaluationTimeSeriesPoints
                            }
                            xScale={timeSeriesXScale}
                            yScale={timeSeriesYScale}
                        />
                    )}

                    {/* Current */}
                    {currentPlotVisible && (
                        <CurrentPlot
                            alertEvaluationTimeSeriesPoints={
                                filteredAlertEvaluationTimeSeriesPoints
                            }
                            xScale={timeSeriesXScale}
                            yScale={timeSeriesYScale}
                        />
                    )}

                    {/* Mouse hover marker  */}
                    <MouseHoverMarker
                        alertEvaluationTimeSeriesTooltipPoint={tooltipData}
                        xScale={timeSeriesXScale}
                        yScale={timeSeriesYScale}
                        onMouseLeave={onTimeSeriesMouseLeave}
                        onMouseMove={onTimeSeriesMouseMove}
                    />

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
                        <AnomaliesPlot
                            alertEvaluationAnomalyPoints={
                                alertEvaluationAnomalyPoints
                            }
                            xScale={brushXScale}
                            yScale={brushYScale}
                        />

                        {/* Upper and lower bound */}
                        <UpperAndLowerBoundPlot
                            alertEvaluationTimeSeriesPoints={
                                alertEvaluationTimeSeriesPoints
                            }
                            xScale={brushXScale}
                            yScale={brushYScale}
                        />

                        {/* Baseline */}
                        <BaselinePlot
                            alertEvaluationTimeSeriesPoints={
                                alertEvaluationTimeSeriesPoints
                            }
                            xScale={brushXScale}
                            yScale={brushYScale}
                        />

                        {/* Current */}
                        <CurrentPlot
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
                            top:
                                timeSeriesHeight +
                                HEIGHT_SEPARATOR_TIME_SERIES_BRUSH,
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
            <Legend
                anomalies={anomaliesPlotVisible}
                baseline={baselinePlotVisible}
                current={currentPlotVisible}
                upperAndLowerBound={upperAndLowerBoundPlotVisible}
                onChange={onLegendChange}
            />
        </>
    );
};
