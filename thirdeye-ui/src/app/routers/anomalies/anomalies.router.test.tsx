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
import { MemoryRouter } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components/app-loading-indicator-v1/app-loading-indicator-v1.component";
import { AppRoute, AppRouteRelative } from "../../utils/routes/routes.util";
import { AnomaliesRouter } from "./anomalies.router";

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
    getAnomaliesPath: jest.fn().mockReturnValue("testAnomaliesPath"),
}));

jest.mock(
    "../../platform/components/app-loading-indicator-v1/app-loading-indicator-v1.component",
    () => ({
        AppLoadingIndicatorV1: jest
            .fn()
            .mockReturnValue("testLoadingIndicatorV1"),
    })
);

jest.mock(
    "../../pages/anomalies-all-page/anomalies-all-page.component",
    () => ({
        AnomaliesAllPage: jest.fn().mockReturnValue("testAnomaliesAllPage"),
    })
);

jest.mock(
    "../../pages/anomalies-view-page/anomalies-view-page.component",
    () => ({
        AnomaliesViewPage: jest.fn().mockReturnValue("testAnomaliesViewPage"),
    })
);

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest.fn().mockReturnValue("testPageNotFoundPage"),
    })
);

describe("Anomalies Router", () => {
    it("should have rendered loading indicator while loading", () => {
        render(
            <MemoryRouter>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        expect(AppLoadingIndicatorV1).toHaveBeenCalled();
    });

    it("should render anomalies all page at exact anomalies path", async () => {
        render(
            <MemoryRouter initialEntries={[`/`]}>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAnomaliesAllPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid anomalies path (no path after an id of an anomaly)", async () => {
        render(
            <MemoryRouter initialEntries={[`/1234/testPath`]}>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render anomalies all page at exact anomalies all path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`/${AppRouteRelative.ANOMALIES_ALL}`]}
            >
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAnomaliesAllPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid anomalies all path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.ANOMALIES_ALL}/testPath`]}
            >
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render anomalies view page at exact anomalies view path", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `/${AppRouteRelative.ANOMALIES_ANOMALY}/${AppRouteRelative.ANOMALIES_ANOMALY_VIEW}?startTime=1&endTime=2&timeRange=CUSTOM`,
                ]}
            >
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAnomaliesViewPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid anomalies view path", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `/${AppRouteRelative.ANOMALIES_ANOMALY}/${AppRouteRelative.ANOMALIES_ANOMALY_VIEW}/testPath`,
                ]}
            >
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found for path after all", async () => {
        render(
            <MemoryRouter initialEntries={["/all/testPath"]}>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });
});
