import { act, fireEvent, render, screen } from "@testing-library/react";
import React from "react";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { ConfigurationPage } from "./configuration-page.component";

jest.mock(
    "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component",
    () => ({
        useAppBreadcrumbs: jest.fn().mockImplementation(() => ({
            setPageBreadcrumbs: mockSetPageBreadcrumbs,
        })),
    })
);

jest.mock("react-router-dom", () => ({
    useHistory: jest.fn().mockImplementation(() => ({
        push: mockPush,
    })),
}));

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

jest.mock("../../utils/routes/routes.util", () => ({
    getSubscriptionGroupsPath: jest
        .fn()
        .mockReturnValue("testSubscriptionGroupsPath"),
    getMetricsPath: jest.fn().mockReturnValue("testMetricsPath"),
}));

jest.mock("../../components/page-contents/page-contents.component", () => ({
    PageContents: jest.fn().mockImplementation((props) => props.children),
}));

describe("Configuration Page", () => {
    it("should set appropriate page breadcrumbs", async () => {
        act(() => {
            render(<ConfigurationPage />);
        });

        expect(mockSetPageBreadcrumbs).toHaveBeenCalledWith([]);
    });

    it("should set appropriate page title", async () => {
        act(() => {
            render(<ConfigurationPage />);
        });

        expect(PageContents).toHaveBeenCalledWith(
            {
                centered: true,
                hideAppBreadcrumbs: true,
                hideTimeRange: true,
                title: "label.configuration",
                children: expect.any(Object),
            },
            {}
        );
    });

    it("should render all navigation buttons", async () => {
        act(() => {
            render(<ConfigurationPage />);
        });

        expect(
            screen.getByText("label.subscription-groups")
        ).toBeInTheDocument();
        expect(screen.getByText("label.metrics")).toBeInTheDocument();
    });

    it("should navigate to subscription groups path on subscription groups button click", async () => {
        act(() => {
            render(<ConfigurationPage />);
        });

        fireEvent.click(screen.getByText("label.subscription-groups"));

        expect(mockPush).toHaveBeenCalledWith("testSubscriptionGroupsPath");
    });

    it("should navigate to metrics path on metrics button click", async () => {
        act(() => {
            render(<ConfigurationPage />);
        });

        fireEvent.click(screen.getByText("label.metrics"));

        expect(mockPush).toHaveBeenCalledWith("testMetricsPath");
    });
});

const mockSetPageBreadcrumbs = jest.fn();

const mockPush = jest.fn();
