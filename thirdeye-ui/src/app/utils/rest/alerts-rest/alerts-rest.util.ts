import axios from "axios";
import yaml from "js-yaml";
import useSWR, { responseInterface } from "swr";
import { Alert } from "./alerts-rest.interfaces";

const BASE_URL_ALERTS = "/api/alerts";

export const useAllAlerts = (): responseInterface<Alert[], Error> => {
    return useSWR(BASE_URL_ALERTS);
};

export const useAlert = (id: number): responseInterface<Alert, Error> => {
    return useSWR(`${BASE_URL_ALERTS}/${id}`);
};

export const createAlert = async (alertYaml: string): Promise<void> => {
    await axios.post(BASE_URL_ALERTS, yaml.safeLoad(alertYaml));
};
