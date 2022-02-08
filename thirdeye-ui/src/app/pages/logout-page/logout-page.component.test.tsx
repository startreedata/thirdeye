import { render, screen } from "@testing-library/react";
import React from "react";
import { LogoutPage } from "./logout-page.component";

jest.mock("@startree-ui/platform-ui", () => ({
    ...(jest.requireActual("@startree-ui/platform-ui") as Record<
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

describe("Logout Page", () => {
    it("should set appropriate page breadcrumbs", async () => {
        render(<LogoutPage />);

        expect(mockSetPageBreadcrumbs).toHaveBeenCalledWith([]);
    });

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

const mockSetPageBreadcrumbs = jest.fn();

const mockLogout = jest.fn();
