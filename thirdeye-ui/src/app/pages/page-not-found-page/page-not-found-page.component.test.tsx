import { PageContentsGridV1 } from "@startree-ui/platform-ui";
import { act, render, screen } from "@testing-library/react";
import React from "react";
import { PageNotFoundPage } from "./page-not-found-page.component";

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
    PageContentsGridV1: jest.fn().mockImplementation((props) => props.children),
    PageHeaderTextV1: jest.fn().mockImplementation((props) => props.children),
    PageHeaderV1: jest.fn().mockImplementation((props) => props.children),
    PageNotFoundIndicatorV1: jest.fn().mockReturnValue("page-not-found"),
    PageV1: jest.fn().mockImplementation((props) => props.children),
}));

describe("Page Not Found Page", () => {
    it("should set appropriate page title", async () => {
        act(() => {
            render(<PageNotFoundPage />);
        });

        expect(PageContentsGridV1).toHaveBeenCalledWith(
            {
                fullHeight: true,
                children: expect.any(Object),
            },
            {}
        );
    });

    it("should render page not found indicator", async () => {
        act(() => {
            render(<PageNotFoundPage />);
        });

        expect(screen.getByText("page-not-found")).toBeInTheDocument();
    });
});
