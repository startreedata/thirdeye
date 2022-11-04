import { Bounds } from "@visx/brush/lib/types";
import { NormalizedSeries } from "../time-series-chart.interfaces";

export interface ChartZoomProps {
    series: NormalizedSeries[];
    height: number;
    width: number;
    colorScale: (name: string) => string;
    onZoomChange: (domain: Bounds | null) => void;
    margins: {
        left: number;
        right: number;
        bottom: number;
        top: number;
    };
}
