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
import { UiDatasource } from "../../rest/dto/ui-datasource.interfaces";
import { filterDatasources } from "../../utils/datasources/datasources.util";
import {
    getDatasourcesUpdatePath,
    getDatasourcesViewPath,
} from "../../utils/routes/routes.util";
import {
    getSearchStatusLabel,
    getSelectedStatusLabel,
} from "../../utils/search/search.util";
import { actionsCellRenderer } from "../data-grid/actions-cell/actions-cell.component";
import { DataGrid } from "../data-grid/data-grid/data-grid.component";
import { linkCellRenderer } from "../data-grid/link-cell/link-cell.component";
import { SearchBar } from "../search-bar/search-bar.component";
import { DatasourceListProps } from "./datasource-list.interfaces";
import { useDatasourceListStyles } from "./datasource-list.styles";

export const DatasourceList: FunctionComponent<DatasourceListProps> = (
    props: DatasourceListProps
) => {
    const datasourceListClasses = useDatasourceListStyles();
    const [filteredUiDatasources, setFilteredUiDatasources] = useState<
        UiDatasource[]
    >([]);
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const [dataGridColumns, setDataGridColumns] = useState<GridColDef[]>([]);
    const [dataGridSelectionModel, setDataGridSelectionModel] = useState<
        GridRowId[]
    >([]);
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        // Input datasources or search changed, reset
        setFilteredUiDatasources(
            filterDatasources(props.datasources || [], searchWords)
        );
        initDataGridColumns();
    }, [props.datasources, searchWords]);

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
                        handleDatasourceViewDetailsByNameAndId
                    ),
            },
            // Type
            {
                field: "type",
                type: "string",
                sortable: true,
                headerName: t("label.type"),
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
                        handleDatasourceViewDetailsById,
                        handleDatasourceEdit,
                        handleDatasourceDelete
                    ),
            },
        ];
        setDataGridColumns(columns);
    };

    const handleDatasourceViewDetailsByNameAndId = (
        _name: string,
        id: number
    ): void => {
        handleDatasourceViewDetailsById(id);
    };

    const handleDatasourceViewDetailsById = (id: number): void => {
        history.push(getDatasourcesViewPath(id));
    };

    const handleDatasourceEdit = (id: number): void => {
        history.push(getDatasourcesUpdatePath(id));
    };

    const handleDatasourceDelete = (id: number): void => {
        const uiDatasource = getUiDatasource(id);
        if (!uiDatasource) {
            return;
        }

        props.onDelete && props.onDelete(uiDatasource);
    };

    const getUiDatasource = (id: number): UiDatasource | null => {
        if (!props.datasources) {
            return null;
        }

        return (
            props.datasources.find((datasource) => datasource.id === id) || null
        );
    };

    const handleDataGridSelectionModelChange = (
        params: GridSelectionModelChangeParams
    ): void => {
        setDataGridSelectionModel(params.selectionModel || []);
    };

    return (
        <Grid
            container
            className={datasourceListClasses.datasourceList}
            direction="column"
        >
            {/* Search */}
            {!props.hideSearchBar && (
                <Grid item>
                    <SearchBar
                        autoFocus
                        setSearchQueryString
                        searchLabel={t("label.search-entity", {
                            entity: t("label.datasources"),
                        })}
                        searchStatusLabel={getSearchStatusLabel(
                            filteredUiDatasources
                                ? filteredUiDatasources.length
                                : 0,
                            props.datasources ? props.datasources.length : 0
                        )}
                        onChange={setSearchWords}
                    />
                </Grid>
            )}

            {/* Datasource list */}
            <Grid item className={datasourceListClasses.dataGrid}>
                <DataGrid
                    autoHeight
                    checkboxSelection
                    columns={dataGridColumns}
                    loading={!props.datasources}
                    noDataAvailableMessage={
                        isEmpty(filteredUiDatasources) && !isEmpty(searchWords)
                            ? t("message.no-search-results")
                            : ""
                    }
                    rows={filteredUiDatasources}
                    searchWords={searchWords}
                    selectedStatusLabelFn={getSelectedStatusLabel}
                    selectionModel={dataGridSelectionModel}
                    onSelectionModelChange={handleDataGridSelectionModelChange}
                />
            </Grid>
        </Grid>
    );
};
