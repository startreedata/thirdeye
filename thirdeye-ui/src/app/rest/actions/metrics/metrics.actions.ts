import { AxiosError } from "axios";
import { useCallback, useState } from "react";
import { LogicalMetric, Metric } from "../../dto/metric.interfaces";
import {
    createMetric,
    deleteMetric,
    getAllMetrics,
    getMetric,
    updateMetric,
} from "../../metrics/metrics.rest";
import { ActionStatus } from "../actions.interfaces";
import {
    CreateMetric,
    DeleteMetric,
    FetchAllMetrics,
    FetchMetric,
    UpdateMetric,
} from "./metrics.actions.interfaces";

export const useFetchMetric = (): FetchMetric => {
    const [metric, setMetric] = useState<Metric | null>(null);
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const fetchMetric = useCallback(async (id: number) => {
        setStatus(ActionStatus.FETCHING);
        try {
            const metric = await getMetric(id);

            setMetric(metric);
            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setMetric(null);
            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { metric, errorMessage, status, fetchMetric };
};

export const useFetchAllMetrics = (): FetchAllMetrics => {
    const [metrics, setMetric] = useState<Metric[] | null>(null);
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const fetchAllMetrics = useCallback(async () => {
        setStatus(ActionStatus.FETCHING);
        try {
            const metrics = await getAllMetrics();

            setMetric(metrics);
            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setMetric(null);
            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { metrics, errorMessage, status, fetchAllMetrics };
};

export const useCreateMetric = (): CreateMetric => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchCreateMetric = useCallback(async (metric: LogicalMetric) => {
        setStatus(ActionStatus.FETCHING);
        try {
            await createMetric(metric);

            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { errorMessage, status, createMetric: dispatchCreateMetric };
};

export const useUpdateMetric = (): UpdateMetric => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchUpdateMetric = useCallback(async (metric: LogicalMetric) => {
        setStatus(ActionStatus.FETCHING);
        try {
            await updateMetric(metric);

            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { errorMessage, status, updateMetric: dispatchUpdateMetric };
};

export const useDeleteMetric = (): DeleteMetric => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchDeleteMetric = useCallback(async (id: number) => {
        setStatus(ActionStatus.FETCHING);
        try {
            await deleteMetric(id);

            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { errorMessage, status, deleteMetric: dispatchDeleteMetric };
};
