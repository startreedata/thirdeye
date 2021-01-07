import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { PageContainer } from "../../components/page-container/page-container.component";
import {
    AppRoute,
    getConfigurationPath,
    getSubscriptionGroupsPath,
} from "../../utils/routes-util/routes-util";
import { SubscriptionGroupsRouter } from "./subscription-groups-router";

jest.mock("../../store/app-breadcrumbs-store/app-breadcrumbs-store", () => ({
    useAppBreadcrumbsStore: jest.fn().mockImplementation((selector) => {
        return selector({
            setAppSectionBreadcrumbs: mockSetAppSectionBreadcrumbs,
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

jest.mock("../../components/page-container/page-container.component", () => ({
    PageContainer: jest.fn().mockImplementation(() => <>testPageContainer</>),
}));

jest.mock(
    "../../pages/subscription-groups-all-page/subscription-groups-all-page.component",
    () => ({
        SubscriptionGroupsAllPage: jest
            .fn()
            .mockImplementation(() => <>testSubscriptionGroupsAllPage</>),
    })
);

jest.mock(
    "../../pages/subscription-groups-detail-page/subscription-groups-detail-page.component",
    () => ({
        SubscriptionGroupsDetailPage: jest
            .fn()
            .mockImplementation(() => <>testSubscriptionGroupsDetailPage</>),
    })
);

jest.mock(
    "../../pages/subscription-groups-create-page/subscription-groups-create-page.component",
    () => ({
        SubscriptionGroupsCreatePage: jest
            .fn()
            .mockImplementation(() => <>testSubscriptionGroupsCreatePage</>),
    })
);

jest.mock(
    "../../pages/subscription-groups-update-page/subscription-groups-update-page.component",
    () => ({
        SubscriptionGroupsUpdatePage: jest
            .fn()
            .mockImplementation(() => <>testSubscriptionGroupsUpdatePage</>),
    })
);

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest
            .fn()
            .mockImplementation(() => <>testPageNotFoundPage</>),
    })
);

describe("Subscription Groups Router", () => {
    test("should have rendered page container while loading", () => {
        render(
            <MemoryRouter>
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        expect(PageContainer).toHaveBeenCalled();
    });

    test("should set appropriate app section breadcrumbs", () => {
        render(
            <MemoryRouter>
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        expect(mockSetAppSectionBreadcrumbs).toHaveBeenCalledWith([
            {
                text: "label.configuration",
                pathFn: getConfigurationPath,
            },
            {
                text: "label.subscription-groups",
                pathFn: getSubscriptionGroupsPath,
            },
        ]);
    });

    test("should render subscription groups all page at exact subscription groups path", () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.SUBSCRIPTION_GROUPS]}>
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        expect(
            screen.getByText("testSubscriptionGroupsAllPage")
        ).toBeInTheDocument();
    });

    test("should render page not found page at invalid subscription groups path", () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.SUBSCRIPTION_GROUPS}/testPath`]}
            >
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testPageNotFoundPage")).toBeInTheDocument();
    });

    test("should render subscription groups all page at exact subscription groups all path", () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.SUBSCRIPTION_GROUPS_ALL]}>
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        expect(
            screen.getByText("testSubscriptionGroupsAllPage")
        ).toBeInTheDocument();
    });

    test("should render page not found page at invalid subscription groups all path", () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `${AppRoute.SUBSCRIPTION_GROUPS_ALL}/testPath`,
                ]}
            >
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testPageNotFoundPage")).toBeInTheDocument();
    });

    test("should render subscription groups detail page at exact subscription groups detail path", () => {
        render(
            <MemoryRouter
                initialEntries={[AppRoute.SUBSCRIPTION_GROUPS_DETAIL]}
            >
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        expect(
            screen.getByText("testSubscriptionGroupsDetailPage")
        ).toBeInTheDocument();
    });

    test("should render page not found page at invalid subscription groups detail path", () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `${AppRoute.SUBSCRIPTION_GROUPS_DETAIL}/testPath`,
                ]}
            >
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testPageNotFoundPage")).toBeInTheDocument();
    });

    test("should render subscription groups create page at exact subscription groups create path", () => {
        render(
            <MemoryRouter
                initialEntries={[AppRoute.SUBSCRIPTION_GROUPS_CREATE]}
            >
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        expect(
            screen.getByText("testSubscriptionGroupsCreatePage")
        ).toBeInTheDocument();
    });

    test("should render page not found page at invalid subscription groups create path", () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `${AppRoute.SUBSCRIPTION_GROUPS_CREATE}/testPath`,
                ]}
            >
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testPageNotFoundPage")).toBeInTheDocument();
    });

    test("should render subscription groups update page at exact subscription groups update path", () => {
        render(
            <MemoryRouter
                initialEntries={[AppRoute.SUBSCRIPTION_GROUPS_UPDATE]}
            >
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        expect(
            screen.getByText("testSubscriptionGroupsUpdatePage")
        ).toBeInTheDocument();
    });

    test("should render page not found page at invalid subscription groups update path", () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `${AppRoute.SUBSCRIPTION_GROUPS_UPDATE}/testPath`,
                ]}
            >
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testPageNotFoundPage")).toBeInTheDocument();
    });

    test("should render page not found page at any other path", () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testPageNotFoundPage")).toBeInTheDocument();
    });

    test("should render page not found page by default", () => {
        render(
            <MemoryRouter>
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testPageNotFoundPage")).toBeInTheDocument();
    });
});

const mockSetAppSectionBreadcrumbs = jest.fn();
