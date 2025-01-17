/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import React, { FunctionComponent, useState } from "react";

import { DataGrid, GridLinkOperator, GridFilterModel } from "@mui/x-data-grid";
import { makeStyles } from "@material-ui/core";
import { StyledDataGridInterfaces } from "./styled-data-grid.interfaces";
import { ToolbarContainer } from "./toolbar-container/toolbar-container.component";
import { debounce } from "lodash";
import { ToolbarWithSearch } from "./toolbar-with-search/toolbar-with-search.component";

const useStyles = makeStyles(() => ({
    root: {
        "& .MuiDataGrid-columnHeaderWrapper": {
            backgroundColor: "#EAECF6",
            "& .MuiDataGrid-columnHeaderTitle": {
                fontWeight: "500",
            },
        },

        "& .MuiDataGrid-columnSeparator": {
            display: "none",
        },
    },
}));

export const StyledDataGrid: FunctionComponent<StyledDataGridInterfaces> = ({
    toolbar,
    searchBarProps,
    ...params
}) => {
    const classes = useStyles();
    const [filterModel, setFilterModel] = useState<GridFilterModel>();
    const [searchTerm, setSearchTerm] = useState<string>("");

    const handleFilterChange = (newValue: string): void => {
        setFilterModel(() => {
            return {
                items: [
                    {
                        columnField: searchBarProps?.searchKey,
                        value: newValue,
                        operatorValue: "contains",
                    },
                ],
                logicOperator: GridLinkOperator.Or,
            };
        });
    };
    const debouncedOnChange = debounce(handleFilterChange, 200);
    const handleSearchInputChange = (
        e: React.ChangeEvent<HTMLTextAreaElement | HTMLInputElement>
    ): void => {
        setSearchTerm(e.currentTarget.value);
        debouncedOnChange(e.currentTarget.value);
    };

    return (
        <DataGrid
            {...params}
            className={classes.root}
            components={{
                Toolbar: toolbar
                    ? searchBarProps
                        ? ToolbarWithSearch
                        : ToolbarContainer
                    : undefined,
            }}
            componentsProps={{
                toolbar: {
                    children: toolbar,
                    searchTerm,
                    onSearchTermChange: handleSearchInputChange,
                    placeholder: searchBarProps?.placeholder,
                },
            }}
            filterModel={filterModel}
            onFilterModelChange={setFilterModel}
        />
    );
};
