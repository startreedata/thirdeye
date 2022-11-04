// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components/app-loading-indicator-v1/app-loading-indicator-v1.component";
import { AppRoute, AppRouteRelative } from "../../utils/routes/routes.util";
import { DatasourcesRouter } from "./datasources.router";

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
    getDatasourcesPath: jest.fn().mockReturnValue("testDatasourcesPath"),
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
    "../../pages/datasources-all-page/datasources-all-page.component",
    () => ({
        DatasourcesAllPage: jest.fn().mockReturnValue("testDatasourcesAllPage"),
    })
);

jest.mock(
    "../../pages/datasources-view-page/datasources-view-page.component",
    () => ({
        DatasourcesViewPage: jest
            .fn()
            .mockReturnValue("testDatasourcesViewPage"),
    })
);

jest.mock(
    "../../pages/datasources-create-page/datasources-create-page.component",
    () => ({
        DatasourcesCreatePage: jest
            .fn()
            .mockReturnValue("testDatasourcesCreatePage"),
    })
);

jest.mock(
    "../../pages/datasources-update-page/datasources-update-page.component",
    () => ({
        DatasourcesUpdatePage: jest
            .fn()
            .mockReturnValue("testDatasourcesUpdatePage"),
    })
);

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest.fn().mockReturnValue("testPageNotFoundPage"),
    })
);

describe("Datasources Router", () => {
    it("should have rendered loading indicator while loading", () => {
        render(
            <MemoryRouter>
                <DatasourcesRouter />
            </MemoryRouter>
        );

        expect(AppLoadingIndicatorV1).toHaveBeenCalled();
    });

    it("should render datasources all page at exact datasources path", async () => {
        render(
            <MemoryRouter initialEntries={[`/`]}>
                <DatasourcesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testDatasourcesAllPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid datasources path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`/${AppRouteRelative.DATASOURCES}/testPath`]}
            >
                <DatasourcesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render datasources all page at exact datasources all path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`/${AppRouteRelative.DATASOURCES_ALL}`]}
            >
                <DatasourcesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testDatasourcesAllPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid datasources all path", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `/${AppRouteRelative.DATASOURCES_ALL}/testPath`,
                ]}
            >
                <DatasourcesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render datasources view page at exact datasources view path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`/${AppRouteRelative.DATASOURCES_VIEW}`]}
            >
                <DatasourcesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testDatasourcesViewPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid datasources view path", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `/${AppRouteRelative.DATASOURCES_VIEW}/testPath`,
                ]}
            >
                <DatasourcesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render datasources create page at exact datasources create path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`/${AppRouteRelative.DATASOURCES_CREATE}`]}
            >
                <DatasourcesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testDatasourcesCreatePage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid datasources create path", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `/${AppRouteRelative.DATASOURCES_CREATE}/testPath`,
                ]}
            >
                <DatasourcesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render datasources update page at exact datasources update path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`/${AppRouteRelative.DATASETS_UPDATE}`]}
            >
                <DatasourcesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testDatasourcesUpdatePage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid datasources update path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.DATASOURCES_UPDATE}/testPath`]}
            >
                <DatasourcesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at any other path", async () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <DatasourcesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });
});

const mockNavigate = jest.fn();
