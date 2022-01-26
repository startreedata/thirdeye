import { Box, useTheme } from "@material-ui/core";
import { AppLoadingIndicatorV1 } from "@startree-ui/platform-ui";
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
import {
    filterAnomaliesByTime,
    getAnomaliesAtTime,
} from "../../../../utils/anomalies/anomalies.util";
import { Dimension } from "../../../../utils/material-ui/dimension.util";
import { Palette } from "../../../../utils/material-ui/palette.util";
import {
    filterAlertEvaluationTimeSeriesPointsByTime,
    getAlertEvaluationAnomalies,
    getAlertEvaluationTimeSeriesPointAtTime,
    getAlertEvaluationTimeSeriesPoints,
    getAlertEvaluationTimeSeriesPointsMaxTimestamp,
    getAlertEvaluationTimeSeriesPointsMaxValue,
    getAlertEvaluationTimeSeriesPointsMinTimestamp,
} from "../../../../utils/visualization/visualization.util";
import { NoDataIndicator } from "../../../no-data-indicator/no-data-indicator.component";
import { LinearAxisLeft } from "../../linear-axis-left/linear-axis-left.component";
import { MouseHoverMarker } from "../../mouse-hover-marker/mouse-hover-marker.component";
import { TimeAxisBottom } from "../../time-axis-bottom/time-axis-bottom.component";
import { TooltipWithBounds } from "../../tooltip-with-bounds/tooltip-with-bounds.component";
import { AlertEvaluationTimeSeriesLegend } from "../alert-evaluation-time-series-legend/alert-evaluation-time-series-legend.component";
import { AlertEvaluationTimeSeriesPlot } from "../alert-evaluation-time-series-plot/alert-evaluation-time-series-plot.component";
import { AlertEvaluationTimeSeriesTooltip } from "../alert-evaluation-time-series-tooltip/alert-evaluation-time-series-tooltip.component";
import { AlertEvaluationTimeSeriesTooltipPoint } from "../alert-evaluation-time-series-tooltip/alert-evaluation-time-series-tooltip.interfaces";
import {
    AlertEvaluationTimeSeriesInternalProps,
    AlertEvaluationTimeSeriesPlotLine,
    AlertEvaluationTimeSeriesProps,
    AlertEvaluationTimeSeriesStateAction,
} from "./alert-evaluation-time-series.interfaces";
import { alertEvaluationTimeSeriesReducer } from "./alert-evaluation-time-series.reducer";
import { useAlertEvaluationTimeSeriesStyles } from "./alert-evaluation-time-series.styles";

const PADDING_TOP_SVG = 10;
const PADDING_BOTTOM_SVG = 30;
const PADDING_LEFT_SVG = 50;
const PADDING_RIGHT_SVG = 50;
const HEIGHT_SEPARATOR_TIME_SERIES_BRUSH = 60;
const HEIGHT_BRUSH = 90;
const HEIGHT_LEGEND_XS = 55;
const HEIGHT_LEGEND_SM_UP = 25;

// Simple wrapper to capture parent container dimensions
export const AlertEvaluationTimeSeries: FunctionComponent<AlertEvaluationTimeSeriesProps> = (
    props: AlertEvaluationTimeSeriesProps
) => {
    return (
        <ParentSize>
            {(parent) => (
                <AlertEvaluationTimeSeriesInternal
                    alertEvaluation={props.alertEvaluation}
                    hideBrush={props.hideBrush}
                    parentHeight={parent.height}
                    parentWidth={parent.width}
                />
            )}
        </ParentSize>
    );
};

