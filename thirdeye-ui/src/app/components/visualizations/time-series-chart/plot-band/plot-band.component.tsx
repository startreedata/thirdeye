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
import { Group } from "@visx/group";
import { Bar } from "@visx/shape";
import { Text } from "@visx/text";
import React, { FunctionComponent } from "react";
import { DEFAULT_PLOTBAND_COLOR } from "../time-series-chart.utils";
import { PlotBandProps } from "./plot-band.interfaces";

export const PlotBand: FunctionComponent<PlotBandProps> = ({
    plotBand,
    xScale,
    yScale,
}) => {
    const width =
        (xScale(plotBand.end) as number) - (xScale(plotBand.start) as number);

    return (
        <Group>
            <Bar
                fill={plotBand.color || DEFAULT_PLOTBAND_COLOR}
                fillOpacity={
                    plotBand.opacity === undefined ? 1 : plotBand.opacity
                }
                height={yScale.range()[0]}
                stroke={plotBand.color || DEFAULT_PLOTBAND_COLOR}
                strokeDasharray={4}
                strokeWidth={2}
                width={width}
                x={xScale(plotBand.start)}
                y={yScale.range()[1]}
                onClick={() => {
                    plotBand.onClick && plotBand.onClick(plotBand);
                }}
            />
            {plotBand.name && (
                <Text
                    style={{ fontSize: "0.75em" }}
                    x={xScale(plotBand.start)}
                    y={yScale.range()[1] - 5}
                >
                    {plotBand.name}
                </Text>
            )}
        </Group>
    );
};
