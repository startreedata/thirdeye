import { AxisScale } from "@visx/axis";
import { UseTooltipParams } from "@visx/tooltip/lib/hooks/useTooltip";
import { Event } from "../../../../rest/dto/event.interfaces";
import { NormalizedSeries } from "../time-series-chart.interfaces";

export interface EventsChartProps {
    series: NormalizedSeries[];
    events: Event[];
    xScale?: AxisScale<number>;
    width: number;
    xMax: number;
    margin: { top: number; right: number; bottom: number; left: number };
    tooltipUtils: UseTooltipParams<{ xValue: number }>;
    left?: number;
    isTooltipEnabled: boolean;
}

export interface EventsTooltipPopoverProps {
    events: Event[];
    colorScale: (id: number) => string;
}
