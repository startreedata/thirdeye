import { AxiosRequestConfig } from "axios";

// Returns axios request interceptor
export const getRequestInterceptor = (
    accessToken: string
): ((config: AxiosRequestConfig) => AxiosRequestConfig) => {
    const requestInterceptor = (
        config: AxiosRequestConfig
    ): AxiosRequestConfig => {
        if (accessToken) {
            // If accessToken is available, attach it to the request
            config.headers = {
                Authorization: `Bearer ${accessToken}`,
            };
        }

        return config;
    };

    return requestInterceptor;
};
