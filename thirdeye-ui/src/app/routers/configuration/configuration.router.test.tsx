import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { AppRoute, AppRouteRelative } from "../../utils/routes/routes.util";
import { ConfigurationRouter } from "./configuration.router";

jest.mock("react-router-dom", () => ({
    ...(jest.requireActual("react-router-dom") as Record<string, unknown>),
    useNavigate: jest.fn().mockImplementation(() => mockNavigate),
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
    it("should render configuration page at exact configuration path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.CONFIGURATION]}>
                <Routes>
                    <Route
                        element={<ConfigurationRouter />}
                        path={`${AppRouteRelative.CONFIGURATION}/*`}
                    />
                </Routes>
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
                <Routes>
                    <Route
                        element={<ConfigurationRouter />}
                        path={`${AppRouteRelative.CONFIGURATION}/*`}
                    />
                </Routes>
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should direct exact subscription groups path to subscription groups router", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.SUBSCRIPTION_GROUPS]}>
                <Routes>
                    <Route
                        element={<ConfigurationRouter />}
                        path={`${AppRouteRelative.CONFIGURATION}/*`}
                    />
                </Routes>
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
                <Routes>
                    <Route
                        element={<ConfigurationRouter />}
                        path={`${AppRouteRelative.CONFIGURATION}/*`}
                    />
                </Routes>
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSubscriptionGroupsRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct exact metrics path to metrics router", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.METRICS]}>
                <Routes>
                    <Route
                        element={<ConfigurationRouter />}
                        path={`${AppRouteRelative.CONFIGURATION}/*`}
                    />
                </Routes>
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testMetricsRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct metrics path to metrics router", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.METRICS}/testPath`]}>
                <Routes>
                    <Route
                        element={<ConfigurationRouter />}
                        path={`${AppRouteRelative.CONFIGURATION}/*`}
                    />
                </Routes>
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testMetricsRouter")
        ).resolves.toBeInTheDocument();
    });
});

const mockNavigate = jest.fn();
