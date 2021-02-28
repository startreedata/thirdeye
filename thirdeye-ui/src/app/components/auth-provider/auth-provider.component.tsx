import axios from "axios";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useAuthStore } from "../../stores/auth/auth.store";
import {
    getFulfilledResponseInterceptor,
    getRejectedResponseInterceptor,
    getRequestInterceptor,
} from "../../utils/axios/axios.util";
import { getWarningSnackbarOption } from "../../utils/snackbar/snackbar.util";
import { AuthProviderProps, UseAuthProps } from "./auth-provider.interfaces";

export const AuthProvider: FunctionComponent<AuthProviderProps> = (
    props: AuthProviderProps
) => {
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
        // Access token changed, initialize axios
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
                getRejectedResponseInterceptor(handleUnauthenticatedAccess)
            )
        );
    };

    const handleUnauthenticatedAccess = (): void => {
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

    return <>{props.children}</>;
};

export const useAuth = (): UseAuthProps => {
    return useAuthStore((state) => ({
        authDisabled: state.authDisabled,
        authenticated: state.authenticated,
        accessToken: state.accessToken,
        disableAuth: state.disableAuth,
        signIn: state.setAccessToken,
        signOut: state.clearAccessToken,
    }));
};
