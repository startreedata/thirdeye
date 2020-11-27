export interface TimeSeriesChartProps {
    height: number;
    width: number;
    margin: {
        left: number;
        right: number;
        top: number;
        bottom: number;
    };
    data: TimeSeriesProps[];
    compact?: boolean;
}

export interface TimeSeriesProps {
    timestamp: Date;
    current: number;
    expacted: number;
    upperBound: number;
    lowerBound: number;
}
