import { Button, Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import { logout } from "../../rest/auth/auth-rest";
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs/application-breadcrumbs-store";
import { useAuthStore } from "../../store/auth/auth-store";
import { getSignOutPath } from "../../utils/route/routes-util";
import { useSignOutPageStyles } from "./sign-out-page.styles";

export const SignOutPage: FunctionComponent = () => {
    const signOutPageClasses = useSignOutPageStyles();
    const [loading, setLoading] = useState(true);
    const [removeAccessToken] = useAuthStore((state) => [
        state.removeAccessToken,
    ]);
    const [setPageBreadcrumbs] = useApplicationBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: t("label.sign-out"),
                path: getSignOutPath(),
            },
        ]);

        setLoading(false);
    }, [setPageBreadcrumbs, t]);

    const performLogout = async (): Promise<void> => {
        await logout();
        removeAccessToken();

        // Let authentication state force reload
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
                className={signOutPageClasses.grid}
                justify="center"
            >
                <Grid item>
                    <Button
                        color="primary"
                        variant="contained"
                        onClick={performLogout}
                    >
                        {t("label.sign-out")}
                    </Button>
                </Grid>
            </Grid>
        </PageContainer>
    );
};
