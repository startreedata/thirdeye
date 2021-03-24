import { Button, Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { useAuth } from "../../components/auth-provider/auth-provider.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { useSignInPageStyles } from "./sign-in-page.styles";

export const SignInPage: FunctionComponent = () => {
    const signInPageClasses = useSignInPageStyles();
    const { signIn } = useAuth();
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    return (
        <PageContents hideHeader title={t("label.sign-in")}>
            <Grid
                container
                alignItems="center"
                className={signInPageClasses.signInPage}
                justify="center"
            >
                <Grid item>
                    {/* Sign in button */}
                    <Button
                        color="primary"
                        variant="contained"
                        onClick={signIn}
                    >
                        {t("label.sign-in")}
                    </Button>
                </Grid>
            </Grid>
        </PageContents>
    );
};
