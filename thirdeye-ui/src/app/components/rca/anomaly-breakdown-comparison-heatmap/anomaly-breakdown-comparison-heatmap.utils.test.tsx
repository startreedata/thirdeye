import {
    formatTreemapData,
    summarizeDimensionValueData,
} from "./anomaly-breakdown-comparison-heatmap.utils";

describe("Anomaly Breakdown Comparison Heatmap Utils", () => {
    describe("summarizeDimensionValueData", () => {
        it("total and summary is expected for non empty input", () => {
            const expectedTotal = 500;
            const [total, summarized] = summarizeDimensionValueData({
                apple: 200,
                coconut: 100,
                tangerine: 200,
            });

            expect(total).toEqual(expectedTotal);
            expect(summarized).toEqual({
                apple: {
                    count: 200,
                    percentage: 200 / expectedTotal,
                    totalCount: expectedTotal,
                },
                coconut: {
                    count: 100,
                    percentage: 100 / expectedTotal,
                    totalCount: expectedTotal,
                },
                tangerine: {
                    count: 200,
                    percentage: 200 / expectedTotal,
                    totalCount: expectedTotal,
                },
            });
        });

        it("zero total and empty summary is expected for empty input", () => {
            const [total, summarized] = summarizeDimensionValueData({});

            expect(total).toEqual(0);
            expect(summarized).toEqual({});
        });
    });

    describe("formatTreemapData", () => {
        const mockComparisonData = {
            contributionDiff: 0.123,
            baselineContributionPercentage: 0.456,
            baselineTotalCount: 250000,
            baseline: 200000,
            currentContributionPercentage: 0.789,
            currentTotalCount: 350000000,
            current: 300000000,
            metricValueDiff: 28000,
            metricValueDiffPercentage: 0.28,
            columnName: "column-name",
        };

        it("total and summary is expected for non empty input", () => {
            const result = formatTreemapData(
                {
                    column: "column name",
                    dimensionComparisonData: {
                        apple: { ...mockComparisonData },
                        coconut: { ...mockComparisonData },
                    },
                },
                "column-name"
            );

            expect(result).toEqual([
                {
                    id: "column name-parent",
                    label: "column name-parent",
                    parent: null,
                    size: 0,
                },
                {
                    extraData: {
                        ...mockComparisonData,
                    },
                    label: "apple: 300m (0.28%)",
                    id: "apple",
                    parent: "column name-parent",
                    size: 300000000,
                },
                {
                    extraData: {
                        ...mockComparisonData,
                    },
                    label: "coconut: 300m (0.28%)",
                    id: "coconut",
                    parent: "column name-parent",
                    size: 300000000,
                },
            ]);
        });
    });
});
