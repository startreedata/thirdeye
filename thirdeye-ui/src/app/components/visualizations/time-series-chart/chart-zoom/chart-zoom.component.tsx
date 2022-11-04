// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { Brush } from "@visx/brush";
import BaseBrush from "@visx/brush/lib/BaseBrush";
import { scaleLinear, scaleTime } from "@visx/scale";
import React, { FunctionComponent, useMemo, useRef } from "react";
import { Palette } from "../../../../utils/material-ui/palette.util";
import { getMinMax } from "../time-series-chart.utils";
import { ChartZoomProps } from "./chart-zoom.interfaces";

const SELECTED_BRUSH_STYLE = {
    fill: Palette.COLOR_VISUALIZATION_STROKE_BRUSH,
    fillOpacity: 0.4,
    strokeOpacity: 1,
    stroke: Palette.COLOR_VISUALIZATION_STROKE_BRUSH,
};

export const ChartZoom: FunctionComponent<ChartZoomProps> = ({
    series,
    height,
    width,
    onZoomChange,
    margins,
}) => {
    const brushRef = useRef<BaseBrush>(null);

    // Bounds
    const xBrushMax = Math.max(width - margins.left - margins.right, 0);
    const yBrushMax = height;

    const dateScale = useMemo(() => {
        // Scales
        const minMaxTimestamp = getMinMax(
            series.filter((s) => s.enabled),
            (d) => d.x
        );

        return scaleTime<number>({
            range: [0, xBrushMax],
            domain: [
                new Date(minMaxTimestamp[0]),
                new Date(minMaxTimestamp[1]),
            ] as [Date, Date],
        });
    }, [xBrushMax, series]);
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

    return (
        <Brush
            resetOnEnd
            useWindowMoveEvents
            brushDirection="horizontal"
            handleSize={8}
            height={yBrushMax}
            innerRef={brushRef}
            margin={{ ...margins }}
            selectedBoxStyle={SELECTED_BRUSH_STYLE}
            width={xBrushMax}
            xScale={dateScale}
            yScale={dataScale}
            onBrushEnd={(domain) => {
                if (domain) {
                    onZoomChange(domain);
                }
            }}
        />
    );
};
