import { Grid } from "@material-ui/core";
import {
    GridColDef,
    GridRowId,
    GridSelectionModelChangeParams,
} from "@material-ui/data-grid";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { filterAnomalies } from "../../utils/anomalies/anomalies.util";
import {
    getAlertsViewPath,
    getAnomaliesViewPath,
} from "../../utils/routes/routes.util";
import {
    getSearchStatusLabel,
    getSelectedStatusLabel,
} from "../../utils/search/search.util";
import { actionsCellRenderer } from "../data-grid/actions-cell/actions-cell.component";
import { DataGrid } from "../data-grid/data-grid/data-grid.component";
import { linkCellRenderer } from "../data-grid/link-cell/link-cell.component";
import { SearchBar } from "../search-bar/search-bar.component";
import { AnomalyListProps } from "./anomaly-list.interfaces";
import { useAnomalyListStyles } from "./anomaly-list.styles";

export const AnomalyList: FunctionComponent<AnomalyListProps> = (
    props: AnomalyListProps
) => {
    const [filteredUiAnomalies, setFilteredUiAnomalies] = useState<UiAnomaly[]>(
        []
    );
    const anomalyListClasses = useAnomalyListStyles();
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const { t } = useTranslation();

    const [dataGridColumns, setDataGridColumns] = useState<GridColDef[]>([]);
    const [dataGridSelectionModel, setDataGridSelectionModel] = useState<
        GridRowId[]
    >([]);
    const history = useHistory();

    useEffect(() => {
        // Input anomalies or search changed, reset
        setFilteredUiAnomalies(
            filterAnomalies(props.anomalies || [], searchWords)
        );
        initDataGridColumns();
    }, [props.anomalies, searchWords]);

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
                flex: 1,
                renderCell: (params) =>
                    linkCellRenderer(
                        params,
                        searchWords,
                        handleAnomalyViewDetailsByNameAndId
                    ),
            },
            // Alert
            {
                field: "alertName",
                type: "string",
                sortable: true,
                headerName: t("label.alert"),
                width: 150,
                renderCell: (params) =>
                    linkCellRenderer(
                        params,
                        searchWords,
                        handleAlertViewDetailsByNameAndId
                    ),
            },
            // Duration
            {
                field: "duration",
                type: "string",
                headerName: t("label.duration"),
                width: 180,
            },
            // Start
            {
                field: "startTime",
                type: "string",
                headerName: t("label.start"),
                width: 200,
            },
            // End
            {
                field: "endTime",
                type: "string",
                headerName: t("label.end"),
                width: 200,
            },
            // Current
            {
                field: "current",
                type: "string",
                sortable: true,
                headerName: t("label.current"),
                align: "right",
                headerAlign: "right",
            },
            // Predicted
            {
                field: "predicted",
                type: "string",
                sortable: true,
                headerName: t("label.predicted"),
                align: "right",
                headerAlign: "right",
            },
            // Deviation
            {
                field: "deviation",
                type: "string",
                sortable: true,
                headerName: t("label.deviation"),
                align: "right",
                headerAlign: "right",
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
                        false,
                        true,
                        handleAnomalyViewDetailsById,
                        undefined,
                        handleAnomalyDelete
                    ),
            },
        ];
        setDataGridColumns(columns);
    };

    const handleAnomalyViewDetailsByNameAndId = (
        _name: string,
        id: number
    ): void => {
        handleAnomalyViewDetailsById(id);
    };

    const handleAnomalyViewDetailsById = (id: number): void => {
        history.push(getAnomaliesViewPath(id));
    };

    const handleAlertViewDetailsByNameAndId = (
        _name: string,
        id: number
    ): void => {
        history.push(getAlertsViewPath(id));
    };

    const handleAnomalyDelete = (id: number): void => {
        const uiAnomaly = getUiAnomaly(id);
        if (!uiAnomaly) {
            return;
        }

        props.onDelete && props.onDelete(uiAnomaly);
    };

    const getUiAnomaly = (id: number): UiAnomaly | null => {
        if (!props.anomalies) {
            return null;
        }

        return props.anomalies.find((anomaly) => anomaly.id === id) || null;
    };

    const handleDataGridSelectionModelChange = (
        params: GridSelectionModelChangeParams
    ): void => {
        setDataGridSelectionModel(params.selectionModel || []);
    };

    return (
        <Grid
            container
            className={anomalyListClasses.anomalyList}
            direction="column"
        >
            {/* Search */}
            {!props.hideSearchBar && (
                <Grid item>
                    <SearchBar
                        autoFocus
                        setSearchQueryString
                        searchLabel={t("label.search-entity", {
                            entity: t("label.anomalies"),
                        })}
                        searchStatusLabel={getSearchStatusLabel(
                            filteredUiAnomalies
                                ? filteredUiAnomalies.length
                                : 0,
                            props.anomalies ? props.anomalies.length : 0
                        )}
                        onChange={setSearchWords}
                    />
                </Grid>
            )}

            {/* Anomaly list */}
            <Grid item className={anomalyListClasses.dataGrid}>
                <DataGrid
                    autoHeight
                    checkboxSelection
                    columns={dataGridColumns}
                    loading={!props.anomalies}
                    noDataAvailableMessage={
                        isEmpty(filteredUiAnomalies) && !isEmpty(searchWords)
                            ? t("message.no-search-results")
                            : ""
                    }
                    rows={filteredUiAnomalies}
                    searchWords={searchWords}
                    selectedStatusLabelFn={getSelectedStatusLabel}
                    selectionModel={dataGridSelectionModel}
                    onSelectionModelChange={handleDataGridSelectionModelChange}
                />
            </Grid>
        </Grid>
    );
};
