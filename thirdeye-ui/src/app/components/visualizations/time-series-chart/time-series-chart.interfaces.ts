import { AxisScale, Orientation } from "@visx/axis";
import { ScaleOrdinal } from "d3-scale";
import React, { FunctionComponent } from "react";
import { Event } from "../../../rest/dto/event.interfaces";

export enum SeriesType {
    LINE = "line",
    AREA_CLOSED = "areaclosed",
    BAR = "bar",
    CUSTOM = "custom",
}

export interface DataPoint<ExtraData = unknown> {
    x: number;
    y: number;
    extraData?: ExtraData;
    // This will override any color from the series for bar chart
    color?: string;
}

export interface ThresholdDataPoint<ExtraData = unknown>
    extends DataPoint<ExtraData> {
    y1: number; // y1 is used in Threshold shapes
}

export interface LineDataPoint<ExtraData = unknown>
    extends ThresholdDataPoint<ExtraData> {
    x1: number; // x1 is used in Line only shapes
}

export interface Series {
    name?: string;
    data: DataPoint[] | ThresholdDataPoint[] | LineDataPoint[];
    type?: SeriesType;
    color?: string;
    stroke?: string;
    enabled?: boolean;
    strokeWidth?: number;
    legendIndex?: number;
    legendIcon?: (svgBound: number, color: string) => React.ReactElement;
    xAccessor?: (d: DataPoint | ThresholdDataPoint) => Date;
    x1Accessor?: (d: LineDataPoint) => Date;
    yAccessor?: (d: DataPoint | ThresholdDataPoint) => number;
    tooltip?: {
        valueFormatter?: (value: number) => string;
        pointFormatter?: (
            d: DataPoint | ThresholdDataPoint | LineDataPoint,
            series: NormalizedSeries
        ) => React.ReactElement | string;
        tooltipFormatter?: (
            d: DataPoint | ThresholdDataPoint | LineDataPoint,
            series: NormalizedSeries
        ) => React.ReactElement | string;
    };
    strokeDasharray?: string;
    /** Fields specific to areaclosed */
    // See https://airbnb.io/visx/docs/gradient#LinearGradient
    gradient?: GradientConfiguration;
    y1Accessor?: (d: ThresholdDataPoint) => number;
    fillOpacity?: number;
    // Custom renderer for very specific case charting
    customRenderer?: (
        xScale: AxisScale<number>,
        yScale: AxisScale<number>
    ) => React.ReactElement[];
}

export interface NormalizedSeries {
    name: string;
    data: DataPoint[] | ThresholdDataPoint[] | LineDataPoint[];
    type: SeriesType;
    color?: string;
    stroke?: string;
    enabled: boolean;
    strokeWidth: number;
    legendIndex?: number;
    legendIcon?: (svgBound: number, color: string) => React.ReactElement;
    xAccessor: (d: DataPoint | ThresholdDataPoint) => Date;
    x1Accessor: (d: LineDataPoint) => Date;
    yAccessor: (d: DataPoint | ThresholdDataPoint) => number;
    tooltip: {
        valueFormatter: (value: number) => string;
        pointFormatter: (
            d: DataPoint | ThresholdDataPoint | LineDataPoint,
            series: NormalizedSeries
        ) => React.ReactElement | string;
        tooltipFormatter?: (
            d: DataPoint | ThresholdDataPoint | LineDataPoint,
            series: NormalizedSeries
        ) => React.ReactElement | string;
    };
    strokeDasharray?: string;
    /** Fields specific to areaclosed */
    // See https://airbnb.io/visx/docs/gradient#LinearGradient
    gradient?: GradientConfiguration;
    y1Accessor: (d: ThresholdDataPoint) => number;
    fillOpacity: number;
    // Custom renderer for very specific case charting
    customRenderer?: (
        xScale: AxisScale<number>,
        yScale: AxisScale<number>
    ) => React.ReactElement[];
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
    tickFormatter?: (date: string) => string;
}

export interface YAxisOptions {
    position?: Orientation;
    enabled?: boolean;
}

export interface TimeSeriesChartProps {
    series: Series[];
    tooltip?: boolean;
    xAxis?: XAxisOptions;
    yAxis?: YAxisOptions;
    legend?: boolean;
    LegendComponent?: FunctionComponent<LegendProps>;
    brush?: boolean;
    zoom?: boolean;
    height?: number;
    chartEvents?: {
        onZoomChange?: (domain: ZoomDomain | null) => void;
    };
    initialZoom?: ZoomDomain;
    events?: Event[];
    margins?: {
        left: number;
        right: number;
        top: number;
        bottom: number;
    };
}

export interface ZoomDomain {
    x0: number;
    x1: number;
}

export interface TimeSeriesChartInternalProps extends TimeSeriesChartProps {
    height: number;
    width: number;
}

export interface LegendProps {
    series: Series[];
    onSeriesClick?: (idx: number) => void;
    colorScale: ScaleOrdinal<string, string, never>;
    events: EventWithChartState[];
    onEventsStateChange: (events: EventWithChartState[]) => void;
}

export interface EventWithChartState extends Event {
    enabled: boolean;
}

export interface GradientConfiguration {
    from: string;
    to: string;
    fromOffset?: number;
    fromOpacity: number;
    toOffset: number;
    toOpacity: number;
}
