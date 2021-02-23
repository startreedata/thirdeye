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
import { filterMetrics } from "../../utils/metrics/metrics.util";
import { getMetricsDetailPath } from "../../utils/routes/routes.util";
import { getSearchStatusLabel } from "../../utils/search/search.util";
import { actionsCellRenderer } from "../data-grid/actions-cell/actions-cell.component";
import { CustomCell } from "../data-grid/custom-cell/custom-cell.component";
import { DataGrid } from "../data-grid/data-grid.component";
import { linkCellRenderer } from "../data-grid/link-cell/link-cell.component";
import { MetricCardData } from "../entity-cards/metric-card/metric-card.interfaces";
import { SearchBar } from "../search-bar/search-bar.component";
import { MetricsListProps } from "./metrics-list.interfaces";
import { useMetricsListStyles } from "./metrics-list.styles";

export const MetricsList: FunctionComponent<MetricsListProps> = (
    props: MetricsListProps
) => {
    const metricsListClasses = useMetricsListStyles();
    const [filteredMetricCardDatas, setFilteredMetricCardDatas] = useState<
        MetricCardData[]
    >([]);
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
        setFilteredMetricCardDatas(
            filterMetrics(
                props.metricCardDatas as MetricCardData[],
                searchWords
            )
        );
    }, [props.metricCardDatas, searchWords]);

    useEffect(() => {
        // Search changed, re-initialize row selection
        setDataGridSelectionModel([...dataGridSelectionModel]);
    }, [searchWords]);

    const initDataGridColumns = (): void => {
        const columns: ColDef[] = [
            // Name
            {
                field: "name",
                type: "string",
                headerName: t("label.name"),
                sortable: true,
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
                headerName: t("label.dataset"),
                sortable: true,
                width: 150,
            },
            // Active/inactive
            {
                field: "active",
                type: "boolean",
                headerName: t("label.active"),
                align: "center",
                headerAlign: "center",
                sortable: true,
                width: 80,
                renderCell: metricStatusRenderer,
                sortComparator: metricStatusComparator,
            },
            // Aggregation column
            {
                field: "aggregationColumn",
                type: "string",
                headerName: t("label.aggregation-column"),
                sortable: true,
                flex: 1,
            },
            // Aggregation function
            {
                field: "aggregationFunction",
                type: "string",
                headerName: t("label.aggregation-function"),
                sortable: true,
                flex: 1,
            },
            // View count
            {
                field: "viewCount",
                type: "number",
                headerName: t("label.views"),
                align: "right",
                headerAlign: "right",
                sortable: true,
                width: 80,
            },
            // Actions
            {
                field: "id",
                headerName: t("label.actions"),
                align: "center",
                headerAlign: "center",
                sortable: false,
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
                        className={metricsListClasses.activeInactiveIcon}
                        htmlColor={theme.palette.success.main}
                    />
                )}

                {/* Inactive */}
                {!params ||
                    (!params.value && (
                        <CloseIcon
                            className={metricsListClasses.activeInactiveIcon}
                            htmlColor={theme.palette.error.main}
                        />
                    ))}
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
        history.push(getMetricsDetailPath(id));
    };

    const handleMetricViewDetailsById = (id: number): void => {
        history.push(getMetricsDetailPath(id));
    };

    const handleMetricDelete = (id: number): void => {
        const metricCardData = getMetricCardData(id);
        if (!metricCardData) {
            return;
        }

        props.onDelete && props.onDelete(metricCardData);
    };

    const getMetricCardData = (id: number): MetricCardData | null => {
        if (!props.metricCardDatas) {
            return null;
        }

        return (
            props.metricCardDatas.find(
                (metricCardData) => metricCardData.id === id
            ) || null
        );
    };

    const handleDataGridSelectionModelChange = (
        params: SelectionModelChangeParams
    ): void => {
        setDataGridSelectionModel(params.selectionModel || []);
    };

    return (
        <Grid
            container
            className={metricsListClasses.metricsList}
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
                        filteredMetricCardDatas
                            ? filteredMetricCardDatas.length
                            : 0,
                        props.metricCardDatas ? props.metricCardDatas.length : 0
                    )}
                    onChange={setSearchWords}
                />
            </Grid>

            {/* Metrics list */}
            <Grid item className={metricsListClasses.list}>
                <DataGrid
                    checkboxSelection
                    columns={dataGridColumns}
                    loading={!props.metricCardDatas}
                    noDataAvailableMessage={
                        isEmpty(filteredMetricCardDatas) &&
                        !isEmpty(searchWords)
                            ? t("message.no-search-results")
                            : ""
                    }
                    rows={filteredMetricCardDatas}
                    searchWords={searchWords}
                    selectionModel={dataGridSelectionModel}
                    onSelectionModelChange={handleDataGridSelectionModelChange}
                />
            </Grid>
        </Grid>
    );
};
