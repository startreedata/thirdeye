import { AxiosError, AxiosRequestConfig } from "axios";
import { isEmpty } from "lodash";
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
        } else if (
            error &&
            error.response &&
            error.response.data &&
            error.response.data.list.length
        ) {
            error.response.data.list.map(
                (err: { code: string; msg: string }) => {
                    // Toast error message
                    if (!isEmpty(err.msg)) {
                        enqueueSnackbar(err.msg, getErrorSnackbarOption());
                    }
                }
            );
        }

        throw error;
    };

    return rejectedResponseInterceptor;
};

export const duplicateKeyForArrayQueryParams = (params: {
    [key: string]: number | string | string[] | boolean;
}): string => {
    const searchStringParts: string[] = [];

    Object.entries(params).forEach(([queryParamKey, value]) => {
        if (Array.isArray(value)) {
            value.forEach((val) => {
                searchStringParts.push(`${queryParamKey}=${val}`);
            });
        } else {
            searchStringParts.push(`${queryParamKey}=${value}`);
        }
    });

    return searchStringParts.length > 0 ? searchStringParts.join("&") : "";
};
