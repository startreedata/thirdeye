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
import { UiDataset } from "../../rest/dto/ui-dataset.interfaces";
import { filterDatasets } from "../../utils/datasets/datasets.util";
import {
    getDatasetsUpdatePath,
    getDatasetsViewPath,
} from "../../utils/routes/routes.util";
import {
    getSearchStatusLabel,
    getSelectedStatusLabel,
} from "../../utils/search/search.util";
import { actionsCellRenderer } from "../data-grid/actions-cell/actions-cell.component";
import { DataGrid } from "../data-grid/data-grid/data-grid.component";
import { linkCellRenderer } from "../data-grid/link-cell/link-cell.component";
import { SearchBar } from "../search-bar/search-bar.component";
import { DatasetListProps } from "./dataset-list.interfaces";
import { useDatasetListStyles } from "./dataset-list.styles";

export const DatasetList: FunctionComponent<DatasetListProps> = (
    props: DatasetListProps
) => {
    const datasetListClasses = useDatasetListStyles();
    const [filteredUiDatasets, setFilteredUiDatasets] = useState<UiDataset[]>(
        []
    );
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const [dataGridColumns, setDataGridColumns] = useState<GridColDef[]>([]);
    const [dataGridSelectionModel, setDataGridSelectionModel] = useState<
        GridRowId[]
    >([]);
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        // Input datasets or search changed, reset
        setFilteredUiDatasets(
            filterDatasets(props.datasets || [], searchWords)
        );
        initDataGridColumns();
    }, [props.datasets, searchWords]);

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
                flex: 1.5,
                renderCell: (params) =>
                    linkCellRenderer(
                        params,
                        searchWords,
                        handleDatasetViewDetailsByNameAndId
                    ),
            },
            // Datasource Name
            {
                field: "datasourceName",
                type: "string",
                sortable: true,
                headerName: t("label.datasource"),
                flex: 1,
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
                        handleDatasetViewDetailsById,
                        handleDatasetEdit,
                        handleDatasetDelete
                    ),
            },
        ];
        setDataGridColumns(columns);
    };

    const handleDatasetViewDetailsByNameAndId = (
        _name: string,
        id: number
    ): void => {
        handleDatasetViewDetailsById(id);
    };

    const handleDatasetViewDetailsById = (id: number): void => {
        history.push(getDatasetsViewPath(id));
    };

    const handleDatasetEdit = (id: number): void => {
        history.push(getDatasetsUpdatePath(id));
    };

    const handleDatasetDelete = (id: number): void => {
        const uiDataset = getUiDataset(id);
        if (!uiDataset) {
            return;
        }

        props.onDelete && props.onDelete(uiDataset);
    };

    const getUiDataset = (id: number): UiDataset | null => {
        if (!props.datasets) {
            return null;
        }

        return props.datasets.find((dataset) => dataset.id === id) || null;
    };

    const handleDataGridSelectionModelChange = (
        params: GridSelectionModelChangeParams
    ): void => {
        setDataGridSelectionModel(params.selectionModel || []);
    };

    return (
        <Grid
            container
            className={datasetListClasses.datasetList}
            direction="column"
        >
            {/* Search */}
            {!props.hideSearchBar && (
                <Grid item>
                    <SearchBar
                        autoFocus
                        setSearchQueryString
                        searchLabel={t("label.search-entity", {
                            entity: t("label.datasets"),
                        })}
                        searchStatusLabel={getSearchStatusLabel(
                            filteredUiDatasets ? filteredUiDatasets.length : 0,
                            props.datasets ? props.datasets.length : 0
                        )}
                        onChange={setSearchWords}
                    />
                </Grid>
            )}

            {/* Dataset list */}
            <Grid item className={datasetListClasses.dataGrid}>
                <DataGrid
                    autoHeight
                    checkboxSelection
                    columns={dataGridColumns}
                    loading={!props.datasets}
                    noDataAvailableMessage={
                        isEmpty(filteredUiDatasets) && !isEmpty(searchWords)
                            ? t("message.no-search-results")
                            : ""
                    }
                    rows={filteredUiDatasets}
                    searchWords={searchWords}
                    selectedStatusLabelFn={getSelectedStatusLabel}
                    selectionModel={dataGridSelectionModel}
                    onSelectionModelChange={handleDataGridSelectionModelChange}
                />
            </Grid>
        </Grid>
    );
};
