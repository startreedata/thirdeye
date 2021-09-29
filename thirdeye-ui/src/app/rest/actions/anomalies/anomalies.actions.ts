import { AxiosError } from "axios";
import { useCallback, useState } from "react";
import {
    deleteAnomaly,
    getAllAnomalies,
    getAnomaliesByAlertId,
    getAnomaliesByAlertIdAndTime,
    getAnomaliesByTime,
    getAnomaly,
} from "../../anomalies/anomalies.rest";
import { Anomaly } from "../../dto/anomaly.interfaces";
import { ActionStatus } from "../actions.interfaces";
import {
    DeleteAnomaly,
    FetchAllAnomalies,
    FetchAnomaliesByAlertId,
    FetchAnomaliesByAlertIdAndTime,
    FetchAnomaliesByTime,
    FetchAnomaly,
} from "./anomalies.actions.interfaces";

export const useFetchAnomaly = (): FetchAnomaly => {
    const [anomaly, setAnomaly] = useState<Anomaly | null>(null);
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const fetchAnomaly = useCallback(async (id: number) => {
        setStatus(ActionStatus.FETCHING);
        try {
            const anomaly = await getAnomaly(id);

            setAnomaly(anomaly);
            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setAnomaly(null);
            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { anomaly, errorMessage, status, fetchAnomaly };
};

export const useFetchAllAnomalies = (): FetchAllAnomalies => {
    const [anomalies, setAnomalies] = useState<Anomaly[] | null>(null);
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const fetchAllAnomalies = useCallback(async () => {
        setStatus(ActionStatus.FETCHING);
        try {
            const anomalies = await getAllAnomalies();

            setAnomalies(anomalies);
            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setAnomalies(null);
            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { anomalies, errorMessage, status, fetchAllAnomalies };
};

export const useFetchAnomaliesByAlertId = (): FetchAnomaliesByAlertId => {
    const [anomalies, setAnomalies] = useState<Anomaly[] | null>(null);
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const fetchAnomaliesByAlertId = useCallback(async (alertId: number) => {
        setStatus(ActionStatus.FETCHING);
        try {
            const anomalies = await getAnomaliesByAlertId(alertId);

            setAnomalies(anomalies);
            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setAnomalies(null);
            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { anomalies, errorMessage, status, fetchAnomaliesByAlertId };
};

export const useFetchAnomaliesByTime = (): FetchAnomaliesByTime => {
    const [anomalies, setAnomalies] = useState<Anomaly[] | null>(null);
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const fetchAnomaliesByTime = useCallback(
        async (startTime: number, endTime: number) => {
            setStatus(ActionStatus.FETCHING);
            try {
                const anomalies = await getAnomaliesByTime(startTime, endTime);

                setAnomalies(anomalies);
                setStatus(ActionStatus.DONE);
                setErrorMessage("");
            } catch (error) {
                const errorMessage = (error as AxiosError).response?.data
                    .message;

                setAnomalies(null);
                setStatus(ActionStatus.ERROR);
                setErrorMessage(errorMessage);
            }
        },
        []
    );

    return { anomalies, errorMessage, status, fetchAnomaliesByTime };
};

export const useFetchAnomaliesByAlertIdAndTime = (): FetchAnomaliesByAlertIdAndTime => {
    const [anomalies, setAnomalies] = useState<Anomaly[] | null>(null);
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const fetchAnomaliesByAlertIdAndTime = useCallback(
        async (alertId: number, startTime: number, endTime: number) => {
            setStatus(ActionStatus.FETCHING);
            try {
                const anomalies = await getAnomaliesByAlertIdAndTime(
                    alertId,
                    startTime,
                    endTime
                );

                setAnomalies(anomalies);
                setStatus(ActionStatus.DONE);
                setErrorMessage("");
            } catch (error) {
                const errorMessage = (error as AxiosError).response?.data
                    .message;

                setAnomalies(null);
                setStatus(ActionStatus.ERROR);
                setErrorMessage(errorMessage);
            }
        },
        []
    );

    return { anomalies, errorMessage, status, fetchAnomaliesByAlertIdAndTime };
};

export const useDeleteAnomaly = (): DeleteAnomaly => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchDeleteAnomaly = useCallback(async (id: number) => {
        setStatus(ActionStatus.FETCHING);
        try {
            await deleteAnomaly(id);

            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { errorMessage, status, deleteAnomaly: dispatchDeleteAnomaly };
};
