import { act, render, screen } from "@testing-library/react";
import React from "react";
import { PageContentsGridV1 } from "../../platform/components/page-v1/page-contents-grid-v1/page-contents-grid-v1";
import { HomePage } from "./home-page.component";

jest.mock(
    "../../platform/components/page-v1/page-contents-grid-v1/page-contents-grid-v1/page-contents-grid-v1.component",
    () => ({
        PageContentsGridV1: jest
            .fn()
            .mockImplementation((props) => props.children),
    })
);

jest.mock(
    "../../platform/components/page-v1/page-v1/page-v1.component",
    () => ({
        PageV1: jest.fn().mockImplementation((props) => props.children),
    })
);

jest.mock("../../platform/components/tile-button-v1", () => ({
    TileButtonIconV1: jest.fn().mockImplementation((props) => props.children),
    TileButtonTextV1: jest.fn().mockImplementation((props) => props.children),
    TileButtonV1: jest.fn().mockImplementation((props) => (
        <a data-testid={props.href} href={props.href}>
            {props.children}
        </a>
    )),
}));

jest.mock("../../components/page-header/page-header.component", () => ({
    PageHeader: jest.fn().mockImplementation((props) => props.title),
}));

jest.mock(
    "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component",
    () => ({
        useAppBreadcrumbs: jest.fn().mockImplementation(() => ({
            setPageBreadcrumbs: mockSetPageBreadcrumbs,
        })),
    })
);

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

jest.mock("../../utils/routes/routes.util", () => ({
    getAlertsPath: jest.fn().mockReturnValue("testAlertsPath"),
    getAnomaliesPath: jest.fn().mockReturnValue("testAnomaliesPath"),
    getConfigurationPath: jest.fn().mockReturnValue("testConfigurationPath"),
    getSubscriptionGroupsPath: jest
        .fn()
        .mockReturnValue("testSubscriptionGroupsPath"),
    getDatasetsPath: jest.fn().mockReturnValue("testDatasetsPath"),
    getDatasourcesPath: jest.fn().mockReturnValue("testDatasourcesPath"),
    getMetricsPath: jest.fn().mockReturnValue("testMetricsPath"),
}));

describe("Home Page", () => {
    it("should set appropriate page breadcrumbs", async () => {
        act(() => {
            render(<HomePage />);
        });

        expect(mockSetPageBreadcrumbs).toHaveBeenCalledWith([]);
    });

    it("should set appropriate page title", async () => {
        act(() => {
            render(<HomePage />);
        });

        expect(PageContentsGridV1).toHaveBeenCalledWith(
            {
                children: expect.any(Object),
            },
            {}
        );
    });

    it("should render all navigation buttons", async () => {
        act(() => {
            render(<HomePage />);
        });

        expect(screen.getByText("label.alerts")).toBeInTheDocument();
        expect(screen.getByText("label.anomalies")).toBeInTheDocument();
        expect(screen.getByText("label.configuration")).toBeInTheDocument();
    });

    it("should have proper link to alerts path on alerts icon button", async () => {
        act(() => {
            render(<HomePage />);
        });

        expect(screen.getByTestId(TEST_PATHS.alerts)).toHaveAttribute(
            "href",
            TEST_PATHS.alerts
        );
    });

    it("should have proper link to anomalies path on anomalies icon button", async () => {
        act(() => {
            render(<HomePage />);
        });

        expect(screen.getByTestId(TEST_PATHS.anomalies)).toHaveAttribute(
            "href",
            TEST_PATHS.anomalies
        );
    });

    it("should have proper link to configuration path on configuration icon button", async () => {
        act(() => {
            render(<HomePage />);
        });

        expect(screen.getByTestId(TEST_PATHS.configuration)).toHaveAttribute(
            "href",
            TEST_PATHS.configuration
        );
    });
});

const mockSetPageBreadcrumbs = jest.fn();

const TEST_PATHS = {
    alerts: "testAlertsPath",
    anomalies: "testAnomaliesPath",
    configuration: "testConfigurationPath",
    subscriptionGroups: "testSubscriptionGroupsPath",
    datasets: "testDatasetsPath",
    datasources: "testDatasourcesPath",
    metrics: "testMetricsPath",
};
