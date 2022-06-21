/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import ArrowDownwardIcon from "@material-ui/icons/ArrowDownward";
import ArrowUpwardIcon from "@material-ui/icons/ArrowUpward";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { DataGridSortOrderV1 } from "../data-grid-v1/data-grid-v1.interfaces";
import { DataGridSortIndicatorV1Props } from "./data-grid-sort-indicator-v1.interfaces";

export const DataGridSortIndicatorV1: FunctionComponent<
    DataGridSortIndicatorV1Props
> = ({ sortOrder, className, ...otherProps }) => {
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
