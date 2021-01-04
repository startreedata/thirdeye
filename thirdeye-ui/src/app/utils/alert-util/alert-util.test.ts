import i18n from "i18next";
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
} from "./alert-util";

jest.mock("i18next");

describe("Alert Util", () => {
    beforeAll(() => {
        i18n.t = jest.fn().mockImplementation((key: string): string => {
            return key;
        });
    });

    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
    });

    test("createEmptyAlertCardData shall create appropriate alert card data", () => {
        const alertCardData = createEmptyAlertCardData();

        expect(alertCardData).toEqual(mockEmptyAlertCardData);
    });

    test("createEmptyAlertDatasetAndMetric shall create appropriate alert dataset and metric", () => {
        const alertDatasetAndMetric = createEmptyAlertDatasetAndMetric();

        expect(alertDatasetAndMetric).toEqual(mockEmptyAlertDatasetAndMetric);
    });

    test("createEmptyAlertSubscriptionGroup shall create appropriate alert subscription group", () => {
        const alertSubscriptionGroup = createEmptyAlertSubscriptionGroup();

        expect(alertSubscriptionGroup).toEqual(mockEmptyAlertSubscriptionGroup);
    });

    test("createAlertEvaluation shall create appropriate alert evaluation", () => {
        const alertEvaluation = createAlertEvaluation(
            {
                id: 1,
            } as Alert,
            2,
            3
        );

        expect(alertEvaluation.alert).toBeDefined();
        expect(alertEvaluation.alert.id).toEqual(1);
        expect(alertEvaluation.start).toEqual(2);
        expect(alertEvaluation.end).toEqual(3);
    });

    test("getAlertCardData shall return empty alert card data for invalid alert", () => {
        const alertCardData = getAlertCardData(
            (null as unknown) as Alert,
            mockSubscriptionGroups
        );

        expect(alertCardData).toEqual(mockEmptyAlertCardData);
    });

    test("getAlertCardData shall return appropriate alert card data for alert when subscription groups are invalid", () => {
        const expectedAlertCardData = cloneDeep(mockAlertCardData1);
        expectedAlertCardData.subscriptionGroups = [];

        const alertCardData = getAlertCardData(
            mockAlert1,
            (null as unknown) as SubscriptionGroup[]
        );

        expect(alertCardData).toEqual(expectedAlertCardData);
    });

    test("getAlertCardData shall return appropriate alert card data for alert when subscription groups are empty", () => {
        const expectedAlertCardData = cloneDeep(mockAlertCardData1);
        expectedAlertCardData.subscriptionGroups = [];

        const alertCardData = getAlertCardData(mockAlert1, []);

        expect(alertCardData).toEqual(expectedAlertCardData);
    });

    test("getAlertCardData shall return appropriate alert card data for alert and subscription groups", () => {
        const alertCardData = getAlertCardData(
            mockAlert1,
            mockSubscriptionGroups
        );

        expect(alertCardData).toEqual(mockAlertCardData1);
    });

    test("getAlertCardDatas shall return empty array for invalid alerts", () => {
        const alertCardDatas = getAlertCardDatas(
            (null as unknown) as Alert[],
            mockSubscriptionGroups
        );

        expect(alertCardDatas).toEqual([]);
    });

    test("getAlertCardDatas shall return empty array for empty alerts", () => {
        const alertCardDatas = getAlertCardDatas([], mockSubscriptionGroups);

        expect(alertCardDatas).toEqual([]);
    });

    test("getAlertCardDatas shall return appropriate alert card data array for alerts when subscription groups are invalid", () => {
        const expectedAlertCardData1 = cloneDeep(mockAlertCardData1);
        expectedAlertCardData1.subscriptionGroups = [];
        const expectedAlertCardData2 = cloneDeep(mockAlertCardData2);
        expectedAlertCardData2.subscriptionGroups = [];

        const alertCardDatas = getAlertCardDatas(
            mockAlerts,
            (null as unknown) as SubscriptionGroup[]
        );

        expect(alertCardDatas).toEqual([
            expectedAlertCardData1,
            expectedAlertCardData2,
        ]);
    });

    test("getAlertCardDatas shall return appropriate alert card data array for alerts when subscription groups are empty", () => {
        const expectedAlertCardData1 = cloneDeep(mockAlertCardData1);
        expectedAlertCardData1.subscriptionGroups = [];
        const expectedAlertCardData2 = cloneDeep(mockAlertCardData2);
        expectedAlertCardData2.subscriptionGroups = [];

        const alertCardDatas = getAlertCardDatas(mockAlerts, []);

        expect(alertCardDatas).toEqual([
            expectedAlertCardData1,
            expectedAlertCardData2,
        ]);
    });

    test("getAlertCardDatas shall return appropriate alert card data array for alerts and subscription groups", () => {
        const alertCardDatas = getAlertCardDatas(
            mockAlerts,
            mockSubscriptionGroups
        );

        expect(alertCardDatas).toEqual(mockAlertCardDatas);
    });

    test("filterAlerts shall return empty array for invalid alert card data array", () => {
        const alerts = filterAlerts(
            (null as unknown) as AlertCardData[],
            mockSearchWords
        );

        expect(alerts).toEqual([]);
    });

    test("filterAlerts shall return empty array for empty alert card data array", () => {
        const alerts = filterAlerts([], mockSearchWords);

        expect(alerts).toEqual([]);
    });

    test("filterAlerts shall return appropriate alert card data array for alert card data array when search words are invlid", () => {
        const alerts = filterAlerts(
            mockAlertCardDatas,
            (null as unknown) as string[]
        );

        expect(alerts).toEqual(mockAlertCardDatas);
    });

    test("filterAlerts shall return appropriate alert card data array for alert card data array when search words are empty", () => {
        const alerts = filterAlerts(mockAlertCardDatas, []);

        expect(alerts).toEqual(mockAlertCardDatas);
    });

    test("filterAlerts shall return appropriate alert card data array for alert card data array and search words", () => {
        const alerts = filterAlerts(mockAlertCardDatas, mockSearchWords);

        expect(alerts).toHaveLength(1);
        expect(alerts[0]).toEqual(mockAlertCardData1);
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
