import {
    formatTreemapData,
    summarizeDimensionValueData,
} from "./anomaly-breakdown-comparison-heatmap.utils";

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
        percentageDiff: 0.123,
        comparisonPercentage: 0.456,
        comparisonTotalCount: 250000,
        comparison: 200000,
        currentPercentage: 0.789,
        currentTotalCount: 350000000,
        current: 300000000,
    };

    it("total and summary is expected for non empty input", () => {
        const result = formatTreemapData({
            column: "column name",
            dimensionComparisonData: {
                apple: { ...mockComparisonData },
                coconut: { ...mockComparisonData },
            },
        });

        expect(result).toEqual([
            {
                id: "column name",
                parent: null,
                size: 0,
            },
            {
                extraData: {
                    ...mockComparisonData,
                },
                id: "apple",
                parent: "column name",
                size: 300000000,
            },
            {
                extraData: {
                    ...mockComparisonData,
                },
                id: "coconut",
                parent: "column name",
                size: 300000000,
            },
        ]);
    });
});
