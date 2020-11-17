import React, { FunctionComponent, useEffect, useState } from "react";
import AlertCard from "../../components/alerts/alert-card.component";
import { PageContainer } from "../../components/containers/page-container.component";
import SearchBar from "../../components/search/search.component";
import { SideBar } from "../../components/sidebar/sidebar.component";
import { getAllAlerts } from "../../rest/alert/alert.rest";
import { Alert } from "../../rest/dto/alert.interfaces";

export const AlertsAllPage: FunctionComponent = () => {
    const [alerts, setAlerts] = useState<Alert[]>();
    const [search, setSearch] = useState("");

    useEffect(() => {
        fetchAllAlerts();
    }, []);

    const fetchAllAlerts = async (): Promise<void> => {
        setAlerts(await getAllAlerts());
    };

    if (!alerts) {
        return <>LOADING</>;
    }

    const filtered = alerts.filter((alert) => alert.name.startsWith(search));

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
