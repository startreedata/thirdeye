/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { isEmpty, map } from "lodash";
import { AnomalyBreakdown } from "../../../rest/dto/rca.interfaces";
import { EMPTY_STRING_DISPLAY } from "../../../utils/anomalies/anomalies.util";
import { formatLargeNumberForVisualization } from "../../../utils/visualization/visualization.util";
import { TreemapData } from "../../visualizations/treemap/treemap.interfaces";
import {
    AnomalyBreakdownComparisonData,
    AnomalyBreakdownComparisonDataByDimensionColumn,
    AnomalyFilterOption,
    DimensionDisplayData,
    SummarizeDataFunctionParams,
    SummaryData,
} from "./anomaly-breakdown-comparison-heatmap.interfaces";

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
        { id: parentId, size: 0, parent: null, label: parentId },
        ...map(dimensionData.dimensionComparisonData, (comparisonData, k) => {
            const comparisonAndDisplayData = { ...comparisonData, columnName };
            let label = `${
                k || EMPTY_STRING_DISPLAY
            }: ${formatLargeNumberForVisualization(
                comparisonAndDisplayData.current
            )}`;

            if (comparisonAndDisplayData.metricValueDiffPercentage !== null) {
                const metricDiffPercentage = Number(
                    comparisonAndDisplayData.metricValueDiffPercentage
                ).toFixed(2);
                label += ` (${metricDiffPercentage.toLocaleString()}%)`;
            } else if (comparisonAndDisplayData.baseline === 0) {
                if (comparisonAndDisplayData.current > 0) {
                    label += ` (100.00%)`;
                }
            }

            return {
                id: k,
                // when current is 0, treemap won't render anything
                // fix: https://cortexdata.atlassian.net/browse/TE-453
                size: comparisonData.current || 1,
                parent: parentId,
                extraData: comparisonAndDisplayData,
                label,
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
