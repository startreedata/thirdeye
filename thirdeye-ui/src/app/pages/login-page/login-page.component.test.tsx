import { act, fireEvent, render, screen } from "@testing-library/react";
import React from "react";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { LoginPage } from "./login-page.component";

jest.mock("../../components/auth-provider/auth-provider.component", () => ({
    useAuth: jest.fn().mockImplementation(() => ({
        login: mockLogin,
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

describe("Login Page", () => {
    it("should set appropriate page breadcrumbs", async () => {
        act(() => {
            render(<LoginPage />);
        });

        expect(mockSetPageBreadcrumbs).toHaveBeenCalledWith([]);
    });

    it("should set appropriate page title", async () => {
        act(() => {
            render(<LoginPage />);
        });

        expect(PageContents).toHaveBeenCalledWith(
            {
                hideHeader: true,
                title: "label.login",
                children: expect.any(Object),
            },
            {}
        );
    });

    it("should render login button", async () => {
        act(() => {
            render(<LoginPage />);
        });

        expect(screen.getByText("label.login")).toBeInTheDocument();
    });

    it("should login on login button click", async () => {
        act(() => {
            render(<LoginPage />);
        });

        fireEvent.click(screen.getByText("label.login"));

        expect(mockLogin).toHaveBeenCalled();
    });
});

const mockSetPageBreadcrumbs = jest.fn();

const mockLogin = jest.fn();
