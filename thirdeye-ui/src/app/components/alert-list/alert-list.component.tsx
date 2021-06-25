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
import { UiAlert } from "../../rest/dto/ui-alert.interfaces";
import { filterAlerts } from "../../utils/alerts/alerts.util";
import {
    getAlertsUpdatePath,
    getAlertsViewPath,
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
import { AlertListProps } from "./alert-list.interfaces";
import { useAlertListStyles } from "./alert-list.styles";

export const AlertList: FunctionComponent<AlertListProps> = (
    props: AlertListProps
) => {
    const alertListClasses = useAlertListStyles();
    const [filteredUiAlerts, setFilteredUiAlerts] = useState<UiAlert[]>([]);
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const { t } = useTranslation();
    const [dataGridColumns, setDataGridColumns] = useState<GridColDef[]>([]);
    const [dataGridSelectionModel, setDataGridSelectionModel] = useState<
        GridRowId[]
    >([]);
    const theme = useTheme();
    const history = useHistory();

    useEffect(() => {
        // Input alerts or search changed, reset
        setFilteredUiAlerts(filterAlerts(props.alerts || [], searchWords));
        initDataGridColumns();
    }, [props.alerts, searchWords]);

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
                        handleAlertViewDetailsByNameAndId
                    ),
            },
            // Created by
            {
                field: "createdBy",
                type: "string",
                sortable: true,
                headerName: t("label.created-by"),
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
                    customCellRenderer(params, alertStatusRenderer),
                sortComparator: alertStatusComparator,
            },
            // Detection types
            {
                field: "detectionTypesCount",
                type: "string",
                sortable: true,
                headerName: t("label.detection-type"),
                width: 80,
                sortComparator: countComparator,
            },
            // Dataset & Metrics
            {
                field: "datasetAndMetricsCount",
                type: "string",
                sortable: true,
                headerName: `${t("label.dataset")}${t(
                    "label.pair-separator"
                )}${t("label.metric")}`,
                width: 80,
                sortComparator: countComparator,
            },
            // Filtered by
            {
                field: "filteredByCount",
                type: "string",
                sortable: true,
                headerName: t("label.filtered-by"),
                width: 80,
                sortComparator: countComparator,
            },
            // Subscription Groups
            {
                field: "subscriptionGroupsCount",
                type: "string",
                sortable: true,
                headerName: t("label.subscription-groups"),
                width: 80,
                sortComparator: countComparator,
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
                        handleAlertViewDetailsById,
                        handleAlertEditDetailsById,
                        handleAlertDelete
                    ),
            },
        ];
        setDataGridColumns(columns);
    };

    const alertStatusRenderer = (params: GridCellParams): ReactElement => {
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

    const alertStatusComparator = (
        value1: GridCellValue,
        value2: GridCellValue
    ): number => {
        return toNumber(value1) - toNumber(value2);
    };

    const countComparator = (
        value1: GridCellValue,
        value2: GridCellValue
    ): number => {
        return toNumber(value1) - toNumber(value2);
    };

    const handleAlertViewDetailsByNameAndId = (
        _name: string,
        id: number
    ): void => {
        handleAlertViewDetailsById(id);
    };

    const handleAlertViewDetailsById = (id: number): void => {
        history.push(getAlertsViewPath(id));
    };

    const handleAlertEditDetailsById = (id: number): void => {
        history.push(getAlertsUpdatePath(id));
    };

    const handleAlertDelete = (id: number): void => {
        const uiAlert = getUiAlert(id);
        if (!uiAlert) {
            return;
        }

        props.onDelete && props.onDelete(uiAlert);
    };

    const getUiAlert = (id: number): UiAlert | null => {
        if (!props.alerts) {
            return null;
        }

        return props.alerts.find((alert) => alert.id === id) || null;
    };

    const handleDataGridSelectionModelChange = (
        params: GridSelectionModelChangeParams
    ): void => {
        setDataGridSelectionModel(params.selectionModel || []);
    };

    return (
        <Grid
            container
            className={alertListClasses.alertList}
            direction="column"
        >
            {/* Search */}
            {!props.hideSearchBar && (
                <Grid item>
                    <SearchBar
                        autoFocus
                        setSearchQueryString
                        searchLabel={t("label.search-entity", {
                            entity: t("label.alerts"),
                        })}
                        searchStatusLabel={getSearchStatusLabel(
                            filteredUiAlerts ? filteredUiAlerts.length : 0,
                            props.alerts ? props.alerts.length : 0
                        )}
                        onChange={setSearchWords}
                    />
                </Grid>
            )}

            {/* Alert list */}
            <Grid item className={alertListClasses.dataGrid}>
                <DataGrid
                    autoHeight
                    checkboxSelection
                    columns={dataGridColumns}
                    loading={!props.alerts}
                    noDataAvailableMessage={
                        isEmpty(filteredUiAlerts) && !isEmpty(searchWords)
                            ? t("message.no-search-results")
                            : ""
                    }
                    rows={filteredUiAlerts}
                    searchWords={searchWords}
                    selectedStatusLabelFn={getSelectedStatusLabel}
                    selectionModel={dataGridSelectionModel}
                    onSelectionModelChange={handleDataGridSelectionModelChange}
                />
            </Grid>
        </Grid>
    );
};
