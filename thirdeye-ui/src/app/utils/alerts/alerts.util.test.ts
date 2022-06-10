///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///


import { cloneDeep } from "lodash";
import { Alert, AlertNodeType } from "../../rest/dto/alert.interfaces";
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
        const result = createDefaultAlert();

        expect(result.name).not.toBeNull();
        expect(result.description).not.toBeNull();
        expect(result.cron).not.toBeNull();
        expect(result.template).not.toBeNull();
        expect(result.templateProperties).not.toBeNull();
    });

    it("createEmptyUiAlert should create appropriate UI alert", () => {
        const result = createEmptyUiAlert();

        expect(result.name).not.toBeNull();
        expect(result.renderedMetadata).not.toBeNull();
        expect(result.active).not.toBeNull();
        expect(result.activeText).not.toBeNull();
        expect(result.userId).not.toBeNull();
        expect(result.createdBy).not.toBeNull();
        expect(result.detectionTypes).not.toBeNull();
        expect(result.datasetAndMetrics).not.toBeNull();
        expect(result.subscriptionGroups).not.toBeNull();
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

const mockEmptyUiAlert = {
    id: -1,
    name: "label.no-data-marker",
    active: false,
    activeText: "label.no-data-marker",
    userId: -1,
    createdBy: "label.no-data-marker",
    detectionTypes: [],
    datasetAndMetrics: [],
    subscriptionGroups: [],
    renderedMetadata: [],
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
    description: "hello world",
    cron: "0 0 0 1/1 * ? *",
    active: true,
    owner: {
        id: 2,
        principal: "testPrincipalOwner2",
    },
    template: {
        nodes: [
            {
                name: "root",
                type: AlertNodeType.ANOMALY_DETECTOR,
                params: {
                    type: "THRESHOLD",
                    "component.timezone": "UTC",
                    "component.monitoringGranularity":
                        "${monitoringGranularity}",
                    "component.timestamp": "ts",
                    "component.metric": "met",
                    "component.max": "${max}",
                    "component.min": "${min}",
                    "anomaly.metric": "${aggregateFunction}(${metric})",
                },
                inputs: [
                    {
                        targetProperty: "current",
                        sourcePlanNode: "currentDataFetcher",
                        sourceProperty: "currentData",
                    },
                ],
            },
            {
                type: AlertNodeType.DATA_FETCHER,
                params: {
                    "component.dataSource": "${dataSource}",
                    "component.query": "query here",
                },
                outputs: [
                    {
                        outputKey: "pinot",
                        outputName: "currentData",
                    },
                ],
            },
        ],
        metadata: {
            metric: {
                name: "orders",
            },
            datasource: {
                name: "${dataSource}",
            },
            dataset: {
                name: "${dataset}",
            },
        },
    },
    templateProperties: {
        dataSource: "pinotQuickStartAzure",
        dataset: "pageviews",
        aggregateFunction: "sum",
        metric: "views",
        monitoringGranularity: "P1D",
        timeColumn: "date",
        timeColumnFormat: "yyyyMMdd",
        max: "850000",
        min: "250000",
    },
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
    detectionTypes: ["THRESHOLD"],
    datasetAndMetrics: [],
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
    renderedMetadata: [
        {
            key: "dataset",
            value: "pageviews",
        },
        {
            key: "datasource",
            value: "pinotQuickStartAzure",
        },
        {
            key: "metric",
            value: "orders",
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
    datasetAndMetrics: [],
    subscriptionGroups: [
        {
            id: 11,
            name: "testNameSubscriptionGroup11",
        },
    ],
    renderedMetadata: [],
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
    datasetAndMetrics: [],
    subscriptionGroups: [],
    renderedMetadata: [],
    alert: mockAlert3,
};

const mockUiAlerts = [mockUiAlert1, mockUiAlert2, mockUiAlert3];

const mockSearchWords = ["testNameMetric3", "testNameSubscriptionGroup11"];
