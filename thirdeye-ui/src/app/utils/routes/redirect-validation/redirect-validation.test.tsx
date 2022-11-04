// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { render, screen } from "@testing-library/react";
import React from "react";
import { RedirectValidation } from "./redirect-validation.component";

jest.mock("react-router-dom", () => ({
    useNavigate: jest.fn().mockImplementation(() => {
        return mockNavigate;
    }),
    useSearchParams: jest.fn().mockImplementation(() => {
        return [mockSearchParams];
    }),
}));

describe("Redirect Validation", () => {
    it("should render children if expected parameters exist in search params", async () => {
        mockSearchParams = new URLSearchParams([
            ["hello", "1"],
            ["world", "2"],
        ]);
        render(
            <RedirectValidation
                queryParams={["hello", "world"]}
                replace={false}
                to="path-to-redirect-to"
            >
                Hello world
            </RedirectValidation>
        );

        expect(await screen.findByText("Hello world")).toBeInTheDocument();
        expect(mockNavigate).not.toHaveBeenCalled();
    });

    it("should call navigate with path to redirect to if missing all expected params", async () => {
        mockSearchParams = new URLSearchParams();
        render(
            <RedirectValidation
                queryParams={["hello", "world"]}
                replace={false}
                to="path-to-redirect-to"
            >
                Hello world
            </RedirectValidation>
        );

        expect(mockNavigate).toHaveBeenLastCalledWith("path-to-redirect-to", {
            replace: false,
        });
    });

    it("should call navigate with path to redirect to if missing one expected params carrying over the one that exists", async () => {
        mockSearchParams = new URLSearchParams([["hello", "1"]]);
        render(
            <RedirectValidation
                queryParams={["hello", "world"]}
                replace={false}
                to="path-to-redirect-to"
            >
                Hello world
            </RedirectValidation>
        );

        expect(mockNavigate).toHaveBeenLastCalledWith(
            "path-to-redirect-to?hello=1",
            {
                replace: false,
            }
        );
    });
});

let mockSearchParams: URLSearchParams;

const mockNavigate = jest.fn();
