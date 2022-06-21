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
import { render, screen } from "@testing-library/react";
import React from "react";
import { LogoutPage } from "./logout-page.component";

jest.mock("../../platform/components", () => ({
    ...(jest.requireActual("../../platform/components") as Record<
        string,
        unknown
    >),
    useAuthProviderV1: jest.fn().mockImplementation(() => ({
        logout: mockLogout,
    })),
    AppLoadingIndicatorV1: jest
        .fn()
        .mockReturnValue("testAppLoadingIndicatorV1"),
    PageV1: jest.fn().mockImplementation((props) => props.children),
    PageHeaderV1: jest.fn().mockImplementation((props) => props.children),
    PageHeaderTextV1: jest
        .fn()
        .mockImplementation((props) => <p>{props.children}</p>),
}));

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

describe("Logout Page", () => {
    it("should set appropriate page title", async () => {
        render(<LogoutPage />);

        expect(await screen.findByText("label.logout")).toBeInTheDocument();
    });

    it("should logout", async () => {
        render(<LogoutPage />);

        expect(mockLogout).toHaveBeenCalled();
    });

    it("should render loading indicator", async () => {
        render(<LogoutPage />);

        expect(
            await screen.findByText("testAppLoadingIndicatorV1")
        ).toBeInTheDocument();
    });
});

const mockLogout = jest.fn();
