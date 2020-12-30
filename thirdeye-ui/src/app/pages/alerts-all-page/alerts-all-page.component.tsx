import { Grid } from "@material-ui/core";
import { isEmpty } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AlertCard } from "../../components/alert-card/alert-card.component";
import { AlertCardData } from "../../components/alert-card/alert-card.interfaces";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SearchBar } from "../../components/search-bar/search-bar.component";
import {
    deleteAlert,
    getAllAlerts,
    updateAlert,
} from "../../rest/alert-rest/alert-rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { getAllSubscriptionGroups } from "../../rest/subscription-group-rest/subscription-group-rest";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import {
    filterAlerts,
    getAlertCardData,
    getAlertCardDatas,
} from "../../utils/alert-util/alert-util";
import { getAlertsAllPath } from "../../utils/routes-util/routes-util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar-util/snackbar-util";

export const AlertsAllPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [alertCardDatas, setAlertCardDatas] = useState<AlertCardData[]>([]);
    const [filteredAlertCardDatas, setFilteredAlertCardDatas] = useState<
        AlertCardData[]
    >([]);
    const [subscriptionGroups, setSubscriptionGroups] = useState<
        SubscriptionGroup[]
    >([]);
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const [setPageBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: t("label.all"),
                pathFn: getAlertsAllPath,
            },
        ]);
    }, []);

    useEffect(() => {
        // Fetch data
        fetchData();
    }, []);

    useEffect(() => {
        // Fetched data or search changed, reset
        setFilteredAlertCardDatas(filterAlerts(alertCardDatas, searchWords));
    }, [alertCardDatas, searchWords]);

    const fetchData = (): void => {
        let fetchedAlertCardDatas: AlertCardData[] = [];
        let fetchedSubscriptionGroups: SubscriptionGroup[] = [];

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
                if (subscriptionGroupsResponse.status === "fulfilled") {
                    fetchedSubscriptionGroups =
                        subscriptionGroupsResponse.value;
                }
                if (alertsResponse.status === "fulfilled") {
                    fetchedAlertCardDatas = getAlertCardDatas(
                        alertsResponse.value,
                        fetchedSubscriptionGroups
                    );
                }
            })
            .finally((): void => {
                setAlertCardDatas(fetchedAlertCardDatas);
                setSubscriptionGroups(fetchedSubscriptionGroups);

                setLoading(false);
            });
    };

    const onAlertChange = (alertCardData: AlertCardData): void => {
        if (!alertCardData || !alertCardData.alert) {
            return;
        }

        // Update
        updateAlert(alertCardData.alert)
            .then((alert: Alert): void => {
                // Replace updated alert in fetched alerts
                replaceAlertCardData(alert);

                enqueueSnackbar(
                    t("message.update-success", { entity: t("label.alert") }),
                    getSuccessSnackbarOption()
                );
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.update-error", { entity: t("label.alert") }),
                    getErrorSnackbarOption()
                );
            });
    };

    const onDeleteAlert = (alertCardData: AlertCardData): void => {
        if (!alertCardData) {
            return;
        }

        // Delete
        deleteAlert(alertCardData.id)
            .then((alert: Alert): void => {
                // Remove deleted alert from fetched alerts
                removeAlertCardData(alert);

                enqueueSnackbar(
                    t("message.delete-success", { entity: t("label.alert") }),
                    getSuccessSnackbarOption()
                );
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.delete-error", { entity: t("label.alert") }),
                    getErrorSnackbarOption()
                );
            });
    };

    const replaceAlertCardData = (alert: Alert): void => {
        if (!alert) {
            return;
        }

        setAlertCardDatas((alertCardDatas) =>
            alertCardDatas.map(
                (alertCardData: AlertCardData): AlertCardData => {
                    if (alertCardData.id === alert.id) {
                        // Replace
                        return getAlertCardData(alert, subscriptionGroups);
                    }

                    return alertCardData;
                }
            )
        );
    };

    const removeAlertCardData = (alert: Alert): void => {
        if (!alert) {
            return;
        }

        setAlertCardDatas((alertCardDatas) =>
            alertCardDatas.filter((alertCardData: AlertCardData): boolean => {
                return alertCardData.id !== alert.id;
            })
        );
    };

    if (loading) {
        return (
            <PageContainer>
                <LoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <PageContainer>
            <PageContents centered title={t("label.alerts")}>
                <Grid container>
                    {/* Search */}
                    <Grid item md={12}>
                        <SearchBar
                            autoFocus
                            setSearchQueryString
                            label={t("label.search-alerts")}
                            searchStatusLabel={t("label.search-count", {
                                count: filteredAlertCardDatas
                                    ? filteredAlertCardDatas.length
                                    : 0,
                                total: alertCardDatas
                                    ? alertCardDatas.length
                                    : 0,
                            })}
                            onChange={setSearchWords}
                        />
                    </Grid>

                    {/* Alerts */}
                    {filteredAlertCardDatas &&
                        filteredAlertCardDatas.map(
                            (filteredAlertCardData, index) => (
                                <Grid item key={index} md={12}>
                                    <AlertCard
                                        alertCardData={filteredAlertCardData}
                                        searchWords={searchWords}
                                        onChange={onAlertChange}
                                        onDelete={onDeleteAlert}
                                    />
                                </Grid>
                            )
                        )}
                </Grid>

                {/* No data available message */}
                {isEmpty(filteredAlertCardDatas) && <NoDataIndicator />}
            </PageContents>
        </PageContainer>
    );
};
