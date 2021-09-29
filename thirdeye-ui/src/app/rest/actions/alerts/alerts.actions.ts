import { AxiosError } from "axios";
import { useCallback, useState } from "react";
import {
    createAlert,
    createAlerts,
    deleteAlert,
    getAlert,
    getAlertEvaluation,
    getAllAlerts,
    updateAlert,
    updateAlerts,
} from "../../alerts/alerts.rest";
import { Alert, AlertEvaluation } from "../../dto/alert.interfaces";
import { ActionStatus } from "../actions.interfaces";
import {
    CreateAlert,
    CreateAlerts,
    DeleteAlert,
    FetchAlert,
    FetchAlertEvaluation,
    FetchAllAlerts,
    UpdateAlert,
    UpdateAlerts,
} from "./alerts.actions.interfaces";

export const useFetchAlert = (): FetchAlert => {
    const [alert, setAlert] = useState<Alert | null>(null);
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const fetchAlert = useCallback(async (id: number) => {
        setStatus(ActionStatus.FETCHING);
        try {
            const alert = await getAlert(id);

            setAlert(alert);
            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setAlert(null);
            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { alert, errorMessage, status, fetchAlert };
};

export const useFetchAllAlerts = (): FetchAllAlerts => {
    const [alerts, setAlerts] = useState<Alert[] | null>(null);
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const fetchAllAlerts = useCallback(async () => {
        setStatus(ActionStatus.FETCHING);
        try {
            const alerts = await getAllAlerts();

            setAlerts(alerts);
            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setAlerts(null);
            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { alerts, errorMessage, status, fetchAllAlerts };
};

export const useCreateAlert = (): CreateAlert => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchCreateAlert = useCallback(async (alert: Alert) => {
        setStatus(ActionStatus.FETCHING);
        try {
            await createAlert(alert);

            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { errorMessage, status, createAlert: dispatchCreateAlert };
};

export const useCreateAlerts = (): CreateAlerts => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchCreateAlerts = useCallback(async (alerts: Alert[]) => {
        setStatus(ActionStatus.FETCHING);
        try {
            await createAlerts(alerts);

            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { errorMessage, status, createAlerts: dispatchCreateAlerts };
};

export const useUpdateAlert = (): UpdateAlert => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchUpdateAlert = useCallback(async (alert: Alert) => {
        setStatus(ActionStatus.FETCHING);
        try {
            await updateAlert(alert);

            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { errorMessage, status, updateAlert: dispatchUpdateAlert };
};

export const useUpdateAlerts = (): UpdateAlerts => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchUpdateAlerts = useCallback(async (alerts: Alert[]) => {
        setStatus(ActionStatus.FETCHING);
        try {
            await updateAlerts(alerts);

            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { errorMessage, status, updateAlerts: dispatchUpdateAlerts };
};

export const useDeleteAlert = (): DeleteAlert => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchDeleteAlert = useCallback(async (id: number) => {
        setStatus(ActionStatus.FETCHING);
        try {
            await deleteAlert(id);

            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { errorMessage, status, deleteAlert: dispatchDeleteAlert };
};

export const useFetchAlertEvaluation = (): FetchAlertEvaluation => {
    const [
        alertEvaluation,
        setAlertEvaluation,
    ] = useState<AlertEvaluation | null>(null);
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const fetchAlertEvaluation = useCallback(
        async (alertEvaluation: AlertEvaluation) => {
            setStatus(ActionStatus.FETCHING);
            try {
                const alertEvaluationResponse = await getAlertEvaluation(
                    alertEvaluation
                );
                setAlertEvaluation(alertEvaluationResponse);

                setStatus(ActionStatus.DONE);
                setErrorMessage("");
            } catch (error) {
                const errorMessage = (error as AxiosError).response?.data
                    .message;

                setAlertEvaluation(null);
                setStatus(ActionStatus.ERROR);
                setErrorMessage(errorMessage);
            }
        },
        []
    );

    return {
        alertEvaluation,
        errorMessage,
        status,
        fetchAlertEvaluation,
    };
};
