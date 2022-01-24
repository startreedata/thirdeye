import {
    act,
    cleanup,
    fireEvent,
    render,
    screen,
} from "@testing-library/react";
import React from "react";
import {
    Alert,
    AlertNode,
    AlertNodeType,
} from "../../rest/dto/alert.interfaces";
import { AlertList } from "./alert-list.component";
import { AlertListProps } from "./alert-list.interfaces";

jest.mock("@startree-ui/platform-ui", () => ({
    AppLoadingIndicatorV1: jest
        .fn()
        .mockImplementation(() => <p>LoadingIndicator</p>),
}));

jest.mock("../search-bar/search-bar.component", () => ({
    SearchBar: jest
        .fn()
        .mockImplementation(({ onChange }) => (
            <p onClick={() => onChange(["a"])}>SearchBar</p>
        )),
}));

jest.mock("../no-data-indicator/no-data-indicator.component", () => ({
    NoDataIndicator: jest.fn().mockImplementation(() => <p>NoDataIndicator</p>),
}));

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

describe("AlertList", () => {
    let mockProps = { ...mockDefaultProps };

    beforeEach(() => cleanup);

    afterEach(() => {
        mockProps = { ...mockDefaultProps };
    });

    it("loading indicator should be present if the alerts are null", async () => {
        const props = { ...mockProps, alerts: null };
        act(() => {
            render(<AlertList {...props} />);
        });

        expect(await screen.findByText("LoadingIndicator")).toBeInTheDocument();
    });

    it("search bar should not get rendered if hideSearchBar is true", async () => {
        const props = { ...mockProps, alerts: [], hideSearchBar: true };
        act(() => {
            render(<AlertList {...props} />);
        });

        const SearchBar = screen.queryByText("SearchBar");

        expect(SearchBar).not.toBeInTheDocument();
    });

    it("search bar should get rendered if hideSearchBar is false", async () => {
        const props = { ...mockProps, alerts: [], hideSearchBar: false };
        act(() => {
            render(<AlertList {...props} />);
        });

        expect(await screen.findByText("SearchBar")).toBeInTheDocument();
    });

    it("search bar should call on change", async () => {
        const params = { ...mockProps, hideSearchBar: false };

        await act(async () => {
            render(<AlertList {...params} />);
        });

        expect(await screen.findByText("SearchBar")).toBeInTheDocument();

        fireEvent.click(await screen.findByText("SearchBar"));
    });

    it("no data present should be displayed if the alerts are empty", async () => {
        const props = { ...mockProps, alerts: [] };
        act(() => {
            render(<AlertList {...props} />);
        });

        expect(await screen.findByText("NoDataIndicator")).toBeInTheDocument();
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

const mockDefaultProps = {
    hideSearchBar: false,
    alerts: [mockUiAlert1],
    onChange: jest.fn(),
    onDelete: jest.fn(),
} as AlertListProps;
