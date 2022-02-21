import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components/app-loading-indicator-v1/app-loading-indicator-v1.component";
import { AppRoute } from "../../utils/routes/routes.util";
import { AlertsRouter } from "./alerts.router";

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
    "../../pages/alerts-create-page/alerts-create-page.component",
    () => ({
        AlertsCreatePage: jest.fn().mockReturnValue("testAlertsCreatePage"),
    })
);

jest.mock(
    "../../pages/alerts-update-page/alerts-update-page.component",
    () => ({
        AlertsUpdatePage: jest.fn().mockReturnValue("testAlertsUpdatePage"),
    })
);

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest.fn().mockReturnValue("testPageNotFoundPage"),
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
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsAllPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid alerts path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ALERTS}/testPath`]}>
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render alerts all page at exact alerts all path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ALERTS_ALL]}>
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsAllPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid alerts all path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ALERTS_ALL}/testPath`]}>
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render alerts view page at exact alerts view path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ALERTS_VIEW]}>
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsViewPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid alerts view path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ALERTS_VIEW}/testPath`]}>
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render alerts create page at exact alerts create path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ALERTS_CREATE]}>
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsCreatePage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid alerts create path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.ALERTS_CREATE}/testPath`]}
            >
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render alerts update page at exact alerts update path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ALERTS_UPDATE]}>
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsUpdatePage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid alerts update path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.ALERTS_UPDATE}/testPath`]}
            >
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at any other path", async () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page by default", async () => {
        render(
            <MemoryRouter>
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });
});

const mockPush = jest.fn();
