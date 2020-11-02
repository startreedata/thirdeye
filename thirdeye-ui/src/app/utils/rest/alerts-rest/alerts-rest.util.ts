import useSWR, { responseInterface } from "swr";
import { Alert } from "./alerts-rest.interfaces";

const BASE_URL_ALERTS = "/api/alerts";

export const useAllAlerts = (): responseInterface<Alert[], Error> => {
    return useSWR(BASE_URL_ALERTS);
};

export const useAlert = (id: number): responseInterface<Alert, Error> => {
    return useSWR(`${BASE_URL_ALERTS}/${id}`);
};
