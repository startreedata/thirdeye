import { Grid } from "@material-ui/core";
import { cloneDeep } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AlertCard } from "../../components/alert-card/alert-card.component";
import { AlertCardData } from "../../components/alert-card/alert-card.interfaces";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SearchBar } from "../../components/search-bar/search-bar.component";
import { getAllAlerts, updateAlert } from "../../rest/alert-rest/alert-rest";
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
import { SnackbarOption } from "../../utils/snackbar-util/snackbar-util";

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
        const init = async (): Promise<void> => {
            await fetchData();

            setLoading(false);
        };

        init();
    }, []);

    useEffect(() => {
        // Fetched data, or search changed, reset
        setFilteredAlertCardDatas(filterAlerts(alertCardDatas, searchWords));
    }, [alertCardDatas, searchWords]);

    const fetchData = async (): Promise<void> => {
        let fetchedAlertCardDatas: AlertCardData[] = [];
        let fetchedSubscriptionGroups: SubscriptionGroup[] = [];
        const [
            alertsResponse,
            subscriptionGroupsResponse,
        ] = await Promise.allSettled([
            getAllAlerts(),
            getAllSubscriptionGroups(),
        ]);

        if (
            alertsResponse.status === "rejected" ||
            subscriptionGroupsResponse.status === "rejected"
        ) {
            enqueueSnackbar(t("message.fetch-error"), SnackbarOption.ERROR);
        } else {
            fetchedAlertCardDatas = getAlertCardDatas(
                alertsResponse.value,
                subscriptionGroupsResponse.value
            );
            fetchedSubscriptionGroups = subscriptionGroupsResponse.value;
        }

        setAlertCardDatas(fetchedAlertCardDatas);
        setSubscriptionGroups(fetchedSubscriptionGroups);
    };

    const onAlertStateToggle = async (
        alertCardData: AlertCardData
    ): Promise<void> => {
        if (!alertCardData || !alertCardData.alert) {
            return;
        }

        let alertCopy = cloneDeep(alertCardData.alert);
        alertCopy.active = !alertCopy.active;

        try {
            alertCopy = await updateAlert(alertCopy);

            // Replace updated alert in fetched alerts
            replaceAlertCardData(alertCopy);
        } catch (error) {
            enqueueSnackbar(
                t("message.update-error", { entity: t("label.alert") }),
                SnackbarOption.ERROR
            );
        }
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

    if (loading) {
        return (
            <PageContainer>
                <LoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <PageContainer>
            <PageContents contentsCenterAlign title={t("label.alerts")}>
                <Grid container md={12}>
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
                                        alert={filteredAlertCardData}
                                        searchWords={searchWords}
                                        onAlertStateToggle={onAlertStateToggle}
                                    />
                                </Grid>
                            )
                        )}
                </Grid>
            </PageContents>
        </PageContainer>
    );
};
