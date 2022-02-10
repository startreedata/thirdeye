import { fireEvent, render, screen } from "@testing-library/react";
import React from "react";
import { SearchBar } from "./search-bar.component";
import { SearchBarProps } from "./search-bar.interfaces";

jest.mock("../../utils/params/params.util", () => ({
    getSearchTextFromQueryString: jest
        .fn()
        .mockReturnValue("TestSearchTextUpdated"),
    getSearchFromQueryString: jest.fn().mockReturnValue(""),
    setSearchInQueryString: jest
        .fn()
        .mockImplementation((props) => props.searchLabel),
    setSearchTextInQueryString: jest
        .fn()
        .mockImplementation((searchWords) => searchWords),
}));

describe("SearchBar", () => {
    it("should load search text and label correctly", async () => {
        render(<SearchBar {...mockDefaultProps} />);

        const input = screen.getByPlaceholderText("TestSearchLabel");

        expect(screen.getByTestId("close-icon-font")).toBeInTheDocument();
        expect(screen.getByTestId("search-icon-font")).toBeInTheDocument();
        expect(
            await screen.findByText("TestSearchStatusLabel")
        ).toBeInTheDocument();
        expect(input).toHaveAttribute("value", "TestSearchText");
        expect(input).toHaveAttribute("placeholder", "TestSearchLabel");
    });

    it("should load not show label, search text and label if they are not empty", async () => {
        const props = {
            ...mockDefaultProps,
            searchText: undefined,
            searchLabel: "",
            searchStatusLabel: "",
        } as SearchBarProps;
        render(<SearchBar {...props} />);

        const input = screen.getByPlaceholderText("");

        expect(
            screen.queryByText("TestSearchStatusLabel")
        ).not.toBeInTheDocument();
        expect(input).toHaveAttribute("value", "");
        expect(input).toHaveAttribute("placeholder", "");
    });

    it("should be able to change the input value", async () => {
        render(<SearchBar {...mockDefaultProps} />);

        const input = screen.getByPlaceholderText("TestSearchLabel");

        fireEvent.change(input, {
            target: {
                value: "TestSearchTextUpdated",
            },
        });

        expect(input).toHaveAttribute("value", "TestSearchTextUpdated");
    });

    it("should be able to clear the value when clicked on close icon", async () => {
        const props = {
            ...mockDefaultProps,
            setSearchQueryString: true,
        } as SearchBarProps;
        render(<SearchBar {...props} />);

        const input = screen.getByPlaceholderText("TestSearchLabel");

        fireEvent.click(screen.getByTestId("close-icon-font"));

        expect(input).toHaveAttribute("value", "");
    });

    it("should be able to pick up search from query string if search text is not present", async () => {
        const props = {
            ...mockDefaultProps,
            searchText: undefined,
            setSearchQueryString: true,
            searchLabel: "",
            searchStatusLabel: "",
        } as SearchBarProps;
        render(<SearchBar {...props} />);

        const input = screen.getByPlaceholderText("");

        expect(input).toHaveAttribute("value", "TestSearchTextUpdated");
    });
});

const mockOnChangeMethod = jest.fn();

const mockDefaultProps = {
    searchText: "TestSearchText",
    autoFocus: false,
    searchLabel: "TestSearchLabel",
    searchStatusLabel: "TestSearchStatusLabel",
    setSearchQueryString: false,
    onChange: mockOnChangeMethod,
} as SearchBarProps;
