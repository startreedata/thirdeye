import { Bounds } from "@visx/brush/lib/types";
import { Series, XAxisOptions } from "../time-series-chart.interfaces";

export interface ChartBrushProps {
    series: Series[];
    height: number;
    width: number;
    top: number;
    colorScale: (name: string) => string;
    onBrushChange: (domain: Bounds | null) => void;
    onBrushClick: () => void;
    xAxisOptions?: XAxisOptions;
}
