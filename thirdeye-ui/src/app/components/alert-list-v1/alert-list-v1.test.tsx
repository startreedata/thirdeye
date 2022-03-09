import { fireEvent, render, screen } from "@testing-library/react";
import React, { ReactNode } from "react";
import { Alert, AlertNodeType } from "../../rest/dto/alert.interfaces";
import { AlertListV1 } from "./alert-list-v1.component";
import { AlertListV1Props } from "./alert-list-v1.interfaces";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

jest.mock("react-router-dom", () => ({
    useNavigate: jest.fn().mockReturnValue(() => null),
}));

jest.mock("../entity-cards/alert-card-v1/alert-card-v1.component", () => ({
    AlertCardV1: jest.fn().mockImplementation((props) => props.uiAlert),
}));

jest.mock("../../platform/components", () => ({
    PageContentsCardV1: jest.fn().mockImplementation((props) => props.children),
    DataGridV1: jest.fn().mockImplementation((props) => (
        <>
            {Array.isArray(props.data) && props.data.length ? (
                props.data.map(
                    (
                        alert: Alert & {
                            children?: {
                                id: number;
                                expandPanelContents: ReactNode;
                            };
                        }
                    ) => {
                        const mockAlert = { ...alert };
                        delete mockAlert.children;

                        return (
                            <span key={alert.id}>
                                {alert.name}
                                <p
                                    onClick={() =>
                                        mockOnChangeMethod(mockAlert)
                                    }
                                >{`Edit${alert.id}`}</p>
                                <p
                                    onClick={() =>
                                        mockOnDeleteMethod(mockAlert)
                                    }
                                >{`Delete${alert.id}`}</p>
                            </span>
                        );
                    }
                )
            ) : (
                <p>NoDataIndicator</p>
            )}
        </>
    )),
    DataGridScrollV1: {
        Body: jest.fn().mockImplementation((props) => props.children),
    },
}));

describe("AlertListV1", () => {
    it("should load with no alerts", async () => {
        const props = { ...mockDefaultProps, alerts: [] };
        render(<AlertListV1 {...props} />);

        expect(await screen.findByText("NoDataIndicator")).toBeInTheDocument();
    });

    it("should load with alerts", async () => {
        render(<AlertListV1 {...mockDefaultProps} />);

        expect(await screen.findByText("testNameAlert1")).toBeInTheDocument();
    });

    it("should call onChange when Edit is clicked", async () => {
        render(<AlertListV1 {...mockDefaultProps} />);

        fireEvent.click(screen.getByText("Edit1"));

        expect(mockOnChangeMethod).toHaveBeenNthCalledWith(1, mockUiAlert);
    });

    it("should call onDelete when Delete is clicked", async () => {
        render(<AlertListV1 {...mockDefaultProps} />);

        fireEvent.click(screen.getByText("Delete1"));

        expect(mockOnDeleteMethod).toHaveBeenNthCalledWith(1, mockUiAlert);
    });
});

const mockAlert = {
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

const mockUiAlert = {
    id: 1,
    name: "testNameAlert1",
    active: true,
    activeText: "label.active",
    userId: 2,
    createdBy: "testPrincipalOwner2",
    detectionTypes: ["testSubTypeAlertNode1", "testSubTypeAlertNode2"],
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
    alert: mockAlert,
};

const mockOnDeleteMethod = jest.fn();

const mockOnChangeMethod = jest.fn();

const mockDefaultProps = {
    alerts: [mockUiAlert],
    onChange: mockOnChangeMethod,
    onDelete: mockOnDeleteMethod,
} as AlertListV1Props;
