import { Button, Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { logout } from "../../rest/auth-rest/auth-rest";
import { useAuthStore } from "../../store/auth-store/auth-store";
import { getSignOutPath } from "../../utils/routes-util/routes-util";
import { useSignOutPageStyles } from "./sign-out-page.styles";

export const SignOutPage: FunctionComponent = () => {
    const signOutPageClasses = useSignOutPageStyles();
    const [loading, setLoading] = useState(true);
    const [clearAccessToken] = useAuthStore((state) => [
        state.clearAccessToken,
    ]);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: t("label.sign-out"),
                onClick: (): void => {
                    history.push(getSignOutPath());
                },
            },
        ]);

        setLoading(false);
    }, []);

    const performSignOut = (): void => {
        logout().then((): void => {
            clearAccessToken();

            // Let authentication state force reload
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
                    className={signOutPageClasses.buttonContainer}
                    justify="center"
                >
                    <Grid item>
                        <Button
                            color="primary"
                            size="large"
                            variant="contained"
                            onClick={performSignOut}
                        >
                            {t("label.sign-out")}
                        </Button>
                    </Grid>
                </Grid>
            </PageContents>
        </PageContainer>
    );
};
