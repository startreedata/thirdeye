import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { Breadcrumb } from "../../components/breadcrumbs/breadcrumbs.interfaces";
import { PageContainer } from "../../components/page-container/page-container.component";
import { AppRoute } from "../../utils/routes-util/routes-util";
import { AlertsRouter } from "./alerts-router";

jest.mock("../../components/app-breadcrumbs/app-breadcrumbs.component", () => ({
    useAppBreadcrumbs: jest.fn().mockImplementation(() => ({
        setRouterBreadcrumbs: mockSetRouterBreadcrumbs,
    })),
}));

jest.mock("react-router-dom", () => ({
    ...(jest.requireActual("react-router-dom") as Record<string, unknown>),
    useHistory: jest.fn().mockImplementation(() => ({
        push: mockPush,
    })),
}));

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string): string => {
            return key;
        },
    }),
}));

jest.mock("../../utils/routes-util/routes-util", () => ({
    ...(jest.requireActual("../../utils/routes-util/routes-util") as Record<
        string,
        unknown
    >),
    getAlertsPath: jest.fn().mockReturnValue("testAlertsPath"),
}));

jest.mock("../../components/page-container/page-container.component", () => ({
    PageContainer: jest.fn().mockReturnValue(<>testPageContainer</>),
}));

jest.mock("../../pages/alerts-all-page/alerts-all-page.component", () => ({
    AlertsAllPage: jest.fn().mockReturnValue(<>testAlertsAllPage</>),
}));

jest.mock(
    "../../pages/alerts-detail-page/alerts-detail-page.component",
    () => ({
        AlertsDetailPage: jest.fn().mockReturnValue(<>testAlertsDetailPage</>),
    })
);

jest.mock(
    "../../pages/alerts-create-page/alerts-create-page.component",
    () => ({
        AlertsCreatePage: jest.fn().mockReturnValue(<>testAlertsCreatePage</>),
    })
);

jest.mock(
    "../../pages/alerts-update-page/alerts-update-page.component",
    () => ({
        AlertsUpdatePage: jest.fn().mockReturnValue(<>testAlertsUpdatePage</>),
    })
);

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest.fn().mockReturnValue(<>testPageNotFoundPage</>),
    })
);

describe("Alerts Router", () => {
    test("should have rendered page container while loading", () => {
        render(
            <MemoryRouter>
                <AlertsRouter />
            </MemoryRouter>
        );

        expect(PageContainer).toHaveBeenCalled();
    });

    test("should set appropriate router breadcrumbs", () => {
        render(
            <MemoryRouter>
                <AlertsRouter />
            </MemoryRouter>
        );

        expect(mockSetRouterBreadcrumbs).toHaveBeenCalled();

        // Get router breadcrumbs
        const breadcrumbs: Breadcrumb[] =
            mockSetRouterBreadcrumbs.mock.calls[0][0];
        // Also invoke the click handlers
        breadcrumbs &&
            breadcrumbs[0] &&
            breadcrumbs[0].onClick &&
            breadcrumbs[0].onClick();

        expect(breadcrumbs).toHaveLength(1);
        expect(breadcrumbs[0].text).toEqual("label.alerts");
        expect(breadcrumbs[0].onClick).toBeDefined();
        expect(mockPush).toHaveBeenCalledWith("testAlertsPath");
    });

    test("should render alerts all page at exact alerts path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ALERTS]}>
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsAllPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page at invalid alerts path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ALERTS}/testPath`]}>
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render alerts all page at exact alerts all path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ALERTS_ALL]}>
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsAllPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page at invalid alerts all path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ALERTS_ALL}/testPath`]}>
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render alerts detail page at exact alerts detail path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ALERTS_DETAIL]}>
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsDetailPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page at invalid alerts detail path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.ALERTS_DETAIL}/testPath`]}
            >
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render alerts create page at exact alerts create path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ALERTS_CREATE]}>
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsCreatePage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page at invalid alerts create path", async () => {
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

    test("should render alerts update page at exact alerts update path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ALERTS_UPDATE]}>
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsUpdatePage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page at invalid alerts update path", async () => {
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

    test("should render page not found page at any other path", async () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <AlertsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page by default", async () => {
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

const mockSetRouterBreadcrumbs = jest.fn();

const mockPush = jest.fn();
