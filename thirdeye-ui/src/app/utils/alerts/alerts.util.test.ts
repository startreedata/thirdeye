import { cloneDeep } from "lodash";
import {
    Alert,
    AlertNode,
    AlertNodeType,
} from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { UiAlert } from "../../rest/dto/ui-alert.interfaces";
import {
    createAlertEvaluation,
    createDefaultAlert,
    createEmptyUiAlert,
    createEmptyUiAlertDatasetAndMetric,
    createEmptyUiAlertSubscriptionGroup,
    filterAlerts,
    getUiAlert,
    getUiAlerts,
    omitNonUpdatableData,
} from "./alerts.util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

describe("Alerts Util", () => {
    it("createDefaultAlert should create appropriate alert", () => {
        expect(createDefaultAlert()).toEqual(mockDefaultAlert);
    });

    it("createEmptyUiAlert should create appropriate UI alert", () => {
        expect(createEmptyUiAlert()).toEqual(mockEmptyUiAlert);
    });

    it("createEmptyUiAlertDatasetAndMetric should create appropriate UI alert dataset and metric", () => {
        expect(createEmptyUiAlertDatasetAndMetric()).toEqual(
            mockEmptyUiAlertDatasetAndMetric
        );
    });

    it("createEmptyUiAlertSubscriptionGroup should create appropriate UI alert subscription group", () => {
        expect(createEmptyUiAlertSubscriptionGroup()).toEqual(
            mockEmptyUiAlertSubscriptionGroup
        );
    });

    it("createAlertEvaluation should create appropriate alert evaluation", () => {
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

    it("getUiAlert should return empty UI alert for invalid alert", () => {
        expect(
            getUiAlert(null as unknown as Alert, mockSubscriptionGroups)
        ).toEqual(mockEmptyUiAlert);
    });

    it("getUiAlert should return appropriate UI alert for alert and invalid subscription groups", () => {
        const expectedUiAlert = cloneDeep(mockUiAlert1);
        expectedUiAlert.subscriptionGroups = [];

        expect(
            getUiAlert(mockAlert1, null as unknown as SubscriptionGroup[])
        ).toEqual(expectedUiAlert);
    });

    it("getUiAlert should return appropriate UI alert for alert and empty subscription groups", () => {
        const expectedUiAlert = cloneDeep(mockUiAlert1);
        expectedUiAlert.subscriptionGroups = [];

        expect(getUiAlert(mockAlert1, [])).toEqual(expectedUiAlert);
    });

    it("getUiAlert should return appropriate UI alert for alert and subscription groups", () => {
        expect(getUiAlert(mockAlert1, mockSubscriptionGroups)).toEqual(
            mockUiAlert1
        );
    });

    it("getUiAlerts should return empty array for invalid alerts", () => {
        expect(
            getUiAlerts(null as unknown as Alert[], mockSubscriptionGroups)
        ).toEqual([]);
    });

    it("getUiAlerts should return empty array for empty alerts", () => {
        expect(getUiAlerts([], mockSubscriptionGroups)).toEqual([]);
    });

    it("getUiAlerts should return appropriate UI alerts for alerts and invalid subscription groups", () => {
        const expectedUiAlert1 = cloneDeep(mockUiAlert1);
        expectedUiAlert1.subscriptionGroups = [];
        const expectedUiAlert2 = cloneDeep(mockUiAlert2);
        expectedUiAlert2.subscriptionGroups = [];
        const expectedUiAlert3 = cloneDeep(mockUiAlert3);
        expectedUiAlert3.subscriptionGroups = [];

        expect(
            getUiAlerts(mockAlerts, null as unknown as SubscriptionGroup[])
        ).toEqual([expectedUiAlert1, expectedUiAlert2, expectedUiAlert3]);
    });

    it("getUiAlerts should return appropriate UI alerts for alerts and empty subscription groups", () => {
        const expectedUiAlert1 = cloneDeep(mockUiAlert1);
        expectedUiAlert1.subscriptionGroups = [];
        const expectedUiAlert2 = cloneDeep(mockUiAlert2);
        expectedUiAlert2.subscriptionGroups = [];
        const expectedUiAlert3 = cloneDeep(mockUiAlert3);
        expectedUiAlert3.subscriptionGroups = [];

        expect(getUiAlerts(mockAlerts, [])).toEqual([
            expectedUiAlert1,
            expectedUiAlert2,
            expectedUiAlert3,
        ]);
    });

    it("getUiAlerts should return appropriate UI alerts for alerts and subscription groups", () => {
        expect(getUiAlerts(mockAlerts, mockSubscriptionGroups)).toEqual(
            mockUiAlerts
        );
    });

    it("filterAlerts should return empty array for invalid UI alerts", () => {
        expect(
            filterAlerts(null as unknown as UiAlert[], mockSearchWords)
        ).toEqual([]);
    });

    it("filterAlerts should return empty array for empty UI alerts", () => {
        expect(filterAlerts([], mockSearchWords)).toEqual([]);
    });

    it("filterAlerts should return appropriate UI alerts for UI alerts and invalid search words", () => {
        expect(filterAlerts(mockUiAlerts, null as unknown as string[])).toEqual(
            mockUiAlerts
        );
    });

    it("filterAlerts should return appropriate UI alerts for UI alerts and empty search words", () => {
        expect(filterAlerts(mockUiAlerts, [])).toEqual(mockUiAlerts);
    });

    it("filterAlerts should return appropriate UI alerts for UI alerts and search words", () => {
        expect(filterAlerts(mockUiAlerts, mockSearchWords)).toEqual([
            mockUiAlert1,
            mockUiAlert2,
        ]);
    });

    it("omitNonUpdatableData should return appropriate alert for update alert", () => {
        expect(omitNonUpdatableData(mockAlert3)).toEqual({});
    });
});

const mockDefaultAlert = {
    name: "new-alert",
    description: "This is the detection used by online service",
    nodes: {
        "detection-1": {
            type: AlertNodeType.DETECTION,
            subType: "PERCENTAGE_RULE",
            metric: {
                name: "views",
                dataset: {
                    name: "pageviews",
                },
            },
            params: {
                offset: "wo1w",
                percentageChange: 0.2,
            },
        },
    },
};

const mockEmptyUiAlert = {
    id: -1,
    name: "label.no-data-marker",
    active: false,
    activeText: "label.no-data-marker",
    userId: -1,
    createdBy: "label.no-data-marker",
    detectionTypes: [],
    filteredBy: [],
    datasetAndMetrics: [],
    subscriptionGroups: [],
    alert: null,
};

const mockEmptyUiAlertDatasetAndMetric = {
    datasetId: -1,
    datasetName: "label.no-data-marker",
    metricId: -1,
    metricName: "label.no-data-marker",
};

const mockEmptyUiAlertSubscriptionGroup = {
    id: -1,
    name: "label.no-data-marker",
};

const mockAlert1 = {
    id: 1,
    name: "testNameAlert1",
    active: true,
    owner: {
        id: 2,
        principal: "testPrincipalOwner2",
    },
    nodes: {
        alertNode1: {
            type: AlertNodeType.DETECTION,
            subType: "testSubTypeAlertNode1",
            metric: {
                id: 3,
                name: "testNameMetric3",
                dataset: {
                    id: 4,
                    name: "testNameDataset4",
                },
            },
        } as AlertNode,
        alertNode2: {
            type: AlertNodeType.DETECTION,
            subType: "testSubTypeAlertNode2",
            metric: {
                id: 5,
                dataset: {
                    id: 6,
                },
            },
        } as AlertNode,
        alertNode3: {
            type: AlertNodeType.FILTER,
            subType: "testSubTypeAlertNode3",
            metric: {
                id: 7,
                name: "testNameMetric7",
            },
        } as AlertNode,
        alertNode4: {
            type: "testTypeAlertNode4" as AlertNodeType,
            subType: "testSubTypeAlertNode4",
        } as AlertNode,
    } as { [index: string]: AlertNode },
} as Alert;

const mockAlert2 = {
    id: 8,
    active: false,
    owner: {
        id: 9,
    },
} as Alert;

const mockAlert3 = {
    id: 10,
} as Alert;

const mockAlerts = [mockAlert1, mockAlert2, mockAlert3];

const mockSubscriptionGroup1 = {
    id: 11,
    name: "testNameSubscriptionGroup11",
    alerts: [
        {
            id: 1,
        },
        {
            id: 8,
        },
    ],
} as SubscriptionGroup;

const mockSubscriptionGroup2 = {
    id: 12,
    alerts: [
        {
            id: 1,
        },
        {
            id: 13,
        },
    ],
} as SubscriptionGroup;

const mockSubscriptionGroup3 = {
    id: 14,
    name: "testNameSubscriptionGroup14",
} as SubscriptionGroup;

const mockSubscriptionGroups = [
    mockSubscriptionGroup1,
    mockSubscriptionGroup2,
    mockSubscriptionGroup3,
];

const mockUiAlert1 = {
    id: 1,
    name: "testNameAlert1",
    active: true,
    activeText: "label.active",
    userId: 2,
    createdBy: "testPrincipalOwner2",
    detectionTypes: ["testSubTypeAlertNode1", "testSubTypeAlertNode2"],
    filteredBy: ["testSubTypeAlertNode3"],
    datasetAndMetrics: [
        {
            datasetId: 4,
            datasetName: "testNameDataset4",
            metricId: 3,
            metricName: "testNameMetric3",
        },
        {
            datasetId: 6,
            datasetName: "label.no-data-marker",
            metricId: 5,
            metricName: "label.no-data-marker",
        },
        {
            datasetId: -1,
            datasetName: "label.no-data-marker",
            metricId: 7,
            metricName: "testNameMetric7",
        },
    ],
    subscriptionGroups: [
        {
            id: 11,
            name: "testNameSubscriptionGroup11",
        },
        {
            id: 12,
            name: "label.no-data-marker",
        },
    ],
    alert: mockAlert1,
};

const mockUiAlert2 = {
    id: 8,
    name: "label.no-data-marker",
    active: false,
    activeText: "label.inactive",
    userId: 9,
    createdBy: "label.no-data-marker",
    detectionTypes: [],
    filteredBy: [],
    datasetAndMetrics: [],
    subscriptionGroups: [
        {
            id: 11,
            name: "testNameSubscriptionGroup11",
        },
    ],
    alert: mockAlert2,
};

const mockUiAlert3 = {
    id: 10,
    name: "label.no-data-marker",
    active: false,
    activeText: "label.inactive",
    userId: -1,
    createdBy: "label.no-data-marker",
    detectionTypes: [],
    filteredBy: [],
    datasetAndMetrics: [],
    subscriptionGroups: [],
    alert: mockAlert3,
};

const mockUiAlerts = [mockUiAlert1, mockUiAlert2, mockUiAlert3];

const mockSearchWords = ["testNameMetric3", "testNameSubscriptionGroup11"];
