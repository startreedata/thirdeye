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
