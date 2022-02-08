import { cleanup, render, screen } from "@testing-library/react";
import React from "react";
import { Breadcrumb } from "../../breadcrumbs/breadcrumbs.interfaces";
import { AppBreadcrumbsContext } from "../app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { AppBreadcrumbsContextProps } from "../app-breadcrumbs-provider/app-breadcrumbs-provider.interfaces";
import { AppBreadcrumbs } from "./app-breadcrumbs.component";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

jest.mock("react-router-dom", () => ({
    useLocation: jest.fn().mockReturnValue({ pathname: "test" }),
}));

describe("AppBreadcrumbs", () => {
    beforeEach(() => cleanup);

    it("should render all the breadcrumbs", async () => {
        render(
            <AppBreadcrumbsContext.Provider value={mockProviderValue}>
                <AppBreadcrumbs />
            </AppBreadcrumbsContext.Provider>
        );

        expect(screen.getByText("breadcrumb")).toBeInTheDocument();
        expect(screen.getByText("breadcrumb2")).toBeInTheDocument();
        expect(screen.getByText("breadcrumb3")).toBeInTheDocument();
    });

    it("should not render more than 1 router breadcrumb", async () => {
        render(
            <AppBreadcrumbsContext.Provider value={mockProviderValue}>
                <AppBreadcrumbs maxRouterBreadcrumbs={1} />
            </AppBreadcrumbsContext.Provider>
        );

        expect(screen.getByText("breadcrumb")).toBeInTheDocument();
        expect(screen.queryByText("breadcrumb2")).not.toBeInTheDocument();
        expect(screen.getByText("breadcrumb3")).toBeInTheDocument();
    });
});

const mockProviderValue = {
    routerBreadcrumbs: [
        { text: "breadcrumb" },
        { text: "breadcrumb2" },
    ] as Breadcrumb[],
    pageBreadcrumbs: [{ text: "breadcrumb3" }] as Breadcrumb[],
} as AppBreadcrumbsContextProps;
