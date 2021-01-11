import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { AppRoute } from "../../utils/routes-util/routes-util";
import { GeneralUnauthenticatedRouter } from "./general-unauthenticated-router";

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

jest.mock("../../store/redirection-path-store/redirection-path-store", () => ({
    useRedirectionPathStore: jest.fn().mockImplementation((selector) => {
        return selector({
            setRedirectionPath: mockSetRedirectionPath,
        });
    }),
}));

jest.mock("react-router-dom", () => ({
    ...(jest.requireActual("react-router-dom") as Record<string, unknown>),
    useLocation: jest.fn().mockImplementation(() => {
        return mockLocation;
    }),
}));

jest.mock("../../utils/routes-util/routes-util", () => ({
    ...(jest.requireActual("../../utils/routes-util/routes-util") as Record<
        string,
        unknown
    >),
    createPathWithRecognizedQueryString: jest
        .fn()
        .mockImplementation((path: string): string => {
            return path;
        }),
}));

jest.mock("../../pages/sign-in-page/sign-in-page.component", () => ({
    SignInPage: jest.fn().mockImplementation(() => <>testSignInPage</>),
}));

describe("General Unauthenticated Router", () => {
    test("should set appropriate app section breadcrumbs", () => {
        render(
            <MemoryRouter>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        expect(mockSetAppSectionBreadcrumbs).toHaveBeenCalledWith([]);
    });

    test("should remove app toolbar", () => {
        render(
            <MemoryRouter>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        expect(mockRemoveAppToolbar).toHaveBeenCalled();
    });

    test("should not set redirection path if location is sign in path", () => {
        mockLocation.pathname = AppRoute.SIGN_IN;
        render(
            <MemoryRouter>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        expect(mockSetRedirectionPath).not.toHaveBeenCalled();
    });

    test("should not set redirection path if location is sign out path", () => {
        mockLocation.pathname = AppRoute.SIGN_OUT;
        render(
            <MemoryRouter>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        expect(mockSetRedirectionPath).not.toHaveBeenCalled();
    });

    test("should set redirection path if location is other than sign in/out path", () => {
        mockLocation.pathname = "testPath";
        render(
            <MemoryRouter>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        expect(mockSetRedirectionPath).toHaveBeenCalledWith("testPath");
    });

    test("should render sign in page at exact sign in path", () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.SIGN_IN]}>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testSignInPage")).toBeInTheDocument();
    });

    test("should render sign in page at invalid sign in path", () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.SIGN_IN}/testPath`]}>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testSignInPage")).toBeInTheDocument();
    });

    test("should render sign in page by default", () => {
        render(
            <MemoryRouter>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testSignInPage")).toBeInTheDocument();
    });
});

const mockSetAppSectionBreadcrumbs = jest.fn();

const mockRemoveAppToolbar = jest.fn();

const mockSetRedirectionPath = jest.fn();

const mockLocation = {
    pathname: "",
};
