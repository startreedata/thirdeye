import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { PageContainer } from "../../components/page-container/page-container.component";
import { SignInPageProps } from "../../pages/sign-in-page/sign-in-page.interfaces";
import { AppRoute } from "../../utils/routes-util/routes-util";
import { GeneralUnauthenticatedRouter } from "./general-unauthenticated-router";

jest.mock("../../components/app-breadcrumbs/app-breadcrumbs.component", () => ({
    useAppBreadcrumbs: jest.fn().mockImplementation(() => ({
        setRouterBreadcrumbs: mockSetRouterBreadcrumbs,
    })),
}));

jest.mock("react-router-dom", () => ({
    ...(jest.requireActual("react-router-dom") as Record<string, unknown>),
    useLocation: jest.fn().mockImplementation(() => ({
        pathname: mockPathname,
    })),
}));

jest.mock("../../utils/routes-util/routes-util", () => ({
    ...(jest.requireActual("../../utils/routes-util/routes-util") as Record<
        string,
        unknown
    >),
    getBasePath: jest.fn().mockReturnValue("testBasePath"),
    createPathWithRecognizedQueryString: jest
        .fn()
        .mockImplementation((path: string): string => {
            return path;
        }),
}));

jest.mock("../../components/page-container/page-container.component", () => ({
    PageContainer: jest.fn().mockReturnValue(<>testPageContainer</>),
}));

jest.mock("../../pages/sign-in-page/sign-in-page.component", () => ({
    SignInPage: jest
        .fn()
        .mockImplementation((props: SignInPageProps) => (
            <>{`testSignInPage:${props.redirectURL}`}</>
        )),
}));

describe("General Unauthenticated Router", () => {
    test("should have rendered page container while loading", () => {
        render(
            <MemoryRouter>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        expect(PageContainer).toHaveBeenCalled();
    });

    test("should set appropriate router breadcrumbs", () => {
        mockPathname = "";
        render(
            <MemoryRouter>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        expect(mockSetRouterBreadcrumbs).toHaveBeenCalledWith([]);
    });

    test("should render sign in page at exact sign in path", async () => {
        mockPathname = "";
        render(
            <MemoryRouter initialEntries={[AppRoute.SIGN_IN]}>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSignInPage:")
        ).resolves.toBeInTheDocument();
    });

    test("should render sign in page at invalid sign in path", async () => {
        mockPathname = "";
        render(
            <MemoryRouter initialEntries={[`${AppRoute.SIGN_IN}/testPath`]}>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSignInPage:")
        ).resolves.toBeInTheDocument();
    });

    test("should render sign in page with base path as redirection URL when location is sign in path", async () => {
        mockPathname = AppRoute.SIGN_IN;
        render(
            <MemoryRouter>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSignInPage:testBasePath")
        ).resolves.toBeInTheDocument();
    });

    test("should render sign in page with base path as redirection URL when location is sign out path", async () => {
        mockPathname = AppRoute.SIGN_OUT;
        render(
            <MemoryRouter>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSignInPage:testBasePath")
        ).resolves.toBeInTheDocument();
    });

    test("should render sign in page with appropriate redirection URL when location is anything other than sign in/out path", async () => {
        mockPathname = "testPath";
        render(
            <MemoryRouter>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSignInPage:testPath")
        ).resolves.toBeInTheDocument();
    });

    test("should render sign in page by default", async () => {
        mockPathname = "";
        render(
            <MemoryRouter>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSignInPage:")
        ).resolves.toBeInTheDocument();
    });
});

const mockSetRouterBreadcrumbs = jest.fn();

let mockPathname = "";
