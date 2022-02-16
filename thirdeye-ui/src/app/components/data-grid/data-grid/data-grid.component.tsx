import { Box, Toolbar, Typography } from "@material-ui/core";
import {
    DataGrid as MuiDataGrid,
    GridCellParams,
    GridColDef,
    GridOverlay,
    GridRowId,
    GridSelectionModelChangeParams,
} from "@material-ui/data-grid";
import { cloneDeep } from "lodash";
import React, {
    FunctionComponent,
    ReactElement,
    useEffect,
    useState,
} from "react";
import { AppLoadingIndicatorV1 } from "../../../platform/components";
import { ErrorIndicator } from "../../error-indicator/error-indicator.component";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { DataGridProps } from "./data-grid.interfaces";
import { useDataGridStyles } from "./data-grid.styles";

export const DataGrid: FunctionComponent<DataGridProps> = (
    props: DataGridProps
) => {
    const dataGridClasses = useDataGridStyles();
    const [dataGridColumns, setDataGridColumns] = useState<GridColDef[]>([]);
    const [dataGridSelectionModel, setDatagridSelectionModel] = useState<
        GridRowId[]
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
        const columns: GridColDef[] = [];

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
        params: GridCellParams
    ): ReactElement => {
        return (
            <Box textAlign={params.colDef && params.colDef.align} width="100%">
                <Typography noWrap variant="body1">
                    <TextHighlighter
                        searchWords={props.searchWords}
                        text={params.value as string}
                    />
                </Typography>
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
                        {/* Toolbar items */}
                        {props.toolbarItems}

                        {/* Row selection status */}
                        {props.checkboxSelection && (
                            <Typography
                                className={dataGridClasses.rowSelectionStatus}
                                color="textSecondary"
                                variant="body1"
                            >
                                {props.selectedStatusLabelFn &&
                                    props.selectedStatusLabelFn(
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
                <AppLoadingIndicatorV1 />
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
                <ErrorIndicator text={props.errorMessage} />
            </GridOverlay>
        );
    };

    const handleDataGridSelectionModelChange = (
        params: GridSelectionModelChangeParams
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
