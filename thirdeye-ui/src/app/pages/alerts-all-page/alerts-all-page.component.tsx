import React, { FunctionComponent, useState } from "react";
import AlertCard from "../../components/alerts/alert-card.component";
import { PageContainer } from "../../components/containers/page-container.component";
import SearchBar from "../../components/search/search.component";
import { SideBar } from "../../components/sidebar/sidebar.component";
import { useAllAlerts } from "../../rest/alert/alert.rest";

export const AlertsAllPage: FunctionComponent = () => {
    const { data: alerts, error } = useAllAlerts();
    const [search, setSearch] = useState("");

    if (!alerts) {
        return <>LOADING</>;
    }
    if (error) {
        console.log(error);
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
