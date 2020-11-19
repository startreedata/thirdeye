import { AxiosRequestConfig } from "axios";
import { getAccessToken, isAuthenticated } from "../auth/auth-util";

// axios request interceptor
export const requestInterceptor = (
    config: AxiosRequestConfig
): AxiosRequestConfig => {
    if (isAuthenticated()) {
        // If authenticated, attach access token to the request
        config.headers = {
            Authorization: `Bearer ${getAccessToken()}`,
        };
    }

    return config;
};
