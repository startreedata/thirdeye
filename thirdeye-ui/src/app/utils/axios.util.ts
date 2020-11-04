import axios, { AxiosRequestConfig } from "axios";
import { getAccessToken, isAuthenticated } from "./authentication.util";

// Initializes axios interceptors
export const initHTTPInterceptors = (): void => {
    axios.interceptors.request.use(
        (config: AxiosRequestConfig): AxiosRequestConfig => {
            if (isAuthenticated()) {
                config.headers = {
                    Authorization: `Bearer ${getAccessToken()}`,
                };
            }

            return config;
        }
    );
};
