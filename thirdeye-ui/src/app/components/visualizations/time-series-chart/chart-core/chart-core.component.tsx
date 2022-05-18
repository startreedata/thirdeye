import { AxisBottom, AxisLeft } from "@visx/axis";
import { Group } from "@visx/group";
import { scaleLinear, scaleTime } from "@visx/scale";
import { AreaClosed, Bar, LinePath } from "@visx/shape";
import React, { FunctionComponent, MouseEvent, useMemo } from "react";
import { StackedLine } from "../../stacked-line/stacked-line.component";
import { PlotBand } from "../plot-band/plot-band.component";
import {
    DataPoint,
    NormalizedSeries,
    SeriesType,
    ThresholdDataPoint,
} from "../time-series-chart.interfaces";
import { getMinMax } from "../time-series-chart.utils";
import { determineXPointForHover } from "../tooltip/tooltip.utils";
import { ChartCoreProps } from "./chart-core.interfaces";

const axisLeftTickLabelProps = {
    dx: "-0.25em",
    dy: "0.25em",
    fontFamily: "Arial",
    fontSize: 10,
    textAnchor: "end" as const,
};
const axisBottomTickLabelProps = {
    textAnchor: "middle" as const,
    fontFamily: "Arial",
    fontSize: 10,
};

export const ChartCore: FunctionComponent<ChartCoreProps> = ({
    series,
    width,
    yMax,
    xMax,
    margin,
    xScale,
    yScale,
    showXAxis = false,
    showYAxis = false,
    top,
    left,
    colorScale,
    children,
    xAxisOptions,
    tooltipUtils,
}) => {
    const marginTop = top || margin.top;
    const marginLeft = left || margin.left;
    // Scales
    const dateScale = useMemo(() => {
        const minMaxTimestamp = getMinMax(
            series.filter((s) => s.enabled),
            (d) => d.x
        );

        return scaleTime<number>({
            range: [0, xMax],
            domain: [
                new Date(minMaxTimestamp[0]),
                new Date(minMaxTimestamp[1]),
            ] as [Date, Date],
        });
    }, [xMax, series]);

    const dataScale = useMemo(
        () =>
            scaleLinear<number>({
                range: [yMax, 0],
                domain: [
                    0,
                    getMinMax(
                        series.filter((s) => s.enabled),
                        (d) => d.y
                    )[1] || 0,
                ],
            }),
        [yMax, series]
    );

    if (width < 10) {
        return null;
    }

    const xScaleToUse = xScale || dateScale;
    const yScaleToUse = yScale || dataScale;

    // Open the tooltip from the time series parent
    const handleMouseOver = (event: MouseEvent<SVGRectElement>): void => {
        const [xValue, coords] = determineXPointForHover(
            event,
            series,
            dateScale,
            marginLeft
        );

        if (xValue === null || coords === null) {
            tooltipUtils && tooltipUtils.hideTooltip();

            return;
        }

        tooltipUtils &&
            tooltipUtils.showTooltip({
                tooltipLeft: dateScale(xValue),
                tooltipTop: coords.y - marginTop,
                tooltipData: {
                    xValue: xValue,
                },
            });
    };

    return (
        <Group left={marginLeft} top={marginTop}>
            {xAxisOptions &&
                xAxisOptions.plotBands &&
                xAxisOptions.plotBands.map((plotBand, idx) => {
                    return (
                        <PlotBand
                            key={`plotband-${idx}`}
                            plotBand={plotBand}
                            xScale={xScaleToUse}
                            yScale={yScaleToUse}
                        />
                    );
                })}
            {series.map((seriesData: NormalizedSeries, idx: number) => {
                if (seriesData.enabled) {
                    const color =
                        seriesData.color === undefined
                            ? colorScale(seriesData.name as string)
                            : seriesData.color;
                    if (seriesData.type === "line") {
                        return (
                            <LinePath<DataPoint>
                                data={seriesData.data}
                                key={seriesData.name || `${idx}`}
                                stroke={color}
                                strokeWidth={seriesData.strokeWidth}
                                x={(d) =>
                                    xScaleToUse(seriesData.xAccessor(d)) || 0
                                }
                                y={(d) =>
                                    yScaleToUse(seriesData.yAccessor(d)) || 0
                                }
                            />
                        );
                    } else if (seriesData.type === "areaclosed") {
                        return (
                            <AreaClosed<ThresholdDataPoint>
                                data={seriesData.data as ThresholdDataPoint[]}
                                defined={(alertEvaluationTimeSeriesPoint) => {
                                    if (
                                        Number.isFinite(
                                            alertEvaluationTimeSeriesPoint.y
                                        ) &&
                                        Number.isFinite(
                                            alertEvaluationTimeSeriesPoint.y1
                                        )
                                    ) {
                                        // Upper and lower bound both available
                                        return true;
                                    }

                                    return false;
                                }}
                                fill={color}
                                fillOpacity={0.6}
                                key={seriesData.name || `${idx}`}
                                x={(d: ThresholdDataPoint) =>
                                    xScaleToUse(seriesData.xAccessor(d)) || 0
                                }
                                y0={(d: ThresholdDataPoint) =>
                                    yScaleToUse(seriesData.yAccessor(d)) || 0
                                }
                                y1={(d: ThresholdDataPoint) =>
                                    yScaleToUse(seriesData.y1Accessor(d)) || 0
                                }
                                yScale={yScaleToUse}
                            />
                        );
                    } else if (seriesData.type === SeriesType.LINE_STACKED) {
                        // Inverting range so that placement should be done upside down
                        const inverseScale = yScaleToUse.copy();
                        inverseScale.range([
                            yScaleToUse.range()[1],
                            yScaleToUse.range()[0],
                        ]);

                        return (
                            <StackedLine
                                points={seriesData.data}
                                stroke={seriesData.color}
                                strokeWidth={seriesData.strokeWidth}
                                x={(d: DataPoint) =>
                                    xScaleToUse(seriesData.xAccessor(d)) || 0
                                }
                                x1={(d: ThresholdDataPoint) =>
                                    xScaleToUse(seriesData.x1Accessor(d)) || 0
                                }
                                y={(d: DataPoint) =>
                                    inverseScale(seriesData.yAccessor(d)) || 0
                                }
                                y1={(d: ThresholdDataPoint) =>
                                    inverseScale(seriesData.y1Accessor(d)) || 0
                                }
                            />
                        );
                    }
                }

                return;
            })}
            {tooltipUtils && (
                /* Mouse hover region for tooltip */
                <Bar
                    cursor="default"
                    height={yScaleToUse.range()[0]}
                    opacity={0}
                    width={xScaleToUse.range()[1]}
                    x={xScaleToUse.range()[0]}
                    y={yScaleToUse.range()[1]}
                    onMouseLeave={tooltipUtils.hideTooltip}
                    onMouseMove={handleMouseOver}
                />
            )}
            {showXAxis && (
                <AxisBottom
                    numTicks={width > 520 ? 10 : 5}
                    scale={xScaleToUse}
                    tickLabelProps={() => axisBottomTickLabelProps}
                    top={yMax}
                />
            )}
            {showYAxis && (
                <AxisLeft
                    numTicks={5}
                    scale={yScaleToUse}
                    tickLabelProps={() => axisLeftTickLabelProps}
                />
            )}
            {children && children(xScaleToUse, yScaleToUse)}
        </Group>
    );
};
