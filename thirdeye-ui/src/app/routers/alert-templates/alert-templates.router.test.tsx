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
import { AppRouteRelative } from "../../utils/routes/routes.util";
import { AlertTemplatesRouter } from "./alert-templates.router";

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
    getAlertTemplatesPath: jest.fn().mockReturnValue("testAlertTemplatesPath"),
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
    "../../pages/alert-templates-all-page/alert-templates-all-page.component",
    () => ({
        AlertTemplatesAllPage: jest
            .fn()
            .mockReturnValue("testAlertTemplatesAllPage"),
    })
);

jest.mock(
    "../../pages/alert-templates-view-page/alert-templates-view-page.component",
    () => ({
        AlertTemplatesViewPage: jest
            .fn()
            .mockReturnValue("testAlertTemplatesViewPage"),
    })
);

jest.mock(
    "../../pages/alert-templates-create-page/alert-templates-create-page.component",
    () => ({
        AlertTemplatesCreatePage: jest
            .fn()
            .mockReturnValue("testAlertTemplatesCreatePage"),
    })
);

jest.mock(
    "../../pages/alert-templates-update-page/alert-templates-update-page.component",
    () => ({
        AlertTemplatesUpdatePage: jest
            .fn()
            .mockReturnValue("testAlertTemplatesUpdatePage"),
    })
);

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest.fn().mockReturnValue("testPageNotFoundPage"),
    })
);

describe("Alert Templates Router", () => {
    it("should have rendered loading indicator while loading", () => {
        render(
            <MemoryRouter>
                <AlertTemplatesRouter />
            </MemoryRouter>
        );

        expect(AppLoadingIndicatorV1).toHaveBeenCalled();
    });

    it("should render alert-templates all page at exact alert-templates path", async () => {
        render(
            <MemoryRouter initialEntries={[`/`]}>
                <AlertTemplatesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertTemplatesAllPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid alert-templates path", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `/${AppRouteRelative.ALERT_TEMPLATES}/testPath`,
                ]}
            >
                <AlertTemplatesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render alert-templates all page at exact alert-templates all path", async () => {
        render(
            <MemoryRouter initialEntries={[`/`]}>
                <AlertTemplatesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertTemplatesAllPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid alert-templates all path", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `/${AppRouteRelative.ALERT_TEMPLATES_ALL}/testPath`,
                ]}
            >
                <AlertTemplatesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render alert-templates view page at index route", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `/${AppRouteRelative.ALERT_TEMPLATES_ALERT_TEMPLATE}`,
                ]}
            >
                <AlertTemplatesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertTemplatesViewPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render alert-templates view page at exact alert-templates view path", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `/${AppRouteRelative.ALERT_TEMPLATES_ALERT_TEMPLATE_VIEW}`,
                ]}
            >
                <AlertTemplatesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertTemplatesViewPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid alert-templates view path", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `/${AppRouteRelative.ALERT_TEMPLATES_ALL}/testPath`,
                ]}
            >
                <AlertTemplatesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render alert-templates create page at exact alert-templates create path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`/${AppRouteRelative.ALERT_TEMPLATES_CREATE}`]}
            >
                <AlertTemplatesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertTemplatesCreatePage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid alert-templates create path", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `/${AppRouteRelative.ALERT_TEMPLATES_CREATE}/testPath`,
                ]}
            >
                <AlertTemplatesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render alert-templates update page at exact alert-templates update path", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `/${AppRouteRelative.ALERT_TEMPLATES_ALERT_TEMPLATE}/${AppRouteRelative.ALERT_TEMPLATES_ALERT_TEMPLATE_UPDATE}`,
                ]}
            >
                <AlertTemplatesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertTemplatesUpdatePage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid alert-templates update path", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `/${AppRouteRelative.ALERTS_ALERT}/${AppRouteRelative.ALERT_TEMPLATES_ALERT_TEMPLATE_UPDATE}/testPath`,
                ]}
            >
                <AlertTemplatesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });
});

const mockNavigate = jest.fn();
