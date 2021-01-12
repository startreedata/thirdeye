import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { AppToolbarConfiguration } from "../../components/app-toolbar-configuration/app-toolbar-configuration.component";
import { Breadcrumb } from "../../components/breadcrumbs/breadcrumbs.interfaces";
import { AppRoute } from "../../utils/routes-util/routes-util";
import { ConfigurationRouter } from "./configuration-router";

jest.mock("../../components/app-breadcrumbs/app-breadcrumbs.component", () => ({
    useAppBreadcrumbs: jest.fn().mockImplementation(() => ({
        setRouterBreadcrumbs: mockSetRouterBreadcrumbs,
    })),
}));

jest.mock("../../store/app-toolbar-store/app-toolbar-store", () => ({
    useAppToolbarStore: jest.fn().mockImplementation((selector) => {
        return selector({
            setAppToolbar: mockSetAppToolbar,
        });
    }),
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

jest.mock(
    "../../components/app-toolbar-configuration/app-toolbar-configuration.component",
    () => ({
        AppToolbarConfiguration: jest
            .fn()
            .mockImplementation(() => <>testAppToolbarConfiguration</>),
    })
);

jest.mock("../subscription-groups-router/subscription-groups-router", () => ({
    SubscriptionGroupsRouter: jest
        .fn()
        .mockImplementation(() => <>testSubscriptionGroupsRouter</>),
}));

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest
            .fn()
            .mockImplementation(() => <>testPageNotFoundPage</>),
    })
);

describe("Configuration Router", () => {
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

    test("should set appropriate app toolbar", () => {
        render(
            <MemoryRouter>
                <ConfigurationRouter />
            </MemoryRouter>
        );

        expect(mockSetAppToolbar).toHaveBeenCalledWith(
            <AppToolbarConfiguration />
        );
    });

    test("should direct exact configuration path to subscription groups router", () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.CONFIGURATION]}>
                <ConfigurationRouter />
            </MemoryRouter>
        );

        expect(
            screen.getByText("testSubscriptionGroupsRouter")
        ).toBeInTheDocument();
    });

    test("should render page not found page at invalid configuration path", () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.CONFIGURATION}/testPath`]}
            >
                <ConfigurationRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testPageNotFoundPage")).toBeInTheDocument();
    });

    test("should direct exact subscription groups path to subscription groups router", () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.SUBSCRIPTION_GROUPS]}>
                <ConfigurationRouter />
            </MemoryRouter>
        );

        expect(
            screen.getByText("testSubscriptionGroupsRouter")
        ).toBeInTheDocument();
    });

    test("should direct subscription groups path to subscription groups router", () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.SUBSCRIPTION_GROUPS}/testPath`]}
            >
                <ConfigurationRouter />
            </MemoryRouter>
        );

        expect(
            screen.getByText("testSubscriptionGroupsRouter")
        ).toBeInTheDocument();
    });

    test("should render page not found page at any other path", () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <ConfigurationRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testPageNotFoundPage")).toBeInTheDocument();
    });

    test("should render page not found page by default", () => {
        render(
            <MemoryRouter>
                <ConfigurationRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testPageNotFoundPage")).toBeInTheDocument();
    });
});

const mockSetRouterBreadcrumbs = jest.fn();

const mockSetAppToolbar = jest.fn();

const mockPush = jest.fn();
