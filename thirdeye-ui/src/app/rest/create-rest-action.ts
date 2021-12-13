import axios, { AxiosError, AxiosResponse } from "axios";
import "axios/index";
import { useCallback, useState } from "react";
import { ActionStatus } from "./actions.interfaces";

export interface UseHTTPActionHook<DataResponseType> {
    data?: DataResponseType;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    makeRequest: (
        url: string,
        ...options: any
    ) => Promise<DataResponseType | undefined>;
    status: ActionStatus;
    errorMessage: string;
}

function useHTTPAction<DataResponseType>(
    fetchFunction = axios.get
): UseHTTPActionHook<DataResponseType> {
    const [data, setData] = useState<DataResponseType>();
    const [status, setStatus] = useState(ActionStatus.Initial);
    const [errorMessage, setErrorMessage] = useState("");

    const makeRequest = useCallback(async (url: string, ...options) => {
        setStatus(ActionStatus.Working);
        try {
            const fetchedData: AxiosResponse<DataResponseType> = await fetchFunction(
                url,
                ...options
            );
            setData(fetchedData.data);
            setStatus(ActionStatus.Done);
            setErrorMessage("");

            return fetchedData.data;
        } catch (error) {
            const axiosError = error as AxiosError;
            setData(undefined);
            setStatus(ActionStatus.Error);
            setErrorMessage(
                axiosError &&
                    axiosError.response &&
                    axiosError.response.data &&
                    axiosError.response.data.message
            );
        }

        return undefined;
    }, []);

    return { data, makeRequest, status, errorMessage };
}

export { useHTTPAction };
