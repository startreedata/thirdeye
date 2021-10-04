import { act, render, screen } from "@testing-library/react";
import React from "react";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { LogoutPage } from "./logout-page.component";

jest.mock("../../components/auth-provider/auth-provider.component", () => ({
    useAuth: jest.fn().mockImplementation(() => ({
        logout: mockLogout,
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

jest.mock(
    "../../components/loading-indicator/loading-indicator.component",
    () => ({
        LoadingIndicator: jest.fn().mockReturnValue("testLoadingIndicator"),
    })
);

describe("Logout Page", () => {
    it("should set appropriate page breadcrumbs", async () => {
        act(() => {
            render(<LogoutPage />);
        });

        expect(mockSetPageBreadcrumbs).toHaveBeenCalledWith([]);
    });

    it("should set appropriate page title", async () => {
        act(() => {
            render(<LogoutPage />);
        });

        expect(PageContents).toHaveBeenCalledWith(
            {
                hideHeader: true,
                title: "label.logout",
                children: expect.any(Object),
            },
            {}
        );
    });

    it("should logout", async () => {
        act(() => {
            render(<LogoutPage />);
        });

        expect(mockLogout).toHaveBeenCalled();
    });

    it("should render loading indicator", async () => {
        act(() => {
            render(<LogoutPage />);
        });

        expect(screen.getByText("testLoadingIndicator")).toBeInTheDocument();
    });
});

const mockSetPageBreadcrumbs = jest.fn();

const mockLogout = jest.fn();
