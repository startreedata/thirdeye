import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { AppRoute } from "../../utils/routes/routes.util";
import { GeneralAuthenticatedRouter } from "./general-authenticated.router";

jest.mock("../../components/app-breadcrumbs/app-breadcrumbs.component", () => ({
    useAppBreadcrumbs: jest.fn().mockImplementation(() => ({
        setRouterBreadcrumbs: mockSetRouterBreadcrumbs,
    })),
}));

jest.mock(
    "../../components/loading-indicator/loading-indicator.component",
    () => ({
        LoadingIndicator: jest.fn().mockReturnValue(<>testLoadingIndicator</>),
    })
);

jest.mock("../../pages/home-page/home-page.component", () => ({
    HomePage: jest.fn().mockReturnValue(<>testHomePage</>),
}));

jest.mock("../../pages/sign-out-page/sign-out-page.component", () => ({
    SignOutPage: jest.fn().mockReturnValue(<>testSignOutPage</>),
}));

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest.fn().mockReturnValue(<>testPageNotFoundPage</>),
    })
);

describe("General Authenticated Router", () => {
    test("should have rendered loading indicator while loading", () => {
        render(
            <MemoryRouter>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(LoadingIndicator).toHaveBeenCalled();
    });

    test("should set appropriate router breadcrumbs", () => {
        render(
            <MemoryRouter>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(mockSetRouterBreadcrumbs).toHaveBeenCalledWith([]);
    });

    test("should render home page at exact base path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.BASE]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testHomePage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page at invalid base path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.BASE}/testPath`]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render home page at exact home path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.HOME]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testHomePage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page at invalid home path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.HOME}/testPath`]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render home page at exact sign in path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.SIGN_IN]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testHomePage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page at invalid sign in path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.SIGN_IN}/testPath`]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render sign out page at exact sign out path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.SIGN_OUT]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSignOutPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page at invalid sign out path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.SIGN_OUT}/testPath`]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page at any other path", async () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render home page by default", async () => {
        render(
            <MemoryRouter>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testHomePage")
        ).resolves.toBeInTheDocument();
    });
});

const mockSetRouterBreadcrumbs = jest.fn();
