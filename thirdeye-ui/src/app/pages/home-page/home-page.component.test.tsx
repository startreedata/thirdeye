import { act, fireEvent, render, screen } from "@testing-library/react";
import React from "react";
import { HomePage } from "./home-page.component";

jest.mock(
    "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component",
    () => ({
        useAppBreadcrumbs: jest.fn().mockImplementation(() => ({
            setPageBreadcrumbs: mockSetPageBreadcrumbs,
        })),
    })
);

jest.mock("react-router-dom", () => ({
    useHistory: jest.fn().mockImplementation(() => ({
        push: mockPush,
    })),
}));

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
    getMetricsPath: jest.fn().mockReturnValue("testMetricsPath"),
}));

jest.mock("../../components/page-contents/page-contents.component", () => ({
    PageContents: jest.fn().mockImplementation((props) => (
        <>
            <div>title:{props.title}</div>
            <div>{props.children}</div>
        </>
    )),
}));

describe("Home Page", () => {
    test("should set appropriate page breadcrumbs", async () => {
        act(() => {
            render(<HomePage />);
        });

        expect(mockSetPageBreadcrumbs).toHaveBeenCalledWith([]);
    });

    test("should set appropriate page title", async () => {
        act(() => {
            render(<HomePage />);
        });

        expect(screen.getByText("title:label.home")).toBeInTheDocument();
    });

    test("should render all navigation buttons", async () => {
        act(() => {
            render(<HomePage />);
        });

        expect(screen.getByText("label.alerts")).toBeInTheDocument();
        expect(screen.getByText("label.anomalies")).toBeInTheDocument();
        expect(screen.getByText("label.configuration")).toBeInTheDocument();
        expect(
            screen.getByText("label.subscription-groups")
        ).toBeInTheDocument();
        expect(screen.getByText("label.metrics")).toBeInTheDocument();
    });

    test("should navigate to alerts path on alerts button click", async () => {
        act(() => {
            render(<HomePage />);
        });

        fireEvent.click(screen.getByText("label.alerts"));

        expect(mockPush).toHaveBeenCalledWith("testAlertsPath");
    });

    test("should navigate to anomalies path on anomalies button click", async () => {
        act(() => {
            render(<HomePage />);
        });

        fireEvent.click(screen.getByText("label.anomalies"));

        expect(mockPush).toHaveBeenCalledWith("testAnomaliesPath");
    });

    test("should navigate to configuartion path on configuration button click", async () => {
        act(() => {
            render(<HomePage />);
        });

        fireEvent.click(screen.getByText("label.configuration"));

        expect(mockPush).toHaveBeenCalledWith("testConfigurationPath");
    });

    test("should navigate to subscription groups path on subscription groups button click", async () => {
        act(() => {
            render(<HomePage />);
        });

        fireEvent.click(screen.getByText("label.subscription-groups"));

        expect(mockPush).toHaveBeenCalledWith("testSubscriptionGroupsPath");
    });

    test("should navigate to metrics path on metrics button click", async () => {
        act(() => {
            render(<HomePage />);
        });

        fireEvent.click(screen.getByText("label.metrics"));

        expect(mockPush).toHaveBeenCalledWith("testMetricsPath");
    });
});

const mockSetPageBreadcrumbs = jest.fn();

const mockPush = jest.fn();
