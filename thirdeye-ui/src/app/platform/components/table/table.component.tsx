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
import React, { ReactElement } from "react";
import {
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
} from "@material-ui/core";
import { TableProps } from "./table.interfaces";
import { useStyles } from "./table.styles";
import { isEmpty } from "lodash";

const DataTable = ({
    data,
    columns,
    emptyStateView,
}: TableProps): ReactElement => {
    const tableStyles = useStyles();

    return (
        <TableContainer>
            <Table aria-label="simple table" className={tableStyles.table}>
                <TableHead className={tableStyles.columnHeaders}>
                    <TableRow>
                        {columns.map((column) => {
                            return (
                                <TableCell align="left" key={column.title}>
                                    {column.title}
                                </TableCell>
                            );
                        })}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {data.map((rowData, idx) => (
                        <TableRow key={idx}>
                            {columns.map((column) => {
                                return (
                                    <TableCell align="left" key={column.title}>
                                        {column.customRender
                                            ? column.customRender(rowData)
                                            : rowData[column.datakey]}
                                    </TableCell>
                                );
                            })}
                        </TableRow>
                    ))}
                    {isEmpty(data) && (
                        <div className={tableStyles.emptyView}>
                            {emptyStateView}
                        </div>
                    )}
                </TableBody>
            </Table>
        </TableContainer>
    );
};

export default DataTable;
