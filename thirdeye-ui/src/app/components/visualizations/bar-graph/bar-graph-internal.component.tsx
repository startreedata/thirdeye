/*
 * Copyright 2024 StarTree Inc
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
import React from "react";
import { Group } from "@visx/group";
import { BarGroup } from "@visx/shape";
import { AxisBottom, AxisRight } from "@visx/axis";
import { scaleBand, scaleLinear, scaleOrdinal } from "@visx/scale";
import { withTooltip, Tooltip, defaultStyles } from "@visx/tooltip";
import { BarData, BarGraphProps, TooltipProps } from "./bar-graph.interfaces";
import { ScaleLinear } from "d3-scale";

const tooltipStyles = {
    ...defaultStyles,
    minWidth: 60,
    color: "black",
};
let tooltipTimeout: number;

export default withTooltip(
    ({
        data,
        height,
        width,
        margins,
        keysColorMapping,
        tooltipOpen,
        tooltipLeft,
        tooltipTop,
        tooltipData,
        hideTooltip,
        showTooltip,
        tooltipRenderer,
    }: BarGraphProps & TooltipProps): JSX.Element => {
        const keys = Object.keys(keysColorMapping);
        const barColors = Object.values(keysColorMapping);
        const xMax = width! - margins!.left - margins!.right;
        const yMax = height! - margins!.top - margins!.bottom - 1;

        const getDate = (d: BarData): string => d.date;

        // Note: The below functions can be passed as props to component if any customizations are needed.

        /* This scales the x-axis based on date or whatever range of
  x-axis values are provided within xMax Range */
        const dateScale = scaleBand<string>({
            domain: data.map(getDate),
            padding: 0.1,
        });
        dateScale.rangeRound([0, xMax]);

        /* This scales the individual group of bars on x-axis based on individual
  bandwidth(pixels in simple terms) reserved for that group */
        const barGroupScale = scaleBand<string>({
            domain: keys,
            padding: 0.01,
        });
        barGroupScale.rangeRound([0, dateScale.bandwidth()]);

        // This mpas the y-axis values and bound them within the yMax range
        const yAxisScale = scaleLinear<number>({
            domain: [
                0,
                Math.max(
                    ...data.map((d) =>
                        Math.max(...keys.map((key) => Number(d[key])))
                    )
                ),
            ],
        });
        yAxisScale.range([yMax, 0]);

        const colorScale = scaleOrdinal<string, string>({
            domain: keys,
            range: barColors,
        });

        // Generates y-axis tick values
        const generateTickValues = (
            scale: ScaleLinear<number, number, never>
        ): number[] => {
            const domain = scale.domain();
            const [min, max] = domain;
            const ticks = [];
            const incrementBy = Math.ceil(Math.floor(max) / 10) || 1;
            for (let i = Math.ceil(min); i <= Math.floor(max); ) {
                ticks.push(i);
                i = i + incrementBy;
            }

            return ticks;
        };

        return (
            <>
                <svg height={height} width={width}>
                    <Group left={margins!.left} top={margins!.top}>
                        <BarGroup
                            color={colorScale}
                            data={data}
                            height={yMax}
                            keys={keys}
                            x0={getDate}
                            x0Scale={dateScale}
                            x1Scale={barGroupScale}
                            yScale={yAxisScale}
                        >
                            {(barGroups) => {
                                return barGroups.map((barGroup) => (
                                    <Group
                                        key={`bar-group-${barGroup.index}-${barGroup.x0}`}
                                        left={barGroup.x0}
                                    >
                                        {barGroup.bars.map((bar) => (
                                            <rect
                                                fill={bar.color}
                                                height={bar.height}
                                                key={`bar-group-bar-${barGroup.index}-${bar.index}-${bar.value}-${bar.key}`}
                                                rx={4}
                                                width={bar.width}
                                                x={bar.x}
                                                y={bar.y}
                                                onMouseLeave={() =>
                                                    hideTooltip && hideTooltip()
                                                }
                                                onMouseMove={() => {
                                                    if (tooltipTimeout) {
                                                        clearTimeout(
                                                            tooltipTimeout
                                                        );
                                                    }
                                                    const top =
                                                        bar.y + margins!.top;
                                                    const left =
                                                        bar.x + margins!.left;
                                                    showTooltip &&
                                                        showTooltip({
                                                            tooltipData:
                                                                data[
                                                                    barGroup
                                                                        .index
                                                                ].tooltipData,
                                                            tooltipTop: top,
                                                            tooltipLeft: left,
                                                        });
                                                }}
                                            />
                                        ))}
                                    </Group>
                                ));
                            }}
                        </BarGroup>
                    </Group>
                    <AxisBottom
                        left={margins!.right}
                        scale={dateScale}
                        stroke="black"
                        tickLabelProps={{
                            fill: "black",
                            fontSize: 10,
                            textAnchor: "middle",
                        }}
                        top={yMax + margins!.top}
                    />
                    <AxisRight
                        left={xMax + margins!.right}
                        scale={yAxisScale}
                        stroke="black"
                        tickLabelProps={{
                            fill: "black",
                            fontSize: 10,
                            textAnchor: "start",
                        }}
                        tickValues={generateTickValues(yAxisScale)}
                        top={margins!.top}
                    />
                </svg>
                {tooltipOpen && tooltipData && tooltipRenderer && (
                    <Tooltip
                        left={tooltipLeft}
                        style={tooltipStyles}
                        top={tooltipTop}
                    >
                        {tooltipRenderer(tooltipData)}
                    </Tooltip>
                )}
            </>
        );
    }
);
