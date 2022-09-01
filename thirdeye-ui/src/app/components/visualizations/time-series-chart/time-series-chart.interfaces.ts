/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { AxisScale, Orientation } from "@visx/axis";
import { ScaleOrdinal } from "d3-scale";
import React, { FunctionComponent } from "react";
import { Event } from "../../../rest/dto/event.interfaces";

export enum SeriesType {
    LINE = "line",
    AREA_CLOSED = "areaclosed",
    CUSTOM = "custom",
}
export interface DataPoint<ExtraData = unknown> {
    x: number;
    y: number;
    extraData?: ExtraData;
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
