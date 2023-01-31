/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import "axios/index";
import { AxiosError } from "axios/index";
import { useCallback, useEffect, useState } from "react";
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

    let isMounted = true;

    useEffect(() => {
        return () => {
            isMounted = false;
        };
    }, []);

    const makeRequest = useCallback(async (...options) => {
        setStatus(ActionStatus.Working);
        // Reset error message to avoid displaying previous errors
        setErrorMessages([]);
        try {
            const fetchedData = await restFunction(...options);
            if (isMounted) {
                setData(fetchedData);
                setStatus(ActionStatus.Done);
                setErrorMessages([]);
            }

            return fetchedData;
        } catch (error) {
            if (isMounted) {
                const axiosError = error as AxiosError;
                setData(null);
                setErrorMessages(getErrorMessages(axiosError));
                setStatus(ActionStatus.Error);
            }
        }

        return undefined;
    }, []);

    return { data, makeRequest, status, errorMessages };
}

export { useHTTPAction };
