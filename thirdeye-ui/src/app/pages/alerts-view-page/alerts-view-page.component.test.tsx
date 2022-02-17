import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter, Route } from "react-router-dom";
import { AppRoute, getAlertsViewPath } from "../../utils/routes/routes.util";
import { AlertsViewPage } from "./alerts-view-page.component";

jest.mock("../../platform/components", () => ({
    ...(jest.requireActual("../../platform/components") as Record<
        string,
        unknown
    >),
    JSONEditorV1: jest.fn().mockImplementation(() => <p>JSONEditor</p>),
    PageV1: jest.fn().mockImplementation((props) => props.children),
    PageContentsGridV1: jest.fn().mockImplementation((props) => props.children),
    PageHeaderV1: jest.fn().mockImplementation((props) => props.children),
    PageHeaderTextV1: jest.fn().mockImplementation((props) => props.children),
    PageHeaderActionsV1: jest
        .fn()
        .mockImplementation((props) => props.children),
    AppLoadingIndicatorV1: jest
        .fn()
        .mockImplementation(() => <p>loading component</p>),
    useNotificationProviderV1: jest.fn().mockImplementation(() => ({
        notify: mockNotify,
    })),
}));

jest.mock("../../rest/alerts/alerts.rest", () => ({
    ...(jest.requireActual("../../rest/alerts/alerts.rest") as Record<
        string,
        unknown
    >),
    getAlertEvaluation: jest
        .fn()
        .mockImplementation(() => mockedGetEvaluateResponse),
    getAlert: jest
        .fn()
        .mockImplementation(() => Promise.resolve(MOCK_ALERT_PAYLOAD)),
}));

jest.mock("../../rest/subscription-groups/subscription-groups.rest", () => ({
    ...(jest.requireActual(
        "../../rest/subscription-groups/subscription-groups.rest"
    ) as Record<string, unknown>),
    getAllSubscriptionGroups: jest
        .fn()
        .mockImplementation(() =>
            Promise.resolve(MOCK_SUBSCRIPTION_GROUP_PAYLOAD)
        ),
}));

describe("Alerts View Page", () => {
    it("should render expected components after successful API calls", async () => {
        render(
            <MemoryRouter initialEntries={[getAlertsViewPath(458076)]}>
                <Route path={AppRoute.ALERTS_VIEW}>
                    <AlertsViewPage />
                </Route>
            </MemoryRouter>
        );

        const jsonEditorComponent = await screen.findByText("JSONEditor");

        expect(jsonEditorComponent).toBeInTheDocument();

        // MOCK_EVALUATE_PAYLOAD has 4 anomalies in it
        const anomaliesCountContainer = await screen.findByText("4");

        expect(anomaliesCountContainer).toBeInTheDocument();
    });

    it("should render error alert id evaluate API fails", async () => {
        mockedGetEvaluateResponse = Promise.reject({
            response: { data: { message: "Error message" } },
        });
        render(
            <MemoryRouter initialEntries={[getAlertsViewPath(458076)]}>
                <Route path={AppRoute.ALERTS_VIEW}>
                    <AlertsViewPage />
                </Route>
            </MemoryRouter>
        );

        const jsonEditorComponent = await screen.findByText("JSONEditor");

        expect(jsonEditorComponent).toBeInTheDocument();

        // Should display 0 since evaluate call failed
        const anomaliesCountContainer = await screen.findByText("0");

        expect(anomaliesCountContainer).toBeInTheDocument();

        expect(mockNotify).toHaveBeenLastCalledWith(
            "error",
            "message.error-while-fetching"
        );
    });
});

const MOCK_SUBSCRIPTION_GROUP_PAYLOAD = [
    {
        id: 458134,
        name: "sg-suvodeep",
        application: {},
        alerts: [
            { id: 458076 },
            { id: 458064 },
            { id: 458477 },
            { id: 460939 },
        ],
        cron: "0 */5 * * * ?",
        notificationSchemes: {
            email: { to: ["cyril@startree.ai", "suvodeep@startree.ai"] },
        },
    },
    {
        id: 513323,
        name: "Test-MM",
        application: {},
        alerts: [{ id: 513324 }],
        cron: "0 */5 * * * ?",
        notificationSchemes: { email: { to: ["madhumita@startree.ai"] } },
    },
];

