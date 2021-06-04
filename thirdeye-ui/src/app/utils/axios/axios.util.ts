import { AxiosError, AxiosRequestConfig } from "axios";
import type { OptionsObject, SnackbarKey, SnackbarMessage } from "notistack";
import { getErrorSnackbarOption } from "../snackbar/snackbar.util";

// Returns axios request interceptor
export const getRequestInterceptor = (
    accessToken: string
): ((requestConfig: AxiosRequestConfig) => AxiosRequestConfig) => {
    const requestInterceptor = (
        requestConfig: AxiosRequestConfig
    ): AxiosRequestConfig => {
        // If access token is available, attach it to the request
        if (accessToken) {
            requestConfig.headers = {
                Authorization: `Bearer ${accessToken}`,
            };
        }

        return requestConfig;
    };

    return requestInterceptor;
};

// Returns axios fulfilled response interceptor
export const getFulfilledResponseInterceptor = (): (<T>(
    response: T
) => T | Promise<T>) => {
    const fulfilledResponseInterceptor = <T>(response: T): T | Promise<T> => {
        // Forward the fulfilled response
        return response;
    };

    return fulfilledResponseInterceptor;
};

// Returns axios rejected response interceptor
export const getRejectedResponseInterceptor = (
    unauthenticatedAccessFn: () => void,
    enqueueSnackbar: (
        message: SnackbarMessage,
        options?: OptionsObject
    ) => SnackbarKey
): ((error: AxiosError) => void) => {
    const rejectedResponseInterceptor = (error: AxiosError): void => {
        if (error && error.response && error.response.status === 401) {
            // Unauthenticated access
            unauthenticatedAccessFn && unauthenticatedAccessFn();
        } else if (error && error.response && error.response.status === 500) {
            // Toast error message
            const erroMessage = error.response.data?.list[0]?.msg || "";
            enqueueSnackbar(erroMessage, getErrorSnackbarOption());
        }

        throw error;
    };

    return rejectedResponseInterceptor;
};
