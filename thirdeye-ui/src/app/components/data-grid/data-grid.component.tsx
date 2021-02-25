import { Box, Typography } from "@material-ui/core";
import {
    CellParams,
    classnames,
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
import { useTranslation } from "react-i18next";
import { useCommonStyles } from "../../utils/material-ui/common.styles";
import { Dimension } from "../../utils/material-ui/dimension.util";
import { Palette } from "../../utils/material-ui/palette.util";
import { formatNumber } from "../../utils/number/number.util";
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
    const { t } = useTranslation();

    useEffect(() => {
        // Input columns changed, initialize data grid columns with text highlighter
        initDataGridColumns();
    }, [props.columns]);

    useEffect(() => {
        // Input rows or selection model changed, reset data grid selection model
        setDatagridSelectionModel(props.selectionModel || []);
    }, [props.rows, props.selectionModel]);

    const initDataGridColumns = (): void => {
        if (!props.columns) {
            return;
        }

        const columns = [];
        for (const column of props.columns) {
            // For columns that don't already have a custom cell renderer, set cell renderer with
            // text highlighter
            const columnCopy = cloneDeep(column);
            (columnCopy.renderCell =
                columnCopy.renderCell || cellRendererWithTextHighlighter),
                columns.push(columnCopy);
        }
        setDataGridColumns(columns);
    };

    const cellRendererWithTextHighlighter = (
        params: CellParams
    ): ReactElement => {
        return (
            // Retain content alignment based on column properties
            <Box
                className={classnames(commonClasses.ellipsis)}
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

    const footerRenderer = (): ReactElement => {
        return (
            <Box
                border={Dimension.WIDTH_BORDER_DEFAULT}
                borderBottom={0}
                borderColor={Palette.COLOR_BORDER_LIGHT}
                borderLeft={0}
                borderRight={0}
            >
                {/* Row selection status */}
                <Typography
                    className={dataGridClasses.rowSelectionStatus}
                    color="textSecondary"
                    variant="body2"
                >
                    {t("label.selected-count", {
                        count: formatNumber(
                            dataGridSelectionModel.length
                        ) as never,
                    })}
                </Typography>
            </Box>
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
            className={dataGridClasses.dataGrid}
            columns={dataGridColumns}
            components={{
                LoadingOverlay: loadingIndicatorRenderer,
                NoRowsOverlay: noDataAvailableRenderer,
                ErrorOverlay: errorIndicatorRenderer,
                Footer: footerRenderer,
            }}
            selectionModel={dataGridSelectionModel}
            onSelectionModelChange={handleDataGridSelectionModelChange}
        />
    );
};
