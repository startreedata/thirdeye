import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { AppToolbarConfiguration } from "../../components/app-toolbar-configuration/app-toolbar-configuration.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import {
    AppRoute,
    getConfigurationPath,
} from "../../utils/routes-util/routes-util";
import { ConfigurationRouter } from "./configuration-router";

jest.mock("../../store/app-breadcrumbs-store/app-breadcrumbs-store", () => ({
    useAppBreadcrumbsStore: jest.fn().mockImplementation((selector) => {
        return selector({
            setAppSectionBreadcrumbs: mockSetAppSectionBreadcrumbs,
        });
    }),
}));

jest.mock("../../store/app-toolbar-store/app-toolbar-store", () => ({
    useAppToolbarStore: jest.fn().mockImplementation((selector) => {
        return selector({
            setAppToolbar: mockSetAppToolbar,
        });
    }),
}));

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string): string => {
            return key;
        },
    }),
}));

jest.mock(
    "../../components/app-toolbar-configuration/app-toolbar-configuration.component",
    () => ({
        AppToolbarConfiguration: jest
            .fn()
            .mockImplementation(() => <>testAppToolbarConfiguration</>),
    })
);

jest.mock("../../components/page-container/page-container.component", () => ({
    PageContainer: jest.fn().mockImplementation(() => <>testPageContainer</>),
}));

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
    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.resetAllMocks();
    });

    test("should have rendered page container while loading", () => {
        render(
            <MemoryRouter>
                <ConfigurationRouter />
            </MemoryRouter>
        );

        expect(PageContainer).toHaveBeenCalled();
    });

    test("should set appropriate app section breadcrumbs", () => {
        render(
            <MemoryRouter>
                <ConfigurationRouter />
            </MemoryRouter>
        );

        expect(mockSetAppSectionBreadcrumbs).toHaveBeenCalledWith([
            {
                text: "label.configuration",
                pathFn: getConfigurationPath,
            },
        ]);
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

const mockSetAppSectionBreadcrumbs = jest.fn();

const mockSetAppToolbar = jest.fn();
