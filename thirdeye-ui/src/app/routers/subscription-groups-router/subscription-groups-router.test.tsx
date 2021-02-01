import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { AppRoute } from "../../utils/routes-util/routes-util";
import { SubscriptionGroupsRouter } from "./subscription-groups-router";

jest.mock(
    "../../pages/subscription-groups-all-page/subscription-groups-all-page.component",
    () => ({
        SubscriptionGroupsAllPage: jest
            .fn()
            .mockReturnValue(<>testSubscriptionGroupsAllPage</>),
    })
);

jest.mock(
    "../../pages/subscription-groups-detail-page/subscription-groups-detail-page.component",
    () => ({
        SubscriptionGroupsDetailPage: jest
            .fn()
            .mockReturnValue(<>testSubscriptionGroupsDetailPage</>),
    })
);

jest.mock(
    "../../pages/subscription-groups-create-page/subscription-groups-create-page.component",
    () => ({
        SubscriptionGroupsCreatePage: jest
            .fn()
            .mockReturnValue(<>testSubscriptionGroupsCreatePage</>),
    })
);

jest.mock(
    "../../pages/subscription-groups-update-page/subscription-groups-update-page.component",
    () => ({
        SubscriptionGroupsUpdatePage: jest
            .fn()
            .mockReturnValue(<>testSubscriptionGroupsUpdatePage</>),
    })
);

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest.fn().mockReturnValue(<>testPageNotFoundPage</>),
    })
);

describe("Subscription Groups Router", () => {
    test("should render subscription groups all page at exact subscription groups path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.SUBSCRIPTION_GROUPS]}>
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSubscriptionGroupsAllPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page at invalid subscription groups path", async () => {
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

    test("should render subscription groups all page at exact subscription groups all path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.SUBSCRIPTION_GROUPS_ALL]}>
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSubscriptionGroupsAllPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page at invalid subscription groups all path", async () => {
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

    test("should render subscription groups detail page at exact subscription groups detail path", async () => {
        render(
            <MemoryRouter
                initialEntries={[AppRoute.SUBSCRIPTION_GROUPS_DETAIL]}
            >
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSubscriptionGroupsDetailPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page at invalid subscription groups detail path", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `${AppRoute.SUBSCRIPTION_GROUPS_DETAIL}/testPath`,
                ]}
            >
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render subscription groups create page at exact subscription groups create path", async () => {
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

    test("should render page not found page at invalid subscription groups create path", async () => {
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

    test("should render subscription groups update page at exact subscription groups update path", async () => {
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

    test("should render page not found page at invalid subscription groups update path", async () => {
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

    test("should render page not found page at any other path", async () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <SubscriptionGroupsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page by default", async () => {
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
