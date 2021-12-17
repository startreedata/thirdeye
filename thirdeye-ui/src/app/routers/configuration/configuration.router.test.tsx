import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { AppRoute } from "../../utils/routes/routes.util";
import { ConfigurationRouter } from "./configuration.router";

jest.mock(
    "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component",
    () => ({
        useAppBreadcrumbs: jest.fn().mockImplementation(() => ({
            setRouterBreadcrumbs: mockSetRouterBreadcrumbs,
        })),
    })
);

jest.mock("react-router-dom", () => ({
    ...(jest.requireActual("react-router-dom") as Record<string, unknown>),
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
    ...(jest.requireActual("../../utils/routes/routes.util") as Record<
        string,
        unknown
    >),
    getConfigurationPath: jest.fn().mockReturnValue("testConfigurationPath"),
}));

jest.mock("../subscription-groups/subscription-groups.router", () => ({
    SubscriptionGroupsRouter: jest
        .fn()
        .mockReturnValue("testSubscriptionGroupsRouter"),
}));

jest.mock("../metrics/metrics.router", () => ({
    MetricsRouter: jest.fn().mockReturnValue("testMetricsRouter"),
}));

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest.fn().mockReturnValue("testPageNotFoundPage"),
    })
);

describe("Configuration Router", () => {
    it("should set appropriate router breadcrumbs", () => {
        render(
            <MemoryRouter>
                <ConfigurationRouter />
            </MemoryRouter>
        );

        expect(mockSetRouterBreadcrumbs).toHaveBeenCalled();

        // Get router breadcrumbs
        const breadcrumbs = mockSetRouterBreadcrumbs.mock.calls[0][0];
        // Also invoke the click handlers
        breadcrumbs &&
            breadcrumbs[0] &&
            breadcrumbs[0].onClick &&
            breadcrumbs[0].onClick();

        expect(breadcrumbs).toHaveLength(1);
        expect(breadcrumbs[0].text).toEqual("label.configuration");
        expect(breadcrumbs[0].onClick).toBeDefined();
        expect(mockPush).toHaveBeenCalledWith("testConfigurationPath");
    });

    it("should render configuration page at exact configuration path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.CONFIGURATION]}>
                <ConfigurationRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSubscriptionGroupsRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid configuration path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.CONFIGURATION}/testPath`]}
            >
                <ConfigurationRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should direct exact subscription groups path to subscription groups router", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.SUBSCRIPTION_GROUPS]}>
                <ConfigurationRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSubscriptionGroupsRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct subscription groups path to subscription groups router", async () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.SUBSCRIPTION_GROUPS}/testPath`]}
            >
                <ConfigurationRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSubscriptionGroupsRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct exact metrics path to metrics router", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.METRICS]}>
                <ConfigurationRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testMetricsRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct metrics path to metrics router", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.METRICS}/testPath`]}>
                <ConfigurationRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testMetricsRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at any other path", async () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <ConfigurationRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page by default", async () => {
        render(
            <MemoryRouter>
                <ConfigurationRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });
});

const mockSetRouterBreadcrumbs = jest.fn();

const mockPush = jest.fn();
