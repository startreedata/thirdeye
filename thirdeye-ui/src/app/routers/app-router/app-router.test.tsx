import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { AppRoute } from "../../utils/routes-util/routes-util";
import { AppRouter } from "./app-router";

jest.mock("../../store/auth-store/auth-store", () => ({
    useAuthStore: jest.fn().mockImplementation((selector) => {
        return selector({
            auth: mockAuth,
        });
    }),
}));

jest.mock("../alerts-router/alerts-router", () => ({
    AlertsRouter: jest.fn().mockImplementation(() => <>testAlertsRouter</>),
}));

jest.mock("../anomalies-router/anomalies-router", () => ({
    AnomaliesRouter: jest
        .fn()
        .mockImplementation(() => <>testAnomaliesRouter</>),
}));

jest.mock("../configuration-router/configuration-router", () => ({
    ConfigurationRouter: jest
        .fn()
        .mockImplementation(() => <>testConfigurationRouter</>),
}));

jest.mock(
    "../general-authenticated-router/general-authenticated-router",
    () => ({
        GeneralAuthenticatedRouter: jest
            .fn()
            .mockImplementation(() => <>testGeneralAuthenticatedRouter</>),
    })
);

jest.mock(
    "../general-unauthenticated-router/general-unauthenticated-router",
    () => ({
        GeneralUnauthenticatedRouter: jest
            .fn()
            .mockImplementation(() => <>testGeneralUnauthenticatedRouter</>),
    })
);

describe("App Router", () => {
    test("should direct exact alerts path to alerts router when authenticated", () => {
        mockAuth = true;
        render(
            <MemoryRouter initialEntries={[AppRoute.ALERTS]}>
                <AppRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testAlertsRouter")).toBeInTheDocument();
    });

    test("should direct alerts path to alerts router when authenticated", () => {
        mockAuth = true;
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ALERTS}/testPath`]}>
                <AppRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testAlertsRouter")).toBeInTheDocument();
    });

    test("should direct exact anomalies path to anomalies router when authenticated", () => {
        mockAuth = true;
        render(
            <MemoryRouter initialEntries={[AppRoute.ANOMALIES]}>
                <AppRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testAnomaliesRouter")).toBeInTheDocument();
    });

    test("should direct anomalies path to anomalies router when authenticated", () => {
        mockAuth = true;
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ANOMALIES}/testPath`]}>
                <AppRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testAnomaliesRouter")).toBeInTheDocument();
    });

    test("should direct exact configuration path to configuration router when authenticated", () => {
        mockAuth = true;
        render(
            <MemoryRouter initialEntries={[AppRoute.CONFIGURATION]}>
                <AppRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testConfigurationRouter")).toBeInTheDocument();
    });

    test("should direct configuration path to configuration router when authenticated", () => {
        mockAuth = true;
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.CONFIGURATION}/testPath`]}
            >
                <AppRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testConfigurationRouter")).toBeInTheDocument();
    });

    test("should direct any other path to general authenticated router when authenticated", () => {
        mockAuth = true;
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <AppRouter />
            </MemoryRouter>
        );

        expect(
            screen.getByText("testGeneralAuthenticatedRouter")
        ).toBeInTheDocument();
    });

    test("should direct to general authenticated router by default when authenticated", () => {
        mockAuth = true;
        render(
            <MemoryRouter>
                <AppRouter />
            </MemoryRouter>
        );

        expect(
            screen.getByText("testGeneralAuthenticatedRouter")
        ).toBeInTheDocument();
    });

    test("should direct any path to general unauthenticated router when not authenticated", () => {
        mockAuth = false;
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <AppRouter />
            </MemoryRouter>
        );

        expect(
            screen.getByText("testGeneralUnauthenticatedRouter")
        ).toBeInTheDocument();
    });

    test("should direct to general unauthenticated router by default when not authenticated", () => {
        mockAuth = false;
        render(
            <MemoryRouter>
                <AppRouter />
            </MemoryRouter>
        );

        expect(
            screen.getByText("testGeneralUnauthenticatedRouter")
        ).toBeInTheDocument();
    });
});

let mockAuth = true;
