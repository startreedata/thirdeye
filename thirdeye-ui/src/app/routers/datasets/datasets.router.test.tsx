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
import { MemoryRouter } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components/app-loading-indicator-v1/app-loading-indicator-v1.component";
import { AppRouteRelative } from "../../utils/routes/routes.util";
import { DatasetsRouter } from "./datasets.router";

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
    getDatasetsPath: jest.fn().mockReturnValue("testDatasetsPath"),
}));

jest.mock(
    "../../platform/components/app-loading-indicator-v1/app-loading-indicator-v1.component",
    () => ({
        AppLoadingIndicatorV1: jest
            .fn()
            .mockReturnValue("testLoadingIndicatorV1"),
    })
);

jest.mock("../../pages/datasets-all-page/datasets-all-page.component", () => ({
    DatasetsAllPage: jest.fn().mockReturnValue("testDatasetsAllPage"),
}));

jest.mock(
    "../../pages/datasets-view-page/datasets-view-page.component",
    () => ({
        DatasetsViewPage: jest.fn().mockReturnValue("testDatasetsViewPage"),
    })
);

jest.mock(
    "../../pages/datasets-onboard-page/datasets-onboard-page.component",
    () => ({
        DatasetsOnboardPage: jest
            .fn()
            .mockReturnValue("testDatasetsOnboardPage"),
    })
);

jest.mock(
    "../../pages/datasets-update-page/datasets-update-page.component",
    () => ({
        DatasetsUpdatePage: jest.fn().mockReturnValue("testDatasetsUpdatePage"),
    })
);

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest.fn().mockReturnValue("testPageNotFoundPage"),
    })
);

describe("Datasets Router", () => {
    it("should have rendered loading indicator while loading", () => {
        render(
            <MemoryRouter>
                <DatasetsRouter />
            </MemoryRouter>
        );

        expect(AppLoadingIndicatorV1).toHaveBeenCalled();
    });

    it("should render datasets all page at exact datasets path", async () => {
        render(
            <MemoryRouter>
                <DatasetsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testDatasetsAllPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid datasets path", async () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <DatasetsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render datasets all page at exact datasets all path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`/${AppRouteRelative.DATASETS_ALL}`]}
            >
                <DatasetsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testDatasetsAllPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid datasets all path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`/${AppRouteRelative.DATASETS_ALL}/testPath`]}
            >
                <DatasetsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render datasets view page at exact datasets view path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`/${AppRouteRelative.DATASETS_VIEW}`]}
            >
                <DatasetsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testDatasetsViewPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid datasets view path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`/${AppRouteRelative.DATASETS_VIEW}/testPath`]}
            >
                <DatasetsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render datasets onboard page at exact datasets onboard path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`/${AppRouteRelative.DATASETS_ONBOARD}`]}
            >
                <DatasetsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testDatasetsOnboardPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid datasets onboard path", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `/${AppRouteRelative.DATASETS_ONBOARD}/testPath`,
                ]}
            >
                <DatasetsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render datasets update page at exact datasets update path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`/${AppRouteRelative.DATASETS_UPDATE}`]}
            >
                <DatasetsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testDatasetsUpdatePage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid datasets update path", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `/${AppRouteRelative.DATASETS_UPDATE}/testPath`,
                ]}
            >
                <DatasetsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at any other path", async () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <DatasetsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });
});

const mockNavigate = jest.fn();
