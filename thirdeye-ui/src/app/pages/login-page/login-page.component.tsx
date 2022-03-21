import { Button, Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { useAuth } from "../../components/auth-provider/auth-provider.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { useLoginPageStyles } from "./login-page.styles";

export const LoginPage: FunctionComponent = () => {
    const loginPageClasses = useLoginPageStyles();
    const { login } = useAuth();
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    return (
        <PageContents hideHeader title={t("label.login")}>
            <Grid
                container
                alignItems="center"
                className={loginPageClasses.loginPage}
                justify="center"
            >
                <Grid item>
                    {/* Login button */}
                    <Button color="primary" variant="contained" onClick={login}>
                        {t("label.login")}
                    </Button>
                </Grid>
            </Grid>
        </PageContents>
    );
};
