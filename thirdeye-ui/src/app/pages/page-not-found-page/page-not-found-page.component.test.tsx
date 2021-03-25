import { act, render, screen } from "@testing-library/react";
import React from "react";
import { PageNotFoundPage } from "./page-not-found-page.component";

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
    "../../components/page-not-found-indicator/page-not-found-indicator.component",
    () => ({
        PageNotFoundIndicator: jest
            .fn()
            .mockReturnValue("testPageNotFoundIndicator"),
    })
);

describe("Page Not Found Page", () => {
    test("should set appropriate page breadcrumbs", async () => {
        act(() => {
            render(<PageNotFoundPage />);
        });

        expect(mockSetPageBreadcrumbs).toHaveBeenCalledWith([]);
    });

    test("should set appropriate page title", async () => {
        act(() => {
            render(<PageNotFoundPage />);
        });

        expect(
            screen.getByText("title:label.page-not-found")
        ).toBeInTheDocument();
    });

    test("should render page not found indicator", async () => {
        act(() => {
            render(<PageNotFoundPage />);
        });

        expect(
            screen.getByText("testPageNotFoundIndicator")
        ).toBeInTheDocument();
    });
});

const mockSetPageBreadcrumbs = jest.fn();
