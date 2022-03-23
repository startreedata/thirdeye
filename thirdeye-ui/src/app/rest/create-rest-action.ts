import { AxiosError } from "axios";
import "axios/index";
import { useCallback, useState } from "react";
import { getErrorMessages } from "../utils/rest/rest.util";
import { ActionStatus } from "./actions.interfaces";

interface FetchFunction<DataResponseType> {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (...options: any[]): Promise<DataResponseType>;
}

export interface UseHTTPActionHook<DataResponseType> {
    data: DataResponseType | null;
    makeRequest: FetchFunction<DataResponseType | undefined>;
    status: ActionStatus;
    errorMessages: string[];
}

function useHTTPAction<DataResponseType>(
    restFunction: FetchFunction<DataResponseType>
): UseHTTPActionHook<DataResponseType> {
    const [data, setData] = useState<DataResponseType | null>(null);
    const [status, setStatus] = useState<ActionStatus>(ActionStatus.Initial);
    const [errorMessages, setErrorMessages] = useState<string[]>([]);

    const makeRequest = useCallback(async (...options) => {
        setStatus(ActionStatus.Working);
        try {
            const fetchedData = await restFunction(...options);
            setData(fetchedData);
            setStatus(ActionStatus.Done);
            setErrorMessages([]);

            return fetchedData;
        } catch (error) {
            const axiosError = error as AxiosError;
            setData(null);
            setStatus(ActionStatus.Error);
            setErrorMessages(getErrorMessages(axiosError));
        }

        return undefined;
    }, []);

    return { data, makeRequest, status, errorMessages };
}

export { useHTTPAction };
