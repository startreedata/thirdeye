import { AppLoadingIndicatorV1 } from "@startree-ui/platform-ui";
import axios from "axios";
import { useSnackbar } from "notistack";
import React, {
    createContext,
    FunctionComponent,
    useContext,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { login } from "../../rest/auth/auth.rest";
import { useAuthStore } from "../../stores/auth/auth.store";
import {
    getFulfilledResponseInterceptor,
    getRejectedResponseInterceptor,
    getRequestInterceptor,
} from "../../utils/axios/axios.util";
import { getAccessTokenFromHashParams } from "../../utils/params/params.util";
import {
    getErrorSnackbarOption,
    getWarningSnackbarOption,
} from "../../utils/snackbar/snackbar.util";
import {
    AuthContextProps,
    AuthProviderProps,
} from "./auth-provider.interfaces";

export const AuthProvider: FunctionComponent<AuthProviderProps> = (
    props: AuthProviderProps
) => {
    const [authLoading, setAuthLoading] = useState(true);
    const [axiosLoading, setAxiosLoading] = useState(true);
    const [axiosRequestInterceptorId, setAxiosRequestInterceptorId] = useState(
        0
    );
    const [
        axiosResponseInterceptorId,
        setAxiosResponseInterceptorId,
    ] = useState(0);
    const [
        authDisabled,
        authenticated,
        accessToken,
        setAccessToken,
        clearAccessToken,
    ] = useAuthStore((state) => [
        state.authDisabled,
        state.authenticated,
        state.accessToken,
        state.setAccessToken,
        state.clearAccessToken,
    ]);
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        setAuthLoading(true);
        initAuth();
    }, []);

    useEffect(() => {
        // Access token changed, initialize axios
        setAxiosLoading(true);
        initAxios();
        setAxiosLoading(false);
    }, [accessToken]);

    const initAuth = (): void => {
        if (authDisabled || authenticated) {
            setAuthLoading(false);

            return;
        }

        // Check to see if access token is available in the URL
        const accessToken = getAccessTokenFromHashParams();
        if (accessToken) {
            setAccessToken(accessToken);
        }

        setAuthLoading(false);
    };

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
                getRejectedResponseInterceptor(
                    handleUnauthenticatedAccess,
                    enqueueSnackbar
                )
            )
        );
    };

    const handleUnauthenticatedAccess = (): void => {
        clearAccessToken();
        enqueueSnackbar(t("message.logout"), getWarningSnackbarOption(true));
    };

    const performLogin = async (): Promise<boolean> => {
        if (authDisabled || authenticated) {
            true;
        }

        try {
            const auth = await login();
            if (!auth || !auth.accessToken) {
                enqueueSnackbar(
                    t("message.login-error"),
                    getErrorSnackbarOption()
                );

                return false;
            }

            setAccessToken(auth.accessToken);
        } catch (error) {
            return false;
        }

        return true;
    };

    const authContext: AuthContextProps = {
        authDisabled: authDisabled,
        authenticated: authenticated,
        accessToken: accessToken,
        login: performLogin,
        logout: clearAccessToken,
    };

    if (authLoading || axiosLoading) {
        return <AppLoadingIndicatorV1 />;
    }

    return (
        <AuthContext.Provider value={authContext}>
            {props.children}
        </AuthContext.Provider>
    );
};

export const AuthContext = createContext<AuthContextProps>(
    {} as AuthContextProps
);

export const useAuth = (): AuthContextProps => {
    return useContext(AuthContext);
};
