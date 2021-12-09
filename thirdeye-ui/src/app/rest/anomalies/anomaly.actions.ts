import { AxiosError } from "axios";
import { useCallback, useState } from "react";
import { ActionStatus } from "../actions.interfaces";
import { Anomaly } from "../dto/anomaly.interfaces";
import { getAnomaly as getAnomalyREST } from "./anomalies.rest";
import { GetAnomaly } from "./anomaly.interfaces";

export const useGetAnomaly = (): GetAnomaly => {
    const [anomaly, setAnomaly] = useState<Anomaly>();
    const [status, setStatus] = useState(ActionStatus.Initial);
    const [errorMessage, setErrorMessage] = useState("");

    const getAnomaly = useCallback(async (anomalyId: number) => {
        setStatus(ActionStatus.Working);
        try {
            const fetchedAnomaly = await getAnomalyREST(anomalyId);
            setAnomaly(fetchedAnomaly);
            setStatus(ActionStatus.Done);
            setErrorMessage("");
        } catch (error) {
            const axiosError = error as AxiosError;
            setAnomaly(undefined);
            setStatus(ActionStatus.Error);
            setErrorMessage(
                axiosError &&
                    axiosError.response &&
                    axiosError.response.data &&
                    axiosError.response.data.message
            );
        }
    }, []);

    return { anomaly, getAnomaly, status, errorMessage };
};
