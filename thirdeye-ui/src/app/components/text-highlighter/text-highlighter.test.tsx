import { render, screen } from "@testing-library/react";
import React from "react";
import { TextHighlighter } from "./text-highlighter.component";
import { TextHighlighterProps } from "./text-highlighter.interfaces";

describe("TextHighlighter", () => {
    it("should load text and search words correctly", async () => {
        render(<TextHighlighter {...mockDefaultProps} />);

        expect(await screen.findByText("TestText")).toBeInTheDocument();
        expect(await screen.findByText("TestSearchWords")).toBeInTheDocument();
    });

    it("should not highlight the word if searchWords are given and none of them are present in the text", async () => {
        const props = { ...mockDefaultProps, searchWords: [] };
        render(<TextHighlighter {...props} />);

        expect(
            await screen.findByText("TestText TestSearchWords")
        ).toBeInTheDocument();
        expect(screen.queryByText("TestSearchWords")).not.toBeInTheDocument();
    });

    it("should not show text if it's not passed", async () => {
        render(<TextHighlighter />);

        expect(
            screen.queryByText("TestText TestSearchWords")
        ).not.toBeInTheDocument();
    });
});

const mockDefaultProps = {
    text: "TestText TestSearchWords",
    searchWords: ["TestSearchWords"],
} as TextHighlighterProps;
