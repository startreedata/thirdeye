import axios from "axios";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AppBar } from "./components/app-bar/app-bar.component";
import { AppRouter } from "./routers/app-router/app-router";
import { useAuthStore } from "./store/auth-store/auth-store";
import {
    getFulfilledResponseInterceptor,
    getRejectedResponseInterceptor,
    getRequestInterceptor,
} from "./utils/axios-util/axios-util";
import { getWarningSnackbarOption } from "./utils/snackbar-util/snackbar-util";

// ThirdEye UI app
export const App: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [axiosRequestInterceptorId, setAxiosRequestInterceptorId] = useState(
        0
    );
    const [
        axiosResponseInterceptorId,
        setAxiosResponseInterceptorId,
    ] = useState(0);
    const [accessToken, clearAccessToken] = useAuthStore((state) => [
        state.accessToken,
        state.clearAccessToken,
    ]);
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        setLoading(true);

        initAxios();

        setLoading(false);
    }, [accessToken]);

    const initAxios = (): void => {
        // Clear existing interceptors
        axios.interceptors.request.eject(axiosRequestInterceptorId);
        axios.interceptors.response.eject(axiosResponseInterceptorId);

        // Set new interceptors
        setAxiosRequestInterceptorId(
            axios.interceptors.request.use(getRequestInterceptor(accessToken))
        );
        setAxiosResponseInterceptorId(
            axios.interceptors.response.use(
                getFulfilledResponseInterceptor(),
                getRejectedResponseInterceptor(unauthenticatedAccessHandler)
            )
        );
    };

    const unauthenticatedAccessHandler = (): void => {
        // Notify
        enqueueSnackbar(
            t("message.signed-out"),
            getWarningSnackbarOption(true)
        );

        // Sign out
        clearAccessToken();
    };

    if (loading) {
        return <></>;
    }

    return (
        <>
            <AppBar />

            <AppRouter />
        </>
    );
};
