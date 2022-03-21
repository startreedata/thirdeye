import { render, screen } from "@testing-library/react";
import React from "react";
import { DimensionHeatmapTooltip } from "./dimension-heatmap-tooltip.component";

describe("Dimension Heatmap Tooltip", () => {
    it("DimensionHeatmapTooltip renders a span if `extraData` is missing", () => {
        const testProps = {
            id: "label for data",
            parent: null,
            size: 200,
        };
        render(<DimensionHeatmapTooltip {...testProps} />);

        const emptySpan = screen.getByTestId("empty-dimension-tooltip");

        expect(emptySpan).toBeInTheDocument();
    });

    it("DimensionHeatmapTooltip renders expected fields for valid `extraData` property", () => {
        const testProps = {
            id: "label for data",
            parent: null,
            size: 200,
            extraData: {
                contributionDiff: 0.123,
                baselineContributionPercentage: 0.456,
                baselineTotalCount: 250000,
                baseline: 200000,
                currentContributionPercentage: 0.789,
                currentTotalCount: 350000000,
                current: 300000000,
                metricValueDiff: 21000,
                metricValueDiffPercentage: 200,
                columnName: "column name",
            },
        };
        render(<DimensionHeatmapTooltip {...testProps} />);

        ["12.30", "45.60", "78.90"].forEach((expectedPercentage) => {
            const elementContainingText = screen.getByText(
                `${expectedPercentage}%`
            );

            expect(elementContainingText).toBeInTheDocument();
        });

        [/200k/, /300m/].forEach((expectedValue) => {
            const elementContainingText = screen.getByText(expectedValue);

            expect(elementContainingText).toBeInTheDocument();
        });
    });
});
