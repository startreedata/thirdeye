import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { PageContainer } from "../../components/page-container/page-container.component";
import { AppRoute } from "../../utils/routes-util/routes-util";
import { GeneralAuthenticatedRouter } from "./general-authenticated-router";

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
            removeAppToolbar: mockRemoveAppToolbar,
        });
    }),
}));

jest.mock("../../components/page-container/page-container.component", () => ({
    PageContainer: jest.fn().mockImplementation(() => <>testPageContainer</>),
}));

jest.mock("../../pages/home-page/home-page.component", () => ({
    HomePage: jest.fn().mockImplementation(() => <>testHomePage</>),
}));

jest.mock("../../pages/sign-out-page/sign-out-page.component", () => ({
    SignOutPage: jest.fn().mockImplementation(() => <>testSignOutPage</>),
}));

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest
            .fn()
            .mockImplementation(() => <>testPageNotFoundPage</>),
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

    test("should set appropriate app section breadcrumbs", () => {
        render(
            <MemoryRouter>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(mockSetAppSectionBreadcrumbs).toHaveBeenCalledWith([]);
    });

    test("should remove app toolbar", () => {
        render(
            <MemoryRouter>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(mockRemoveAppToolbar).toHaveBeenCalled();
    });

    test("should render home page at exact base path", () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.BASE]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testHomePage")).toBeInTheDocument();
    });

    test("should render page not found page at invalid base path", () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.BASE}/testPath`]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testPageNotFoundPage")).toBeInTheDocument();
    });

    test("should render home page at exact sign in path", () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.SIGN_IN]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testHomePage")).toBeInTheDocument();
    });

    test("should render page not found page at invalid sign in path", () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.SIGN_IN}/testPath`]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testPageNotFoundPage")).toBeInTheDocument();
    });

    test("should render sign out page at exact sign out path", () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.SIGN_OUT]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testSignOutPage")).toBeInTheDocument();
    });

    test("should render page not found page at invalid sign out path", () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.SIGN_OUT}/testPath`]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testPageNotFoundPage")).toBeInTheDocument();
    });

    test("should render page not found page at any other path", () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testPageNotFoundPage")).toBeInTheDocument();
    });

    test("should render home page by default", () => {
        render(
            <MemoryRouter>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testHomePage")).toBeInTheDocument();
    });
});

const mockSetAppSectionBreadcrumbs = jest.fn();

const mockRemoveAppToolbar = jest.fn();
