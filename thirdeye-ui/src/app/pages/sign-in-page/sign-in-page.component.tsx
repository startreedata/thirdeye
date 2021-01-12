import { Button, Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { login } from "../../rest/auth-rest/auth-rest";
import { Auth } from "../../rest/dto/auth.interfaces";
import { useAuthStore } from "../../store/auth-store/auth-store";
import { getSignInPath } from "../../utils/routes-util/routes-util";
import { SignInPageProps } from "./sign-in-page.interfaces";
import { useSignInPageStyles } from "./sign-in-page.styles";

export const SignInPage: FunctionComponent<SignInPageProps> = (
    props: SignInPageProps
) => {
    const signInPageClasses = useSignInPageStyles();
    const [loading, setLoading] = useState(true);
    const [setAccessToken] = useAuthStore((state) => [state.setAccessToken]);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: t("label.sign-in"),
                onClick: (): void => {
                    history.push(getSignInPath());
                },
            },
        ]);

        setLoading(false);
    }, []);

    const performSignIn = (): void => {
        login().then((auth: Auth): void => {
            setAccessToken(auth.accessToken);

            // Redirect
            history.push(props.redirectionURL);
        });
    };

    if (loading) {
        return (
            <PageContainer>
                <LoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <PageContainer>
            <PageContents hideTimeRange>
                <Grid
                    container
                    alignItems="center"
                    className={signInPageClasses.buttonContainer}
                    justify="center"
                >
                    <Grid item>
                        <Button
                            color="primary"
                            size="large"
                            variant="contained"
                            onClick={performSignIn}
                        >
                            {t("label.sign-in")}
                        </Button>
                    </Grid>
                </Grid>
            </PageContents>
        </PageContainer>
    );
};
