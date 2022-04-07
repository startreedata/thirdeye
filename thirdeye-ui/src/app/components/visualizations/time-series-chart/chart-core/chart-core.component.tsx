import { AxisBottom, AxisLeft } from "@visx/axis";
import { Group } from "@visx/group";
import { scaleLinear, scaleTime } from "@visx/scale";
import { Bar, LinePath } from "@visx/shape";
import React, { FunctionComponent, MouseEvent, useMemo } from "react";
import { PlotBand } from "../plot-band/plot-band.component";
import { DataPoint, Series } from "../time-series-chart.interfaces";
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
    xAccessor = (d: DataPoint) => new Date(d.x),
    yAccessor = (d: DataPoint) => d.y,
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
            {series.map((seriesData: Series, idx: number) => {
                if (seriesData.enabled) {
                    return (
                        <LinePath<DataPoint>
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
