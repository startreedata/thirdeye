/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
    getDatasourcesPath: jest.fn().mockReturnValue("testDatasourcePath"),
}));

jest.mock("../subscription-groups/subscription-groups.router", () => ({
    SubscriptionGroupsRouter: jest
        .fn()
        .mockReturnValue("testSubscriptionGroupsRouter"),
}));

jest.mock("../datasources/datasources.router", () => ({
    DatasourcesRouter: jest.fn().mockReturnValue("testDatasourcesRouter"),
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

        expect(
            await screen.findByText("testDatasourcesRouter")
        ).toBeInTheDocument();
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
