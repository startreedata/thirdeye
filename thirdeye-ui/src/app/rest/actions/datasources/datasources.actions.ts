import { AxiosError } from "axios";
import { useCallback, useState } from "react";
import {
    createDatasource,
    createDatasources,
    deleteDatasource,
    getAllDatasources,
    getDatasource,
    updateDatasource,
    updateDatasources,
} from "../../datasources/datasources.rest";
import { Datasource } from "../../dto/datasource.interfaces";
import { ActionStatus } from "../actions.interfaces";
import {
    CreateDatasource,
    CreateDatasources,
    DeleteDatasource,
    FetchAllDatasource,
    FetchDatasource,
    UpdateDatasource,
    UpdateDatasources,
} from "./datasources.actions.interfaces";

export const useFetchDatasource = (): FetchDatasource => {
    const [datasource, setDatasource] = useState<Datasource | null>(null);
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const fetchDatasource = useCallback(async (id: number) => {
        setStatus(ActionStatus.FETCHING);
        try {
            const datasource = await getDatasource(id);

            setDatasource(datasource);
            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setDatasource(null);
            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { datasource, errorMessage, status, fetchDatasource };
};

export const useFetchAllDatasource = (): FetchAllDatasource => {
    const [datasources, setDatasources] = useState<Datasource[] | null>(null);
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const fetchAllDatasource = useCallback(async () => {
        setStatus(ActionStatus.FETCHING);
        try {
            const datasources = await getAllDatasources();

            setDatasources(datasources);
            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setDatasources(null);
            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { datasources, errorMessage, status, fetchAllDatasource };
};

export const useCreateDatasource = (): CreateDatasource => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchCreateDatasource = useCallback(
        async (datasource: Datasource) => {
            setStatus(ActionStatus.FETCHING);
            try {
                await createDatasource(datasource);

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

    return { errorMessage, status, createDatasource: dispatchCreateDatasource };
};

export const useCreateDatasources = (): CreateDatasources => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchCreateDatasources = useCallback(
        async (datasources: Datasource[]) => {
            setStatus(ActionStatus.FETCHING);
            try {
                await createDatasources(datasources);

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

    return {
        errorMessage,
        status,
        createDatasources: dispatchCreateDatasources,
    };
};

export const useUpdateDatasource = (): UpdateDatasource => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchUpdateDatasource = useCallback(
        async (datasource: Datasource) => {
            setStatus(ActionStatus.FETCHING);
            try {
                await updateDatasource(datasource);

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

    return { errorMessage, status, updateDatasource: dispatchUpdateDatasource };
};

export const useUpdateDatasources = (): UpdateDatasources => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchUpdateDatasources = useCallback(
        async (datasources: Datasource[]) => {
            setStatus(ActionStatus.FETCHING);
            try {
                await updateDatasources(datasources);

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

    return {
        errorMessage,
        status,
        updateDatasources: dispatchUpdateDatasources,
    };
};

export const useDeleteDatasource = (): DeleteDatasource => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchDeleteDatasource = useCallback(async (id: number) => {
        setStatus(ActionStatus.FETCHING);
        try {
            await deleteDatasource(id);

            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { errorMessage, status, deleteDatasource: dispatchDeleteDatasource };
};
