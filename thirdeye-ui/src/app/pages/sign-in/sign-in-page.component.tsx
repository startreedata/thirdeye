import { Button, Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import { login } from "../../rest/auth/auth-rest";
import { Auth } from "../../rest/dto/auth.interfaces";
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs/application-breadcrumbs-store";
import { useAuthStore } from "../../store/auth/auth-store";
import { getSignInPath } from "../../utils/route/routes-util";
import { signInPageStyles } from "./sign-in-page.styles";

export const SignInPage: FunctionComponent = () => {
    const signInPageClasses = signInPageStyles();

    const [loading, setLoading] = useState(true);
    const [setAccessToken] = useAuthStore((state) => [state.setAccessToken]);
    const [setPageBreadcrumbs] = useApplicationBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
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

        location.reload();
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
