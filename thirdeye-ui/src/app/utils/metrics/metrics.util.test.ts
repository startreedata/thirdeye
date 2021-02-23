import { MetricCardData } from "../../components/entity-cards/metric-card/metric-card.interfaces";
import { Metric, MetricAggFunction } from "../../rest/dto/metric.interfaces";
import {
    createEmptyMetricCardData,
    createEmptyMetricLogicalView,
    filterMetrics,
    getMetricCardData,
    getMetricCardDatas,
} from "./metrics.util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

describe("Metrics Util", () => {
    test("createEmptyMetricCardData should create appropriate metric card data", () => {
        expect(createEmptyMetricCardData()).toEqual(mockEmptyMetricCardData);
    });

    test("createEmptyMetricLogicalView should create appropriate metric logical view", () => {
        expect(createEmptyMetricLogicalView()).toEqual(
            mockEmptyMetricLogicalView
        );
    });

    test("getMetricCardData should return empty metric card data for invalid metric", () => {
        expect(getMetricCardData((null as unknown) as Metric)).toEqual(
            mockEmptyMetricCardData
        );
    });

    test("getMetricCardData should return appropriate metric card data for metric", () => {
        expect(getMetricCardData(mockMetric1)).toEqual(mockMetricCardData1);
    });

    test("getMetricCardDatas should return empty array for invalid metric", () => {
        expect(getMetricCardDatas((null as unknown) as Metric[])).toEqual([]);
    });

    test("getMetricCardDatas should return empty array for empty metrics", () => {
        expect(getMetricCardDatas([])).toEqual([]);
    });

    test("getMetricCardDatas should return appropriate metric card data array for metrics", () => {
        expect(getMetricCardDatas(mockMetrics)).toEqual(mockMetricCardDatas);
    });

    test("filterMetrics should return empty array for invalid metric card data array", () => {
        expect(
            filterMetrics(
                (null as unknown) as MetricCardData[],
                mockSearchWords
            )
        ).toEqual([]);
    });

    test("filterMetrics should return empty array for empty metric card data array", () => {
        expect(filterMetrics([], mockSearchWords)).toEqual([]);
    });

    test("filterMetrics should return appropriate metric card data array for metric card data array and invalid search words", () => {
        expect(
            filterMetrics(mockMetricCardDatas, (null as unknown) as string[])
        ).toEqual(mockMetricCardDatas);
    });

    test("filterMetrics should return appropriate metric card data array for metric card data array and empty search words", () => {
        expect(filterMetrics(mockMetricCardDatas, [])).toEqual(
            mockMetricCardDatas
        );
    });

    test("filterMetrics should return appropriate metric card data array for metric card data array and search words", () => {
        expect(filterMetrics(mockMetricCardDatas, mockSearchWords)).toEqual([
            mockMetricCardData1,
        ]);
    });
});

const mockEmptyMetricCardData = {
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

const mockMetricCardData1 = {
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

const mockMetricCardData2 = {
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

const mockMetricCardData3 = {
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

const mockMetricCardDatas = [
    mockMetricCardData1,
    mockMetricCardData2,
    mockMetricCardData3,
];

const mockSearchWords = [MetricAggFunction.AVG];
