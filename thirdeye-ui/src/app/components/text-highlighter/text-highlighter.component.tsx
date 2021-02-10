import React, { FunctionComponent } from "react";
import Highlighter from "react-highlight-words";
import { TextHighlighterProps } from "./text-highlighter.interfaces";
import { useTextHighlighterStyles } from "./text-highlighter.styles";

export const TextHighlighter: FunctionComponent<TextHighlighterProps> = (
    props: TextHighlighterProps
) => {
    const textHighlighterClasses = useTextHighlighterStyles();

    return (
        <Highlighter
            highlightClassName={textHighlighterClasses.highlight}
            searchWords={props.searchWords || []}
            textToHighlight={props.text || ""}
        />
    );
};
