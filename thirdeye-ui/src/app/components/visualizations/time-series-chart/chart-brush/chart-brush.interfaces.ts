import { Bounds } from "@visx/brush/lib/types";
import {
    NormalizedSeries,
    XAxisOptions,
    ZoomDomain,
} from "../time-series-chart.interfaces";

export interface ChartBrushProps {
    series: NormalizedSeries[];
    height: number;
    width: number;
    top: number;
    colorScale: (name: string) => string;
    onBrushChange: (domain: Bounds | null) => void;
    onBrushClick: () => void;
    xAxisOptions?: XAxisOptions;
    initialZoom?: ZoomDomain;
}
