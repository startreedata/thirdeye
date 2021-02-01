import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { Breadcrumb } from "../../components/breadcrumbs/breadcrumbs.interfaces";
import { PageContainer } from "../../components/page-container/page-container.component";
import { AppRoute } from "../../utils/routes-util/routes-util";
import { ConfigurationRouter } from "./configuration-router";

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
    getConfigurationPath: jest.fn().mockReturnValue("testConfigurationPath"),
}));

jest.mock("../../components/page-container/page-container.component", () => ({
    PageContainer: jest.fn().mockReturnValue(<>testPageContainer</>),
}));

jest.mock(
    "../../pages/configuration-page/configuration-page.component",
    () => ({
        ConfigurationPage: jest
            .fn()
            .mockReturnValue(<>testConfigurationPage</>),
    })
);

jest.mock("../subscription-groups-router/subscription-groups-router", () => ({
    SubscriptionGroupsRouter: jest
        .fn()
        .mockReturnValue(<>testSubscriptionGroupsRouter</>),
}));

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest.fn().mockReturnValue(<>testPageNotFoundPage</>),
    })
);

describe("Configuration Router", () => {
    test("should have rendered page container while loading", () => {
        render(
            <MemoryRouter>
                <ConfigurationRouter />
            </MemoryRouter>
        );

        expect(PageContainer).toHaveBeenCalled();
    });

    test("should set appropriate router breadcrumbs", () => {
        render(
            <MemoryRouter>
                <ConfigurationRouter />
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
        expect(breadcrumbs[0].text).toEqual("label.configuration");
        expect(breadcrumbs[0].onClick).toBeDefined();
        expect(mockPush).toHaveBeenCalledWith("testConfigurationPath");
    });

    test("should render configuration page at exact configuration path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.CONFIGURATION]}>
                <ConfigurationRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testConfigurationPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page at invalid configuration path", async () => {
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

    test("should direct exact subscription groups path to subscription groups router", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.SUBSCRIPTION_GROUPS]}>
                <ConfigurationRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSubscriptionGroupsRouter")
        ).resolves.toBeInTheDocument();
    });

    test("should direct subscription groups path to subscription groups router", async () => {
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

    test("should render page not found page at any other path", async () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <ConfigurationRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page by default", async () => {
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