const MOCK_ALERT_PAYLOAD = {
    id: 458076,
    name: "threshold-alert-pull-request-merge-events",
    description: "Threshold alert on pull request merge events",
    template: { name: "threshold-template" },
    templateProperties: {
        dataSource: "pinotQuickStartAzure",
        dataset: "pullRequestMergedEvents",
        metric: "sum(numCommits)",
        metricColumn: "numCommits",
        monitoringGranularity: "2:HOURS",
        timeColumn: "mergedTimeMillis",
        timeColumnFormat: "1:MILLISECONDS:EPOCH",
        max: "15000",
        min: "1000",
    },
    cron: "0 0 0/2 1/1 * ? *",
    active: true,
    owner: { principal: "no-auth-user" },
};

const MOCK_EVALUATE_PAYLOAD = {
    alert: {
        template: {
            id: 458010,
            name: "threshold-template",
            description: "Threshold Template",
            cron: "0 0/1 * 1/1 * ? * ",
            nodes: [
                {
                    name: "root",
                    type: "AnomalyDetector",
                    params: {
                        "component.min": "1000",
                        "component.metric": "met",
                        "anomaly.source":
                            "threshold-alert-pull-request-merge-events/root",
                        type: "THRESHOLD",
                        "component.monitoringGranularity": "2_HOURS",
                        "component.max": "15000",
                        "component.timestamp": "ts",
                        "anomaly.metric": "sum(numCommits)",
                        "component.timezone": "US/Pacific",
                        "component.dimensions": [],
                        "component.offset": "mo1m",
                        "component.pattern": "down",
                    },
                    inputs: [
                        {
                            targetProperty: "current",
                            sourcePlanNode: "currentDataFetcher",
                            sourceProperty: "currentOutput",
                        },
                    ],
                    outputs: [],
                },
                {
                    name: "currentDataFetcher",
                    type: "DataFetcher",
                    params: {
                        "component.dataSource": "pinotQuickStartAzure",
                        "component.query": "SELECT TRUNCATEDx",
                    },
                    inputs: [],
                    outputs: [
                        { outputKey: "pinot", outputName: "currentOutput" },
                    ],
                },
            ],
            rca: {
                datasource: "pinotQuickStartAzure",
                dataset: "pullRequestMergedEvents",
                metric: "numCommits",
            },
        },
    },
    detectionEvaluations: {
        output_AnomalyDetectorResult_0: {
            data: {
                timestamp: [1631181600000, 1631188800000, 1631196000000],
                upperBound: [15000.0, 15000.0, 15000.0],
                lowerBound: [1000.0, 1000.0, 1000.0],
                current: [13327.0, 5738.0, 9573.0],
                expected: [13327.0, 5738.0, 9573.0],
            },
            anomalies: [
                {
                    startTime: 1631131200000,
                    endTime: 1631138400000,
                    avgCurrentVal: 16921.0,
                    avgBaselineVal: 15000.0,
                    score: 0.0,
                    weight: 0.0,
                    impactToGlobal: 0.0,
                    sourceType: "DEFAULT_ANOMALY_DETECTION",
                    created: 1643657971227,
                    notified: false,
                    alert: {},
                },
                {
                    startTime: 1634551200000,
                    endTime: 1634558400000,
                    avgCurrentVal: 15371.0,
                    avgBaselineVal: 15000.0,
                    score: 0.0,
                    weight: 0.0,
                    impactToGlobal: 0.0,
                    sourceType: "DEFAULT_ANOMALY_DETECTION",
                    created: 1643755805344,
                    notified: false,
                    alert: {},
                },
                {
                    startTime: 1634551200000,
                    endTime: 1634558400000,
                    avgCurrentVal: 15371.0,
                    avgBaselineVal: 15000.0,
                    score: 0.0,
                    weight: 0.0,
                    impactToGlobal: 0.0,
                    sourceType: "DEFAULT_ANOMALY_DETECTION",
                    created: 1643755805344,
                    notified: false,
                    alert: {},
                },
                {
                    startTime: 1634551200000,
                    endTime: 1634558400000,
                    avgCurrentVal: 15371.0,
                    avgBaselineVal: 15000.0,
                    score: 0.0,
                    weight: 0.0,
                    impactToGlobal: 0.0,
                    sourceType: "DEFAULT_ANOMALY_DETECTION",
                    created: 1643755805344,
                    notified: false,
                    alert: {},
                },
            ],
        },
    },
};

const mockNotify = jest.fn();
let mockedGetEvaluateResponse = Promise.resolve(MOCK_EVALUATE_PAYLOAD);
