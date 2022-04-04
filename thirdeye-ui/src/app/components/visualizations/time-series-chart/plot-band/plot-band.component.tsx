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
                height={yScale.range()[0]}
                opacity={plotBand.opacity === undefined ? 1 : plotBand.opacity}
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
