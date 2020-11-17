import React, { FunctionComponent, useEffect } from "react";
import { login } from "../../rest/auth/auth.rest";
import { Auth } from "../../rest/dto/auth.interfaces";
import { setAuthentication } from "../../utils/auth/auth.util";

export const SignInPage: FunctionComponent = () => {
    useEffect(() => {
        performLogin();
    }, []);

    const performLogin = async (): Promise<void> => {
        const authentication: Auth = await login();
        setAuthentication(authentication.accessToken);

        location.reload();
    };

    return <></>;
};
