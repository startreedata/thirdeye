import { act, cleanup, render, screen } from "@testing-library/react";
import React from "react";
import { AppBar } from "./app-bar.component";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

jest.mock("@startree-ui/platform-ui", () => ({
    ...(jest.requireActual("@startree-ui/platform-ui") as Record<
        string,
        unknown
    >),
    NavBarV1: jest.fn().mockImplementation(({ children }) => <>{children}</>),
    useAuthProviderV1: jest.fn().mockImplementation(() => ({
        authenticated: mockAuthenticated,
        authDisabled: mockAuthDisabled,
    })),
}));

jest.mock("react-router-dom", () => ({
    useLocation: jest.fn().mockReturnValue({ pathname: "test" }),
}));

describe("AppBar", () => {
    beforeEach(() => cleanup);

    it("should render all the entity cards", async () => {
        act(() => {
            render(<AppBar />);
        });

        expect(screen.getByText("label.home")).toBeInTheDocument();
        expect(screen.getByText("label.alerts")).toBeInTheDocument();
        expect(screen.getByText("label.anomalies")).toBeInTheDocument();
        expect(screen.getByText("label.configuration")).toBeInTheDocument();
    });

    it("should render login if not authenticated & not auth-disabled", () => {
        act(() => {
            render(<AppBar />);
        });

        expect(screen.getByText("label.login")).toBeInTheDocument();
    });

    it("should render logout if authenticated & not auth-disabled", async () => {
        mockAuthenticated = true;

        act(() => {
            render(<AppBar />);
        });

        expect(screen.getByText("label.logout")).toBeInTheDocument();
    });

    it("should not render login or logout if auth-disabled is true", async () => {
        mockAuthDisabled = true;

        act(() => {
            render(<AppBar />);
        });

        expect(screen.queryByText("label.logout")).not.toBeInTheDocument();
        expect(screen.queryByText("label.login")).not.toBeInTheDocument();
    });
});

let mockAuthDisabled = false;

let mockAuthenticated = false;
