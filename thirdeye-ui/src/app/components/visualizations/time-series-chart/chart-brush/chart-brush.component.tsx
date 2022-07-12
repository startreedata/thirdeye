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
import { Brush } from "@visx/brush";
import BaseBrush from "@visx/brush/lib/BaseBrush";
import { PartialBrushStartEnd } from "@visx/brush/lib/types";
import { scaleLinear, scaleTime } from "@visx/scale";
import React, { FunctionComponent, useMemo, useRef } from "react";
import { ChartCore } from "../chart-core/chart-core.component";
import { ChartCoreProps } from "../chart-core/chart-core.interfaces";
import { getMinMax } from "../time-series-chart.utils";
import { ChartBrushProps } from "./chart-brush.interfaces";

const BRUSH_MARGIN = { top: 10, bottom: 15, left: 50, right: 20 };

const SELECTED_BRUSH_STYLE = {
    fill: "rgba(0, 0, 0, 0.25)",
    stroke: "white",
};

export const ChartBrush: FunctionComponent<ChartBrushProps> = ({
    series,
    height,
    width,
    colorScale,
    top,
    onBrushChange,
    onBrushClick,
    xAxisOptions,
    initialZoom,
}) => {
    const brushRef = useRef<BaseBrush>(null);

    // Bounds
    const xBrushMax = Math.max(
        width - BRUSH_MARGIN.left - BRUSH_MARGIN.right,
        0
    );
    const yBrushMax = Math.max(
        height - BRUSH_MARGIN.top - BRUSH_MARGIN.bottom,
        0
    );

    // Scales
    const minMaxTimestamp = getMinMax(
        series.filter((s) => s.enabled),
        (d) => d.x
    );
    const dateScale = useMemo(
        () =>
            scaleTime<number>({
                range: [0, xBrushMax],
                domain: [
                    new Date(minMaxTimestamp[0]),
                    new Date(minMaxTimestamp[1]),
                ] as [Date, Date],
            }),
        [xBrushMax, series]
    );
    const dataScale = useMemo(() => {
        const minMaxValues = getMinMax(
            series.filter((s) => s.enabled),
            (d) => d.y
        );

        return scaleLinear<number>({
            range: [yBrushMax, 0],
            domain: [minMaxValues[0], minMaxValues[1] || 0],
        });
    }, [yBrushMax, series]);

    const chartOptions: ChartCoreProps = {
        series,
        width,
        yMax: yBrushMax,
        xMax: xBrushMax,
        showXAxis: true,
        showYAxis: false,
        colorScale,
        margin: BRUSH_MARGIN,
        top,
        xScale: dateScale,
        yScale: dataScale,
    };

    if (xAxisOptions && xAxisOptions.plotBands) {
        chartOptions.xAxisOptions = { ...xAxisOptions };
        chartOptions.xAxisOptions.plotBands = xAxisOptions.plotBands.map(
            (plotBand) => {
                const clone = { ...plotBand };
                clone.name = "";

                return clone;
            }
        );
    }

    return (
        <ChartCore {...chartOptions}>
            {() => (
                <Brush
                    useWindowMoveEvents
                    brushDirection="horizontal"
                    handleSize={8}
                    height={yBrushMax}
                    initialBrushPosition={
                        initialZoom
                            ? ({
                                  start: { x: dateScale(initialZoom.x0) },
                                  end: { x: dateScale(initialZoom.x1) },
                              } as PartialBrushStartEnd)
                            : undefined
                    }
                    innerRef={brushRef}
                    margin={BRUSH_MARGIN}
                    resizeTriggerAreas={["left", "right"]}
                    selectedBoxStyle={SELECTED_BRUSH_STYLE}
                    width={xBrushMax}
                    xScale={dateScale}
                    yScale={dataScale}
                    onChange={onBrushChange}
                    onClick={onBrushClick}
                />
            )}
        </ChartCore>
    );
};
