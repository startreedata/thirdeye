// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { render, screen } from "@testing-library/react";
import React from "react";
import { AnomalyBreakdownComparisonHeatmap } from "./anomaly-breakdown-comparison-heatmap.component";

jest.mock("../../../platform/components", () => ({
    ...(jest.requireActual("../../../platform/components") as Record<
        string,
        unknown
    >),
    useNotificationProviderV1: jest.fn().mockImplementation(() => ({
        notify: mockNotify,
    })),
}));

jest.mock("../../../rest/rca/rca.rest", () => ({
    ...(jest.requireActual("../../../rest/rca/rca.rest") as Record<
        string,
        unknown
    >),
    getAnomalyMetricBreakdown: jest
        .fn()
        .mockImplementation(() => mockedGetAnomalyMetricBreakdownResponse),
}));

jest.mock("../../no-data-indicator/no-data-indicator.component", () => ({
    NoDataIndicator: jest
        .fn()
        .mockImplementation(() => <span>MockedNoDataIndicator</span>),
}));

jest.mock("react-router-dom", () => ({
    useSearchParams: jest.fn().mockImplementation(() => [
        {
            get: mockSearchParamsGet,
            set: mockSearchParamsSet,
            delete: mockSearchParamsDelete,
        },
        jest.fn(),
    ]),
}));

jest.useFakeTimers();

describe("AnomalyBreakdownComparisonHeatmap", () => {
    beforeEach(() => {
        mockedGetAnomalyMetricBreakdownResponse = Promise.resolve(
            MOCK_HEATMAP_DATA_RESPONSE
        );
    });

    it("should show all the UI components for valid data responses", async () => {
        expect.assertions(7);

        render(
            <AnomalyBreakdownComparisonHeatmap
                anomalyId={451751}
                chartTimeSeriesFilterSet={[]}
                comparisonOffset={mockComparisonOffset}
                onAddFilterSetClick={() => null}
            />
        );

        // Wait for the filter controls to be rendered
        const filterDataControlsContainer = await screen.findByText(
            "Filter Data Controls"
        );

        expect(filterDataControlsContainer).toBeInTheDocument();

        // Ensure a treemap shows for each dimension in the data payload.
        // These are the labels for each treemap
        const containersForTreemaps = [
            "browser",
            "country",
            "device",
            "gender",
            "os",
            "version",
        ].map(async (dimensionColumn) => {
            const containerForTreemapForDimension = await screen.findByText(
                dimensionColumn
            );

            expect(containerForTreemapForDimension).toBeInTheDocument();
        });

        await Promise.all(containersForTreemaps);
    });

    it("should call notify indicating error if data requests errors", async () => {
        mockedGetAnomalyMetricBreakdownResponse = Promise.reject("Error");

        expect.assertions(1);

        render(
            <AnomalyBreakdownComparisonHeatmap
                anomalyId={451751}
                chartTimeSeriesFilterSet={[]}
                comparisonOffset={mockComparisonOffset}
                onAddFilterSetClick={() => null}
            />
        );
        jest.runAllTimers();

        await screen.findByText(/MockedNoDataIndicator/);

        expect(mockNotify).toHaveBeenCalledWith(
            "error",
            "message.error-while-fetching"
        );
    });
});

const MOCK_HEATMAP_DATA_RESPONSE = {
    metric: {
        name: "numCommits",
        dataset: {
            name: "pullRequestMergedEvents",
        },
    },
    baseline: {
        breakdown: {
            browser: {
                chrome: 360666.0,
                safari: 216624.0,
                firefox: 53886.0,
                ie: 21340.0,
                others: 249150.0,
            },
            country: { BR: 180232.0, IN: 315746.0, US: 360930.0, CA: 44758.0 },
            device: { tablet: 44630.0, desktop: 315594.0, phone: 541442.0 },
            gender: { F: 450833.0, M: 450833.0 },
            os: {
                Linux: 44834.0,
                OSX: 89986.0,
                Windows: 180334.0,
                iOS: 270674.0,
                Android: 315838.0,
            },
            version: { "0.1": 17514.0, "0.2": 71684.0, "0.3": 812468.0 },
        },
    },
    current: {
        breakdown: {
            browser: {
                chrome: 547246.0,
                safari: 218694.0,
                firefox: 54466.0,
                ie: 21606.0,
                others: 251496.0,
            },
            country: { BR: 218620.0, IN: 382886.0, US: 437640.0, CA: 54362.0 },
            device: { tablet: 54206.0, desktop: 382718.0, phone: 656584.0 },
            gender: { F: 546754.0, M: 546754.0 },
            os: {
                Linux: 54444.0,
                OSX: 109184.0,
                Windows: 218700.0,
                iOS: 328214.0,
                Android: 382966.0,
            },
            version: { "0.1": 21366.0, "0.2": 87034.0, "0.3": 985108.0 },
        },
    },
};

const mockNotify = jest.fn();
let mockedGetAnomalyMetricBreakdownResponse = Promise.resolve(
    MOCK_HEATMAP_DATA_RESPONSE
);

const mockSearchParamsGet = jest.fn();
const mockSearchParamsSet = jest.fn();
const mockSearchParamsDelete = jest.fn();
const mockComparisonOffset = "P1W";
