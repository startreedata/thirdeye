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
import { AxisBottom, AxisLeft, AxisRight, Orientation } from "@visx/axis";
import { LinearGradient } from "@visx/gradient";
import { Group } from "@visx/group";
import { scaleLinear, scaleTime } from "@visx/scale";
import { AreaClosed, Bar, LinePath } from "@visx/shape";
import React, { FunctionComponent, useMemo } from "react";
import {
    determineGranularity,
    formatLargeNumberForVisualization,
} from "../../../../utils/visualization/visualization.util";
import { PlotBand } from "../plot-band/plot-band.component";
import {
    DataPoint,
    NormalizedSeries,
    SeriesType,
    ThresholdDataPoint,
} from "../time-series-chart.interfaces";
import { getMinMax } from "../time-series-chart.utils";
import { ChartCoreProps } from "./chart-core.interfaces";

const axisBottomTickLabelProps = {
    textAnchor: "middle" as const,
    fontFamily: "Arial",
    fontSize: 10,
};

export const ChartCore: FunctionComponent<ChartCoreProps> = ({
    series,
    width,
    height,
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
    yAxisOptions,
    onMouseLeave,
    onMouseMove,
    onMouseEnter,
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

    const dataScale = useMemo(() => {
        const minMaxValues = getMinMax(
            series.filter((s) => s.enabled),
            (d, seriesOptions) => {
                if (seriesOptions.type === SeriesType.AREA_CLOSED) {
                    const dThreshold = d as ThresholdDataPoint;

                    return [dThreshold.y, dThreshold.y1];
                } else {
                    return [d.y];
                }
            }
        );

        return scaleLinear<number>({
            range: [yMax, 0],
            domain: [minMaxValues[0], minMaxValues[1] || 0],
            nice: true,
            clamp: true,
        });
    }, [yMax, series]);

    const xScaleToUse = xScale || dateScale;
    const yScaleToUse = yScale || dataScale;

    /**
     *  Determine if it's Right or Left axis. Default it to left oriented axis
     */
    const yAxisConfiguration = useMemo(() => {
        if (yAxisOptions && yAxisOptions.position === Orientation.right) {
            return {
                AxisToUse: AxisRight,
                left: width - marginLeft - margin.right,
                orientation: Orientation.right,
            };
        } else {
            return {
                AxisToUse: AxisLeft,
                left: width - marginLeft - margin.right,
                orientation: Orientation.left,
            };
        }
    }, [yAxisOptions, width, marginLeft, margin]);

    if (width < 10) {
        return null;
    }

    let afterChildren: React.ReactElement[] = [];

    return (
        <Group
            left={marginLeft}
            top={marginTop}
            onMouseEnter={onMouseEnter}
            onMouseLeave={onMouseLeave}
            onMouseMove={onMouseMove}
        >
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
                        seriesData.color ??
                        colorScale(seriesData.name as string);
                    if (seriesData.type === SeriesType.LINE) {
                        return (
                            <LinePath<DataPoint>
                                data={seriesData.data}
                                defined={(timeSeriesPoint) => {
                                    return !!Number.isFinite(timeSeriesPoint.y);
                                }}
                                key={seriesData.name || `${idx}`}
                                stroke={seriesData.stroke || color}
                                strokeDasharray={seriesData.strokeDasharray}
                                strokeWidth={seriesData.strokeWidth}
                                x={(d) =>
                                    xScaleToUse(seriesData.xAccessor(d)) || 0
                                }
                                y={(d) =>
                                    yScaleToUse(seriesData.yAccessor(d)) || 0
                                }
                            />
                        );
                    } else if (seriesData.type === SeriesType.AREA_CLOSED) {
                        const gradientId = `${seriesData.name}-gradient`;

                        return (
                            <>
                                {seriesData.gradient && (
                                    <LinearGradient
                                        {...seriesData.gradient}
                                        id={gradientId}
                                    />
                                )}

                                <AreaClosed<ThresholdDataPoint>
                                    data={
                                        seriesData.data as ThresholdDataPoint[]
                                    }
                                    defined={(timeSeriesPoint) => {
                                        return !!(
                                            Number.isFinite(
                                                timeSeriesPoint.y
                                            ) &&
                                            Number.isFinite(timeSeriesPoint.y1)
                                        );
                                    }}
                                    fill={
                                        seriesData.gradient
                                            ? `url(#${gradientId})`
                                            : color
                                    }
                                    fillOpacity={seriesData.fillOpacity}
                                    key={seriesData.name || `${idx}`}
                                    stroke={seriesData.stroke}
                                    strokeOpacity={1}
                                    strokeWidth={seriesData.strokeWidth}
                                    x={(d: ThresholdDataPoint) =>
                                        xScaleToUse(seriesData.xAccessor(d)) ||
                                        0
                                    }
                                    y0={(d: ThresholdDataPoint) =>
                                        yScaleToUse(seriesData.yAccessor(d)) ||
                                        0
                                    }
                                    y1={(d: ThresholdDataPoint) =>
                                        yScaleToUse(seriesData.y1Accessor(d)) ||
                                        0
                                    }
                                    yScale={yScaleToUse}
                                />
                            </>
                        );
                    } else if (seriesData.type === SeriesType.BAR) {
                        const data = seriesData.data as DataPoint[];

                        const granularity = determineGranularity(
                            data.map((d: DataPoint) => d.x)
                        );

                        return (
                            <>
                                {data.map((d: DataPoint) => {
                                    const barHeight =
                                        yMax - (dataScale(d.y) ?? 0);
                                    const barX = dateScale(d.x);
                                    const barY = yMax - barHeight;

                                    let barWidth =
                                        dateScale(granularity * 2) -
                                        dateScale(granularity) -
                                        2;
                                    const isOverDomain =
                                        barX + barWidth / 2 >
                                        dateScale(dateScale.domain()[1]);

                                    let xValue = barX - barWidth / 2;

                                    if (isOverDomain) {
                                        xValue =
                                            dateScale(dateScale.domain()[1]) -
                                            barWidth / 4;
                                        barWidth = barWidth / 4;
                                    }

                                    return (
                                        <Bar
                                            fill={d.color ?? color}
                                            height={barHeight}
                                            key={`${seriesData.name}-${d.x}`}
                                            width={barWidth}
                                            x={xValue}
                                            y={barY}
                                        />
                                    );
                                })}
                            </>
                        );
                    } else if (seriesData.type === SeriesType.CUSTOM) {
                        // #TODO implement intuitive ordering system
                        if (seriesData.customRenderer) {
                            afterChildren = [
                                ...afterChildren,
                                ...seriesData.customRenderer(
                                    xScaleToUse,
                                    yScaleToUse
                                ),
                            ];
                        }
                    }
                }

                return;
            })}
            {children && children(xScaleToUse, yScaleToUse)}
            {/* #TODO implement intuitive ordering system */}
            {afterChildren}
            {showXAxis && (
                <AxisBottom
                    numTicks={width > 520 ? 10 : 5}
                    scale={xScaleToUse}
                    tickFormat={
                        xAxisOptions ? xAxisOptions.tickFormatter : undefined
                    }
                    tickLabelProps={() => axisBottomTickLabelProps}
                    top={yMax}
                />
            )}
            {showYAxis && (
                <yAxisConfiguration.AxisToUse
                    left={yAxisConfiguration.left}
                    numTicks={height > 520 ? undefined : 5}
                    orientation={yAxisConfiguration.orientation}
                    scale={yScaleToUse}
                    tickFormat={formatLargeNumberForVisualization}
                />
            )}
        </Group>
    );
};
