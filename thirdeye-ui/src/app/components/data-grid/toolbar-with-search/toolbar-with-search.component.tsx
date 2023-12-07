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
import React, { FunctionComponent } from "react";
import { Grid, InputAdornment, TextField } from "@material-ui/core";
import SearchIcon from "@material-ui/icons/Search";
import { ToolbarContainer } from "../toolbar-container/toolbar-container.component";
import { ToolbarWithSearchProps } from "./toolbar-with-search.interfaces";

export const ToolbarWithSearch: FunctionComponent<ToolbarWithSearchProps> = ({
    searchTerm,
    placeholder,
    onSearchTermChange,
    children,
}) => {
    return (
        <ToolbarContainer>
            <Grid container alignItems="center" justifyContent="space-between">
                <Grid item>{children}</Grid>
                <Grid item>
                    <TextField
                        InputProps={{
                            startAdornment: (
                                <InputAdornment position="start">
                                    <SearchIcon />
                                </InputAdornment>
                            ),
                        }}
                        defaultValue={searchTerm}
                        placeholder={placeholder}
                        onChange={onSearchTermChange}
                    />
                </Grid>
            </Grid>
        </ToolbarContainer>
    );
};
