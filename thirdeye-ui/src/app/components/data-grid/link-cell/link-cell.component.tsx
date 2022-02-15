import { Box, Link } from "@material-ui/core";
import { GridCellParams } from "@material-ui/data-grid";
import { isNil, toNumber } from "lodash";
import React, { ReactElement, useEffect, useState } from "react";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { LinkCellProps } from "./link-cell.interfaces";
import { useLinkCellStyles } from "./link-cell.styles";

function LinkCell<T>(props: LinkCellProps<T>): ReactElement {
    const linkCellClasses = useLinkCellStyles();
    const [value, setValue] = useState<T>();
    const [rowId, setRowId] = useState(-1);
    const [align, setAlign] = useState("");

    useEffect(() => {
        setValue(props.params && (props.params.value as unknown as T));
        setRowId(
            toNumber(props.params && props.params.row && props.params.row.id)
        );
        setAlign(
            props.params && props.params.colDef && props.params.colDef.align
        );
    }, []);

    const getValueText = (): string => {
        if (props.valueTextFn) {
            return props.valueTextFn(value as T);
        }

        if (typeof value === "string") {
            return value;
        }

        return "";
    };

    const handleLinkClick = (): void => {
        if (!value || isNil(rowId) || rowId < 0) {
            return;
        }

        props.onClick && props.onClick(value as T, rowId);
    };

    return (
        <Box textAlign={align} width="100%">
            <Link
                noWrap
                className={linkCellClasses.link}
                component="button"
                variant="body1"
                onClick={handleLinkClick}
            >
                <TextHighlighter
                    searchWords={props.searchWords}
                    text={getValueText()}
                />
            </Link>
        </Box>
    );
}

export function linkCellRenderer<T>(
    params: GridCellParams,
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
