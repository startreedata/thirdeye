import { Box, Link } from "@material-ui/core";
import { CellParams } from "@material-ui/data-grid";
import { toNumber } from "lodash";
import React, { ReactElement, useEffect, useState } from "react";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { LinkCellProps } from "./link-cell.interfaces";

function LinkCell<T>(props: LinkCellProps<T>): ReactElement {
    const [value, setValue] = useState<T>();
    const [rowId, setRowId] = useState(-1);
    const [align, setAlign] = useState("");

    useEffect(() => {
        // Input cell parameters changed
        setValue(props.params && ((props.params.value as unknown) as T));
        setRowId(
            toNumber(props.params && props.params.row && props.params.row.id)
        );
        setAlign(
            props.params && props.params.colDef && props.params.colDef.align
        );
    }, [props.params]);

    const getValueText = (): string => {
        if (props.valueTextFn) {
            return props.valueTextFn(value as T);
        }

        if (typeof value === "string") {
            return value;
        }

        return "";
    };

    const handleClick = (): void => {
        props.onClick && props.onClick(value as T, rowId);
    };

    return (
        <Box textAlign={align} width="100%">
            <Link noWrap display="block" onClick={handleClick}>
                <TextHighlighter
                    searchWords={props.searchWords}
                    text={getValueText()}
                />
            </Link>
        </Box>
    );
}

export function linkCellRenderer<T>(
    params: CellParams,
    searchWords?: string[],
    onClick?: (value: T, rowId: number) => void,
    valueTextFn?: (value: T) => string
): ReactElement {
    return (
        <LinkCell<T>
            params={params}
            searchWords={searchWords}
            valueTextFn={valueTextFn}
            onClick={onClick}
        />
    );
}
