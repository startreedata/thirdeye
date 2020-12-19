import { Typography, useTheme } from "@material-ui/core";
import BaseBrush from "@visx/brush/lib/BaseBrush";
import { Bounds } from "@visx/brush/lib/types";
import {
    AreaClosed,
    AxisBottom,
    AxisLeft,
    Brush,
    curveNatural,
    Group,
    Legend,
    LegendItem,
    LegendLabel,
    LinePath,
    ParentSize,
    scaleLinear,
    scaleOrdinal,
    scaleTime,
} from "@visx/visx";
import classnames from "classnames";
import { debounce, isEmpty, kebabCase } from "lodash";
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
import { Palette } from "../../utils/material-ui-util/palette-util";
import {
    formatLargeNumberForVisualization,
    formatMonthDayDateForVisualization,
    getAlertEvaluationTimeSeriesPoints,
    getAlertEvaluationTimeSeriesPointsMaxTimestamp,
    getAlertEvaluationTimeSeriesPointsMaxValue,
    getAlertEvaluationTimeSeriesPointsMinTimestamp,
} from "../../utils/visualization/visualization-util";
import { LoadingIndicator } from "../loading-indicator/loading-indicator.component";
import { NoDataAvailableIndicator } from "../no-data-available-indicator/no-data-available-indicator.component";
import {
    AlertEvaluationTimeSeriesInternalProps,
    AlertEvaluationTimeSeriesPlot,
    AlertEvaluationTimeSeriesPoint,
    AlertEvaluationTimeSeriesProps,
} from "./alert-evaluation-time-series.interfaces";
import { useAlertEvaluationTimeSeriesInternalStyles } from "./alert-evaluation-time-series.styles";

