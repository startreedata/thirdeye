import React, { FunctionComponent } from "react";
import { useAllAlerts } from "../../utils/rest/alerts-rest/alerts-rest.util";

export const AlertsAllPage: FunctionComponent = () => {
    const { data: allAlerts } = useAllAlerts();

    if (!allAlerts) {
        return <>LOADING</>;
    }

    return <>{allAlerts}</>;
};