const AlertEvaluationTimeSeriesInternal: FunctionComponent<AlertEvaluationTimeSeriesInternalProps> = (
    props: AlertEvaluationTimeSeriesInternalProps
) => {
    const alertEvaluationTimeSeriesClasses = useAlertEvaluationTimeSeriesStyles();
    const [
        {
            loading,
            noData,
            alertEvaluationTimeSeriesPoints,
            filteredAlertEvaluationTimeSeriesPoints,
            alertEvaluationAnomalies,
            filteredAlertEvaluationAnomalies,
            currentPlotVisible,
            baselinePlotVisible,
            upperAndLowerBoundPlotVisible,
            anomaliesPlotVisible,
        },
        dispatch,
    ] = useReducer(alertEvaluationTimeSeriesReducer, {
        loading: true,
        noData: false,
        alertEvaluationTimeSeriesPoints: [],
        filteredAlertEvaluationTimeSeriesPoints: [],
        alertEvaluationAnomalies: [],
        filteredAlertEvaluationAnomalies: [],
        currentPlotVisible: true,
        baselinePlotVisible: true,
        upperAndLowerBoundPlotVisible: false,
        anomaliesPlotVisible: true,
    });
    const {
        tooltipTop,
        tooltipLeft,
        tooltipData,
        showTooltip,
        hideTooltip,
    } = useTooltip<AlertEvaluationTimeSeriesTooltipPoint>();
    const brushRef = useRef<BaseBrush>(null);
    const theme = useTheme();

    // Legend height
    // Legend items wrap to new line when parent container width is roughly equal to screen width xs
    const legendHeight =
        props.parentWidth < theme.breakpoints.width("sm")
            ? HEIGHT_LEGEND_XS
            : HEIGHT_LEGEND_SM_UP;

    // SVG bounds
    const svgHeight = props.parentHeight - legendHeight; // Container height - space for legend
    const svgWidth = props.parentWidth; // Container width

    // Time series bounds
    const timeSeriesHeight =
        svgHeight -
        PADDING_TOP_SVG -
        HEIGHT_SEPARATOR_TIME_SERIES_BRUSH -
        HEIGHT_BRUSH; // Available SVG height - top SVG padding - separator height between time series and brush - space for brush
    const timeSeriesXMax = svgWidth - PADDING_LEFT_SVG - PADDING_RIGHT_SVG; // Available SVG width - left and right SVG padding
    const timeSeriesYMax = timeSeriesHeight;

    // Brush bounds
    const brushHeight = HEIGHT_BRUSH - PADDING_BOTTOM_SVG; // Brush height - bottom SVG padding
    const brushXMax = svgWidth - PADDING_LEFT_SVG - PADDING_RIGHT_SVG; // Available SVG width - left and right SVG padding
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
    }, [props.parentWidth, filteredAlertEvaluationTimeSeriesPoints]);
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
    }, [props.parentHeight, filteredAlertEvaluationTimeSeriesPoints]);

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
    }, [props.parentWidth, alertEvaluationTimeSeriesPoints]);
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
    }, [props.parentHeight, alertEvaluationTimeSeriesPoints]);

    useEffect(() => {
        // Input changed, reset
        resetTimeSeries();
    }, [props.alertEvaluation]);

    useEffect(() => {
        // Width changed, update brush selection, if any
        brushRef &&
            brushRef.current &&
            brushRef.current.updateBrush(brushUpdater);
    }, [props.parentWidth]);

    const resetTimeSeries = (): void => {
        dispatch({
            type: AlertEvaluationTimeSeriesStateAction.UPDATE,
            payload: {
                loading: true,
                noData: false,
                alertEvaluationTimeSeriesPoints: [],
                filteredAlertEvaluationTimeSeriesPoints: [],
                alertEvaluationAnomalies: [],
                filteredAlertEvaluationAnomalies: [],
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
        const newAlertEvaluationAnomalies = getAlertEvaluationAnomalies(
            props.alertEvaluation
        );
        if (isEmpty(newAlertEvaluationTimeSeriesPoints)) {
            dispatch({
                type: AlertEvaluationTimeSeriesStateAction.UPDATE,
                payload: {
                    loading: false,
                    noData: true,
                },
            });

            return;
        }

        dispatch({
            type: AlertEvaluationTimeSeriesStateAction.UPDATE,
            payload: {
                loading: false,
                noData: false,
                alertEvaluationTimeSeriesPoints: newAlertEvaluationTimeSeriesPoints,
                filteredAlertEvaluationTimeSeriesPoints: newAlertEvaluationTimeSeriesPoints,
                alertEvaluationAnomalies: newAlertEvaluationAnomalies,
                filteredAlertEvaluationAnomalies: newAlertEvaluationAnomalies,
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

        // Calculate brush selection
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

    const handleTimeSeriesMouseMove = (
        event: MouseEvent<SVGRectElement>
    ): void => {
        handleTimeSeriesMouseMoveDebounced(
            localPoint(event) as Point // Event coordinates to SVG coordinates
        );
    };

    const handleTimeSeriesMouseMoveDebounced = useCallback(
        debounce((svgPoint: Point): void => {
            if (!svgPoint) {
                hideTooltip();

                return;
            }

            // Determine time series time scale value from SVG coordinate, accounting for SVG
            // padding
            const xValue = timeSeriesXScale.invert(
                svgPoint.x - PADDING_LEFT_SVG
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
                hideTooltip();

                return;
            }
            const alertEvaluationAnomalies = getAnomaliesAtTime(
                filteredAlertEvaluationAnomalies,
                xValue.getTime()
            );

            showTooltip({
                tooltipLeft: svgPoint.x,
                tooltipTop: timeSeriesYScale(
                    alertEvaluationTimeSeriesPoint.current
                ),
                tooltipData: {
                    timestamp: alertEvaluationTimeSeriesPoint.timestamp,
                    current: alertEvaluationTimeSeriesPoint.current,
                    expected: alertEvaluationTimeSeriesPoint.expected,
                    upperBound: alertEvaluationTimeSeriesPoint.upperBound,
                    lowerBound: alertEvaluationTimeSeriesPoint.lowerBound,
                    anomalies: alertEvaluationAnomalies,
                },
            });
        }, 1),
        [props.parentWidth, filteredAlertEvaluationTimeSeriesPoints]
    );

    const handleTimeSeriesMouseLeave = (): void => {
        hideTooltip();
    };

    const handleBrushChangeDebounced = useCallback(
        debounce((domain: Bounds | null): void => {
            if (!domain || domain.x1 - domain.x0 === 0) {
                // Reset brush selection
                dispatch({
                    type: AlertEvaluationTimeSeriesStateAction.UPDATE,
                    payload: {
                        filteredAlertEvaluationTimeSeriesPoints: alertEvaluationTimeSeriesPoints,
                        filteredAlertEvaluationAnomalies: alertEvaluationAnomalies,
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
            const newFilteredAlertEvaluationAnomalies = filterAnomaliesByTime(
                alertEvaluationAnomalies,
                domain.x0,
                domain.x1
            );

            dispatch({
                type: AlertEvaluationTimeSeriesStateAction.UPDATE,
                payload: {
                    filteredAlertEvaluationTimeSeriesPoints: newFilteredAlertEvaluationTimeSeriesPoints,
                    filteredAlertEvaluationAnomalies: newFilteredAlertEvaluationAnomalies,
                },
            });
        }, 1),
        [alertEvaluationTimeSeriesPoints, alertEvaluationAnomalies]
    );

    const handleLegendChange = (
        alertEvaluationTimeSeriesPlotLine: AlertEvaluationTimeSeriesPlotLine
    ): void => {
        switch (alertEvaluationTimeSeriesPlotLine) {
            case AlertEvaluationTimeSeriesPlotLine.CURRENT: {
                dispatch({
                    type:
                        AlertEvaluationTimeSeriesStateAction.TOGGLE_CURRENT_PLOT_VISIBLE,
                });

                break;
            }
            case AlertEvaluationTimeSeriesPlotLine.BASELINE: {
                dispatch({
                    type:
                        AlertEvaluationTimeSeriesStateAction.TOGGLE_BASELINE_PLOT_VISIBLE,
                });

                break;
            }
            case AlertEvaluationTimeSeriesPlotLine.UPPER_AND_LOWER_BOUND: {
                dispatch({
                    type:
                        AlertEvaluationTimeSeriesStateAction.TOGGLE_UPPER_AND_LOWER_BOUND_PLOT_VISIBLE,
                });

                break;
            }
            case AlertEvaluationTimeSeriesPlotLine.ANOMALIES: {
                dispatch({
                    type:
                        AlertEvaluationTimeSeriesStateAction.TOGGLE_ANOMALIES_PLOT_VISIBLE,
                });

                break;
            }
        }
    };

    if (loading) {
        return <AppLoadingIndicatorV1 />;
    }

    if (noData) {
        return <NoDataIndicator />;
    }

    return (
        <TooltipWithBounds
            left={tooltipLeft}
            open={Boolean(tooltipData)}
            title={
                <AlertEvaluationTimeSeriesTooltip
                    alertEvaluationTimeSeriesTooltipPoint={tooltipData}
                />
            }
            top={tooltipTop}
        >
            {/* SVG container with calculated SVG bounds */}
            <Box height={svgHeight} width="100%">
                <svg className={alertEvaluationTimeSeriesClasses.svg}>
                    {/* Time series */}
                    <Group left={PADDING_LEFT_SVG} top={PADDING_TOP_SVG}>
                        {/* Time series plot */}
                        <AlertEvaluationTimeSeriesPlot
                            alertEvaluationAnomalies={
                                filteredAlertEvaluationAnomalies
                            }
                            alertEvaluationTimeSeriesPoints={
                                filteredAlertEvaluationTimeSeriesPoints
                            }
                            anomalies={anomaliesPlotVisible}
                            baseline={baselinePlotVisible}
                            current={currentPlotVisible}
                            upperAndLowerBound={upperAndLowerBoundPlotVisible}
                            xScale={timeSeriesXScale}
                            yScale={timeSeriesYScale}
                        />

                        {/* X axis */}
                        <TimeAxisBottom
                            parentWidth={props.parentWidth}
                            scale={timeSeriesXScale}
                            top={timeSeriesYMax}
                        />

                        {/* Y axis */}
                        <LinearAxisLeft scale={timeSeriesYScale} />

                        {/* Mouse hover marker  */}
                        <MouseHoverMarker
                            x={tooltipData && tooltipData.timestamp}
                            xScale={timeSeriesXScale}
                            y={tooltipData && tooltipData.current}
                            yScale={timeSeriesYScale}
                            onMouseLeave={handleTimeSeriesMouseLeave}
                            onMouseMove={handleTimeSeriesMouseMove}
                        />
                    </Group>

                    {/* Brush */}
                    <Group
                        left={PADDING_LEFT_SVG}
                        top={
                            timeSeriesHeight +
                            HEIGHT_SEPARATOR_TIME_SERIES_BRUSH
                        }
                    >
                        <Group opacity={0.5}>
                            {/* Time series plot */}
                            <AlertEvaluationTimeSeriesPlot
                                alertEvaluationAnomalies={
                                    alertEvaluationAnomalies
                                }
                                alertEvaluationTimeSeriesPoints={
                                    alertEvaluationTimeSeriesPoints
                                }
                                anomalies={anomaliesPlotVisible}
                                baseline={baselinePlotVisible}
                                current={currentPlotVisible}
                                upperAndLowerBound={
                                    upperAndLowerBoundPlotVisible
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
                                left: PADDING_LEFT_SVG,
                                right: PADDING_RIGHT_SVG,
                                bottom: 0,
                            }}
                            selectedBoxStyle={{
                                fill: Palette.COLOR_VISUALIZATION_STROKE_BRUSH,
                                fillOpacity: 0.4,
                                strokeOpacity: 1,
                                stroke:
                                    Palette.COLOR_VISUALIZATION_STROKE_BRUSH,
                                strokeWidth:
                                    Dimension.WIDTH_VISUALIZATION_STROKE_DEFAULT,
                            }}
                            width={brushXMax}
                            xScale={brushXScale}
                            yScale={brushYScale}
                            onChange={handleBrushChangeDebounced}
                        />

                        {/* X axis */}
                        <TimeAxisBottom
                            parentWidth={props.parentWidth}
                            scale={brushXScale}
                            top={brushYMax}
                        />
                    </Group>
                </svg>
            </Box>

            {/* Legend */}
            <AlertEvaluationTimeSeriesLegend
                anomalies={anomaliesPlotVisible}
                baseline={baselinePlotVisible}
                current={currentPlotVisible}
                parentWidth={props.parentWidth}
                upperAndLowerBound={upperAndLowerBoundPlotVisible}
                onChange={handleLegendChange}
            />
        </TooltipWithBounds>
    );
};
