import { Box, Button } from "@material-ui/core";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { useAuth } from "../../components/auth-provider/auth-provider.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { login } from "../../rest/auth/auth.rest";
import { getErrorSnackbarOption } from "../../utils/snackbar/snackbar.util";
import { SignInPageProps } from "./sign-in-page.interfaces";

export const SignInPage: FunctionComponent<SignInPageProps> = (
    props: SignInPageProps
) => {
    const [loading, setLoading] = useState(false);
    const { signIn } = useAuth();
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { enqueueSnackbar } = useSnackbar();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    const handleSignIn = (): void => {
        setLoading(true);

        login()
            .then((auth) => {
                signIn(auth.accessToken);

                // Redirect
                history.push(props.redirectURL);
            })
            .catch(() => {
                enqueueSnackbar(
                    t("message.sign-in-error"),
                    getErrorSnackbarOption()
                );

                setLoading(false);
            });
    };

    if (loading) {
        return <LoadingIndicator />;
    }

    return (
        <PageContents hideHeader title={t("label.sign-in")}>
            <Box
                alignItems="center"
                display="flex"
                flex={1}
                height="100%"
                justifyContent="center"
                width="100%"
            >
                {/* Sign in */}
                <Button
                    color="primary"
                    variant="contained"
                    onClick={handleSignIn}
                >
                    {t("label.sign-in")}
                </Button>
            </Box>
        </PageContents>
    );
};
