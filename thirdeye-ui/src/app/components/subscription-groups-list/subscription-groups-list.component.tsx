import { Grid } from "@material-ui/core";
import {
    CellParams,
    ColDef,
    RowId,
    SelectionModelChangeParams,
} from "@material-ui/data-grid";
import { isEmpty, toNumber } from "lodash";
import React, {
    FunctionComponent,
    ReactElement,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import {
    getAlertsDetailPath,
    getSubscriptionGroupsDetailPath,
    getSubscriptionGroupsUpdatePath,
} from "../../utils/routes/routes.util";
import { getSearchStatusLabel } from "../../utils/search/search.util";
import {
    filterSubscriptionGroups,
    getSubscriptionGroupAlertName,
} from "../../utils/subscription-groups/subscription-groups.util";
import { ActionCell } from "../data-grid/action-cell/action-cell.component";
import { DataGrid } from "../data-grid/data-grid.component";
import { MultiValueCell } from "../data-grid/multi-value-cell/multi-value-cell.component";
import {
    SubscriptionGroupAlert,
    SubscriptionGroupCardData,
} from "../entity-cards/subscription-group-card/subscription-group-card.interfaces";
import { SearchBar } from "../search-bar/search-bar.component";
import { SubscriptionGroupsListProps } from "./subscription-groups-list.interfaces";
import { useSubscriptionGroupsListStyles } from "./subscription-groups-list.styles";

export const SubscriptionGroupsList: FunctionComponent<SubscriptionGroupsListProps> = (
    props: SubscriptionGroupsListProps
) => {
    const subscriptionGroupsListClasses = useSubscriptionGroupsListStyles();
    const [
        filteredSubscriptionGroupCardDatas,
        setFilteredSubscriptionGroupCardDatas,
    ] = useState<SubscriptionGroupCardData[]>([]);
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const [columns, setColumns] = useState<ColDef[]>([]);
    const [dataGridSelectionModel, setDatagridSelectionModel] = useState<
        RowId[]
    >([]);
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        // Input subscription groups or search changed, reset
        initColumns();
        setFilteredSubscriptionGroupCardDatas(
            filterSubscriptionGroups(
                props.subscriptionGroupCardDatas as SubscriptionGroupCardData[],
                searchWords
            )
        );
    }, [props.subscriptionGroupCardDatas, searchWords]);

    useEffect(() => {
        // Search changed, re-initialize row selection
        setDatagridSelectionModel([...dataGridSelectionModel]);
    }, [searchWords]);

    const initColumns = (): void => {
        const columns: ColDef[] = [
            // Name
            {
                field: "name",
                type: "string",
                headerName: t("label.name"),
                sortable: true,
                flex: 1,
            },
            // Alerts
            {
                field: "alerts",
                headerName: t("label.subscribed-alerts"),
                sortable: false,
                flex: 1,
                renderCell: alertsRenderer,
            },
            // Emails
            {
                field: "emails",
                headerName: t("label.subscribed-emails"),
                sortable: false,
                flex: 1,
                renderCell: emailsRenderer,
            },
            // Actions
            {
                field: "id",
                type: "number",
                headerName: t("label.actions"),
                headerAlign: "right",
                sortable: false,
                flex: 0.8,
                renderCell: actionsRenderer,
            },
        ];
        setColumns(columns);
    };

    const alertsRenderer = (params: CellParams): ReactElement => {
        return (
            <MultiValueCell<SubscriptionGroupAlert>
                link
                id={toNumber(params.row && params.row.id)}
                valueTextFn={getSubscriptionGroupAlertName}
                values={params.value as SubscriptionGroupAlert[]}
                onClick={handleAlertViewDetails}
                onMore={handleSubscriptionGroupViewDetails}
            />
        );
    };

    const emailsRenderer = (params: CellParams): ReactElement => {
        return (
            <MultiValueCell<string>
                id={toNumber(params.row && params.row.id)}
                values={params.value as string[]}
                onMore={handleSubscriptionGroupViewDetails}
            />
        );
    };

    const actionsRenderer = (params: CellParams): ReactElement => {
        return (
            <ActionCell
                delete
                edit
                viewDetails
                id={params.value as number}
                onDelete={handleSubscriptionGroupDelete}
                onEdit={handleSubscriptionGroupEdit}
                onViewDetails={handleSubscriptionGroupViewDetails}
            />
        );
    };

    const handleSubscriptionGroupViewDetails = (id: number): void => {
        history.push(getSubscriptionGroupsDetailPath(id));
    };

    const handleSubscriptionGroupEdit = (id: number): void => {
        history.push(getSubscriptionGroupsUpdatePath(id));
    };

    const handleSubscriptionGroupDelete = (id: number): void => {
        const subscriptionGroupCardData = getSubscriptionGroupCardData(id);
        if (!subscriptionGroupCardData) {
            return;
        }

        props.onDelete && props.onDelete(subscriptionGroupCardData);
    };

    const handleAlertViewDetails = (
        subscriptionGroupAlert: SubscriptionGroupAlert
    ): void => {
        if (!subscriptionGroupAlert) {
            return;
        }

        history.push(getAlertsDetailPath(subscriptionGroupAlert.id));
    };

    const getSubscriptionGroupCardData = (
        id: number
    ): SubscriptionGroupCardData | null => {
        if (!props.subscriptionGroupCardDatas) {
            return null;
        }

        return (
            props.subscriptionGroupCardDatas.find(
                (subscriptionGroupCardData) =>
                    subscriptionGroupCardData.id !== id
            ) || null
        );
    };

    const handleDataGridSelectionModelChange = (
        param: SelectionModelChangeParams
    ): void => {
        setDatagridSelectionModel(param.selectionModel || []);
    };

    return (
        <Grid
            container
            className={subscriptionGroupsListClasses.subscriptionGroupsList}
            direction="column"
        >
            {/* Search */}
            <Grid item>
                <SearchBar
                    autoFocus
                    setSearchQueryString
                    searchLabel={t("label.search-entity", {
                        entity: t("label.subscription-groups"),
                    })}
                    searchStatusLabel={getSearchStatusLabel(
                        filteredSubscriptionGroupCardDatas
                            ? filteredSubscriptionGroupCardDatas.length
                            : 0,
                        props.subscriptionGroupCardDatas
                            ? props.subscriptionGroupCardDatas.length
                            : 0
                    )}
                    onChange={setSearchWords}
                />
            </Grid>

            {/* Subscription groups list */}
            <Grid item className={subscriptionGroupsListClasses.list}>
                <DataGrid
                    columns={columns}
                    loading={!props.subscriptionGroupCardDatas}
                    noDataAvailableMessage={
                        isEmpty(filteredSubscriptionGroupCardDatas) &&
                        !isEmpty(searchWords)
                            ? t("message.no-search-results")
                            : ""
                    }
                    rowSelectionCount={dataGridSelectionModel.length}
                    rows={filteredSubscriptionGroupCardDatas}
                    searchWords={searchWords}
                    selectionModel={dataGridSelectionModel}
                    onSelectionModelChange={handleDataGridSelectionModelChange}
                />
            </Grid>
        </Grid>
    );
};
