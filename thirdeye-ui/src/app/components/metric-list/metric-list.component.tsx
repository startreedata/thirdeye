import { Grid, useTheme } from "@material-ui/core";
import {
    GridCellParams,
    GridCellValue,
    GridColDef,
    GridRowId,
    GridSelectionModelChangeParams,
} from "@material-ui/data-grid";
import CheckIcon from "@material-ui/icons/Check";
import CloseIcon from "@material-ui/icons/Close";
import { isEmpty, toNumber } from "lodash";
import React, {
    FunctionComponent,
    ReactElement,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { UiMetric } from "../../rest/dto/ui-metric.interfaces";
import { filterMetrics } from "../../utils/metrics/metrics.util";
import {
    getMetricsUpdatePath,
    getMetricsViewPath,
} from "../../utils/routes/routes.util";
import {
    getSearchStatusLabel,
    getSelectedStatusLabel,
} from "../../utils/search/search.util";
import { actionsCellRenderer } from "../data-grid/actions-cell/actions-cell.component";
import { customCellRenderer } from "../data-grid/custom-cell/custom-cell.component";
import { DataGrid } from "../data-grid/data-grid/data-grid.component";
import { linkCellRenderer } from "../data-grid/link-cell/link-cell.component";
import { SearchBar } from "../search-bar/search-bar.component";
import { MetricListProps } from "./metric-list.interfaces";
import { useMetricListStyles } from "./metric-list.styles";

export const MetricList: FunctionComponent<MetricListProps> = (
    props: MetricListProps
) => {
    const metricListClasses = useMetricListStyles();
    const [filteredUiMetrics, setFilteredUiMetrics] = useState<UiMetric[]>([]);
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const [dataGridColumns, setDataGridColumns] = useState<GridColDef[]>([]);
    const [dataGridSelectionModel, setDataGridSelectionModel] = useState<
        GridRowId[]
    >([]);
    const theme = useTheme();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        // Input metrics or search changed, reset
        setFilteredUiMetrics(filterMetrics(props.metrics || [], searchWords));
        initDataGridColumns();
    }, [props.metrics, searchWords]);

    useEffect(() => {
        // Search changed, reset data grid selection model
        setDataGridSelectionModel([...dataGridSelectionModel]);
    }, [searchWords]);

    const initDataGridColumns = (): void => {
        const columns: GridColDef[] = [
            // Name
            {
                field: "name",
                type: "string",
                sortable: true,
                headerName: t("label.name"),
                width: 150,
                renderCell: (params) =>
                    linkCellRenderer(
                        params,
                        searchWords,
                        handleMetricViewDetailsByNameAndId
                    ),
            },
            // Dataset
            {
                field: "datasetName",
                type: "string",
                sortable: true,
                headerName: t("label.dataset"),
                width: 150,
            },
            // Active/inactive
            {
                field: "active",
                type: "boolean",
                sortable: true,
                headerName: t("label.active"),
                align: "center",
                headerAlign: "center",
                width: 80,
                renderCell: (params) =>
                    customCellRenderer(params, metricStatusRenderer),
                sortComparator: metricStatusComparator,
            },
            // Aggregation column
            {
                field: "aggregationColumn",
                type: "string",
                sortable: true,
                headerName: t("label.aggregation-column"),
                flex: 1,
            },
            // Aggregation function
            {
                field: "aggregationFunction",
                type: "string",
                sortable: true,
                headerName: t("label.aggregation-function"),
                flex: 1,
            },
            // View count
            {
                field: "viewCount",
                type: "string",
                sortable: true,
                headerName: t("label.views"),
                align: "right",
                headerAlign: "right",
                width: 80,
                sortComparator: metricViewCountComparator,
            },
            // Actions
            {
                field: "id",
                sortable: false,
                headerName: t("label.actions"),
                align: "center",
                headerAlign: "center",
                width: 150,
                renderCell: (params) =>
                    actionsCellRenderer(
                        params,
                        true,
                        true,
                        true,
                        handleMetricViewDetailsById,
                        handleMetricEdit,
                        handleMetricDelete
                    ),
            },
        ];
        setDataGridColumns(columns);
    };

    const handleMetricEdit = (id: number): void => {
        history.push(getMetricsUpdatePath(id));
    };

    const metricStatusRenderer = (params: GridCellParams): ReactElement => {
        return (
            <>
                {/* Active */}
                {params.value && (
                    <CheckIcon
                        fontSize="small"
                        htmlColor={theme.palette.success.main}
                    />
                )}

                {/* Inactive */}
                {!params.value && (
                    <CloseIcon
                        fontSize="small"
                        htmlColor={theme.palette.error.main}
                    />
                )}
            </>
        );
    };

    const metricStatusComparator = (
        value1: GridCellValue,
        value2: GridCellValue
    ): number => {
        return toNumber(value1) - toNumber(value2);
    };

    const metricViewCountComparator = (
        _value1: GridCellValue,
        _value2: GridCellValue,
        params1: GridCellParams,
        params2: GridCellParams
    ): number => {
        const uiMetric1 = getUiMetric(params1.row && params1.row.rowId);
        const uiMetric2 = getUiMetric(params2.row && params2.row.rowId);

        if (!uiMetric1 || !uiMetric1.views) {
            return -1;
        }

        if (!uiMetric2 || !uiMetric2.views) {
            return 1;
        }

        return uiMetric1.views.length - uiMetric2.views.length;
    };

    const handleMetricViewDetailsByNameAndId = (
        _name: string,
        id: number
    ): void => {
        handleMetricViewDetailsById(id);
    };

    const handleMetricViewDetailsById = (id: number): void => {
        history.push(getMetricsViewPath(id));
    };

    const handleMetricDelete = (id: number): void => {
        const uiMetric = getUiMetric(id);
        if (!uiMetric) {
            return;
        }

        props.onDelete && props.onDelete(uiMetric);
    };

    const getUiMetric = (id: number): UiMetric | null => {
        if (!props.metrics) {
            return null;
        }

        return props.metrics.find((metric) => metric.id === id) || null;
    };

    const handleDataGridSelectionModelChange = (
        params: GridSelectionModelChangeParams
    ): void => {
        setDataGridSelectionModel(params.selectionModel || []);
    };

    return (
        <Grid
            container
            className={metricListClasses.metricList}
            direction="column"
        >
            {/* Search */}
            {!props.hideSearchBar && (
                <Grid item>
                    <SearchBar
                        autoFocus
                        setSearchQueryString
                        searchLabel={t("label.search-entity", {
                            entity: t("label.metrics"),
                        })}
                        searchStatusLabel={getSearchStatusLabel(
                            filteredUiMetrics ? filteredUiMetrics.length : 0,
                            props.metrics ? props.metrics.length : 0
                        )}
                        onChange={setSearchWords}
                    />
                </Grid>
            )}

            {/* Metric list */}
            <Grid item className={metricListClasses.dataGrid}>
                <DataGrid
                    autoHeight
                    checkboxSelection
                    columns={dataGridColumns}
                    loading={!props.metrics}
                    noDataAvailableMessage={
                        isEmpty(filteredUiMetrics) && !isEmpty(searchWords)
                            ? t("message.no-search-results")
                            : ""
                    }
                    rows={filteredUiMetrics}
                    searchWords={searchWords}
                    selectedStatusLabelFn={getSelectedStatusLabel}
                    selectionModel={dataGridSelectionModel}
                    onSelectionModelChange={handleDataGridSelectionModelChange}
                />
            </Grid>
        </Grid>
    );
};
