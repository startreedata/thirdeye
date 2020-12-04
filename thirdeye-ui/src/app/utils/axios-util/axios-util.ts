import { AxiosError, AxiosRequestConfig } from "axios";

// Returns axios request interceptor
export const getRequestInterceptor = (
    accessToken: string
): ((requestConfig: AxiosRequestConfig) => AxiosRequestConfig) => {
    const requestInterceptor = (
        requestConfig: AxiosRequestConfig
    ): AxiosRequestConfig => {
        if (accessToken) {
            // If accessToken is available, attach it to the request
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
        // Do nothing, forward the fulfilled response
        return response;
    };

    return fulfilledResponseInterceptor;
};

// Returns axios rejected response interceptor
export const getRejectedResponseInterceptor = (
    unauthenticatedAccessHandler: () => void
): ((error: AxiosError) => void) => {
    const rejectedResponseInterceptor = (error: AxiosError): void => {
        if (error.response?.status === 401) {
            // Unauthenticated access
            unauthenticatedAccessHandler();
        }

        throw error;
    };

    return rejectedResponseInterceptor;
};
