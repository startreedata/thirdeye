import { act, cleanup, render, screen } from "@testing-library/react";
import React from "react";
import {
    Alert,
    AlertNode,
    AlertNodeType,
} from "../../rest/dto/alert.interfaces";
import { AlertListV1 } from "./alert-list-v1.component";
import { AlertListV1Props } from "./alert-list-v1.interfaces";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

jest.mock("@startree-ui/platform-ui", () => ({
    PageContentsCardV1: jest.fn().mockImplementation((props) => props.children),
    DataGridV1: jest.fn().mockImplementation((props) => (
        <>
            {Array.isArray(props.data) && props.data.length ? (
                props.data.map((alert: Alert) => (
                    <span key={alert.id}>
                        {alert.name}
                        {props.toolbarComponent}
                    </span>
                ))
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
    let mockProps = { ...mockDefaultProps };

    beforeEach(() => cleanup);

    afterEach(() => {
        mockProps = { ...mockDefaultProps };
    });

    it("component should load with no alerts", async () => {
        const props = { ...mockProps, alerts: [] };
        act(() => {
            render(<AlertListV1 {...props} />);
        });

        expect(await screen.findByText("NoDataIndicator")).toBeInTheDocument();
    });

    it("component should load with alerts", async () => {
        const props = { ...mockProps };
        act(() => {
            render(<AlertListV1 {...props} />);
        });

        expect(await screen.findByText("testNameAlert1")).toBeInTheDocument();
    });
});

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

const mockMethod = jest.fn();

const mockDefaultProps = {
    alerts: [mockUiAlert1],
    onChange: mockMethod,
    onDelete: mockMethod,
} as AlertListV1Props;
