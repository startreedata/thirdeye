import { Button, Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { PageContainer } from "../../components/page-container/page-container.component";
import { logout } from "../../rest/auth/auth.rest";
import { removeAccessToken } from "../../utils/auth/auth.util";
import { signOutPageStyles } from "./sign-out-page.styles";

export const SignOutPage: FunctionComponent = () => {
    const signOutPageClasses = signOutPageStyles();

    const { t } = useTranslation();

    const performLogout = async (): Promise<void> => {
        await logout();
        removeAccessToken();

        location.reload();
    };

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
