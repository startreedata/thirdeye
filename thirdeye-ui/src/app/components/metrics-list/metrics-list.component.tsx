import { Grid } from "@material-ui/core";
import {
    CellParams,
    ColDef,
    RowId,
    SelectionModelChangeParams,
} from "@material-ui/data-grid";
import CheckIcon from "@material-ui/icons/Check";
import CloseIcon from "@material-ui/icons/Close";
import { isEmpty } from "lodash";
import React, {
    FunctionComponent,
    ReactElement,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { theme } from "../../utils/material-ui/theme.util";
import { filterMetrics } from "../../utils/metrics-util/metrics-util";
import { getMetricsDetailPath } from "../../utils/routes/routes.util";
import { getSearchStatusLabel } from "../../utils/search/search.util";
import { ActionCell } from "../data-grid/action-cell/action-cell.component";
import { DataGrid } from "../data-grid/data-grid.component";
import { SearchBar } from "../search-bar/search-bar.component";
import { MetricsListData, MetricsListProps } from "./metrics-list.interfaces";
import { useMetricsListStyles } from "./metrics-list.styles";

export const MetricsList: FunctionComponent<MetricsListProps> = (
    props: MetricsListProps
) => {
    const metricListClasses = useMetricsListStyles();
    const [filteredMetricsListDatas, setFilteredMetricsListDatas] = useState<
        MetricsListData[]
    >([]);
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const [columns, setColumns] = useState<ColDef[]>([]);
    const [dataGridSelectionModel, setDatagridSelectionModel] = useState<
        RowId[]
    >([]);
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        initColumns();
        setFilteredMetricsListDatas(
            filterMetrics(props.metrics as MetricsListData[], searchWords)
        );
    }, [props.metrics, searchWords]);

    useEffect(() => {
        // Search changed, re-initialize row selection
        setDatagridSelectionModel([...dataGridSelectionModel]);
    }, [searchWords]);

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

    const actionsRenderer = (params: CellParams): ReactElement => {
        return (
            <ActionCell
                delete
                edit
                viewDetails
                id={params.value as number}
                onDelete={handleMetricDelete}
                /* onEdit={handleMetricEdit} */
                onViewDetails={handleMetricViewDetails}
            />
        );
    };

    const initColumns = (): void => {
        const columns: ColDef[] = [
            // Name
            {
                field: "name",
                headerName: t("label.name"),
                flex: 1,
            },
            // Dataset Name
            {
                field: "datasetName",
                headerName: t("label.dataset"),
                flex: 1,
            },
            // Active
            {
                field: "active",
                headerName: t("label.active"),
                flex: 1,
                renderCell: renderActiveCell,
            },
            // Aggregation function
            {
                field: "aggregationFunction",
                headerName: t("label.function"),
                flex: 1,
            },
            // Rollup threshold
            {
                field: "rollupThresholdText",
                headerName: t("label.threshold"),
                flex: 1,
            },
            // Actions
            {
                field: "id",
                type: "number",
                headerName: t("label.actions"),
                headerAlign: "right",
                sortable: false,
                flex: 1,
                renderCell: actionsRenderer,
            },
        ];
        setColumns(columns);
    };

    const handleMetricViewDetails = (id: number): void => {
        history.push(getMetricsDetailPath(id));
    };

    /* const handleMetricEdit = (id: number): void => {
        
    };*/

    const handleMetricDelete = (id: number): void => {
        const metricsListData = getMetricTableData(id);
        if (!metricsListData) {
            return;
        }

        props.onDelete && props.onDelete(metricsListData);
    };

    const getMetricTableData = (id: number): MetricsListData | null => {
        if (!props.metrics) {
            return null;
        }

        return props.metrics.find((metric) => metric.id === id) || null;
    };

    const handleDataGridSelectionModelChange = (
        param: SelectionModelChangeParams
    ): void => {
        setDatagridSelectionModel(param.selectionModel || []);
    };

    return (
        <Grid
            container
            className={metricListClasses.metricsList}
            direction="column"
        >
            {/* Search */}
            <Grid item>
                <SearchBar
                    autoFocus
                    setSearchQueryString
                    searchLabel={t("label.search-entity", {
                        entity: t("label.metrics"),
                    })}
                    searchStatusLabel={getSearchStatusLabel(
                        filteredMetricsListDatas
                            ? filteredMetricsListDatas.length
                            : 0,
                        props.metrics ? props.metrics.length : 0
                    )}
                    onChange={setSearchWords}
                />
            </Grid>
            {/* Metrics list */}
            <Grid item className={metricListClasses.list}>
                <DataGrid
                    columns={columns}
                    loading={!props.metrics}
                    noDataAvailableMessage={
                        isEmpty(filteredMetricsListDatas) &&
                        !isEmpty(searchWords)
                            ? t("message.no-search-results")
                            : ""
                    }
                    rowSelectionCount={dataGridSelectionModel.length}
                    rows={filteredMetricsListDatas}
                    searchWords={searchWords}
                    selectionModel={dataGridSelectionModel}
                    onSelectionModelChange={handleDataGridSelectionModelChange}
                />
            </Grid>
        </Grid>
    );
};
