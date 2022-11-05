import { AxisScale } from "@visx/axis";
import { UseTooltipParams } from "@visx/tooltip/lib/hooks/useTooltip";
import {
    EventWithChartState,
    NormalizedSeries,
} from "../time-series-chart.interfaces";

export interface EventsChartProps {
    series: NormalizedSeries[];
    events: EventWithChartState[];
    xScale?: AxisScale<number>;
    width: number;
    xMax: number;
    margin: { top: number; right: number; bottom: number; left: number };
    tooltipUtils: UseTooltipParams<{ xValue: number }>;
    left?: number;
    isTooltipEnabled: boolean;
}

export interface EventsTooltipPopoverProps {
    events: EventWithChartState[];
    colorScale: (id: number) => string;
}
