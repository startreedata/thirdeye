import React, { FunctionComponent, useEffect } from "react";
import { setAuthentication } from "../../utils/authentication.util";
import { Authentication } from "../../utils/rest/authentication-rest/authentication-rest.interfaces";
import { login } from "../../utils/rest/authentication-rest/authentication-rest.util";

export const SignInPage: FunctionComponent = () => {
    useEffect(() => {
        performLogin();
    }, []);

    const performLogin = async (): Promise<void> => {
        const authentication: Authentication = await login();
        setAuthentication(authentication.accessToken);

        location.reload;
    };

    return <></>;
};
