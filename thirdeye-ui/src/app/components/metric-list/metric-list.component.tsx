import { Grid, useTheme } from "@material-ui/core";
import {
    CellParams,
    CellValue,
    ColDef,
    RowId,
    SelectionModelChangeParams,
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
import { getMetricsDetailPath } from "../../utils/routes/routes.util";
import { getSearchStatusLabel } from "../../utils/search/search.util";
import { actionsCellRenderer } from "../data-grid/actions-cell/actions-cell.component";
import { CustomCell } from "../data-grid/custom-cell/custom-cell.component";
import { DataGrid } from "../data-grid/data-grid.component";
import { linkCellRenderer } from "../data-grid/link-cell/link-cell.component";
import { SearchBar } from "../search-bar/search-bar.component";
import { MetricListProps } from "./metric-list.interfaces";

export const MetricList: FunctionComponent<MetricListProps> = (
    props: MetricListProps
) => {
    const [filteredUiMetrics, setFilteredUiMetrics] = useState<UiMetric[]>([]);
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const [dataGridColumns, setDataGridColumns] = useState<ColDef[]>([]);
    const [dataGridSelectionModel, setDataGridSelectionModel] = useState<
        RowId[]
    >([]);
    const theme = useTheme();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        // Input metrics or search changed, reset
        initDataGridColumns();
        setFilteredUiMetrics(
            filterMetrics(props.uiMetrics as UiMetric[], searchWords)
        );
    }, [props.uiMetrics, searchWords]);

    useEffect(() => {
        // Search changed, reset data grid selection model
        setDataGridSelectionModel([...dataGridSelectionModel]);
    }, [searchWords]);

    const initDataGridColumns = (): void => {
        const columns: ColDef[] = [
            // Name
            {
                field: "name",
                type: "string",
                sortable: true,
                headerName: t("label.name"),
                width: 150,
                renderCell: (params) =>
                    linkCellRenderer<string>(
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
                align: "center",
                headerAlign: "center",
                headerName: t("label.active"),
                width: 80,
                renderCell: metricStatusRenderer,
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
                type: "number",
                sortable: true,
                align: "right",
                headerAlign: "right",
                headerName: t("label.views"),
                width: 80,
            },
            // Actions
            {
                field: "id",
                sortable: false,
                align: "center",
                headerAlign: "center",
                headerName: t("label.actions"),
                width: 150,
                renderCell: (params) =>
                    actionsCellRenderer(
                        params,
                        true,
                        false,
                        true,
                        handleMetricViewDetailsById,
                        undefined,
                        handleMetricDelete
                    ),
            },
        ];
        setDataGridColumns(columns);
    };

    const metricStatusRenderer = (params: CellParams): ReactElement => {
        return (
            <CustomCell params={params}>
                {/* Active */}
                {params && params.value && (
                    <CheckIcon
                        fontSize="small"
                        htmlColor={theme.palette.success.main}
                    />
                )}

                {/* Inactive */}
                {(!params || !params.value) && (
                    <CloseIcon
                        fontSize="small"
                        htmlColor={theme.palette.error.main}
                    />
                )}
            </CustomCell>
        );
    };

    const metricStatusComparator = (
        cellValue1: CellValue,
        cellValue2: CellValue
    ): number => {
        return toNumber(cellValue1) - toNumber(cellValue2);
    };

    const handleMetricViewDetailsByNameAndId = (
        _name: string,
        id: number
    ): void => {
        handleMetricViewDetailsById(id);
    };

    const handleMetricViewDetailsById = (id: number): void => {
        history.push(getMetricsDetailPath(id));
    };

    const handleMetricDelete = (id: number): void => {
        const uiMetric = getUiMetric(id);
        if (!uiMetric) {
            return;
        }

        props.onDelete && props.onDelete(uiMetric);
    };

    const getUiMetric = (id: number): UiMetric | null => {
        if (!props.uiMetrics) {
            return null;
        }

        return props.uiMetrics.find((uiMetric) => uiMetric.id === id) || null;
    };

    const handleDataGridSelectionModelChange = (
        params: SelectionModelChangeParams
    ): void => {
        setDataGridSelectionModel(params.selectionModel || []);
    };

    return (
        <Grid container>
            {/* Search */}
            {!props.hideSearchBar && (
                <Grid item xs={12}>
                    <SearchBar
                        autoFocus
                        setSearchQueryString
                        searchLabel={t("label.search-entity", {
                            entity: t("label.metrics"),
                        })}
                        searchStatusLabel={getSearchStatusLabel(
                            filteredUiMetrics ? filteredUiMetrics.length : 0,
                            props.uiMetrics ? props.uiMetrics.length : 0
                        )}
                        onChange={setSearchWords}
                    />
                </Grid>
            )}

            {/* Metric list */}
            <Grid item xs={12}>
                <DataGrid
                    autoHeight
                    checkboxSelection
                    columns={dataGridColumns}
                    loading={!props.uiMetrics}
                    noDataAvailableMessage={
                        isEmpty(filteredUiMetrics) && !isEmpty(searchWords)
                            ? t("message.no-search-results")
                            : ""
                    }
                    rows={filteredUiMetrics}
                    searchWords={searchWords}
                    selectionModel={dataGridSelectionModel}
                    onSelectionModelChange={handleDataGridSelectionModelChange}
                />
            </Grid>
        </Grid>
    );
};
