import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components/app-loading-indicator-v1/app-loading-indicator-v1.component";
import { AppRoute } from "../../utils/routes/routes.util";
import { GeneralUnauthenticatedRouter } from "./general-unauthenticated.router";

jest.mock(
    "../../platform/components/app-loading-indicator-v1/app-loading-indicator-v1.component",
    () => ({
        AppLoadingIndicatorV1: jest
            .fn()
            .mockReturnValue("testLoadingIndicatorV1"),
    })
);

jest.mock("../../pages/login-page/login-page.component", () => ({
    LoginPage: jest.fn().mockReturnValue("testLoginPage"),
}));

describe("General Unauthenticated Router", () => {
    it("should have rendered loading indicator while loading", () => {
        render(
            <MemoryRouter>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        expect(AppLoadingIndicatorV1).toHaveBeenCalled();
    });

    it("should render login page at exact login path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.LOGIN]}>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testLoginPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render login page at invalid login path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.LOGIN}/testPath`]}>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testLoginPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render login page at any other path", async () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testLoginPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render login page by default", async () => {
        render(
            <MemoryRouter>
                <GeneralUnauthenticatedRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testLoginPage")
        ).resolves.toBeInTheDocument();
    });
});
