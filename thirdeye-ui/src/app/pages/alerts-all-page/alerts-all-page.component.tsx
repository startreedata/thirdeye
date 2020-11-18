import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import AlertCard from "../../components/alerts/alert-card.component";
import { PageContainer } from "../../components/containers/page-container.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import SearchBar from "../../components/search/search.component";
import { SideBar } from "../../components/sidebar/sidebar.component";
import { getAllAlerts } from "../../rest/alert/alert.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs/application-breadcrumbs.store";
import { getAlertsAllPath } from "../../utils/route/routes.util";

export const AlertsAllPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [push] = useApplicationBreadcrumbsStore((state) => [state.push]);
    const [alerts, setAlerts] = useState<Alert[]>();
    const [search, setSearch] = useState("");
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumb
        push([
            {
                text: t("label.all"),
                path: getAlertsAllPath(),
            },
        ]);

        fetchAllAlerts();

        setLoading(false);
    }, [push, t]);

    const fetchAllAlerts = async (): Promise<void> => {
        setAlerts(await getAllAlerts());
    };

    if (!alerts) {
        return <>LOADING</>;
    }

    const filtered = alerts.filter((alert) => alert.name.startsWith(search));

    if (loading) {
        return (
            <PageContainer>
                <PageLoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <>
            <SideBar />
            <PageContainer centered={false}>
                <SearchBar searchValue={search} onSearch={setSearch} />
                {filtered.map((alert) => (
                    <AlertCard data={alert} key={alert.name} mode="list" />
                ))}
            </PageContainer>
        </>
    );
};
