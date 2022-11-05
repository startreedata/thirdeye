/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components/app-loading-indicator-v1/app-loading-indicator-v1.component";
import { AppRoute, AppRouteRelative } from "../../utils/routes/routes.util";
import { AlertsRouter } from "./alerts.router";

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
    getAlertsPath: jest.fn().mockReturnValue("testAlertsPath"),
}));

jest.mock(
    "../../platform/components/app-loading-indicator-v1/app-loading-indicator-v1.component",
    () => ({
        AppLoadingIndicatorV1: jest
            .fn()
            .mockReturnValue("testLoadingIndicatorV1"),
    })
);

jest.mock("../../pages/alerts-all-page/alerts-all-page.component", () => ({
    AlertsAllPage: jest.fn().mockReturnValue("testAlertsAllPage"),
}));

jest.mock("../../pages/alerts-view-page/alerts-view-page.component", () => ({
    AlertsViewPage: jest.fn().mockReturnValue("testAlertsViewPage"),
}));

jest.mock(
    "../../pages/alerts-create-page/alerts-create-base-page.component",
    () => ({
        AlertsCreateBasePage: jest
            .fn()
            .mockReturnValue("testAlertsCreateBasePage"),
    })
);

jest.mock(
    "../../pages/alerts-create-page/alerts-create-copy-page.component",
    () => ({
        AlertsCreateCopyPage: jest
            .fn()
            .mockReturnValue("testAlertsCreateCopyPage"),
    })
);

jest.mock(
    "../../pages/alerts-create-page/alerts-create-simple-page/alerts-create-simple-page.component",
    () => ({
        AlertsCreateNewPage: jest
            .fn()
            .mockReturnValue("testAlertsCreateSimplePage"),
    })
);

jest.mock(
    "../../pages/alerts-update-page/alerts-update-base-page.component",
    () => ({
        AlertsUpdateBasePage: jest
            .fn()
            .mockReturnValue("testAlertsUpdateBasePage"),
    })
);

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest.fn().mockReturnValue("testPageNotFoundPage"),
    })
);

jest.mock(
    "../../components/time-range/time-range-provider/time-range-provider.component",
    () => ({
        useTimeRange: jest.fn().mockImplementation(() => {
            return {
                timeRangeDuration: mockTimeRangeDuration,
            };
        }),
    })
);

describe("Alerts Router", () => {
    it("should have rendered loading indicator while loading", () => {
        render(
            <MemoryRouter>
                <AlertsRouter />
            </MemoryRouter>
        );

        expect(AppLoadingIndicatorV1).toHaveBeenCalled();
    });

    it("should render alerts all page at exact alerts path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ALERTS]}>
                <Routes>
                    <Route
                        element={<AlertsRouter />}
                        path={`${AppRouteRelative.ALERTS}/*`}
                    />
                </Routes>
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsAllPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render alerts all page at exact alerts all path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ALERTS_ALL]}>
                <Routes>
                    <Route
                        element={<AlertsRouter />}
                        path={`${AppRouteRelative.ALERTS}/*`}
                    />
                </Routes>
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsAllPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid alerts all path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ALERTS_ALL}/testPath`]}>
                <Routes>
                    <Route
                        element={<AlertsRouter />}
                        path={`${AppRouteRelative.ALERTS}/*`}
                    />
                </Routes>
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render alerts view page at exact alerts view path", async () => {
        render(
            <MemoryRouter initialEntries={[`/alerts/1234`]}>
                <Routes>
                    <Route
                        element={<AlertsRouter />}
                        path={`${AppRouteRelative.ALERTS}/*`}
                    />
                </Routes>
            </MemoryRouter>
        );

        expect(
            await screen.findByText("testAlertsViewPage")
        ).toBeInTheDocument();
    });

    it("should render page not found page at invalid alerts view path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.ALERTS_ALERT}/testPath`]}
            >
                <Routes>
                    <Route
                        element={<AlertsRouter />}
                        path={`${AppRouteRelative.ALERTS}/*`}
                    />
                </Routes>
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render alerts create page at exact alerts create path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ALERTS_CREATE]}>
                <Routes>
                    <Route
                        element={<AlertsRouter />}
                        path={`${AppRouteRelative.ALERTS}/*`}
                    />
                </Routes>
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsCreateBasePage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid alerts create path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.ALERTS_CREATE}/testPath`]}
            >
                <Routes>
                    <Route
                        element={<AlertsRouter />}
                        path={`${AppRouteRelative.ALERTS}/*`}
                    />
                </Routes>
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render alerts update page at exact alerts update path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ALERTS_UPDATE]}>
                <Routes>
                    <Route
                        element={<AlertsRouter />}
                        path={`${AppRouteRelative.ALERTS}/*`}
                    />
                </Routes>
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsUpdateBasePage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid alerts update path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.ALERTS_UPDATE}/testPath`]}
            >
                <Routes>
                    <Route
                        element={<AlertsRouter />}
                        path={`${AppRouteRelative.ALERTS}/*`}
                    />
                </Routes>
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });
});

const mockTimeRangeDuration = {
    timeRange: "CUSTOM",
    startTime: 1,
    endTime: 2,
};
