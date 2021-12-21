import { AxiosError } from "axios";
import "axios/index";
import { useCallback, useState } from "react";
import { ActionStatus } from "./actions.interfaces";

interface FetchFunction<DataResponseType> {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (...options: any[]): Promise<DataResponseType>;
}

export interface UseHTTPActionHook<DataResponseType> {
    data: DataResponseType | null;
    makeRequest: FetchFunction<DataResponseType | undefined>;
    status: ActionStatus;
    errorMessage: string;
}

function useHTTPAction<DataResponseType>(
    restFunction: FetchFunction<DataResponseType>
): UseHTTPActionHook<DataResponseType> {
    const [data, setData] = useState<DataResponseType | null>(null);
    const [status, setStatus] = useState<ActionStatus>(ActionStatus.Initial);
    const [errorMessage, setErrorMessage] = useState("");

    const makeRequest = useCallback(async (...options) => {
        setStatus(ActionStatus.Working);
        try {
            const fetchedData = await restFunction(...options);
            setData(fetchedData);
            setStatus(ActionStatus.Done);
            setErrorMessage("");

            return fetchedData;
        } catch (error) {
            const axiosError = error as AxiosError;
            setData(null);
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
