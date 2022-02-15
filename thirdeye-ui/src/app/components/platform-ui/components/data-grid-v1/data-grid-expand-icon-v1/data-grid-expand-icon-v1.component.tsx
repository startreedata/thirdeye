// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { Box, IconButton, useTheme } from "@material-ui/core";
import ArrowDropDownIcon from "@material-ui/icons/ArrowDropDown";
import ArrowRightIcon from "@material-ui/icons/ArrowRight";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { DataGridExpandIconV1Props } from "./data-grid-expand-icon-v1.interfaces";

export const DataGridExpandIconV1: FunctionComponent<DataGridExpandIconV1Props> = ({
    depth,
    expandable,
    expanded,
    className,
    onExpand,
    ...otherProps
}: DataGridExpandIconV1Props) => {
    const theme = useTheme();

    const handleClick = (): void => {
        onExpand && onExpand(!expanded);
    };

    return (
        // Container to add child row indentation
        <Box
            {...otherProps}
            alignItems="center"
            className={classNames(
                "BaseTable__expand-icon",
                {
                    "BaseTable__expand-icon--expanded": expanded,
                },
                className,
                "data-grid-expand-icon-v1"
            )} // Classes to be added just as the original implementation of expand icon in React Base Table
            display="flex"
            justifyContent="center"
            marginLeft={-(theme.spacing(1) / 8)}
            marginRight={expandable ? 0.3 : theme.spacing(1) / 2}
            paddingLeft={depth * (theme.spacing(1) / 4)}
        >
            {/* Expand/collapse button */}
            {expandable && (
                <IconButton
                    className="data-grid-expand-icon-v1-button"
                    size="small"
                    onClick={handleClick}
                >
                    {/* Expand icon */}
                    {!expanded && (
                        <ArrowRightIcon color="action" fontSize="medium" />
                    )}

                    {/* Collapse icon */}
                    {expanded && (
                        <ArrowDropDownIcon color="action" fontSize="medium" />
                    )}
                </IconButton>
            )}
        </Box>
    );
};
