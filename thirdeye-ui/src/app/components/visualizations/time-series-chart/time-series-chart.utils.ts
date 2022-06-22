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
import {
    DataPoint,
    LineDataPoint,
    NormalizedSeries,
    Series,
    SeriesType,
    ThresholdDataPoint,
    ZoomDomain,
} from "./time-series-chart.interfaces";

const DEFAULT_CHART_TYPE = SeriesType.LINE;

export const DEFAULT_PLOTBAND_COLOR = "rgba(23, 233, 217, .5)";
export const COLOR_PALETTE = [
    "#fd7f6f",
    "#7eb0d5",
    "#b2e061",
    "#bd7ebe",
    "#ffb55a",
    "#ffee65",
    "#beb9db",
    "#fdcce5",
    "#8bd3c7",
];

/**
 * Helper function to extract the values from the `data` array of objects
 */
export function getMinMax(
    series: Pick<Series, "data">[],
    extract = (d: DataPoint | ThresholdDataPoint) => d.x
): [number, number] {
    const arrayOfArrayOfValues: number[][] = [];

    series.forEach((seriesOptions) => {
        const values: number[] = [];
        seriesOptions.data.forEach((item) => values.push(extract(item)));
        arrayOfArrayOfValues.push(values);
    });

    const flattened: number[] = arrayOfArrayOfValues.reduce(
        (soFar: number[], values: number[]) => {
            return soFar.concat(values);
        },
        []
    );

    return [Math.min(...flattened), Math.max(...flattened)];
}

/**
 * Normalize series data to always have `name` and `enabled` flag and `type`
 */
export function normalizeSeries(
    series: Series[],
    filterByDomain?: ZoomDomain
): NormalizedSeries[] {
    return series.map((item, idx) => {
        const hasY1 = item.type === SeriesType.AREA_CLOSED;

        return {
            ...item,
            data: item.data.filter(
                (dataPoint: DataPoint | ThresholdDataPoint) => {
                    let shouldKeep = dataPoint.y !== null;

                    if (hasY1) {
                        shouldKeep =
                            shouldKeep &&
                            !!(dataPoint as ThresholdDataPoint).y1;
                    }

                    if (filterByDomain) {
                        shouldKeep =
                            shouldKeep &&
                            dataPoint.x >= filterByDomain.x0 &&
                            dataPoint.x <= filterByDomain.x1;
                    }

                    return shouldKeep;
                }
            ),
            name: item.name || `Series ${idx}`,
            enabled: item.enabled === undefined ? true : item.enabled,
            type: item.type === undefined ? DEFAULT_CHART_TYPE : item.type,
            strokeWidth: item.strokeWidth === undefined ? 1 : item.strokeWidth,
            xAccessor: item.xAccessor ?? defaultXAccessor,
            x1Accessor: item.x1Accessor ?? defaultX1Accessor,
            yAccessor: item.yAccessor ?? defaultYAccessor,
            y1Accessor: item.y1Accessor ?? defaultY1Accessor,
            tooltipValueFormatter:
                item.tooltipValueFormatter ?? defaultTooltipValueFormatter,
        };
    });
}

export const syncEnabledDisabled = (seriesData: Series): boolean => {
    return seriesData.enabled === undefined ? true : seriesData.enabled;
};

export const defaultXAccessor = (d: DataPoint | ThresholdDataPoint): Date => {
    return new Date(d.x);
};

export const defaultX1Accessor = (d: LineDataPoint): Date => {
    return new Date(d.x1);
};

export const defaultYAccessor = (d: DataPoint | ThresholdDataPoint): number => {
    return d.y;
};

export const defaultY1Accessor = (d: ThresholdDataPoint): number => {
    return d.y1;
};

export const defaultTooltipValueFormatter = (value: number): string => {
    return value.toString();
};
