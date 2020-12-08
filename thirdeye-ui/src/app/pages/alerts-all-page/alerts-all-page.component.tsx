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
import { Search } from "../../components/search/search.component";
import { getAllAlerts, updateAlert } from "../../rest/alert-rest/alert-rest";
import { getAllSubscriptionGroups } from "../../rest/subscription-group-rest/subscription-group-rest";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import {
    filterAlerts,
    getAlertCardDatas,
} from "../../utils/alert-util/alert-util";
import { getAlertsAllPath } from "../../utils/routes-util/routes-util";
import { SnackbarOption } from "../../utils/snackbar-util/snackbar-util";

export const AlertsAllPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [setPageBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const [alerts, setAlerts] = useState<AlertCardData[]>([]);
    const [filteredAlerts, setfilteredAlerts] = useState<AlertCardData[]>([]);
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: t("label.all"),
                path: getAlertsAllPath(),
            },
        ]);
    }, []);

    useEffect(() => {
        const init = async (): Promise<void> => {
            await fetchData();

            setLoading(false);
        };

        init();
    }, []);

    const fetchData = async (): Promise<void> => {
        let alerts: AlertCardData[] = [];
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
            alerts = getAlertCardDatas(
                alertsResponse.value,
                subscriptionGroupsResponse.value
            );
        }

        setAlerts(alerts);
        setfilteredAlerts(filterAlerts(alerts, searchWords));
    };

    const onSearch = (searchWords: string[]): void => {
        setSearchWords(searchWords);
        setfilteredAlerts(filterAlerts(alerts, searchWords));
    };

    const onAlertStateToggle = async (
        alertCardData: AlertCardData
    ): Promise<void> => {
        if (!alertCardData.alert) {
            return;
        }

        let alertCopy = cloneDeep(alertCardData.alert);
        alertCopy.active = !alertCopy.active;

        try {
            alertCopy = await updateAlert(alertCopy);

            fetchData();
        } catch (error) {
            enqueueSnackbar(
                t("message.update-error", { entity: t("label.alert") }),
                SnackbarOption.ERROR
            );
        }
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
            <PageContents centerAlign title={t("label.alerts")}>
                <Grid container>
                    <Grid item md={12}>
                        <Search
                            autoFocus
                            syncSearchWithURL
                            searchStatusText={t("label.search-count", {
                                count: filteredAlerts.length,
                                total: alerts.length,
                            })}
                            onChange={onSearch}
                        />
                    </Grid>

                    {filteredAlerts.map((alert) => (
                        <Grid item key={alert.id} md={12}>
                            <AlertCard
                                alert={alert}
                                searchWords={searchWords}
                                onAlertStateToggle={onAlertStateToggle}
                            />
                        </Grid>
                    ))}
                </Grid>
            </PageContents>
        </PageContainer>
    );
};
