import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { AppRoute } from "../../utils/routes/routes.util";
import { GeneralUnauthenticatedRouter } from "./general-unauthenticated.router";

jest.mock(
    "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component",
    () => ({
        useAppBreadcrumbs: jest.fn().mockImplementation(() => ({
            setRouterBreadcrumbs: mockSetRouterBreadcrumbs,
        })),
    })
);

jest.mock(
    "../../components/loading-indicator/loading-indicator.component",
    () => ({
        LoadingIndicator: jest.fn().mockReturnValue(<>testLoadingIndicator</>),
    })
);

jest.mock("../../pages/sign-in-page/sign-in-page.component", () => ({
    SignInPage: jest.fn().mockReturnValue("testSignInPage"),
}));

describe("General Unauthenticated Router", () => {
    it("should have rendered loading indicator while loading", () => {
        render(
            <MemoryRouter>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        expect(LoadingIndicator).toHaveBeenCalled();
    });

    it("should set appropriate router breadcrumbs", () => {
        render(
            <MemoryRouter>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        expect(mockSetRouterBreadcrumbs).toHaveBeenCalledWith([]);
    });

    it("should render sign in page at exact sign in path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.SIGN_IN]}>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSignInPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render sign in page at invalid sign in path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.SIGN_IN}/testPath`]}>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSignInPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render sign in page at any other path", async () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSignInPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render sign in page by default", async () => {
        render(
            <MemoryRouter>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSignInPage")
        ).resolves.toBeInTheDocument();
    });
});

const mockSetRouterBreadcrumbs = jest.fn();
