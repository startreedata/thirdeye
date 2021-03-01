import { Grid } from "@material-ui/core";
import {
    CellParams,
    CellValue,
    ColDef,
    RowId,
    SelectionModelChangeParams,
} from "@material-ui/data-grid";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { UiSubscriptionGroup } from "../../rest/dto/ui-subscription-group.interfaces";
import {
    getSubscriptionGroupsDetailPath,
    getSubscriptionGroupsUpdatePath,
} from "../../utils/routes/routes.util";
import { getSearchStatusLabel } from "../../utils/search/search.util";
import { filterSubscriptionGroups } from "../../utils/subscription-groups/subscription-groups.util";
import { actionsCellRenderer } from "../data-grid/actions-cell/actions-cell.component";
import { DataGrid } from "../data-grid/data-grid.component";
import { linkCellRenderer } from "../data-grid/link-cell/link-cell.component";
import { SearchBar } from "../search-bar/search-bar.component";
import { SubscriptionGroupListProps } from "./subscription-group-list.interfaces";
import { useSubscriptionGroupListStyles } from "./subscription-group-list.styles";

export const SubscriptionGroupList: FunctionComponent<SubscriptionGroupListProps> = (
    props: SubscriptionGroupListProps
) => {
    const subscriptionGroupListClasses = useSubscriptionGroupListStyles();
    const [
        filteredUiSubscriptionGroups,
        setFilteredUiSubscriptionGroups,
    ] = useState<UiSubscriptionGroup[]>([]);
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const [dataGridColumns, setDataGridColumns] = useState<ColDef[]>([]);
    const [dataGridSelectionModel, setDataGridSelectionModel] = useState<
        RowId[]
    >([]);
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        // Input subscription groups or search changed, reset
        setFilteredUiSubscriptionGroups(
            filterSubscriptionGroups(
                props.subscriptionGroups || [],
                searchWords
            )
        );
        initDataGridColumns();
    }, [props.subscriptionGroups, searchWords]);

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
                flex: 1.5,
                renderCell: (params) =>
                    linkCellRenderer(
                        params,
                        searchWords,
                        handleSubscriptionGroupViewDetailsByNameAndId
                    ),
            },
            // Alert count
            {
                field: "alertCount",
                type: "string",
                sortable: true,
                headerName: t("label.subscribed-alerts"),
                align: "right",
                headerAlign: "right",
                flex: 1,
                sortComparator: subscriptionGroupAlertCountComparator,
            },
            // Email count
            {
                field: "emailCount",
                type: "string",
                sortable: true,
                headerName: t("label.subscribed-emails"),
                align: "right",
                headerAlign: "right",
                flex: 1,
                sortComparator: subscriptionGroupEmailCountComparator,
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
                        handleSubscriptionGroupViewDetailsById,
                        handleSubscriptionGroupEdit,
                        handleSubscriptionGroupDelete
                    ),
            },
        ];
        setDataGridColumns(columns);
    };

    const subscriptionGroupAlertCountComparator = (
        _value1: CellValue,
        _value2: CellValue,
        params1: CellParams,
        params2: CellParams
    ): number => {
        const uiSubscriptionGroup1 = getUiSubscriptionGroup(
            params1.row && params1.row.rowId
        );
        const uiSubscriptionGroup2 = getUiSubscriptionGroup(
            params2.row && params2.row.rowId
        );

        if (!uiSubscriptionGroup1 || !uiSubscriptionGroup1.alerts) {
            return -1;
        }

        if (!uiSubscriptionGroup2 || !uiSubscriptionGroup2.alerts) {
            return 1;
        }

        return (
            uiSubscriptionGroup1.alerts.length -
            uiSubscriptionGroup2.alerts.length
        );
    };

    const subscriptionGroupEmailCountComparator = (
        _value1: CellValue,
        _value2: CellValue,
        params1: CellParams,
        params2: CellParams
    ): number => {
        const uiSubscriptionGroup1 = getUiSubscriptionGroup(
            params1.row && params1.row.rowId
        );
        const uiSubscriptionGroup2 = getUiSubscriptionGroup(
            params2.row && params2.row.rowId
        );

        if (!uiSubscriptionGroup1 || !uiSubscriptionGroup1.emails) {
            return -1;
        }

        if (!uiSubscriptionGroup2 || !uiSubscriptionGroup2.emails) {
            return 1;
        }

        return (
            uiSubscriptionGroup1.emails.length -
            uiSubscriptionGroup2.emails.length
        );
    };

    const handleSubscriptionGroupViewDetailsByNameAndId = (
        _name: string,
        id: number
    ): void => {
        handleSubscriptionGroupViewDetailsById(id);
    };

    const handleSubscriptionGroupViewDetailsById = (id: number): void => {
        history.push(getSubscriptionGroupsDetailPath(id));
    };

    const handleSubscriptionGroupEdit = (id: number): void => {
        history.push(getSubscriptionGroupsUpdatePath(id));
    };

    const handleSubscriptionGroupDelete = (id: number): void => {
        const uiSubscriptionGroup = getUiSubscriptionGroup(id);
        if (!uiSubscriptionGroup) {
            return;
        }

        props.onDelete && props.onDelete(uiSubscriptionGroup);
    };

    const getUiSubscriptionGroup = (id: number): UiSubscriptionGroup | null => {
        if (!props.subscriptionGroups) {
            return null;
        }

        return (
            props.subscriptionGroups.find(
                (subscriptionGroup) => subscriptionGroup.id === id
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
            className={subscriptionGroupListClasses.subscriptionGroupList}
            direction="column"
        >
            {/* Search */}
            {!props.hideSearchBar && (
                <Grid item>
                    <SearchBar
                        autoFocus
                        setSearchQueryString
                        searchLabel={t("label.search-entity", {
                            entity: t("label.subscription-groups"),
                        })}
                        searchStatusLabel={getSearchStatusLabel(
                            filteredUiSubscriptionGroups
                                ? filteredUiSubscriptionGroups.length
                                : 0,
                            props.subscriptionGroups
                                ? props.subscriptionGroups.length
                                : 0
                        )}
                        onChange={setSearchWords}
                    />
                </Grid>
            )}

            {/* Subscription group list */}
            <Grid item className={subscriptionGroupListClasses.dataGrid}>
                <DataGrid
                    autoHeight
                    checkboxSelection
                    columns={dataGridColumns}
                    loading={!props.subscriptionGroups}
                    noDataAvailableMessage={
                        isEmpty(filteredUiSubscriptionGroups) &&
                        !isEmpty(searchWords)
                            ? t("message.no-search-results")
                            : ""
                    }
                    rows={filteredUiSubscriptionGroups}
                    searchWords={searchWords}
                    selectionModel={dataGridSelectionModel}
                    onSelectionModelChange={handleDataGridSelectionModelChange}
                />
            </Grid>
        </Grid>
    );
};
