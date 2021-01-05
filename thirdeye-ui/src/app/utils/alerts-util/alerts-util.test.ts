import { cloneDeep } from "lodash";
import {
    AlertCardData,
    AlertDatasetAndMetric,
    AlertSubscriptionGroup,
} from "../../components/entity-card/alert-card/alert-card.interfaces";
import {
    Alert,
    AlertNode,
    AlertNodeType,
} from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    createAlertEvaluation,
    createEmptyAlertCardData,
    createEmptyAlertDatasetAndMetric,
    createEmptyAlertSubscriptionGroup,
    filterAlerts,
    getAlertCardData,
    getAlertCardDatas,
} from "./alerts-util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key: string): string => {
        return key;
    }),
}));

describe("Alerts Util", () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
    });

    test("createEmptyAlertCardData should create appropriate alert card data", () => {
        expect(createEmptyAlertCardData()).toEqual(mockEmptyAlertCardData);
    });

    test("createEmptyAlertDatasetAndMetric should create appropriate alert dataset and metric", () => {
        expect(createEmptyAlertDatasetAndMetric()).toEqual(
            mockEmptyAlertDatasetAndMetric
        );
    });

    test("createEmptyAlertSubscriptionGroup should create appropriate alert subscription group", () => {
        expect(createEmptyAlertSubscriptionGroup()).toEqual(
            mockEmptyAlertSubscriptionGroup
        );
    });

    test("createAlertEvaluation should create appropriate alert evaluation", () => {
        expect(
            createAlertEvaluation(
                {
                    id: 1,
                } as Alert,
                2,
                3
            )
        ).toEqual({
            alert: {
                id: 1,
            },
            start: 2,
            end: 3,
        });
    });

    test("getAlertCardData should return empty alert card data for invalid alert", () => {
        expect(
            getAlertCardData((null as unknown) as Alert, mockSubscriptionGroups)
        ).toEqual(mockEmptyAlertCardData);
    });

    test("getAlertCardData should return appropriate alert card data for alert and invalid subscription groups", () => {
        const expectedAlertCardData = cloneDeep(mockAlertCardData1);
        expectedAlertCardData.subscriptionGroups = [];

        expect(
            getAlertCardData(
                mockAlert1,
                (null as unknown) as SubscriptionGroup[]
            )
        ).toEqual(expectedAlertCardData);
    });

    test("getAlertCardData should return appropriate alert card data for alert and empty subscription groups", () => {
        const expectedAlertCardData = cloneDeep(mockAlertCardData1);
        expectedAlertCardData.subscriptionGroups = [];

        expect(getAlertCardData(mockAlert1, [])).toEqual(expectedAlertCardData);
    });

    test("getAlertCardData should return appropriate alert card data for alert and subscription groups", () => {
        expect(getAlertCardData(mockAlert1, mockSubscriptionGroups)).toEqual(
            mockAlertCardData1
        );
    });

    test("getAlertCardDatas should return empty array for invalid alerts", () => {
        expect(
            getAlertCardDatas(
                (null as unknown) as Alert[],
                mockSubscriptionGroups
            )
        ).toEqual([]);
    });

    test("getAlertCardDatas should return empty array for empty alerts", () => {
        expect(getAlertCardDatas([], mockSubscriptionGroups)).toEqual([]);
    });

    test("getAlertCardDatas should return appropriate alert card data array for alerts and invalid subscription groups", () => {
        const expectedAlertCardData1 = cloneDeep(mockAlertCardData1);
        expectedAlertCardData1.subscriptionGroups = [];
        const expectedAlertCardData2 = cloneDeep(mockAlertCardData2);
        expectedAlertCardData2.subscriptionGroups = [];

        expect(
            getAlertCardDatas(
                mockAlerts,
                (null as unknown) as SubscriptionGroup[]
            )
        ).toEqual([expectedAlertCardData1, expectedAlertCardData2]);
    });

    test("getAlertCardDatas should return appropriate alert card data array for alerts and empty subscription groups", () => {
        const expectedAlertCardData1 = cloneDeep(mockAlertCardData1);
        expectedAlertCardData1.subscriptionGroups = [];
        const expectedAlertCardData2 = cloneDeep(mockAlertCardData2);
        expectedAlertCardData2.subscriptionGroups = [];

        expect(getAlertCardDatas(mockAlerts, [])).toEqual([
            expectedAlertCardData1,
            expectedAlertCardData2,
        ]);
    });

    test("getAlertCardDatas should return appropriate alert card data array for alerts and subscription groups", () => {
        expect(getAlertCardDatas(mockAlerts, mockSubscriptionGroups)).toEqual(
            mockAlertCardDatas
        );
    });

    test("filterAlerts should return empty array for invalid alert card data array", () => {
        expect(
            filterAlerts((null as unknown) as AlertCardData[], mockSearchWords)
        ).toEqual([]);
    });

    test("filterAlerts should return empty array for empty alert card data array", () => {
        expect(filterAlerts([], mockSearchWords)).toEqual([]);
    });

    test("filterAlerts should return appropriate alert card data array for alert card data array and invalid search words", () => {
        expect(
            filterAlerts(mockAlertCardDatas, (null as unknown) as string[])
        ).toEqual(mockAlertCardDatas);
    });

    test("filterAlerts should return appropriate alert card data array for alert card data array and empty search words", () => {
        expect(filterAlerts(mockAlertCardDatas, [])).toEqual(
            mockAlertCardDatas
        );
    });

    test("filterAlerts should return appropriate alert card data array for alert card data array and search words", () => {
        expect(filterAlerts(mockAlertCardDatas, mockSearchWords)).toEqual([
            mockAlertCardData1,
        ]);
    });
});

