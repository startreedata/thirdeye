/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { act, cleanup, render, screen } from "@testing-library/react";
import React from "react";
import { AppBar } from "./app-bar.component";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

jest.mock("../../platform/components", () => ({
    ...(jest.requireActual("../../platform/components") as Record<
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
