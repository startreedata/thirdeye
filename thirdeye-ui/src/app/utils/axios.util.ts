import axios, { AxiosRequestConfig } from "axios";
import { getAccessToken, isAuthenticated } from "./authentication.util";

export const AxiosUtil = {
    setupInterceptors: (): void => {
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
    },
} as const;
