import { Box, Typography } from "@material-ui/core";
import {
    CellParams,
    classnames,
    ColDef,
    DataGrid as MuiDataGrid,
    GridOverlay,
} from "@material-ui/data-grid";
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
    const { t } = useTranslation();

    useEffect(() => {
        // Input columns changed, initialize columns with text highlighter
        initDataGridColumns();
    }, [props.columns]);

    const initDataGridColumns = (): void => {
        if (!props.columns) {
            return;
        }

        const columns = [];
        for (const column of props.columns) {
            columns.push({
                ...column,
                // For columns that don't already have a custom cell renderer
                renderCell:
                    column.renderCell || cellRendererWithTextHighlighter,
            });
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

    const loadingIndicatorRenderer: FunctionComponent = () => {
        return (
            <GridOverlay>
                <LoadingIndicator />
            </GridOverlay>
        );
    };

    const noDataAvailableRenderer: FunctionComponent = () => {
        return (
            <GridOverlay>
                <NoDataIndicator text={props.noDataAvailableMessage} />
            </GridOverlay>
        );
    };

    const errorIndicatorRenderer: FunctionComponent = () => {
        return (
            <GridOverlay>
                <ErrorIndicator />
            </GridOverlay>
        );
    };

    const footerRenderer: FunctionComponent = () => {
        return (
            <Box
                border={Dimension.WIDTH_BORDER_DEFAULT}
                borderBottom={0}
                borderColor={Palette.COLOR_BORDER_LIGHT}
                borderLeft={0}
                borderRight={0}
            >
                <Typography
                    className={dataGridClasses.rowSelectionStatus}
                    color="textSecondary"
                    variant="body2"
                >
                    {t("label.selected-count", {
                        count: formatNumber(
                            props.rowSelectionCount || 0
                        ) as never,
                    })}
                </Typography>
            </Box>
        );
    };

    return (
        <MuiDataGrid
            {...props}
            checkboxSelection
            disableColumnMenu
            hideFooterPagination
            hideFooterRowCount
            className={dataGridClasses.dataGrid}
            columns={dataGridColumns}
            components={{
                LoadingOverlay: loadingIndicatorRenderer,
                NoRowsOverlay: noDataAvailableRenderer,
                ErrorOverlay: errorIndicatorRenderer,
                Footer: footerRenderer,
            }}
            localeText={{ noRowsLabel: "" }}
        />
    );
};
