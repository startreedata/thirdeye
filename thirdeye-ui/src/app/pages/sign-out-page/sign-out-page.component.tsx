import { Button, Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { logout } from "../../rest/auth-rest/auth-rest";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { useAuthStore } from "../../store/auth-store/auth-store";
import { getSignOutPath } from "../../utils/routes-util/routes-util";
import { useSignOutPageStyles } from "./sign-out-page.styles";

export const SignOutPage: FunctionComponent = () => {
    const signOutPageClasses = useSignOutPageStyles();
    const [loading, setLoading] = useState(true);
    const [removeAccessToken] = useAuthStore((state) => [
        state.removeAccessToken,
    ]);
    const [setPageBreadcrumbs] = useAppBreadcrumbsStore((state) => [
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
    }, []);

    const performLogout = async (): Promise<void> => {
        await logout();
        removeAccessToken();

        // Let authentication state force reload
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
            <Grid
                container
                alignItems="center"
                className={signOutPageClasses.container}
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
