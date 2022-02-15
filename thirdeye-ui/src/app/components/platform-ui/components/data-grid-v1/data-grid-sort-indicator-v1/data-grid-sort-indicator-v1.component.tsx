// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import ArrowDownwardIcon from "@material-ui/icons/ArrowDownward";
import ArrowUpwardIcon from "@material-ui/icons/ArrowUpward";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { DataGridSortOrderV1 } from "../data-grid-v1/data-grid-v1.interfaces";
import { DataGridSortIndicatorV1Props } from "./data-grid-sort-indicator-v1.interfaces";

export const DataGridSortIndicatorV1: FunctionComponent<DataGridSortIndicatorV1Props> = ({
    sortOrder,
    className,
    ...otherProps
}: DataGridSortIndicatorV1Props) => {
    return (
        <div
            {...otherProps}
            className={classNames(className, "data-grid-sort-indicator-v1")}
        >
            {/* Ascending sort indicator */}
            {sortOrder === DataGridSortOrderV1.ASC && (
                <ArrowUpwardIcon color="action" fontSize="small" />
            )}

            {/* Descending sort indicator */}
            {sortOrder === DataGridSortOrderV1.DESC && (
                <ArrowDownwardIcon color="action" fontSize="small" />
            )}
        </div>
    );
};
