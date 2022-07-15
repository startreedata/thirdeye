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
import { Metric, MetricAggFunction } from "../../rest/dto/metric.interfaces";
import { UiMetric } from "../../rest/dto/ui-metric.interfaces";
import {
    createEmptyMetric,
    createEmptyMetricLogicalView,
    createEmptyUiMetric,
    filterMetrics,
    getUiMetric,
    getUiMetrics,
} from "./metrics.util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

jest.mock("@startree-ui/platform-ui", () => ({
    formatNumberV1: jest.fn().mockImplementation((num) => num.toString()),
}));

describe("Metrics Util", () => {
    it("createEmptyUiMetric should create appropriate UI metric", () => {
        expect(createEmptyUiMetric()).toEqual(mockEmptyUiMetric);
    });

    it("createEmptyMetric should create appropriate metric", () => {
        expect(createEmptyMetric()).toEqual(mockEmptyMetric);
    });

    it("createEmptyMetricLogicalView should create appropriate metric logical view", () => {
        expect(createEmptyMetricLogicalView()).toEqual(
            mockEmptyMetricLogicalView
        );
    });

    it("getUiMetric should return empty UI metric for invalid metric", () => {
        expect(getUiMetric(null as unknown as Metric)).toEqual(
            mockEmptyUiMetric
        );
    });

    it("getUiMetric should return appropriate UI metric for metric", () => {
        expect(getUiMetric(mockMetric1)).toEqual(mockUiMetric1);
    });

    it("getUiMetrics should return empty array for invalid metric", () => {
        expect(getUiMetrics(null as unknown as Metric[])).toEqual([]);
    });

    it("getUiMetrics should return empty array for empty metrics", () => {
        expect(getUiMetrics([])).toEqual([]);
    });

    it("getUiMetrics should return appropriate UI metrics for metrics", () => {
        expect(getUiMetrics(mockMetrics)).toEqual(mockUiMetrics);
    });

    it("filterMetrics should return empty array for invalid UI metrics", () => {
        expect(
            filterMetrics(null as unknown as UiMetric[], mockSearchWords)
        ).toEqual([]);
    });

    it("filterMetrics should return empty array for empty UI metrics", () => {
        expect(filterMetrics([], mockSearchWords)).toEqual([]);
    });

    it("filterMetrics should return appropriate UI metrics for UI metrics and invalid search words", () => {
        expect(
            filterMetrics(mockUiMetrics, null as unknown as string[])
        ).toEqual(mockUiMetrics);
    });

    it("filterMetrics should return appropriate UI metrics for UI metrics and empty search words", () => {
        expect(filterMetrics(mockUiMetrics, [])).toEqual(mockUiMetrics);
    });

    it("filterMetrics should return appropriate UI metrics for UI metrics and search words", () => {
        expect(filterMetrics(mockUiMetrics, mockSearchWords)).toEqual([
            mockUiMetric1,
        ]);
    });
});

const mockEmptyMetric = {
    name: "",
    active: true,
    aggregationFunction: MetricAggFunction.SUM,
    dataset: {
        name: "",
    },
    rollupThreshold: 0,
};

const mockEmptyUiMetric = {
    id: -1,
    name: "label.no-data-marker",
    datasetId: -1,
    datasetName: "label.no-data-marker",
    active: false,
    activeText: "label.no-data-marker",
    aggregationColumn: "label.no-data-marker",
    aggregationFunction: "label.no-data-marker",
    views: [],
    viewCount: "0",
};

const mockEmptyMetricLogicalView = {
    name: "label.no-data-marker",
    query: "label.no-data-marker",
};

const mockMetric1 = {
    id: 1,
    name: "testNameMetric1",
    dataset: {
        id: 2,
        name: "testNameDataset2",
    },
    active: true,
    aggregationColumn: "testAggregationColumnMetric1",
    aggregationFunction: MetricAggFunction.AVG,
    views: [
        {
            name: "testNameViews1Metric1",
            query: "testQueryViews1Metric1",
        },
        {
            name: "testNameViews2Metric1",
            query: "testQueryViews2Metric1",
        },
    ],
} as Metric;

const mockMetric2 = {
    id: 3,
    dataset: {
        id: 4,
    },
    active: false,
    views: [{}],
} as Metric;

const mockMetric3 = {
    id: 5,
} as Metric;

const mockMetrics = [mockMetric1, mockMetric2, mockMetric3];

const mockUiMetric1 = {
    id: 1,
    name: "testNameMetric1",
    datasetId: 2,
    datasetName: "testNameDataset2",
    active: true,
    activeText: "label.active",
    aggregationColumn: "testAggregationColumnMetric1",
    aggregationFunction: MetricAggFunction.AVG,
    views: [
        {
            name: "testNameViews1Metric1",
            query: "testQueryViews1Metric1",
        },
        {
            name: "testNameViews2Metric1",
            query: "testQueryViews2Metric1",
        },
    ],
    viewCount: "2",
};

const mockUiMetric2 = {
    id: 3,
    name: "label.no-data-marker",
    datasetId: 4,
    datasetName: "label.no-data-marker",
    active: false,
    activeText: "label.inactive",
    aggregationColumn: "label.no-data-marker",
    aggregationFunction: "label.no-data-marker" as MetricAggFunction,
    views: [
        {
            name: "label.no-data-marker",
            query: "label.no-data-marker",
        },
    ],
    viewCount: "1",
};

const mockUiMetric3 = {
    id: 5,
    name: "label.no-data-marker",
    datasetId: -1,
    datasetName: "label.no-data-marker",
    active: false,
    activeText: "label.inactive",
    aggregationColumn: "label.no-data-marker",
    aggregationFunction: "label.no-data-marker" as MetricAggFunction,
    views: [],
    viewCount: "0",
};

const mockUiMetrics = [mockUiMetric1, mockUiMetric2, mockUiMetric3];

const mockSearchWords = [MetricAggFunction.AVG];
