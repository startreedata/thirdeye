import { DataPoint, Series } from "./time-series-chart.interfaces";

export const LINE = "line";
export const BAR = "bar";
const DEFAULT_CHART_TYPE = LINE;

export const DEFAULT_PLOTBAND_COLOR = "rgba(23, 233, 217, .5)";

/**
 * Helper function to extract the values from the `data` array of objects
 */
export function getMinMax(
    series: Pick<Series, "data">[],
    extract = (d: DataPoint) => d.x
): [number, number] {
    const arrayOfArrayOfValues: number[][] = series.map((seriesOptions) => {
        return seriesOptions.data.map(extract);
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
export function normalizeSeries(series: Series[]): Series[] {
    return series.map((item, idx) => {
        return {
            ...item,
            name: item.name || `Series ${idx}`,
            enabled: item.enabled === undefined ? true : item.enabled,
            type: item.type === undefined ? DEFAULT_CHART_TYPE : item.type,
        };
    });
}

export const syncEnabledDisabled = (seriesData: Series): boolean => {
    return seriesData.enabled === undefined ? true : seriesData.enabled;
};
