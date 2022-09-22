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
import { render, screen } from "@testing-library/react";
import React from "react";
import { DimensionHeatmapTooltip } from "./dimension-heatmap-tooltip.component";

describe("Dimension Heatmap Tooltip", () => {
    it("DimensionHeatmapTooltip renders a span if `extraData` is missing", () => {
        const testProps = {
            id: "id for data",
            label: "label for data",
            parent: null,
            size: 200,
        };
        render(<DimensionHeatmapTooltip {...testProps} />);

        const emptySpan = screen.getByTestId("empty-dimension-tooltip");

        expect(emptySpan).toBeInTheDocument();
    });

    it("DimensionHeatmapTooltip renders expected fields for valid `extraData` property", () => {
        const testProps = {
            id: "id for data",
            label: "label for data",
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
