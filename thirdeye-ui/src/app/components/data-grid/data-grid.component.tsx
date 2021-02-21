import { CellParams, DataGrid as MuiDataGrid } from "@material-ui/data-grid";
import { isFunction } from "lodash";
import React, { FunctionComponent, ReactElement } from "react";
import { TextHighlighter } from "../text-highlighter/text-highlighter.component";
import { DataGridProps } from "./data-grid.interfaces";
import { dataGridStyles } from "./data-grid.styles";

export const DataGrid: FunctionComponent<DataGridProps> = (
    props: DataGridProps
) => {
    const dataGridClasses = dataGridStyles();

    const renderCellWithHighlighter = (text: CellParams): ReactElement => (
        <TextHighlighter
            searchWords={props.searchWords}
            text={text.value as string}
        />
    );

    const columns = props.dataGrid.columns.map((column) => {
        if (column.renderCell && isFunction(column.renderCell)) {
            return column;
        }

        return { ...column, renderCell: renderCellWithHighlighter };
    });

    const gridData = {
        ...props.dataGrid,
        columns,
    };

    console.log(props);

    return (
        <MuiDataGrid
            autoHeight
            checkboxSelection
            disableColumnMenu
            hideFooter
            className={dataGridClasses.root}
            {...gridData}
        />
    );
};
