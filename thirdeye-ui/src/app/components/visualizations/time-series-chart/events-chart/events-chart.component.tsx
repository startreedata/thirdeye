import { Group } from "@visx/group";
import { scaleLinear, scaleOrdinal, scaleTime } from "@visx/scale";
import { Circle, Line } from "@visx/shape";
import { TooltipWithBounds } from "@visx/tooltip";
import React, { FunctionComponent, useMemo } from "react";
import { Dimension } from "../../../../utils/material-ui/dimension.util";
import { COLOR_PALETTE, getMinMax } from "../time-series-chart.utils";
import { TOOLTIP_LINE_COLOR } from "../tooltip/tooltip.utils";
import { EventsChartProps } from "./events-chart.interfaces";
import { EventsTooltipPopover } from "./events-tooltip-popover.component";

const DEFAULT_GAP_BETWEEN_LINES = 10;

export const EventsChart: FunctionComponent<EventsChartProps> = ({
    series,
    events,
    width,
    xMax,
    margin,
    xScale,
    left,
    isTooltipEnabled,
    tooltipUtils,
}) => {
    const { tooltipData, tooltipLeft } = tooltipUtils;

    const eventsForXValue =
        isTooltipEnabled && tooltipData
            ? events.filter((event) => {
                  return (
                      event.enabled &&
                      tooltipData.xValue >= event.startTime &&
                      tooltipData.xValue <= event.endTime
                  );
              })
            : [];

    const height =
        events.length *
        (DEFAULT_GAP_BETWEEN_LINES +
            Dimension.WIDTH_VISUALIZATION_STROKE_BASELINE);

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

    const yScale = useMemo(
        () =>
            scaleLinear<number>({
                range: [0, height],
                domain: [
                    0,
                    (events.length + 1) *
                        (DEFAULT_GAP_BETWEEN_LINES +
                            Dimension.WIDTH_VISUALIZATION_STROKE_BASELINE),
                ],
            }),
        [height, events]
    );

    const colorScale = scaleOrdinal({
        domain: events.map((x) => x.id) as number[],
        range: COLOR_PALETTE,
    });

    const xScaleToUse = xScale || dateScale;

    return (
        <>
            <svg height={height} width={width}>
                <Group left={marginLeft}>
                    {events &&
                        events
                            .filter((event) => event.enabled)
                            .map((event, index) => {
                                const from = {
                                    x: dateScale(event.startTime),
                                    y: yScale(
                                        (index + 1) * DEFAULT_GAP_BETWEEN_LINES
                                    ),
                                };
                                const to = {
                                    x: xScaleToUse(event.endTime),
                                    y: yScale(
                                        (index + 1) * DEFAULT_GAP_BETWEEN_LINES
                                    ),
                                };

                                return (
                                    <React.Fragment key={`event-line-${index}`}>
                                        <Circle
                                            cx={from.x}
                                            cy={from.y}
                                            fill={colorScale(event.id)}
                                            r={
                                                Dimension.WIDTH_VISUALIZATION_STROKE_BASELINE
                                            }
                                        />
                                        <Circle
                                            cx={to.x}
                                            cy={to.y}
                                            fill={colorScale(event.id)}
                                            r={
                                                Dimension.WIDTH_VISUALIZATION_STROKE_BASELINE
                                            }
                                        />
                                        <Line
                                            from={from}
                                            key={index}
                                            stroke={colorScale(event.id)}
                                            strokeWidth={
                                                Dimension.WIDTH_VISUALIZATION_STROKE_BASELINE
                                            }
                                            to={to}
                                        />
                                    </React.Fragment>
                                );
                            })}
                </Group>
                {tooltipData && (
                    <Group left={marginLeft}>
                        {/* Vertical Line */}
                        <Line
                            from={{ x: dateScale(tooltipData.xValue), y: 0 }}
                            pointerEvents="none"
                            stroke={TOOLTIP_LINE_COLOR}
                            strokeDasharray="5,2"
                            strokeWidth={2}
                            to={{
                                x: dateScale(tooltipData.xValue),
                                y: height,
                            }}
                        />
                    </Group>
                )}
            </svg>
            {eventsForXValue.length > 0 && !!tooltipLeft && (
                <TooltipWithBounds
                    // set this to random so it correctly updates with parent bounds
                    key={Math.random()}
                    left={tooltipLeft + margin.left}
                >
                    <EventsTooltipPopover
                        colorScale={colorScale}
                        events={eventsForXValue}
                    />
                </TooltipWithBounds>
            )}
        </>
    );
};
