import { Button, Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import { login } from "../../rest/auth-rest/auth-rest";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { useAuthStore } from "../../store/auth-store/auth-store";
import { useRedirectionPathStore } from "../../store/redirection-path-store/redirection-path-store";
import { getSignInPath } from "../../utils/routes-util/routes-util";
import { useSignInPageStyles } from "./sign-in-page.styles";

export const SignInPage: FunctionComponent = () => {
    const signInPageClasses = useSignInPageStyles();
    const [loading, setLoading] = useState(true);
    const [setAccessToken] = useAuthStore((state) => [state.setAccessToken]);
    const [setPageBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const [
        redirectionPath,
        clearRedirectionPath,
    ] = useRedirectionPathStore((state) => [
        state.redirectionPath,
        state.clearRedirectionPath,
    ]);
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: t("label.sign-in"),
                path: getSignInPath(),
            },
        ]);

        setLoading(false);
    }, []);

    const performLogin = async (): Promise<void> => {
        const auth = await login();
        setAccessToken(auth.accessToken);

        // Redirect if a path to redirect to is available, or let authentication state force reload
        if (redirectionPath) {
            history.push(redirectionPath);

            clearRedirectionPath();
        }
    };

    if (loading) {
        return (
            <PageContainer>
                <PageLoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <PageContainer>
            <Grid
                container
                alignItems="center"
                className={signInPageClasses.container}
                justify="center"
            >
                <Grid item>
                    <Button
                        color="primary"
                        variant="contained"
                        onClick={performLogin}
                    >
                        {t("label.sign-in")}
                    </Button>
                </Grid>
            </Grid>
        </PageContainer>
    );
};
