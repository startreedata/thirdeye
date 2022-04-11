export interface DataPoint {
    x: number;
    y: number;
}

export interface Series {
    name?: string;
    data: DataPoint[];
    type?: string;
    enabled?: boolean;
}

export interface PlotBand {
    start: number;
    end: number;
    name: string;
    onClick?: (plotBand: PlotBand) => void;
    color?: string;
    opacity?: number;
}

export interface XAxisOptions {
    useUTC?: boolean;
    hideTime?: boolean;
    enabled?: boolean;
    plotBands?: PlotBand[];
}
export interface TimeSeriesChartProps {
    series: Series[];
    tooltip?: boolean;
    xAxis?: XAxisOptions;
    yAxis?: boolean;
    legend?: boolean;
    brush?: boolean;
    height?: number;
}

export interface TimeSeriesChartInternalProps extends TimeSeriesChartProps {
    height: number;
    width: number;
}
