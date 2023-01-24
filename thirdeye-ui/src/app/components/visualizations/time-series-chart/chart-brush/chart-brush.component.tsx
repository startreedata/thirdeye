/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Brush } from "@visx/brush";
import BaseBrush, {
    BaseBrushState,
    UpdateBrush,
} from "@visx/brush/lib/BaseBrush";
import { Group } from "@visx/group";
import { PatternLines } from "@visx/pattern";
import { scaleLinear, scaleTime } from "@visx/scale";
import { isEqual } from "lodash";
import React, {
    FunctionComponent,
    useEffect,
    useMemo,
    useRef,
    useState,
} from "react";
import { ChartCore } from "../chart-core/chart-core.component";
import { ChartCoreProps } from "../chart-core/chart-core.interfaces";
import { getMinMax } from "../time-series-chart.utils";
import { BrushHandle } from "./chart-brush-handle.component";
import { ChartBrushProps } from "./chart-brush.interfaces";

const PATTERN_ID = "brush_pattern";
const accentColor = "#BBBBBB";
const selectedBrushStyle = {
    fill: `url(#${PATTERN_ID})`,
    stroke: "#AAAAAA",
};
const UPDATE_STOP_WAIT = 500;

export const ChartBrush: FunctionComponent<ChartBrushProps> = ({
    series,
    height,
    width,
    colorScale,
    top,
    onBrushChange,
    onBrushClick,
    xAxisOptions,
    currentZoom,
    margins,
    onMouseEnter,
}) => {
    const [isChangeFromParent, setIsChangeFromParent] = useState(false);
    const [isChangeFromDrag, setIsChangeFromDrag] = useState(false);
    const brushRef = useRef<BaseBrush>(null);

    // Bounds
    const xBrushMax = Math.max(width - margins.left - margins.right, 0);
    const yBrushMax = Math.max(height - margins.top - margins.bottom, 0);

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
        height,
        yMax: yBrushMax,
        xMax: xBrushMax,
        showXAxis: true,
        showYAxis: false,
        colorScale,
        margin: margins,
        top,
        xScale: dateScale,
        yScale: dataScale,
        xAxisOptions,
    };

    if (xAxisOptions && xAxisOptions.plotBands) {
        chartOptions.xAxisOptions = chartOptions.xAxisOptions ?? {
            ...xAxisOptions,
        };
        chartOptions.xAxisOptions.plotBands = xAxisOptions.plotBands.map(
            (plotBand) => {
                const clone = { ...plotBand };
                clone.name = "";

                return clone;
            }
        );
    }

    useEffect(() => {
        if (isChangeFromDrag) {
            const id = setTimeout(() => {
                setIsChangeFromDrag(false);
            }, UPDATE_STOP_WAIT);

            return (): void => {
                clearTimeout(id);
            };
        }

        if (
            !brushRef ||
            !brushRef.current ||
            brushRef.current.state.isBrushing
        ) {
            return;
        }

        let newExtent: {
            x0: number;
            x1: number;
            y0: number;
            y1: number;
        };
        if (currentZoom) {
            newExtent = brushRef.current.getExtent(
                { x: dateScale(currentZoom.x0) },
                { x: dateScale(currentZoom.x1) }
            );
        } else {
            newExtent = brushRef.current.getExtent(
                { x: dateScale(minMaxTimestamp[0]) },
                { x: dateScale(minMaxTimestamp[1]) }
            );
        }

        // Exit if the zoom levels are the same
        if (isEqual(newExtent, brushRef.current.state.extent)) {
            return;
        }

        const updater: UpdateBrush = (prevBrush) => {
            const newState: BaseBrushState = {
                ...prevBrush,
                start: { y: newExtent.y0, x: newExtent.x0 },
                end: { y: newExtent.y1, x: newExtent.x1 },
                extent: newExtent,
            };

            return newState;
        };

        setIsChangeFromParent(true);
        brushRef.current.updateBrush(updater);

        return;
    }, [currentZoom, height, brushRef.current]);

    return (
        <Group onMouseEnter={onMouseEnter}>
            <PatternLines
                height={8}
                id={PATTERN_ID}
                orientation={["diagonal"]}
                stroke={accentColor}
                strokeWidth={1}
                width={8}
            />
            <ChartCore {...chartOptions}>
                {() => (
                    <Brush
                        useWindowMoveEvents
                        brushDirection="horizontal"
                        handleSize={8}
                        height={yBrushMax}
                        innerRef={brushRef}
                        margin={{ ...margins }}
                        renderBrushHandle={(props) => (
                            <BrushHandle {...props} />
                        )}
                        resizeTriggerAreas={["left", "right"]}
                        selectedBoxStyle={selectedBrushStyle}
                        width={xBrushMax}
                        xScale={dateScale}
                        yScale={dataScale}
                        onBrushStart={() => {
                            setIsChangeFromDrag(true);
                        }}
                        onChange={(s) => {
                            !isChangeFromParent && onBrushChange(s);
                            isChangeFromParent &&
                                !isChangeFromDrag &&
                                setTimeout(() => {
                                    setIsChangeFromParent(false);
                                }, UPDATE_STOP_WAIT);
                        }}
                        onClick={onBrushClick}
                    />
                )}
            </ChartCore>
        </Group>
    );
};
