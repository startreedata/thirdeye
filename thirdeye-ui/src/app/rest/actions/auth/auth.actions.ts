import { AxiosError } from "axios";
import { useCallback, useState } from "react";
import { login, logout } from "../../auth/auth.rest";
import { Auth } from "../../dto/auth.interfaces";
import { ActionStatus } from "../actions.interfaces";
import { Login, Logout } from "./auth.actions.interfaces";

export const useLogin = (): Login => {
    const [auth, setAuth] = useState<Auth | null>(null);
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchLogin = useCallback(async () => {
        setStatus(ActionStatus.FETCHING);
        try {
            const auth = await login();

            setAuth(auth);
            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setAuth(null);
            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { auth, errorMessage, status, login: dispatchLogin };
};

export const useLogout = (): Logout => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchLogout = useCallback(async () => {
        setStatus(ActionStatus.FETCHING);
        try {
            await logout();

            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { errorMessage, status, logout: dispatchLogout };
};
