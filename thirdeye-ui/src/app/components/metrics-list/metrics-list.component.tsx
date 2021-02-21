import { Box, Grid, IconButton, Toolbar, Typography } from "@material-ui/core";
import { CellParams, ColDef, DataGrid, RowId } from "@material-ui/data-grid";
import CheckIcon from "@material-ui/icons/Check";
import CloseIcon from "@material-ui/icons/Close";
import DeleteIcon from "@material-ui/icons/Delete";
import EditIcon from "@material-ui/icons/Edit";
import VisibilityIcon from "@material-ui/icons/Visibility";
import { isEmpty } from "lodash";
import React, {
    FunctionComponent,
    ReactElement,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { theme } from "../../utils/material-ui/theme.util";
import { filterMetrics } from "../../utils/metrics-util/metrics-util";
import { NoDataIndicator } from "../no-data-indicator/no-data-indicator.component";
import { SearchBar } from "../search-bar/search-bar.component";
import { TextHighlighter } from "../text-highlighter/text-highlighter.component";
import { MetricsListData, MetricsListProps } from "./metrics-list.interfaces";
import { useMetricsListStyles } from "./metrics-list.styles";

export const MetricsList: FunctionComponent<MetricsListProps> = (
    props: MetricsListProps
) => {
    const [selectedRows, setSelectedRows] = useState<RowId[]>([]);
    const [filteredRecords, setFilteredRecords] = useState<MetricsListData[]>(
        []
    );
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const { t } = useTranslation();

    const metricListClasses = useMetricsListStyles();

    useEffect(() => {
        setFilteredRecords(filterMetrics(props.metrics, searchWords));
    }, [props.metrics, searchWords]);

    const renderCellWithHighlighter = (text: CellParams): ReactElement => (
        <TextHighlighter
            searchWords={searchWords}
            text={text.value as string}
        />
    );

    const renderActiveCell = (text: CellParams): ReactElement => {
        return (
            <>
                {text.value ? (
                    <CheckIcon
                        style={{
                            color: theme.palette.success.main,
                        }}
                    />
                ) : (
                    <CloseIcon
                        style={{
                            color: theme.palette.error.main,
                        }}
                    />
                )}
            </>
        );
    };

    const columns: ColDef[] = [
        {
            field: "idText",
            headerName: t("label.id"),
            align: "right",
            flex: 0.5,
            sortable: false,
            renderCell: renderCellWithHighlighter,
        },
        {
            field: "name",
            headerName: t("label.name"),
            flex: 1,
            renderCell: renderCellWithHighlighter,
        },
        {
            field: "datasetName",
            headerName: t("label.dataset"),
            flex: 1,
            renderCell: renderCellWithHighlighter,
        },
        {
            field: "active",
            headerName: t("label.active"),
            flex: 1,
            renderCell: renderActiveCell,
        },
        {
            field: "aggregationFunction",
            headerName: t("label.function"),
            flex: 1,
            renderCell: renderCellWithHighlighter,
        },
        {
            field: "rollupThresholdText",
            headerName: t("label.threshold"),
            flex: 1,
            renderCell: renderCellWithHighlighter,
        },
    ];

    const handleSearch = (searchWords: string[]): void => {
        setSearchWords(searchWords);
    };

    return (
        <>
            <Grid container>
                {/* Toolbar for table */}
                <Toolbar className={metricListClasses.toolbar}>
                    {/* View button */}
                    <IconButton
                        color="primary"
                        disabled={selectedRows.length !== 1}
                    >
                        <VisibilityIcon />
                    </IconButton>
                    {/* Edit button */}
                    <IconButton
                        color="primary"
                        disabled={selectedRows.length !== 1}
                    >
                        <EditIcon />
                    </IconButton>
                    {/* Delete button */}
                    <IconButton
                        color="primary"
                        disabled={selectedRows.length === 0}
                    >
                        <DeleteIcon />
                    </IconButton>

                    <Typography
                        className={metricListClasses.rightAlign}
                        color="textSecondary"
                        variant="button"
                    >
                        {selectedRows.length
                            ? selectedRows.length + " selected"
                            : ""}
                    </Typography>

                    {/* Searchbar */}
                    <Box className={metricListClasses.searchContainer}>
                        <SearchBar
                            searchStatusLabel={t("label.search-count", {
                                count: filteredRecords
                                    ? filteredRecords.length
                                    : 0,
                                total: props.metrics ? props.metrics.length : 0,
                            })}
                            onChange={handleSearch}
                        />
                    </Box>
                </Toolbar>

                {/* Table */}
                <Grid item sm={12}>
                    {!isEmpty(filteredRecords) && (
                        <DataGrid
                            autoHeight
                            checkboxSelection
                            disableColumnMenu
                            hideFooter
                            className={metricListClasses.root}
                            columns={columns}
                            rows={filteredRecords}
                            onSelectionChange={(rowSelection): void => {
                                setSelectedRows(rowSelection.rowIds);
                            }}
                        />
                    )}
                </Grid>
            </Grid>

            {/* No search results available message */}
            {isEmpty(filteredRecords) && !isEmpty(searchWords) && (
                <NoDataIndicator text={t("message.no-search-results")} />
            )}

            {/* No data available message */}
            {isEmpty(filteredRecords) && isEmpty(searchWords) && (
                <NoDataIndicator />
            )}
        </>
    );
};
