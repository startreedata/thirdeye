import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components/app-loading-indicator-v1/app-loading-indicator-v1.component";
import { AppRoute } from "../../utils/routes/routes.util";
import { GeneralAuthenticatedRouter } from "./general-authenticated.router";

jest.mock(
    "../../platform/components/app-loading-indicator-v1/app-loading-indicator-v1.component",
    () => ({
        AppLoadingIndicatorV1: jest
            .fn()
            .mockReturnValue("testLoadingIndicatorV1"),
    })
);

jest.mock("../../pages/home-page/home-page.component", () => ({
    HomePage: jest.fn().mockReturnValue("testHomePage"),
}));

jest.mock("../../pages/logout-page/logout-page.component", () => ({
    LogoutPage: jest.fn().mockReturnValue("testLogoutPage"),
}));

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest.fn().mockReturnValue("testPageNotFoundPage"),
    })
);

describe("General Authenticated Router", () => {
    it("should have rendered loading indicator while loading", () => {
        render(
            <MemoryRouter>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        expect(AppLoadingIndicatorV1).toHaveBeenCalled();
    });

    it("should render home page at exact base path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.BASE]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testHomePage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid base path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.BASE}/testPath`]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render home page at exact home path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.HOME]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testHomePage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid home path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.HOME}/testPath`]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render home page at exact login path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.LOGIN]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testHomePage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid login path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.LOGIN}/testPath`]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render logout page at exact logout path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.LOGOUT]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testLogoutPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid logout path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.LOGOUT}/testPath`]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at any other path", async () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <GeneralAuthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render home page by default", async () => {
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