const WIDTH_CONTAINER_MIN = 160;
const HEIGHT_CONTAINER_MIN = 160;
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
    const [showUpperAndLowerBound, setShowUpperAndLowerBound] = useState(true);
    const [showCurrent, setShowCurrent] = useState(true);
    const [showBaseline, setShowBaseline] = useState(true);
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
    const timeSeriesXScale = useMemo(
        () =>
            scaleTime<number>({
                range: [0, timeSeriesXMax],
                domain: [
                    getAlertEvaluationTimeSeriesPointsMinTimestamp(
                        filteredAlertEvaluationTimeSeriesPoints
                    ),
                    getAlertEvaluationTimeSeriesPointsMaxTimestamp(
                        filteredAlertEvaluationTimeSeriesPoints
                    ),
                ],
            }),
        [props.width, filteredAlertEvaluationTimeSeriesPoints]
    );
    const timeSeriesYScale = useMemo(
        () =>
            scaleLinear<number>({
                range: [timeSeriesYMax, 0],
                domain: [
                    0,
                    getAlertEvaluationTimeSeriesPointsMaxValue(
                        filteredAlertEvaluationTimeSeriesPoints
                    ),
                ],
                nice: true,
            }),
        [props.height, filteredAlertEvaluationTimeSeriesPoints]
    );

    // Brush scales
    const brushXScale = useMemo(
        () =>
            scaleTime<number>({
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
            }),
        [props.width, alertEvaluationTimeSeriesPoints]
    );
    const brushYScale = useMemo(
        () =>
            scaleLinear<number>({
                range: [brushYMax, 0],
                domain: [
                    0,
                    getAlertEvaluationTimeSeriesPointsMaxValue(
                        alertEvaluationTimeSeriesPoints
                    ),
                ],
                nice: true,
            }),
        [props.height, alertEvaluationTimeSeriesPoints]
    );

    // Legend scale
    const legendOrdinalScale = scaleOrdinal({
        domain: [
            AlertEvaluationTimeSeriesPlot.CURRENT,
            AlertEvaluationTimeSeriesPlot.BASELINE,
            AlertEvaluationTimeSeriesPlot.UPPER_AND_LOWER_BOUND,
        ],
        range: [
            Palette.COLOR_VISUALIZATION_STROKE_DEFAULT,
            Palette.COLOR_VISUALIZATION_STROKE_BASELINE,
            theme.palette.primary.main,
        ],
    });

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
        brushRef.current?.reset();

        setAlertEvaluationTimeSeriesPoints(alertEvaluationTimeSeriesPoints);
        setFilteredAlertEvaluationTimeSeriesPoints(
            alertEvaluationTimeSeriesPoints
        );

        setLoading(false);
        setNoDataAvailable(false);
    }, [props.alertEvaluation]);

    const onBrushChange = useCallback(
        debounce((domain: Bounds | null): void => {
            if (!domain) {
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

    const getTimeSeriesState = (
        alertEvaluationTimeSeries: AlertEvaluationTimeSeriesPlot
    ): boolean => {
        switch (alertEvaluationTimeSeries) {
            case AlertEvaluationTimeSeriesPlot.UPPER_AND_LOWER_BOUND: {
                return showUpperAndLowerBound;
            }
            case AlertEvaluationTimeSeriesPlot.CURRENT: {
                return showCurrent;
            }
            case AlertEvaluationTimeSeriesPlot.BASELINE: {
                return showBaseline;
            }
        }

        return false;
    };

    const toggleTimeSeriesState = (
        alertEvaluationTimeSeries: AlertEvaluationTimeSeriesPlot
    ): void => {
        switch (alertEvaluationTimeSeries) {
            case AlertEvaluationTimeSeriesPlot.UPPER_AND_LOWER_BOUND: {
                setShowUpperAndLowerBound(
                    (showUpperAndLowerBound) => !showUpperAndLowerBound
                );

                break;
            }
            case AlertEvaluationTimeSeriesPlot.CURRENT: {
                setShowCurrent((showCurrent) => !showCurrent);

                break;
            }
            case AlertEvaluationTimeSeriesPlot.BASELINE: {
                setShowBaseline((showBaseline) => !showBaseline);

                break;
            }
        }
    };

    if (
        loading ||
        props.width < WIDTH_CONTAINER_MIN ||
        props.height < HEIGHT_CONTAINER_MIN
    ) {
        return <LoadingIndicator />;
    }

    if (noDataAvailable) {
        return <NoDataAvailableIndicator />;
    }

    return (
        <div>
            {/* SVG container with parent dimensions */}
            <svg height={svgHeight} width={svgWidth}>
                {/* Time series */}
                <Group left={MARGIN_LEFT} top={MARGIN_TOP}>
                    {/* Upper and lower bound */}
                    {showUpperAndLowerBound && (
                        <AreaClosed
                            curve={curveNatural}
                            data={filteredAlertEvaluationTimeSeriesPoints}
                            defined={(
                                alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                            ): boolean => {
                                return (
                                    isFinite(
                                        alertEvaluationTimeSeriesPoint.lowerBound
                                    ) &&
                                    isFinite(
                                        alertEvaluationTimeSeriesPoint.upperBound
                                    )
                                );
                            }}
                            fill={theme.palette.primary.main}
                            stroke={theme.palette.primary.main}
                            strokeWidth={
                                Dimension.WIDTH_VISUALIZATION_STROKE_DEFAULT
                            }
                            x={(
                                alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                            ): number => {
                                return timeSeriesXScale(
                                    alertEvaluationTimeSeriesPoint.timestamp
                                );
                            }}
                            y0={(
                                alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                            ): number => {
                                return timeSeriesYScale(
                                    alertEvaluationTimeSeriesPoint.lowerBound
                                );
                            }}
                            y1={(
                                alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                            ): number => {
                                return timeSeriesYScale(
                                    alertEvaluationTimeSeriesPoint.upperBound
                                );
                            }}
                            yScale={timeSeriesYScale}
                        />
                    )}

                    {/* Current */}
                    {showCurrent && (
                        <LinePath
                            curve={curveNatural}
                            data={filteredAlertEvaluationTimeSeriesPoints}
                            defined={(
                                alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                            ): boolean => {
                                return isFinite(
                                    alertEvaluationTimeSeriesPoint.current
                                );
                            }}
                            stroke={Palette.COLOR_VISUALIZATION_STROKE_DEFAULT}
                            strokeWidth={
                                Dimension.WIDTH_VISUALIZATION_STROKE_DEFAULT
                            }
                            x={(
                                alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                            ): number => {
                                return timeSeriesXScale(
                                    alertEvaluationTimeSeriesPoint.timestamp
                                );
                            }}
                            y={(
                                alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                            ): number => {
                                return timeSeriesYScale(
                                    alertEvaluationTimeSeriesPoint.current
                                );
                            }}
                        />
                    )}

                    {/* Baseline */}
                    {showBaseline && (
                        <LinePath
                            curve={curveNatural}
                            data={filteredAlertEvaluationTimeSeriesPoints}
                            defined={(
                                alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                            ): boolean => {
                                return isFinite(
                                    alertEvaluationTimeSeriesPoint.expected
                                );
                            }}
                            stroke={Palette.COLOR_VISUALIZATION_STROKE_BASELINE}
                            strokeDasharray={
                                Dimension.WIDTH_VISUALIZATION_STROKE_DASHARRAY
                            }
                            strokeWidth={
                                Dimension.WIDTH_VISUALIZATION_STROKE_DEFAULT
                            }
                            x={(
                                alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                            ): number => {
                                return timeSeriesXScale(
                                    alertEvaluationTimeSeriesPoint.timestamp
                                );
                            }}
                            y={(
                                alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                            ): number => {
                                return timeSeriesYScale(
                                    alertEvaluationTimeSeriesPoint.expected
                                );
                            }}
                        />
                    )}

                    {/* X axis */}
                    <AxisBottom
                        numTicks={8}
                        scale={timeSeriesXScale}
                        tickClassName={
                            alertEvaluationTimeSeriesInternalClasses.axisLabel
                        }
                        tickFormat={formatMonthDayDateForVisualization}
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
                    {/* Upper and lower bound */}
                    <AreaClosed
                        curve={curveNatural}
                        data={alertEvaluationTimeSeriesPoints}
                        defined={(
                            alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                        ): boolean => {
                            return (
                                isFinite(
                                    alertEvaluationTimeSeriesPoint.lowerBound
                                ) &&
                                isFinite(
                                    alertEvaluationTimeSeriesPoint.upperBound
                                )
                            );
                        }}
                        fill={theme.palette.primary.main}
                        opacity={0.5}
                        stroke={theme.palette.primary.main}
                        strokeWidth={
                            Dimension.WIDTH_VISUALIZATION_STROKE_DEFAULT
                        }
                        x={(
                            alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                        ): number => {
                            return brushXScale(
                                alertEvaluationTimeSeriesPoint.timestamp
                            );
                        }}
                        y0={(
                            alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                        ): number => {
                            return brushYScale(
                                alertEvaluationTimeSeriesPoint.lowerBound
                            );
                        }}
                        y1={(
                            alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                        ): number => {
                            return brushYScale(
                                alertEvaluationTimeSeriesPoint.upperBound
                            );
                        }}
                        yScale={brushYScale}
                    />

                    {/* Current */}
                    <LinePath
                        curve={curveNatural}
                        data={alertEvaluationTimeSeriesPoints}
                        defined={(
                            alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                        ): boolean => {
                            return isFinite(
                                alertEvaluationTimeSeriesPoint.current
                            );
                        }}
                        opacity={0.5}
                        stroke={Palette.COLOR_VISUALIZATION_STROKE_DEFAULT}
                        strokeWidth={
                            Dimension.WIDTH_VISUALIZATION_STROKE_DEFAULT
                        }
                        x={(
                            alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                        ): number => {
                            return brushXScale(
                                alertEvaluationTimeSeriesPoint.timestamp
                            );
                        }}
                        y={(
                            alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                        ): number => {
                            return brushYScale(
                                alertEvaluationTimeSeriesPoint.current
                            );
                        }}
                    />

                    {/* Baseline */}
                    <LinePath
                        curve={curveNatural}
                        data={alertEvaluationTimeSeriesPoints}
                        defined={(
                            alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                        ): boolean => {
                            return isFinite(
                                alertEvaluationTimeSeriesPoint.expected
                            );
                        }}
                        opacity={0.5}
                        stroke={Palette.COLOR_VISUALIZATION_STROKE_BASELINE}
                        strokeDasharray={
                            Dimension.WIDTH_VISUALIZATION_STROKE_DASHARRAY
                        }
                        strokeWidth={
                            Dimension.WIDTH_VISUALIZATION_STROKE_DEFAULT
                        }
                        x={(
                            alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                        ): number => {
                            return brushXScale(
                                alertEvaluationTimeSeriesPoint.timestamp
                            );
                        }}
                        y={(
                            alertEvaluationTimeSeriesPoint: AlertEvaluationTimeSeriesPoint
                        ): number => {
                            return brushYScale(
                                alertEvaluationTimeSeriesPoint.expected
                            );
                        }}
                    />

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
                    <AxisBottom
                        scale={brushXScale}
                        tickClassName={
                            alertEvaluationTimeSeriesInternalClasses.axisLabel
                        }
                        tickFormat={formatMonthDayDateForVisualization}
                        top={brushYMax}
                    />
                </Group>
            </svg>

            {/* Legend */}
            <Legend direction="row" scale={legendOrdinalScale}>
                {(labels): ReactNode => (
                    <div
                        className={
                            alertEvaluationTimeSeriesInternalClasses.legendContainer
                        }
                    >
                        {labels.map(
                            (label, index): ReactNode => {
                                return (
                                    <LegendItem
                                        className={classnames(
                                            alertEvaluationTimeSeriesInternalClasses.legendItem,
                                            getTimeSeriesState(
                                                label.text as AlertEvaluationTimeSeriesPlot
                                            )
                                                ? ""
                                                : alertEvaluationTimeSeriesInternalClasses.legendItemDisabled
                                        )}
                                        key={index}
                                        onClick={(): void => {
                                            toggleTimeSeriesState(
                                                label.text as AlertEvaluationTimeSeriesPlot
                                            );
                                        }}
                                    >
                                        {/* Glyph */}
                                        <svg
                                            height={15}
                                            opacity={
                                                getTimeSeriesState(
                                                    label.text as AlertEvaluationTimeSeriesPlot
                                                )
                                                    ? 1
                                                    : 0.5
                                            }
                                            width={15}
                                        >
                                            <rect
                                                fill={label.value}
                                                height={15}
                                                width={15}
                                            />
                                        </svg>

                                        {/* Label */}
                                        <LegendLabel
                                            className={
                                                alertEvaluationTimeSeriesInternalClasses.legendItemText
                                            }
                                        >
                                            {
                                                <Typography variant="body2">
                                                    {t(
                                                        `label.${kebabCase(
                                                            label.text
                                                        )}`
                                                    )}
                                                </Typography>
                                            }
                                        </LegendLabel>
                                    </LegendItem>
                                );
                            }
                        )}
                    </div>
                )}
            </Legend>
        </div>
    );
};
