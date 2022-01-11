import { render, screen } from "@testing-library/react";
import React from "react";
import { DimensionHeatmapTooltip } from "./dimension-heatmap-tooltip.component";

it("DimensionHeatmapTooltip renders a span if `extraData` is missing", () => {
    const tesProps = {
        id: "label for data",
        parent: null,
        size: 200,
    };
    render(<DimensionHeatmapTooltip {...tesProps} />);

    const emptySpan = screen.getByTestId("empty-dimension-tooltip");

    expect(emptySpan).toBeInTheDocument();
});

it("DimensionHeatmapTooltip renders expected fields for valid `extraData` property", () => {
    const tesProps = {
        id: "label for data",
        parent: null,
        size: 200,
        extraData: {
            percentageDiff: 0.123,
            comparisonPercentage: 0.456,
            comparisonTotalCount: 250000,
            comparison: 200000,
            currentPercentage: 0.789,
            currentTotalCount: 350000000,
            current: 300000000,
        },
    };
    render(<DimensionHeatmapTooltip {...tesProps} />);

    ["12.30", "45.60", "78.90"].forEach((expectedPercentage) => {
        const elementContainingText = screen.getByText(
            `${expectedPercentage}%`
        );

        expect(elementContainingText).toBeInTheDocument();
    });

    [/\(of 250k\)/, /\(of 350m\)/].forEach((expectedValue) => {
        const elementContainingText = screen.getByText(expectedValue);

        expect(elementContainingText).toBeInTheDocument();
    });
});
