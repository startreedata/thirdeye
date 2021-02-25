import { Grid } from "@material-ui/core";
import { isEmpty } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { AlertCard } from "../../components/entity-cards/alert-card/alert-card.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SearchBar } from "../../components/search-bar/search-bar.component";
import {
    deleteAlert,
    getAllAlerts,
    updateAlert,
} from "../../rest/alerts/alerts.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { UiAlert } from "../../rest/dto/ui-alert.interfaces";
import { getAllSubscriptionGroups } from "../../rest/subscription-groups/subscription-groups.rest";
import {
    filterAlerts,
    getUiAlert,
    getUiAlerts,
} from "../../utils/alerts/alerts.util";
import { getSearchStatusLabel } from "../../utils/search/search.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";

export const AlertsAllPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [uiAlerts, setUiAlerts] = useState<UiAlert[]>([]);
    const [filteredUiAlerts, setFilteredUiAlerts] = useState<UiAlert[]>([]);
    const [subscriptionGroups, setSubscriptionGroups] = useState<
        SubscriptionGroup[]
    >([]);
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
        fetchAllAlerts();
    }, []);

    useEffect(() => {
        // Fetched alerts or search changed, reset
        setFilteredUiAlerts(filterAlerts(uiAlerts, searchWords));
    }, [uiAlerts, searchWords]);

    const onAlertChange = (uiAlert: UiAlert): void => {
        if (!uiAlert || !uiAlert.alert) {
            return;
        }

        updateAlert(uiAlert.alert)
            .then((alert: Alert): void => {
                enqueueSnackbar(
                    t("message.update-success", { entity: t("label.alert") }),
                    getSuccessSnackbarOption()
                );

                // Replace updated alert in fetched alerts
                replaceUiAlert(alert);
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.update-error", { entity: t("label.alert") }),
                    getErrorSnackbarOption()
                );
            });
    };

    const onDeleteAlert = (uiAlert: UiAlert): void => {
        if (!uiAlert) {
            return;
        }

        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", {
                name: uiAlert.name,
            }),
            okButtonLabel: t("label.delete"),
            onOk: (): void => {
                onDeleteAlertConfirmation(uiAlert);
            },
        });
    };

    const onDeleteAlertConfirmation = (uiAlert: UiAlert): void => {
        if (!uiAlert) {
            return;
        }

        deleteAlert(uiAlert.id)
            .then((alert: Alert): void => {
                enqueueSnackbar(
                    t("message.delete-success", { entity: t("label.alert") }),
                    getSuccessSnackbarOption()
                );

                // Remove deleted alert from fetched alerts
                removeUiAlert(alert);
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.delete-error", { entity: t("label.alert") }),
                    getErrorSnackbarOption()
                );
            });
    };

    const fetchAllAlerts = (): void => {
        Promise.allSettled([getAllAlerts(), getAllSubscriptionGroups()])
            .then(([alertsResponse, subscriptionGroupsResponse]): void => {
                // Determine if any of the calls failed
                if (
                    alertsResponse.status === "rejected" ||
                    subscriptionGroupsResponse.status === "rejected"
                ) {
                    enqueueSnackbar(
                        t("message.fetch-error"),
                        getErrorSnackbarOption()
                    );
                }

                // Attempt to gather data
                let fetchedSubscriptionGroups: SubscriptionGroup[] = [];
                if (subscriptionGroupsResponse.status === "fulfilled") {
                    fetchedSubscriptionGroups =
                        subscriptionGroupsResponse.value;
                    setSubscriptionGroups(fetchedSubscriptionGroups);
                }
                if (alertsResponse.status === "fulfilled") {
                    setUiAlerts(
                        getUiAlerts(
                            alertsResponse.value,
                            fetchedSubscriptionGroups
                        )
                    );
                }
            })
            .finally((): void => {
                setLoading(false);
            });
    };

    const replaceUiAlert = (alert: Alert): void => {
        if (!alert) {
            return;
        }

        setUiAlerts((uiAlerts) =>
            uiAlerts.map(
                (uiAlert: UiAlert): UiAlert => {
                    if (uiAlert.id === alert.id) {
                        // Replace
                        return getUiAlert(alert, subscriptionGroups);
                    }

                    return uiAlert;
                }
            )
        );
    };

    const removeUiAlert = (alert: Alert): void => {
        if (!alert) {
            return;
        }

        setUiAlerts((uiAlerts) =>
            uiAlerts.filter((uiAlert: UiAlert): boolean => {
                return uiAlert.id !== alert.id;
            })
        );
    };

    if (loading) {
        return <LoadingIndicator />;
    }

    return (
        <PageContents centered hideAppBreadcrumbs title={t("label.alerts")}>
            <Grid container>
                {/* Search */}
                <Grid item sm={12}>
                    <SearchBar
                        autoFocus
                        setSearchQueryString
                        searchLabel={t("label.search-alerts")}
                        searchStatusLabel={getSearchStatusLabel(
                            filteredUiAlerts ? filteredUiAlerts.length : 0,
                            uiAlerts ? uiAlerts.length : 0
                        )}
                        onChange={setSearchWords}
                    />
                </Grid>

                {/* Alerts */}
                {filteredUiAlerts &&
                    filteredUiAlerts.map((filteredUiAlert, index) => (
                        <Grid item key={index} sm={12}>
                            <AlertCard
                                showViewDetails
                                searchWords={searchWords}
                                uiAlert={filteredUiAlert}
                                onChange={onAlertChange}
                                onDelete={onDeleteAlert}
                            />
                        </Grid>
                    ))}
            </Grid>

            {/* No data available message */}
            {isEmpty(filteredUiAlerts) && isEmpty(searchWords) && (
                <NoDataIndicator />
            )}

            {/* No search results available message */}
            {isEmpty(filteredUiAlerts) && !isEmpty(searchWords) && (
                <NoDataIndicator text={t("message.no-search-results")} />
            )}
        </PageContents>
    );
};
