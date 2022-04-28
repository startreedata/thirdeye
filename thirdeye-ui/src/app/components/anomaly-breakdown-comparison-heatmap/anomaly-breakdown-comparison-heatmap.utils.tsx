import { isEmpty, map } from "lodash";
import { AnomalyBreakdownAPIOffsetValues } from "../../pages/anomalies-view-page/anomalies-view-page.interfaces";
import { AnomalyBreakdown } from "../../rest/dto/rca.interfaces";
import { WEEK_IN_MILLISECONDS } from "../../utils/time/time.util";
import { TreemapData } from "../visualizations/treemap/treemap.interfaces";
import {
    AnomalyBreakdownComparisonData,
    AnomalyBreakdownComparisonDataByDimensionColumn,
    AnomalyFilterOption,
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

export function formatDimensionOptions(
    anomalyMetricBreakdown: AnomalyBreakdown
): AnomalyFilterOption[] {
    let options: AnomalyFilterOption[] = [];
    Object.keys(anomalyMetricBreakdown.current.breakdown).forEach(
        (dimensionColumnName) => {
            const optionsForColumn = Object.keys(
                anomalyMetricBreakdown.current.breakdown[dimensionColumnName]
            ).map((value) => ({
                key: dimensionColumnName,
                value,
            }));
            options = [...options, ...optionsForColumn];
        }
    );

    return options;
}

export function formatComparisonData(
    anomalyMetricBreakdown: AnomalyBreakdown
): AnomalyBreakdownComparisonDataByDimensionColumn[] {
    const breakdownComparisonDataByDimensionColumn: AnomalyBreakdownComparisonDataByDimensionColumn[] =
        [];

    Object.keys(anomalyMetricBreakdown.current.breakdown).forEach(
        (dimensionColumnName) => {
            const [currentTotal, currentDimensionValuesData] =
                summarizeDimensionValueData(
                    anomalyMetricBreakdown.current.breakdown[
                        dimensionColumnName
                    ]
                );
            const [baselineTotal, baselineDimensionValuesData] =
                summarizeDimensionValueData(
                    anomalyMetricBreakdown.baseline.breakdown[
                        dimensionColumnName
                    ]
                );
            const dimensionComparisonData: {
                [key: string]: AnomalyBreakdownComparisonData;
            } = {};

            Object.keys(currentDimensionValuesData).forEach(
                (dimension: string) => {
                    const currentDataForDimension =
                        currentDimensionValuesData[dimension];
                    const baselineDataForDimension =
                        baselineDimensionValuesData[dimension] || {};
                    const baselineMetricValue =
                        baselineDataForDimension.count || 0;

                    dimensionComparisonData[dimension] = {
                        current: currentDataForDimension.count,
                        baseline: baselineMetricValue,
                        metricValueDiff:
                            currentDataForDimension.count - baselineMetricValue,
                        metricValueDiffPercentage: null,
                        currentContributionPercentage:
                            currentDataForDimension.percentage || 0,
                        baselineContributionPercentage:
                            baselineDataForDimension.percentage || 0,
                        contributionDiff:
                            (currentDataForDimension.percentage || 0) -
                            (baselineDataForDimension.percentage || 0),
                        currentTotalCount: currentTotal,
                        baselineTotalCount: baselineTotal,
                    };

                    if (baselineMetricValue > 0) {
                        dimensionComparisonData[
                            dimension
                        ].metricValueDiffPercentage =
                            ((currentDataForDimension.count -
                                baselineMetricValue) /
                                baselineMetricValue) *
                            100;
                    }
                }
            );

            breakdownComparisonDataByDimensionColumn.push({
                column: dimensionColumnName,
                dimensionComparisonData,
            });
        }
    );

    return breakdownComparisonDataByDimensionColumn;
}
