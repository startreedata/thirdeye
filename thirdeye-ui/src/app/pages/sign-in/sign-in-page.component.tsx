import { Button, Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import { login } from "../../rest/auth/auth-rest";
import { Auth } from "../../rest/dto/auth.interfaces";
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs/application-breadcrumbs-store";
import { useAuthStore } from "../../store/auth/auth-store";
import { useRedirectionPathStore } from "../../store/redirection-path/redirection-path-store";
import { getSignInPath } from "../../utils/route/routes-util";
import { useSignInPageStyles } from "./sign-in-page.styles";

export const SignInPage: FunctionComponent = () => {
    const signInPageClasses = useSignInPageStyles();
    const [loading, setLoading] = useState(true);
    const [setAccessToken] = useAuthStore((state) => [state.setAccessToken]);
    const [setPageBreadcrumbs] = useApplicationBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const [
        redirectToPath,
        clearRedirectToPath,
    ] = useRedirectionPathStore((state) => [
        state.redirectToPath,
        state.clearRedirectToPath,
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
    }, [setPageBreadcrumbs, t]);

    const performLogin = async (): Promise<void> => {
        const auth: Auth = await login();
        setAccessToken(auth.accessToken);

        // Redirect if a path to redirect to is available, or let authentication state force reload
        if (redirectToPath) {
            history.push(redirectToPath);

            clearRedirectToPath();
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
                className={signInPageClasses.grid}
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
