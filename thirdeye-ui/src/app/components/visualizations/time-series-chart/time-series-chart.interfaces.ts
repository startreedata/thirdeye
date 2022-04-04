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

export interface XAxisOptions {
    useUTC?: boolean;
    hideTime?: boolean;
    enabled?: boolean;
}
export interface TimeSeriesChartProps {
    series: Series[];
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
