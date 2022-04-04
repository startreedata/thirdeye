import { AxisBottom, AxisLeft } from "@visx/axis";
import { curveMonotoneX } from "@visx/curve";
import { Group } from "@visx/group";
import { scaleLinear, scaleTime } from "@visx/scale";
import { LinePath } from "@visx/shape";
import React, { FunctionComponent, useMemo } from "react";
import { DataPoint, Series } from "../time-series-chart.interfaces";
import { getMinMax } from "../time-series-chart.utils";
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
    xAccessor = (d: DataPoint) => new Date(d.x),
    yAccessor = (d: DataPoint) => d.y,
    children,
}) => {
    // Scales
    const dateScale = useMemo(() => {
        const minMaxTimestamp = getMinMax(series, (d) => d.x);

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
                domain: [0, getMinMax(series, (d) => d.y)[1] || 0],
                nice: true,
            }),
        [yMax, series]
    );

    if (width < 10) {
        return null;
    }

    const xScaleToUse = xScale || dateScale;
    const yScaleToUse = yScale || dataScale;

    return (
        <Group left={left || margin.left} top={top || margin.top}>
            {series.map((seriesData: Series, idx: number) => {
                if (seriesData.enabled) {
                    return (
                        <LinePath<DataPoint>
                            curve={curveMonotoneX}
                            data={seriesData.data}
                            key={seriesData.name || `${idx}`}
                            stroke={colorScale(seriesData.name as string)}
                            strokeWidth={1}
                            x={(d) => xScaleToUse(xAccessor(d)) || 0}
                            y={(d) => yScaleToUse(yAccessor(d)) || 0}
                        />
                    );
                } else {
                    return;
                }
            })}
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
            {children}
        </Group>
    );
};
