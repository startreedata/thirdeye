import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { PageContainer } from "../../components/page-container/page-container.component";
import { AppRoute } from "../../utils/routes-util/routes-util";
import { GeneralAuthenticatedRouter } from "./general-authenticated-router";

jest.mock("../../components/app-breadcrumbs/app-breadcrumbs.component", () => ({
    useAppBreadcrumbs: jest.fn().mockImplementation(() => ({
        setRouterBreadcrumbs: mockSetRouterBreadcrumbs,
    })),
}));

jest.mock("../../store/app-toolbar-store/app-toolbar-store", () => ({
    useAppToolbarStore: jest.fn().mockImplementation((selector) => {
        return selector({
            removeAppToolbar: mockRemoveAppToolbar,
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
    PageContainer: jest.fn().mockReturnValue(<>testPageContainer</>),
}));

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
    test("should have rendered page container while loading", () => {
        render(
            <MemoryRouter>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(PageContainer).toHaveBeenCalled();
    });

    test("should set appropriate router breadcrumbs", () => {
        render(
            <MemoryRouter>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(mockSetRouterBreadcrumbs).toHaveBeenCalledWith([]);
    });

    test("should remove app toolbar", () => {
        render(
            <MemoryRouter>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(mockRemoveAppToolbar).toHaveBeenCalled();
    });

    test("should render home page at exact base path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.BASE]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(await screen.findByText("testHomePage")).toBeInTheDocument();
    });

    test("should render page not found page at invalid base path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.BASE}/testPath`]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(
            await screen.findByText("testPageNotFoundPage")
        ).toBeInTheDocument();
    });

    test("should render home page at exact sign in path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.SIGN_IN]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(await screen.findByText("testHomePage")).toBeInTheDocument();
    });

    test("should render page not found page at invalid sign in path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.SIGN_IN}/testPath`]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(
            await screen.findByText("testPageNotFoundPage")
        ).toBeInTheDocument();
    });

    test("should render sign out page at exact sign out path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.SIGN_OUT]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(await screen.findByText("testSignOutPage")).toBeInTheDocument();
    });

    test("should render page not found page at invalid sign out path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.SIGN_OUT}/testPath`]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(
            await screen.findByText("testPageNotFoundPage")
        ).toBeInTheDocument();
    });

    test("should render page not found page at any other path", async () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(
            await screen.findByText("testPageNotFoundPage")
        ).toBeInTheDocument();
    });

    test("should render home page by default", async () => {
        render(
            <MemoryRouter>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(await screen.findByText("testHomePage")).toBeInTheDocument();
    });
});

const mockSetRouterBreadcrumbs = jest.fn();

const mockRemoveAppToolbar = jest.fn();
