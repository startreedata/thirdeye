import { Grid } from "@material-ui/core";
import _ from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AlertCard } from "../../components/alert-card/alert-card.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import { Search } from "../../components/search/search.component";
import { getAllAlerts, updateAlert } from "../../rest/alert/alert-rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs/application-breadcrumbs-store";
import { filterAlerts } from "../../utils/alert/alert-util";
import { getAlertsAllPath } from "../../utils/route/routes-util";

export const AlertsAllPage: FunctionComponent = () => {
    const [fetch, setFetch] = useState(true);
    const [loading, setLoading] = useState(true);
    const [setPageBreadcrumbs] = useApplicationBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const [alerts, setAlerts] = useState<Alert[]>([]);
    const [filteredAlerts, setfilteredAlerts] = useState<Alert[]>([]);
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: t("label.all"),
                path: getAlertsAllPath(),
            },
        ]);
    }, [setPageBreadcrumbs, t]);

    useEffect(() => {
        const fetchData = async (): Promise<void> => {
            const alerts = await getAllAlerts();

            setAlerts(alerts);
            setfilteredAlerts(filterAlerts(alerts, searchWords));

            setLoading(false);
        };

        fetchData();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [fetch]); // Doesn't need to depend on searchWords

    const onSearch = (searchWords: string[]): void => {
        setSearchWords(searchWords);
        setfilteredAlerts(filterAlerts(alerts, searchWords));
    };

    const onAlertStateToggle = async (alert: Alert): Promise<void> => {
        // Original alert need not be modified unless it's really updated in the backend
        const alertCopy = _.cloneDeep(alert);
        alertCopy.active = !alertCopy.active;

        await updateAlert(alertCopy);

        // Fetch all alerts
        setFetch(!fetch);
    };

    if (loading) {
        return (
            <PageContainer>
                <PageLoadingIndicator />
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
