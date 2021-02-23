import { Box, Link } from "@material-ui/core";
import React, { ReactElement } from "react";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { LinkCellProps } from "./link-cell.interfaces";

export function LinkCell<T>(props: LinkCellProps<T>): ReactElement {
    const getValueText = (value: T): string => {
        if (props.valueTextFn) {
            return props.valueTextFn(value);
        }

        if (typeof value === "string") {
            return value;
        }

        return "";
    };

    const handleClick = (): void => {
        props.onClick && props.onClick(props.value, props.rowId);
    };

    return (
        <Box textAlign={props.align} width="100%">
            <Link noWrap display="block" onClick={handleClick}>
                <TextHighlighter
                    searchWords={props.searchWords}
                    text={getValueText(props.value)}
                />
            </Link>
        </Box>
    );
}
