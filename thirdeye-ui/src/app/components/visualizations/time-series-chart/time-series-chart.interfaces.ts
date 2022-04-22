export interface DataPoint {
    x: number;
    y: number;
}

export interface ThresholdDataPoint extends DataPoint {
    y1: number; // y1 is used in Threshold shapes
}

export interface Series {
    name?: string;
    data: DataPoint[] | ThresholdDataPoint[];
    type?: SeriesType;
    color?: string;
    enabled?: boolean;
    xAccessor?: (d: DataPoint | ThresholdDataPoint) => Date;
    yAccessor?: (d: DataPoint | ThresholdDataPoint) => number;
    y1Accessor?: (d: ThresholdDataPoint) => number;
}

export interface NormalizedSeries {
    name: string;
    data: DataPoint[] | ThresholdDataPoint[];
    type: SeriesType;
    color?: string;
    enabled: boolean;
    xAccessor: (d: DataPoint | ThresholdDataPoint) => Date;
    yAccessor: (d: DataPoint | ThresholdDataPoint) => number;
    y1Accessor: (d: ThresholdDataPoint) => number;
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

export enum SeriesType {
    LINE = "line",
    AREA_CLOSED = "areaclosed",
}
