import { Grid } from "@material-ui/core";
import {
    ColDef,
    RowId,
    SelectionModelChangeParams,
} from "@material-ui/data-grid";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import {
    UiSubscriptionGroup,
    UiSubscriptionGroupAlert,
} from "../../rest/dto/ui-subscription-group.interfaces";
import {
    getAlertsDetailPath,
    getSubscriptionGroupsDetailPath,
    getSubscriptionGroupsUpdatePath,
} from "../../utils/routes/routes.util";
import { getSearchStatusLabel } from "../../utils/search/search.util";
import {
    filterSubscriptionGroups,
    getUiSubscriptionGroupAlertName,
} from "../../utils/subscription-groups/subscription-groups.util";
import { actionsCellRenderer } from "../data-grid/actions-cell/actions-cell.component";
import { DataGrid } from "../data-grid/data-grid.component";
import { linkCellRenderer } from "../data-grid/link-cell/link-cell.component";
import { multiValueCellRenderer } from "../data-grid/multi-value-cell/multi-value-cell.component";
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
                width: 150,
                renderCell: (params) =>
                    linkCellRenderer<string>(
                        params,
                        searchWords,
                        handleSubscriptionGroupViewDetailsByNameAndId
                    ),
            },
            // Alerts
            {
                field: "alerts",
                sortable: false,
                headerName: t("label.subscribed-alerts"),
                flex: 1,
                renderCell: (params) =>
                    multiValueCellRenderer<UiSubscriptionGroupAlert>(
                        params,
                        true,
                        searchWords,
                        handleSubscriptionGroupViewDetailsById,
                        handleAlertClick,
                        getUiSubscriptionGroupAlertName
                    ),
            },
            // Emails
            {
                field: "emails",
                sortable: false,
                headerName: t("label.subscribed-emails"),
                flex: 1,
                renderCell: (params) =>
                    multiValueCellRenderer<string>(
                        params,
                        false,
                        searchWords,
                        handleSubscriptionGroupViewDetailsById
                    ),
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
        return (
            (props.subscriptionGroups &&
                props.subscriptionGroups.find(
                    (subscriptionGroup) => subscriptionGroup.id === id
                )) ||
            null
        );
    };

    const handleAlertClick = (
        uiSubscriptionGroupAlert: UiSubscriptionGroupAlert
    ): void => {
        if (!uiSubscriptionGroupAlert) {
            return;
        }

        history.push(getAlertsDetailPath(uiSubscriptionGroupAlert.id));
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
            <Grid
                item
                className={
                    subscriptionGroupListClasses.subscriptionGroupListDataGrid
                }
            >
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
