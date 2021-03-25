import { act, render, screen } from "@testing-library/react";
import React from "react";
import { SignOutPage } from "./sign-out-page.component";

jest.mock("../../components/auth-provider/auth-provider.component", () => ({
    useAuth: jest.fn().mockImplementation(() => ({
        signOut: mockSignOut,
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
    PageContents: jest.fn().mockImplementation((props) => (
        <>
            <div>title:{props.title}</div>
            <div>{props.children}</div>
        </>
    )),
}));

jest.mock(
    "../../components/loading-indicator/loading-indicator.component",
    () => ({
        LoadingIndicator: jest.fn().mockReturnValue("testLoadingIndicator"),
    })
);

describe("Sign Out Page", () => {
    test("should set appropriate page breadcrumbs", async () => {
        act(() => {
            render(<SignOutPage />);
        });

        expect(mockSetPageBreadcrumbs).toHaveBeenCalledWith([]);
    });

    test("should set appropriate page title", async () => {
        act(() => {
            render(<SignOutPage />);
        });

        expect(screen.getByText("title:label.sign-out")).toBeInTheDocument();
    });

    test("should sign out", async () => {
        act(() => {
            render(<SignOutPage />);
        });

        expect(mockSignOut).toHaveBeenCalled();
    });

    test("should render loading indicator", async () => {
        act(() => {
            render(<SignOutPage />);
        });

        expect(screen.getByText("testLoadingIndicator")).toBeInTheDocument();
    });
});

const mockSetPageBreadcrumbs = jest.fn();

const mockSignOut = jest.fn();
