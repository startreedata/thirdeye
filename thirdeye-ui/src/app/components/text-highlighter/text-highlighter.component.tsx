import React, { FunctionComponent } from "react";
import Highlighter from "react-highlight-words";
import { TextHighlighterProps } from "./text-highlighter.interfaces";
import { useTextHighlighterStyles } from "./text-highlighter.styles";

export const TextHighlighter: FunctionComponent<TextHighlighterProps> = ({
    searchWords = [],
    textToHighlight = "",
}: TextHighlighterProps) => {
    const textHighlighterClasses = useTextHighlighterStyles();

    return (
        <Highlighter
            highlightClassName={textHighlighterClasses.highlight}
            searchWords={searchWords}
            textToHighlight={textToHighlight}
        />
    );
};
