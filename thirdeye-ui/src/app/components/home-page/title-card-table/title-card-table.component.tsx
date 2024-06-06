/*
 * Copyright 2024 StarTree Inc
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
import {
    Table,
    TableHead,
    TableHeadProps,
    TableProps,
} from "@material-ui/core";
import * as React from "react";
import { useTitleCardTableStyles } from "./title-card-table.styles";

export const TitleCardTable: React.FunctionComponent<TableProps> = (props) => {
    const titleCardTableStyles = useTitleCardTableStyles();

    return <Table {...props} classes={{ root: titleCardTableStyles.table }} />;
};

export const TitleCardTableHead: React.FunctionComponent<TableHeadProps> = (
    props
) => {
    const titleCardTableStyles = useTitleCardTableStyles();

    return (
        <TableHead {...props} classes={{ root: titleCardTableStyles.header }} />
    );
};
