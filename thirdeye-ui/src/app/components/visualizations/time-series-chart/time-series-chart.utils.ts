import {
    DataPoint,
    NormalizedSeries,
    Series,
    SeriesType,
    ThresholdDataPoint,
} from "./time-series-chart.interfaces";

const DEFAULT_CHART_TYPE = SeriesType.LINE;

export const DEFAULT_PLOTBAND_COLOR = "rgba(23, 233, 217, .5)";

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
export function normalizeSeries(series: Series[]): NormalizedSeries[] {
    return series.map((item, idx) => {
        return {
            ...item,
            name: item.name || `Series ${idx}`,
            enabled: item.enabled === undefined ? true : item.enabled,
            type: item.type === undefined ? DEFAULT_CHART_TYPE : item.type,
            xAccessor:
                item.xAccessor === undefined
                    ? defaultXAccessor
                    : item.xAccessor,
            yAccessor:
                item.yAccessor === undefined
                    ? defaultYAccessor
                    : item.yAccessor,
            y1Accessor:
                item.y1Accessor === undefined
                    ? defaultY1Accessor
                    : item.y1Accessor,
        };
    });
}

export const syncEnabledDisabled = (seriesData: Series): boolean => {
    return seriesData.enabled === undefined ? true : seriesData.enabled;
};

export const defaultXAccessor = (d: DataPoint | ThresholdDataPoint): Date => {
    return new Date(d.x);
};

export const defaultYAccessor = (d: DataPoint | ThresholdDataPoint): number => {
    return d.y;
};

export const defaultY1Accessor = (d: ThresholdDataPoint): number => {
    return d.y1;
};
