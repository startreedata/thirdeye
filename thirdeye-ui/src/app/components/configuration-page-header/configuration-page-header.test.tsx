import { cleanup, render, screen } from "@testing-library/react";
import React from "react";
import { ConfigurationPageHeader } from "./configuration-page-header.component";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

jest.mock("@startree-ui/platform-ui", () => ({
    ...(jest.requireActual("@startree-ui/platform-ui") as Record<
        string,
        unknown
    >),
    PageHeaderV1: jest
        .fn()
        .mockImplementation(({ children }) => <>{children}</>),
    PageHeaderTextV1: jest
        .fn()
        .mockImplementation(({ children }) => <>{children}</>),
    PageHeaderTabV1: jest
        .fn()
        .mockImplementation(({ children, href }) => (
            <a href={href}>{children}</a>
        )),
    PageHeaderTabsV1: jest
        .fn()
        .mockImplementation(({ children, selectedIndex }) => (
            <div data-testid="page-header-tabs" selected-index={selectedIndex}>
                {children}
            </div>
        )),
}));

jest.mock("react-router-dom", () => ({
    useLocation: jest.fn().mockReturnValue({ pathname: "test" }),
}));

jest.mock("../create-menu-button/create-menu-button.component", () => ({
    CreateMenuButton: jest
        .fn()
        .mockImplementation(() => <button>Create</button>),
}));

describe("ConfigurationPageHeader", () => {
    beforeEach(() => cleanup);

    it("should render ConfigurationPageHeader properly", () => {
        render(<ConfigurationPageHeader selectedIndex={1} />);

        expect(screen.getByText("label.configuration")).toBeInTheDocument();
        expect(
            screen.getByText("label.subscription-groups")
        ).toBeInTheDocument();
        expect(screen.getByText("label.datasets")).toBeInTheDocument();
        expect(screen.getByText("label.datasources")).toBeInTheDocument();
        expect(screen.getByText("label.metrics")).toBeInTheDocument();
        expect(screen.getByText("Create")).toBeInTheDocument();
        expect(screen.getByTestId("page-header-tabs")).toHaveAttribute(
            "selected-index",
            "1"
        );
    });

    it("should render page header tabs with selectedIndex passed from props", () => {
        render(<ConfigurationPageHeader selectedIndex={2} />);

        expect(screen.getByTestId("page-header-tabs")).toHaveAttribute(
            "selected-index",
            "2"
        );
    });
});
