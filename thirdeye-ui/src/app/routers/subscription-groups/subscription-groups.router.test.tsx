import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components/app-loading-indicator-v1/app-loading-indicator-v1.component";
import { AppRoute } from "../../utils/routes/routes.util";
import { SubscriptionGroupsRouter } from "./subscription-groups.router";

jest.mock(
    "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component",
    () => ({
        useAppBreadcrumbs: jest.fn().mockImplementation(() => ({
            setRouterBreadcrumbs: mockSetRouterBreadcrumbs,
        })),
    })
);

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
    getConfigurationPath: jest.fn().mockReturnValue("testConfigurationPath"),
    getSubscriptionGroupsPath: jest
        .fn()
        .mockReturnValue("testSubscriptionGroupsPath"),
}));

jest.mock("@startree-ui/platform-ui", () => ({
    AppLoadingIndicatorV1: jest.fn().mockReturnValue("testLoadingIndicatorV1"),
}));

jest.mock(
    "../../pages/subscription-groups-all-page/subscription-groups-all-page.component",
    () => ({
        SubscriptionGroupsAllPage: jest
            .fn()
            .mockReturnValue("testSubscriptionGroupsAllPage"),
    })
);

jest.mock(
    "../../pages/subscription-groups-view-page/subscription-groups-view-page.component",
    () => ({
        SubscriptionGroupsViewPage: jest
            .fn()
            .mockReturnValue("testSubscriptionGroupsViewPage"),
    })
);

jest.mock(
    "../../pages/subscription-groups-create-page/subscription-groups-create-page.component",
    () => ({
        SubscriptionGroupsCreatePage: jest
            .fn()
            .mockReturnValue("testSubscriptionGroupsCreatePage"),
    })
);

jest.mock(
    "../../pages/subscription-groups-update-page/subscription-groups-update-page.component",
    () => ({
        SubscriptionGroupsUpdatePage: jest
            .fn()
            .mockReturnValue("testSubscriptionGroupsUpdatePage"),
    })
);

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest.fn().mockReturnValue("testPageNotFoundPage"),
    })
);

describe("Subscription Groups Router", () => {
    it("should have rendered loading indicator while loading", () => {
        render(
            <MemoryRouter>
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        expect(AppLoadingIndicatorV1).toHaveBeenCalled();
    });

    it("should set appropriate router breadcrumbs", () => {
        render(
            <MemoryRouter>
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        expect(mockSetRouterBreadcrumbs).toHaveBeenCalled();

        // Get router breadcrumbs
        const breadcrumbs = mockSetRouterBreadcrumbs.mock.calls[0][0];
        // Also invoke the click handlers
        breadcrumbs &&
            breadcrumbs[0] &&
            breadcrumbs[0].onClick &&
            breadcrumbs[0].onClick();
        breadcrumbs &&
            breadcrumbs[1] &&
            breadcrumbs[1].onClick &&
            breadcrumbs[1].onClick();

        expect(breadcrumbs).toHaveLength(2);
        expect(breadcrumbs[0].text).toEqual("label.configuration");
        expect(breadcrumbs[0].onClick).toBeDefined();
        expect(mockPush).toHaveBeenNthCalledWith(1, "testConfigurationPath");
        expect(breadcrumbs[1].text).toEqual("label.subscription-groups");
        expect(breadcrumbs[1].onClick).toBeDefined();
        expect(mockPush).toHaveBeenNthCalledWith(
            2,
            "testSubscriptionGroupsPath"
        );
    });

    it("should render subscription groups all page at exact subscription groups path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.SUBSCRIPTION_GROUPS]}>
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSubscriptionGroupsAllPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid subscription groups path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.SUBSCRIPTION_GROUPS}/testPath`]}
            >
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render subscription groups all page at exact subscription groups all path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.SUBSCRIPTION_GROUPS_ALL]}>
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSubscriptionGroupsAllPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid subscription groups all path", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `${AppRoute.SUBSCRIPTION_GROUPS_ALL}/testPath`,
                ]}
            >
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render subscription groups view page at exact subscription groups view path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.SUBSCRIPTION_GROUPS_VIEW]}>
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSubscriptionGroupsViewPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid subscription groups view path", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `${AppRoute.SUBSCRIPTION_GROUPS_VIEW}/testPath`,
                ]}
            >
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render subscription groups create page at exact subscription groups create path", async () => {
        render(
            <MemoryRouter
                initialEntries={[AppRoute.SUBSCRIPTION_GROUPS_CREATE]}
            >
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSubscriptionGroupsCreatePage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid subscription groups create path", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `${AppRoute.SUBSCRIPTION_GROUPS_CREATE}/testPath`,
                ]}
            >
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render subscription groups update page at exact subscription groups update path", async () => {
        render(
            <MemoryRouter
                initialEntries={[AppRoute.SUBSCRIPTION_GROUPS_UPDATE]}
            >
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSubscriptionGroupsUpdatePage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid subscription groups update path", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `${AppRoute.SUBSCRIPTION_GROUPS_UPDATE}/testPath`,
                ]}
            >
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at any other path", async () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page by default", async () => {
        render(
            <MemoryRouter>
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });
});

const mockSetRouterBreadcrumbs = jest.fn();

const mockPush = jest.fn();
