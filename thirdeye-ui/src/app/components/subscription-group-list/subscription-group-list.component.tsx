import { Box, Grid, IconButton, Toolbar, Typography } from "@material-ui/core";
import { CellParams, ColDef, RowId } from "@material-ui/data-grid";
import DeleteIcon from "@material-ui/icons/Delete";
import EditIcon from "@material-ui/icons/Edit";
import MoreHorizIcon from "@material-ui/icons/MoreHoriz";
import VisibilityIcon from "@material-ui/icons/Visibility";
import { isEmpty } from "lodash";
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
import { filterSubscriptionGroupsList } from "../../utils/subscription-groups/subscription-groups.util";
import { DataGrid } from "../data-grid/data-grid.component";
import { useDialog } from "../dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../dialogs/dialog-provider/dialog-provider.interfaces";
import { NoDataIndicator } from "../no-data-indicator/no-data-indicator.component";
import { SearchBar } from "../search-bar/search-bar.component";
import { TextHighlighter } from "../text-highlighter/text-highlighter.component";
import {
    SubscriptionGroupAlert,
    SubscriptionGroupListData,
    SubscriptionGroupListProps,
} from "./subscription-group-list.interfaces";
import { useSubscriptionGroupListStyles } from "./subscription-group-list.styles";

export const SubscriptionGroupList: FunctionComponent<SubscriptionGroupListProps> = ({
    subscriptionGroups,
    onDelete,
}: SubscriptionGroupListProps) => {
    const [selectedRows, setSelectedRows] = useState<RowId[]>([]);
    const [filteredRecords, setFilteredRecords] = useState<
        SubscriptionGroupListData[]
    >([]);
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const history = useHistory();
    const { showDialog } = useDialog();
    const { t } = useTranslation();

    const subscriptionListClass = useSubscriptionGroupListStyles();

    useEffect(() => {
        setFilteredRecords(
            filterSubscriptionGroupsList(subscriptionGroups, searchWords)
        );
    }, [subscriptionGroups, searchWords]);

    const renderAlertsCell = (alerts: CellParams): ReactElement => {
        const alertValues = alerts.value as SubscriptionGroupAlert[];
        // Allow max 4 alerts to be shown in the cell
        const maxAlerts = alertValues.slice(0, 4) as SubscriptionGroupAlert[];

        return (
            <>
                <div className={subscriptionListClass.listContainer}>
                    {maxAlerts.map((alert) => (
                        <Typography
                            className={subscriptionListClass.paddingRight}
                            color="primary"
                            key={alert.id}
                            variant="subtitle2"
                            onClick={() =>
                                history.push(
                                    getAlertsDetailPath(Number(alert.id))
                                )
                            }
                        >
                            <TextHighlighter
                                searchWords={searchWords}
                                text={alert.name}
                            />
                        </Typography>
                    ))}
                </div>

                <IconButton
                    className={subscriptionListClass.moreIcon}
                    color="primary"
                    size="small"
                    onClick={() =>
                        history.push(
                            getSubscriptionGroupsDetailPath(
                                Number(alerts.row.id)
                            )
                        )
                    }
                >
                    <MoreHorizIcon />
                </IconButton>
            </>
        );
    };

    const renderEmailCell = (emails: CellParams): ReactElement => {
        const emailValue = emails.value as string[];

        const maxEmails = emailValue.slice(0, 2);

        return (
            <>
                <div className={subscriptionListClass.listContainer}>
                    {maxEmails.map((email, index) => (
                        <Typography
                            className={subscriptionListClass.paddingRight}
                            color="primary"
                            key={email + index}
                            variant="subtitle2"
                        >
                            <TextHighlighter
                                searchWords={searchWords}
                                text={email}
                            />
                        </Typography>
                    ))}
                </div>
                <IconButton
                    className={subscriptionListClass.moreIcon}
                    color="primary"
                    size="small"
                    onClick={() =>
                        history.push(
                            getSubscriptionGroupsDetailPath(
                                Number(emails.row.id)
                            )
                        )
                    }
                >
                    <MoreHorizIcon />
                </IconButton>
            </>
        );
    };

    const columns: ColDef[] = [
        {
            field: "idText",
            headerName: t("label.id"),
            align: "right",
            flex: 0.5,
            sortable: false,
        },
        {
            field: "name",
            headerName: t("label.name"),
            flex: 1,
        },
        {
            field: "alerts",
            headerName: t("label.alerts"),
            flex: 1,
            renderCell: renderAlertsCell,
        },
        {
            field: "emails",
            headerName: t("label.subscribed-emails"),
            flex: 1,
            renderCell: renderEmailCell,
        },
    ];

    const handleSearch = (searchWords: string[]): void => {
        setSearchWords(searchWords);
    };

    const handleDeleteSubscriptionGroups = (): void => {
        const subscriptionGroupsSelected = subscriptionGroups.filter(
            (subscriptionGroup) =>
                selectedRows.includes(subscriptionGroup.id.toString())
        );

        onDelete(subscriptionGroupsSelected);
        setSelectedRows([]);
    };

    const showDeletePopup = (): void => {
        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", {
                name:
                    t("label.selected") + " " + t("label.subscription-groups"),
            }),
            okButtonLabel: t("label.delete"),
            onOk: handleDeleteSubscriptionGroups,
        });
    };

    const onViewSubscriptionGroupDetails = (): void => {
        history.push(getSubscriptionGroupsDetailPath(Number(selectedRows[0])));
    };

    const onEditSubscriptionGroup = (): void => {
        history.push(getSubscriptionGroupsUpdatePath(Number(selectedRows[0])));
    };

    return (
        <Grid container>
            {/* Toolbar for table */}
            <Toolbar className={subscriptionListClass.toolbar}>
                {/* View button */}
                <IconButton
                    color="primary"
                    disabled={selectedRows.length !== 1}
                    onClick={onViewSubscriptionGroupDetails}
                >
                    <VisibilityIcon />
                </IconButton>
                {/* Edit button */}
                <IconButton
                    color="primary"
                    disabled={selectedRows.length !== 1}
                    onClick={onEditSubscriptionGroup}
                >
                    <EditIcon />
                </IconButton>
                {/* Delete button */}
                <IconButton
                    color="primary"
                    disabled={selectedRows.length === 0}
                    onClick={showDeletePopup}
                >
                    <DeleteIcon />
                </IconButton>

                <Typography
                    className={subscriptionListClass.rightAlign}
                    color="textSecondary"
                    variant="button"
                >
                    {selectedRows.length
                        ? selectedRows.length + " " + t("label.selected")
                        : ""}
                </Typography>

                {/* Searchbar */}
                <Box className={subscriptionListClass.searchContainer}>
                    <SearchBar
                        searchStatusLabel={t("label.search-count", {
                            count: filteredRecords ? filteredRecords.length : 0,
                            total: subscriptionGroups
                                ? subscriptionGroups.length
                                : 0,
                        })}
                        onChange={handleSearch}
                    />
                </Box>
            </Toolbar>

            {/* Table */}
            <Grid item sm={12}>
                {!isEmpty(filteredRecords) && (
                    <DataGrid
                        dataGrid={{
                            columns: columns,
                            rows: filteredRecords,
                            onSelectionChange: (rowSelection): void => {
                                setSelectedRows(rowSelection.rowIds);
                            },
                        }}
                        searchWords={searchWords}
                    />
                )}
            </Grid>

            {/* No data available message */}
            {isEmpty(filteredRecords) && isEmpty(searchWords) && (
                <NoDataIndicator />
            )}

            {/* No search results available message */}
            {isEmpty(filteredRecords) && !isEmpty(searchWords) && (
                <NoDataIndicator text={t("message.no-search-results")} />
            )}
        </Grid>
    );
};
