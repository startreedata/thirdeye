import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { AppRoute } from "../../utils/routes/routes.util";
import { AppRouter } from "./app.router";

jest.mock("@startree-ui/platform-ui", () => ({
    ...(jest.requireActual("@startree-ui/platform-ui") as Record<
        string,
        unknown
    >),
    useAuthProviderV1: jest.fn().mockImplementation(() => ({
        authDisabled: mockAuthDisabled,
        authenticated: mockAuthenticated,
    })),
}));

jest.mock("../alerts/alerts.router", () => ({
    AlertsRouter: jest.fn().mockReturnValue("testAlertsRouter"),
}));

jest.mock("../anomalies/anomalies.router", () => ({
    AnomaliesRouter: jest.fn().mockReturnValue("testAnomaliesRouter"),
}));

jest.mock("../configuration/configuration.router", () => ({
    ConfigurationRouter: jest.fn().mockReturnValue("testConfigurationRouter"),
}));

jest.mock("../general-authenticated/general-authenticated.router", () => ({
    GeneralAuthenticatedRouter: jest
        .fn()
        .mockReturnValue("testGeneralAuthenticatedRouter"),
}));

jest.mock("../general-unauthenticated/general-unauthenticated.router", () => ({
    GeneralUnauthenticatedRouter: jest
        .fn()
        .mockReturnValue("testGeneralUnauthenticatedRouter"),
}));

describe("App Router", () => {
    it("should direct exact alerts path to alerts router when auth disabled", async () => {
        mockAuthDisabled = true;
        mockAuthenticated = false;
        render(
            <MemoryRouter initialEntries={[AppRoute.ALERTS]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct exact alerts path to alerts router when auth enabled and authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = true;
        render(
            <MemoryRouter initialEntries={[AppRoute.ALERTS]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct exact alerts path to general unauthenticated router when auth enabled and not authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = false;
        render(
            <MemoryRouter initialEntries={[AppRoute.ALERTS]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testGeneralUnauthenticatedRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct alerts path to alerts router when auth disabled", async () => {
        mockAuthDisabled = true;
        mockAuthenticated = false;
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ALERTS}/testPath`]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct alerts path to alerts router when auth enabled and authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = true;
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ALERTS}/testPath`]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct alerts path to general unauthenticated router when auth enabled and not authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = false;
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ALERTS}/testPath`]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testGeneralUnauthenticatedRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct exact anomalies path to anomalies router when auth disabled", async () => {
        mockAuthDisabled = true;
        mockAuthenticated = false;
        render(
            <MemoryRouter initialEntries={[AppRoute.ANOMALIES]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAnomaliesRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct exact anomalies path to anomalies router when auth enabled and authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = true;
        render(
            <MemoryRouter initialEntries={[AppRoute.ANOMALIES]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAnomaliesRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct exact anomalies path to general unauthenticated router when auth enabled and not authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = false;
        render(
            <MemoryRouter initialEntries={[AppRoute.ANOMALIES]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testGeneralUnauthenticatedRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct anomalies path to anomalies router when auth disabled", async () => {
        mockAuthDisabled = true;
        mockAuthenticated = false;
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ANOMALIES}/testPath`]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAnomaliesRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct anomalies path to anomalies router when auth enbled and authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = true;
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ANOMALIES}/testPath`]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAnomaliesRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct anomalies path to general unauthenticated router when auth enbled and not authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = false;
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ANOMALIES}/testPath`]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testGeneralUnauthenticatedRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct exact configuration path to configuration router when auth disabled", async () => {
        mockAuthDisabled = true;
        mockAuthenticated = false;
        render(
            <MemoryRouter initialEntries={[AppRoute.CONFIGURATION]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testConfigurationRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct exact configuration path to configuration router when auth enabled and authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = true;
        render(
            <MemoryRouter initialEntries={[AppRoute.CONFIGURATION]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testConfigurationRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct exact configuration path to general unauthenticated router when auth enabled and not authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = false;
        render(
            <MemoryRouter initialEntries={[AppRoute.CONFIGURATION]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testGeneralUnauthenticatedRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct configuration path to configuration router when auth disabled", async () => {
        mockAuthDisabled = true;
        mockAuthenticated = false;
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.CONFIGURATION}/testPath`]}
            >
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testConfigurationRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct configuration path to configuration router when auth enabled and authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = true;
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.CONFIGURATION}/testPath`]}
            >
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testConfigurationRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct configuration path to general unauthenticated router when auth enabled and not authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = false;
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.CONFIGURATION}/testPath`]}
            >
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testGeneralUnauthenticatedRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct any other path to general authenticated router when auth disabled", async () => {
        mockAuthDisabled = true;
        mockAuthenticated = false;
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testGeneralAuthenticatedRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct any other path to general authenticated router when auth enabled and authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = true;
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testGeneralAuthenticatedRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct any other path to general unauthenticated router when auth enabled and not authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = false;
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testGeneralUnauthenticatedRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct to general authenticated router by default when auth disabled", async () => {
        mockAuthDisabled = true;
        mockAuthenticated = false;
        render(
            <MemoryRouter>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testGeneralAuthenticatedRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct to general authenticated router by default when auth enabled and authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = true;
        render(
            <MemoryRouter>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testGeneralAuthenticatedRouter")
        ).resolves.toBeInTheDocument();
    });

    it("should direct to general unauthenticated router by default when auth enabled and not authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = false;
        render(
            <MemoryRouter>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testGeneralUnauthenticatedRouter")
        ).resolves.toBeInTheDocument();
    });
});

let mockAuthDisabled = false;

let mockAuthenticated = false;
