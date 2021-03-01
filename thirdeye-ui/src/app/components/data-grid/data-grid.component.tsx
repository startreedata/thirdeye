import { Box, Toolbar, Typography } from "@material-ui/core";
import {
    CellParams,
    ColDef,
    DataGrid as MuiDataGrid,
    GridOverlay,
    RowId,
    SelectionModelChangeParams,
} from "@material-ui/data-grid";
import { cloneDeep } from "lodash";
import React, {
    FunctionComponent,
    ReactElement,
    useEffect,
    useState,
} from "react";
import { useCommonStyles } from "../../utils/material-ui/common.styles";
import { getSelectedStatusLabel } from "../../utils/search/search.util";
import { ErrorIndicator } from "../error-indicator/error-indicator.component";
import { LoadingIndicator } from "../loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../no-data-indicator/no-data-indicator.component";
import { TextHighlighter } from "../text-highlighter/text-highlighter.component";
import { DataGridProps } from "./data-grid.interfaces";
import { useDataGridStyles } from "./data-grid.styles";

export const DataGrid: FunctionComponent<DataGridProps> = (
    props: DataGridProps
) => {
    const dataGridClasses = useDataGridStyles();
    const commonClasses = useCommonStyles();
    const [dataGridColumns, setDataGridColumns] = useState<ColDef[]>([]);
    const [dataGridSelectionModel, setDatagridSelectionModel] = useState<
        RowId[]
    >([]);

    useEffect(() => {
        // Input columns changed, initialize data grid columns
        initDataGridColumns();
    }, [props.columns]);

    useEffect(() => {
        // Input rows or selection model changed, reset data grid selection model
        setDatagridSelectionModel(props.selectionModel || []);
    }, [props.rows, props.selectionModel]);

    const initDataGridColumns = (): void => {
        const columns: ColDef[] = [];

        if (!props.columns) {
            setDataGridColumns(columns);

            return;
        }

        for (const column of props.columns) {
            // For columns that don't already have a custom cell renderer, set cell renderer with
            // text highlighter
            const columnCopy = cloneDeep(column);
            columnCopy.renderCell =
                columnCopy.renderCell || cellRendererWithTextHighlighter;
            columns.push(columnCopy);
        }
        setDataGridColumns(columns);
    };

    const cellRendererWithTextHighlighter = (
        params: CellParams
    ): ReactElement => {
        return (
            <Box
                className={commonClasses.ellipsis}
                textAlign={params.colDef && params.colDef.align}
                width="100%"
            >
                <TextHighlighter
                    searchWords={props.searchWords}
                    text={params.value as string}
                />
            </Box>
        );
    };

    const toolbarRenderer = (): ReactElement => {
        return (
            <>
                {(props.showToolbar || props.checkboxSelection) && (
                    <Toolbar
                        className={dataGridClasses.toolbar}
                        variant="dense"
                    >
                        {/* Row selection status */}
                        {props.checkboxSelection && (
                            <Typography
                                className={dataGridClasses.rowSelectionStatus}
                                color="textSecondary"
                                variant="body1"
                            >
                                {getSelectedStatusLabel(
                                    dataGridSelectionModel
                                        ? dataGridSelectionModel.length
                                        : 0
                                )}
                            </Typography>
                        )}
                    </Toolbar>
                )}
            </>
        );
    };

    const loadingIndicatorRenderer = (): ReactElement => {
        return (
            <GridOverlay>
                <LoadingIndicator />
            </GridOverlay>
        );
    };

    const noDataAvailableRenderer = (): ReactElement => {
        return (
            <GridOverlay>
                <NoDataIndicator text={props.noDataAvailableMessage} />
            </GridOverlay>
        );
    };

    const errorIndicatorRenderer = (): ReactElement => {
        return (
            <GridOverlay>
                <ErrorIndicator />
            </GridOverlay>
        );
    };

    const handleDataGridSelectionModelChange = (
        params: SelectionModelChangeParams
    ): void => {
        setDatagridSelectionModel(params.selectionModel || []);
        props.onSelectionModelChange && props.onSelectionModelChange(params);
    };

    return (
        <MuiDataGrid
            {...props}
            disableColumnMenu
            hideFooter
            className={dataGridClasses.dataGrid}
            columns={dataGridColumns}
            components={{
                Toolbar: toolbarRenderer,
                LoadingOverlay: loadingIndicatorRenderer,
                NoRowsOverlay: noDataAvailableRenderer,
                ErrorOverlay: errorIndicatorRenderer,
            }}
            selectionModel={dataGridSelectionModel}
            onSelectionModelChange={handleDataGridSelectionModelChange}
        />
    );
};
