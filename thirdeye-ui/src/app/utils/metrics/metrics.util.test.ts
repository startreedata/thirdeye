import { Metric, MetricAggFunction } from "../../rest/dto/metric.interfaces";
import { UiMetric } from "../../rest/dto/ui-metric.interfaces";
import {
    createEmptyMetricLogicalView,
    createEmptyUiMetric,
    filterMetrics,
    getUiMetric,
    getUiMetrics,
} from "./metrics.util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

describe("Metrics Util", () => {
    test("createEmptyUiMetric should create appropriate UI metric", () => {
        expect(createEmptyUiMetric()).toEqual(mockEmptyUiMetric);
    });

    test("createEmptyMetricLogicalView should create appropriate metric logical view", () => {
        expect(createEmptyMetricLogicalView()).toEqual(
            mockEmptyMetricLogicalView
        );
    });

    test("getUiMetric should return empty UI metric for invalid metric", () => {
        expect(getUiMetric((null as unknown) as Metric)).toEqual(
            mockEmptyUiMetric
        );
    });

    test("getUiMetric should return appropriate UI metric for metric", () => {
        expect(getUiMetric(mockMetric1)).toEqual(mockUiMetric1);
    });

    test("getUiMetrics should return empty array for invalid metric", () => {
        expect(getUiMetrics((null as unknown) as Metric[])).toEqual([]);
    });

    test("getUiMetrics should return empty array for empty metrics", () => {
        expect(getUiMetrics([])).toEqual([]);
    });

    test("getUiMetrics should return appropriate UI metrics for metrics", () => {
        expect(getUiMetrics(mockMetrics)).toEqual(mockUiMetrics);
    });

    test("filterMetrics should return empty array for invalid UI metrics", () => {
        expect(
            filterMetrics((null as unknown) as UiMetric[], mockSearchWords)
        ).toEqual([]);
    });

    test("filterMetrics should return empty array for empty UI metrics", () => {
        expect(filterMetrics([], mockSearchWords)).toEqual([]);
    });

    test("filterMetrics should return appropriate UI metrics for UI metrics and invalid search words", () => {
        expect(
            filterMetrics(mockUiMetrics, (null as unknown) as string[])
        ).toEqual(mockUiMetrics);
    });

    test("filterMetrics should return appropriate UI metrics for UI metrics and empty search words", () => {
        expect(filterMetrics(mockUiMetrics, [])).toEqual(mockUiMetrics);
    });

    test("filterMetrics should return appropriate UI metrics for UI metrics and search words", () => {
        expect(filterMetrics(mockUiMetrics, mockSearchWords)).toEqual([
            mockUiMetric1,
        ]);
    });
});

const mockEmptyUiMetric = {
    id: -1,
    name: "label.no-data-marker",
    datasetId: -1,
    datasetName: "label.no-data-marker",
    active: false,
    activeText: "label.no-data-marker",
    aggregationColumn: "label.no-data-marker",
    aggregationFunction: "label.no-data-marker",
    viewCount: "0",
    views: [],
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
    viewCount: "2",
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
    viewCount: "1",
    views: [
        {
            name: "label.no-data-marker",
            query: "label.no-data-marker",
        },
    ],
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
    viewCount: "0",
    views: [],
};

const mockUiMetrics = [mockUiMetric1, mockUiMetric2, mockUiMetric3];

const mockSearchWords = [MetricAggFunction.AVG];
