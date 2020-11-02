import React, { FunctionComponent, useState } from "react";
import AlertCard from "../../components/alerts/alert-card.component";
import { PageContainer } from "../../components/containers/page-container.component";
import SearchBar from "../../components/search/search.component";
import { SideBar } from "../../components/sidebar/sidebar.component";
import { useAllAlerts } from "../../utils/rest/alerts-rest/alerts-rest.util";
import { alerts as alertsMock } from "./../../mock";

export const AlertsAllPage: FunctionComponent = () => {
    const { data: allAlerts } = useAllAlerts();
    const [search, setSearch] = useState("");

    // To show mock data
    // Remove this when we have actual data
    const alerts = allAlerts?.length ? allAlerts : alertsMock;

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
                    <AlertCard data={alert} key={alert.name} />
                ))}
            </PageContainer>
        </>
    );
};
