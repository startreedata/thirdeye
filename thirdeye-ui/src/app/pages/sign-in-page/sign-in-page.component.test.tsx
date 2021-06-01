import { act, fireEvent, render, screen } from "@testing-library/react";
import React from "react";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SignInPage } from "./sign-in-page.component";

jest.mock("../../components/auth-provider/auth-provider.component", () => ({
    useAuth: jest.fn().mockImplementation(() => ({
        signIn: mockSignIn,
    })),
}));

jest.mock(
    "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component",
    () => ({
        useAppBreadcrumbs: jest.fn().mockImplementation(() => ({
            setPageBreadcrumbs: mockSetPageBreadcrumbs,
        })),
    })
);

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

jest.mock("../../components/page-contents/page-contents.component", () => ({
    PageContents: jest.fn().mockImplementation((props) => props.children),
}));

describe("Sign In Page", () => {
    it("should set appropriate page breadcrumbs", async () => {
        act(() => {
            render(<SignInPage />);
        });

        expect(mockSetPageBreadcrumbs).toHaveBeenCalledWith([]);
    });

    it("should set appropriate page title", async () => {
        act(() => {
            render(<SignInPage />);
        });

        expect(PageContents).toHaveBeenCalledWith(
            {
                hideHeader: true,
                title: "label.sign-in",
                children: expect.any(Object),
            },
            {}
        );
    });

    it("should render sign in button", async () => {
        act(() => {
            render(<SignInPage />);
        });

        expect(screen.getByText("label.sign-in")).toBeInTheDocument();
    });

    it("should sign in on sign in button click", async () => {
        act(() => {
            render(<SignInPage />);
        });

        fireEvent.click(screen.getByText("label.sign-in"));

        expect(mockSignIn).toHaveBeenCalled();
    });
});

const mockSetPageBreadcrumbs = jest.fn();

const mockSignIn = jest.fn();
