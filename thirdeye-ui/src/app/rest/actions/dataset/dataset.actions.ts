import { AxiosError } from "axios";
import { useCallback, useState } from "react";
import {
    createDataset,
    createDatasets,
    deleteDataset,
    getAllDatasets,
    getDataset,
    onBoardDataset,
    updateDataset,
    updateDatasets,
} from "../../datasets/datasets.rest";
import { Dataset } from "../../dto/dataset.interfaces";
import { ActionStatus } from "../actions.interfaces";
import {
    CreateDataset,
    CreateDatasets,
    DeleteDataset,
    FetchAllDataset,
    FetchDataset,
    OnBoardDataset,
    UpdateDataset,
    UpdateDatasets,
} from "./dataset.actions.interfaces";

export const useFetchDataset = (): FetchDataset => {
    const [dataset, setDataset] = useState<Dataset | null>(null);
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const fetchDataset = useCallback(async (id: number) => {
        setStatus(ActionStatus.FETCHING);
        try {
            const dataset = await getDataset(id);

            setDataset(dataset);
            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setDataset(null);
            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { dataset, errorMessage, status, fetchDataset };
};

export const useFetchAllDataset = (): FetchAllDataset => {
    const [datasets, setDatasets] = useState<Dataset[] | null>(null);
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const fetchAllDataset = useCallback(async () => {
        setStatus(ActionStatus.FETCHING);
        try {
            const datasets = await getAllDatasets();

            setDatasets(datasets);
            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setDatasets(null);
            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { datasets, errorMessage, status, fetchAllDataset };
};

export const useCreateDataset = (): CreateDataset => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchCreateDataset = useCallback(async (dataset: Dataset) => {
        setStatus(ActionStatus.FETCHING);
        try {
            await createDataset(dataset);

            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { errorMessage, status, createDataset: dispatchCreateDataset };
};

export const useCreateDatasets = (): CreateDatasets => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchCreateDatasets = useCallback(async (datasets: Dataset[]) => {
        setStatus(ActionStatus.FETCHING);
        try {
            await createDatasets(datasets);

            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { errorMessage, status, createDatasets: dispatchCreateDatasets };
};

export const useOnBoardDataset = (): OnBoardDataset => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchOnBoardDataset = useCallback(
        async (datasetName: string, dataSourceName: string) => {
            setStatus(ActionStatus.FETCHING);
            try {
                await onBoardDataset(datasetName, dataSourceName);

                setStatus(ActionStatus.DONE);
                setErrorMessage("");
            } catch (error) {
                const errorMessage = (error as AxiosError).response?.data
                    .message;

                setStatus(ActionStatus.ERROR);
                setErrorMessage(errorMessage);
            }
        },
        []
    );

    return { errorMessage, status, onBoardDataset: dispatchOnBoardDataset };
};

export const useUpdateDataset = (): UpdateDataset => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchUpdateDataset = useCallback(async (dataset: Dataset) => {
        setStatus(ActionStatus.FETCHING);
        try {
            await updateDataset(dataset);

            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { errorMessage, status, updateDataset: dispatchUpdateDataset };
};

export const useUpdateDatasets = (): UpdateDatasets => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchUpdateDatasets = useCallback(async (datasets: Dataset[]) => {
        setStatus(ActionStatus.FETCHING);
        try {
            await updateDatasets(datasets);

            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { errorMessage, status, updateDatasets: dispatchUpdateDatasets };
};

export const useDeleteDataset = (): DeleteDataset => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchDeleteDataset = useCallback(async (id: number) => {
        setStatus(ActionStatus.FETCHING);
        try {
            await deleteDataset(id);

            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { errorMessage, status, deleteDataset: dispatchDeleteDataset };
};