const mockEmptyAlertCardData: AlertCardData = {
    id: -1,
    name: "label.no-data-available-marker",
    active: false,
    activeText: "label.no-data-available-marker",
    userId: -1,
    createdBy: "label.no-data-available-marker",
    detectionTypes: [],
    filteredBy: [],
    datasetAndMetrics: [],
    subscriptionGroups: [],
    alert: null,
};

const mockEmptyAlertDatasetAndMetric: AlertDatasetAndMetric = {
    datasetId: -1,
    datasetName: "label.no-data-available-marker",
    metricId: -1,
    metricName: "label.no-data-available-marker",
};

const mockEmptyAlertSubscriptionGroup: AlertSubscriptionGroup = {
    id: -1,
    name: "label.no-data-available-marker",
};

const mockAlert1: Alert = {
    id: 1,
    name: "testAlertName1",
    active: true,
    owner: {
        id: 2,
        principal: "testOwnerName2",
    },
    nodes: {
        alertNode1: {
            type: AlertNodeType.DETECTION,
            subType: "testSubType1",
            metric: {
                id: 3,
                name: "testMetricName3",
                dataset: {
                    id: 4,
                    name: "testDatasetName4",
                },
            },
        } as AlertNode,
        alertNode2: {
            type: AlertNodeType.FILTER,
            subType: "testSubType2",
            metric: {
                id: 5,
                name: "testMetricName5",
            },
        } as AlertNode,
        alertNode3: {
            type: "testType3" as AlertNodeType,
            subType: "testSubType3",
        } as AlertNode,
    } as { [index: string]: AlertNode },
} as Alert;

const mockAlert2: Alert = {
    id: 6,
    name: "testAlertName6",
    active: false,
} as Alert;

const mockAlerts = [mockAlert1, mockAlert2];

const mockSubscriptionGroup1: SubscriptionGroup = {
    id: 7,
    name: "testSubscriptionGroupName7",
    alerts: [
        {
            id: 1,
        },
        {
            id: 6,
        },
    ],
} as SubscriptionGroup;

const mockSubscriptionGroup2: SubscriptionGroup = {
    id: 8,
    name: "testSubscriptionGroupName8",
    alerts: [
        {
            id: 1,
        },
    ],
} as SubscriptionGroup;

const mockSubscriptionGroup3: SubscriptionGroup = {
    id: 9,
    name: "testSubscriptionGroupName9",
} as SubscriptionGroup;

const mockSubscriptionGroups = [
    mockSubscriptionGroup1,
    mockSubscriptionGroup2,
    mockSubscriptionGroup3,
];

const mockAlertCardData1: AlertCardData = {
    id: 1,
    name: "testAlertName1",
    active: true,
    activeText: "label.active",
    userId: 2,
    createdBy: "testOwnerName2",
    detectionTypes: ["testSubType1"],
    filteredBy: ["testSubType2"],
    datasetAndMetrics: [
        {
            datasetId: 4,
            datasetName: "testDatasetName4",
            metricId: 3,
            metricName: "testMetricName3",
        },
        {
            datasetId: -1,
            datasetName: "label.no-data-available-marker",
            metricId: 5,
            metricName: "testMetricName5",
        },
    ],
    subscriptionGroups: [
        {
            id: 7,
            name: "testSubscriptionGroupName7",
        },
        {
            id: 8,
            name: "testSubscriptionGroupName8",
        },
    ],
    alert: mockAlert1,
};

const mockAlertCardData2: AlertCardData = {
    id: 6,
    name: "testAlertName6",
    active: false,
    activeText: "label.inactive",
    userId: -1,
    createdBy: "label.no-data-available-marker",
    detectionTypes: [],
    filteredBy: [],
    datasetAndMetrics: [],
    subscriptionGroups: [
        {
            id: 7,
            name: "testSubscriptionGroupName7",
        },
    ],
    alert: mockAlert2,
};

const mockAlertCardDatas = [mockAlertCardData1, mockAlertCardData2];

const mockSearchWords = ["name1", "name9"];
