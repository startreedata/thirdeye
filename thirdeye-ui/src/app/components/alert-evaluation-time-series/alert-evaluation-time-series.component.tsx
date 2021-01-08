import { useTheme } from "@material-ui/core";
import BaseBrush from "@visx/brush/lib/BaseBrush";
import { Bounds } from "@visx/brush/lib/types";
import {
    AxisLeft,
    Brush,
    Group,
    ParentSize,
    scaleLinear,
    scaleTime,
} from "@visx/visx";
import { debounce, isEmpty } from "lodash";
import React, {
    createRef,
    FunctionComponent,
    ReactNode,
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { Dimension } from "../../utils/material-ui-util/dimension-util";
import {
    formatLargeNumberForVisualization,
    getAlertEvaluationTimeSeriesPoints,
    getAlertEvaluationTimeSeriesPointsMaxTimestamp,
    getAlertEvaluationTimeSeriesPointsMaxValue,
    getAlertEvaluationTimeSeriesPointsMinTimestamp,
} from "../../utils/visualization-util/visualization-util";
import { LoadingIndicator } from "../loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../no-data-indicator/no-data-indicator.component";
import { VisxCustomTimeAxisBottom } from "../visx-custom-time-axis-bottom/visx-custom-time-axis-bottom.component";
import { AlertEvaluationTimeSeriesBaselinePlot } from "./alert-evaluation-time-series-baseline-plot/alert-evaluation-time-series-baseline-plot.component";
import { AlertEvaluationTimeSeriesCurrentPlot } from "./alert-evaluation-time-series-current-plot/alert-evaluation-time-series-current-plot.component";
import { AlertEvaluationTimeSeriesLegend } from "./alert-evaluation-time-series-legend/alert-evaluation-time-series-legend.component";
import { AlertEvaluationTimeSeriesUpperAndLowerBoundPlot } from "./alert-evaluation-time-series-upper-and-lower-bound-plot/alert-evaluation-time-series-upper-and-lower-bound-plot.component";
import {
    AlertEvaluationTimeSeriesInternalProps,
    AlertEvaluationTimeSeriesPlot,
    AlertEvaluationTimeSeriesPoint,
    AlertEvaluationTimeSeriesProps,
} from "./alert-evaluation-time-series.interfaces";
import { useAlertEvaluationTimeSeriesInternalStyles } from "./alert-evaluation-time-series.styles";

const WIDTH_CONTAINER_MIN = 620;
const HEIGHT_CONTAINER_MIN = 310;
const MARGIN_LEFT = 40;
const MARGIN_RIGHT = 40;
const MARGIN_TOP = 30;
const MARGIN_BOTTOM = 10;
const HEIGHT_DIVIDER = 70;
const HEIGHT_LEGEND = 30;

// Simple wrapper around AlertEvaluationTimeSeriesInternal to capture parent container dimensions
export const AlertEvaluationTimeSeries: FunctionComponent<AlertEvaluationTimeSeriesProps> = (
    props: AlertEvaluationTimeSeriesProps
) => {
    return (
        <ParentSize>
            {(parent): ReactNode => (
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
    const alertEvaluationTimeSeriesInternalClasses = useAlertEvaluationTimeSeriesInternalStyles();
    const [loading, setLoading] = useState(true);
    const [noDataAvailable, setNoDataAvailable] = useState(false);
    const [
        alertEvaluationTimeSeriesPoints,
        setAlertEvaluationTimeSeriesPoints,
    ] = useState<AlertEvaluationTimeSeriesPoint[]>([]);
    const [
        filteredAlertEvaluationTimeSeriesPoints,
        setFilteredAlertEvaluationTimeSeriesPoints,
    ] = useState<AlertEvaluationTimeSeriesPoint[]>([]);
    const [upperAndLowerBoundVisible, setUpperAndLowerBoundVisible] = useState(
        true
    );
    const [currentVisible, setCurrentVisible] = useState(true);
    const [baselineVisible, setBaselineVisible] = useState(true);
    const brushRef = createRef<BaseBrush>();
    const theme = useTheme();
    const { t } = useTranslation();

    // SVG bounds
    const svgWidth = props.width; // container width
    const svgHeight = props.height - HEIGHT_LEGEND; // container height - space for legend

    // Time series bounds
    const timeSeriesXMax = svgWidth - MARGIN_LEFT - MARGIN_RIGHT; // Available SVG width - left and right margins
    const timeSeriesHeight = (svgHeight - MARGIN_TOP - MARGIN_BOTTOM) * 0.8; // 85% of (available SVG height - top and bottom margins)
    const timeSeriesYMax = timeSeriesHeight - HEIGHT_DIVIDER; // Time series height - space between time series and brush

    // Brush bounds
    const brushXMax = svgWidth - MARGIN_LEFT - MARGIN_RIGHT; // Available SVG width - left and right margins
    const brushHeight =
        svgHeight - timeSeriesHeight - MARGIN_TOP - MARGIN_BOTTOM; // Remaining SVG height after time series - top and bottom margins
    const brushYMax = brushHeight; // Brush height

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
            nice: true,
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
            nice: true,
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
        });
    }, [props.height, alertEvaluationTimeSeriesPoints]);

    useEffect(() => {
        // Input changed, reset
        setLoading(true);
        setNoDataAvailable(false);
        setAlertEvaluationTimeSeriesPoints([]);
        setFilteredAlertEvaluationTimeSeriesPoints([]);

        if (!props.alertEvaluation) {
            return;
        }

        const alertEvaluationTimeSeriesPoints = getAlertEvaluationTimeSeriesPoints(
            props.alertEvaluation
        );
        if (isEmpty(alertEvaluationTimeSeriesPoints)) {
            setLoading(false);
            setNoDataAvailable(true);

            return;
        }
        // Reset brush selection
        brushRef && brushRef.current && brushRef.current.reset();

        setAlertEvaluationTimeSeriesPoints(alertEvaluationTimeSeriesPoints);
        setFilteredAlertEvaluationTimeSeriesPoints(
            alertEvaluationTimeSeriesPoints
        );

        setLoading(false);
        setNoDataAvailable(false);
    }, [props.alertEvaluation]);

    const onBrushChange = useCallback(
        debounce((domain: Bounds | null): void => {
            if (!domain || domain.x1 - domain.x0 === 0) {
                // Reset brush selection
                setFilteredAlertEvaluationTimeSeriesPoints(
                    alertEvaluationTimeSeriesPoints
                );

                return;
            }

            // Filter time series based on brush selection
            const filteredAlertEvaluationTimeSeriesPoints = alertEvaluationTimeSeriesPoints.filter(
                (
                    alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                ): boolean => {
                    return (
                        alertEvaluationTimeSeriesPoint.timestamp >= domain.x0 &&
                        alertEvaluationTimeSeriesPoint.timestamp <= domain.x1
                    );
                }
            );

            setFilteredAlertEvaluationTimeSeriesPoints(
                filteredAlertEvaluationTimeSeriesPoints
            );
        }, 1),
        [alertEvaluationTimeSeriesPoints]
    );

    const onLegendChange = (
        alertEvaluationTimeSeries: AlertEvaluationTimeSeriesPlot
    ): void => {
        switch (alertEvaluationTimeSeries) {
            case AlertEvaluationTimeSeriesPlot.UPPER_AND_LOWER_BOUND: {
                setUpperAndLowerBoundVisible(
                    (showUpperAndLowerBound) => !showUpperAndLowerBound
                );

                break;
            }
            case AlertEvaluationTimeSeriesPlot.CURRENT: {
                setCurrentVisible((showCurrent) => !showCurrent);

                break;
            }
            case AlertEvaluationTimeSeriesPlot.BASELINE: {
                setBaselineVisible((showBaseline) => !showBaseline);

                break;
            }
        }
    };

    if (loading) {
        return <LoadingIndicator />;
    }

    if (noDataAvailable) {
        return <NoDataIndicator />;
    }

    if (
        props.width < WIDTH_CONTAINER_MIN ||
        props.height < HEIGHT_CONTAINER_MIN
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
                <Group left={MARGIN_LEFT} top={MARGIN_TOP}>
                    {/* Upper and lower bound */}
                    {upperAndLowerBoundVisible && (
                        <AlertEvaluationTimeSeriesUpperAndLowerBoundPlot
                            alertEvaluationTimeSeriesPoints={
                                filteredAlertEvaluationTimeSeriesPoints
                            }
                            xScale={timeSeriesXScale}
                            yScale={timeSeriesYScale}
                        />
                    )}

                    {/* Current */}
                    {currentVisible && (
                        <AlertEvaluationTimeSeriesCurrentPlot
                            alertEvaluationTimeSeriesPoints={
                                filteredAlertEvaluationTimeSeriesPoints
                            }
                            xScale={timeSeriesXScale}
                            yScale={timeSeriesYScale}
                        />
                    )}

                    {/* Baseline */}
                    {baselineVisible && (
                        <AlertEvaluationTimeSeriesBaselinePlot
                            alertEvaluationTimeSeriesPoints={
                                filteredAlertEvaluationTimeSeriesPoints
                            }
                            xScale={timeSeriesXScale}
                            yScale={timeSeriesYScale}
                        />
                    )}

                    {/* X axis */}
                    <VisxCustomTimeAxisBottom
                        numTicks={5}
                        scale={timeSeriesXScale}
                        tickClassName={
                            alertEvaluationTimeSeriesInternalClasses.axisLabel
                        }
                        top={timeSeriesYMax}
                    />

                    {/* Y axis */}
                    <AxisLeft
                        scale={timeSeriesYScale}
                        tickClassName={
                            alertEvaluationTimeSeriesInternalClasses.axisLabel
                        }
                        tickFormat={formatLargeNumberForVisualization}
                    />
                </Group>

                {/* Brush */}
                <Group left={MARGIN_LEFT} top={timeSeriesHeight}>
                    {/* Time series in the brush to be always visible and slightly transparent */}
                    <Group opacity={0.5}>
                        {/* Upper and lower bound */}
                        <AlertEvaluationTimeSeriesUpperAndLowerBoundPlot
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

                        {/* Baseline */}
                        <AlertEvaluationTimeSeriesBaselinePlot
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
                            left: MARGIN_LEFT,
                            right: MARGIN_RIGHT,
                            bottom: 0,
                        }}
                        selectedBoxStyle={{
                            fill: theme.palette.primary.main,
                            fillOpacity: 0.2,
                            stroke: theme.palette.primary.main,
                            strokeWidth:
                                Dimension.WIDTH_VISUALIZATION_STROKE_DEFAULT,
                        }}
                        width={brushXMax}
                        xScale={brushXScale}
                        yScale={brushYScale}
                        onChange={onBrushChange}
                    />

                    {/* X axis */}
                    <VisxCustomTimeAxisBottom
                        numTicks={5}
                        scale={brushXScale}
                        tickClassName={
                            alertEvaluationTimeSeriesInternalClasses.axisLabel
                        }
                        top={brushYMax}
                    />
                </Group>
            </svg>

            {/* Legend */}
            <AlertEvaluationTimeSeriesLegend onChange={onLegendChange} />
        </>
    );
};
