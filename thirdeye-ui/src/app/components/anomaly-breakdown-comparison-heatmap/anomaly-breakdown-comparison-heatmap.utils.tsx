import { isEmpty, map } from "lodash";
import { AnomalyBreakdownAPIOffsetValues } from "../../pages/anomalies-view-page/anomalies-view-page.interfaces";
import { WEEK_IN_MILLISECONDS } from "../../utils/time/time.util";
import { TreemapData } from "../visualizations/treemap/treemap.interfaces";
import {
    AnomalyBreakdownComparisonData,
    AnomalyBreakdownComparisonDataByDimensionColumn,
    DimensionDisplayData,
    SummarizeDataFunctionParams,
    SummaryData,
} from "./anomaly-breakdown-comparison-heatmap.interfaces";

export const OFFSET_TO_MILLISECONDS = {
    [AnomalyBreakdownAPIOffsetValues.CURRENT]: 0,
    [AnomalyBreakdownAPIOffsetValues.ONE_WEEK_AGO]: WEEK_IN_MILLISECONDS,
    [AnomalyBreakdownAPIOffsetValues.TWO_WEEKS_AGO]: 2 * WEEK_IN_MILLISECONDS,
    [AnomalyBreakdownAPIOffsetValues.THREE_WEEKS_AGO]: 3 * WEEK_IN_MILLISECONDS,
    [AnomalyBreakdownAPIOffsetValues.FOUR_WEEKS_AGO]: 4 * WEEK_IN_MILLISECONDS,
};
export const OFFSET_TO_HUMAN_READABLE = {
    [AnomalyBreakdownAPIOffsetValues.CURRENT]: "",
    [AnomalyBreakdownAPIOffsetValues.ONE_WEEK_AGO]: "One Week Ago",
    [AnomalyBreakdownAPIOffsetValues.TWO_WEEKS_AGO]: "Two Weeks Ago",
    [AnomalyBreakdownAPIOffsetValues.THREE_WEEKS_AGO]: "Three Weeks Ago",
    [AnomalyBreakdownAPIOffsetValues.FOUR_WEEKS_AGO]: "Four Weeks Ago",
};

export function summarizeDimensionValueData(
    dimensionValueData: SummarizeDataFunctionParams
): [number, SummaryData] {
    const summarized: SummaryData = {};
    if (isEmpty(dimensionValueData)) {
        return [0, summarized];
    }

    const totalCount = Object.keys(dimensionValueData).reduce(
        (total, dimensionValueKey) =>
            total + dimensionValueData[dimensionValueKey],
        0
    );

    Object.keys(dimensionValueData).forEach((dimension: string) => {
        summarized[dimension] = {
            count: dimensionValueData[dimension],
            percentage: dimensionValueData[dimension] / totalCount,
            totalCount,
        };
    });

    return [totalCount, summarized];
}

export function formatTreemapData(
    dimensionData: AnomalyBreakdownComparisonDataByDimensionColumn,
    columnName: string
): TreemapData<AnomalyBreakdownComparisonData & DimensionDisplayData>[] {
    const parentId = `${dimensionData.column}-parent`;

    return [
        { id: parentId, size: 0, parent: null },
        ...map(dimensionData.dimensionComparisonData, (comparisonData, k) => {
            const comparisonAndDisplayData = { ...comparisonData, columnName };

            return {
                id: k,
                // when current is 0, treemap won't render anything
                // fix: https://cortexdata.atlassian.net/browse/TE-453
                size: comparisonData.current || 1,
                parent: parentId,
                extraData: comparisonAndDisplayData,
            };
        }),
    ];
}
