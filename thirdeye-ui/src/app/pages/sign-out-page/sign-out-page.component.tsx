import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { useAuth } from "../../components/auth-provider/auth-provider.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { logout } from "../../rest/auth-rest/auth-rest";
import { getSignOutPath } from "../../utils/routes-util/routes-util";

export const SignOutPage: FunctionComponent = () => {
    const { signOut } = useAuth();
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

        performSignOut();
    }, []);

    const performSignOut = (): void => {
        logout().then((): void => {
            signOut();

            // Let authentication state force reload
        });
    };

    return (
        <PageContainer>
            <LoadingIndicator />
        </PageContainer>
    );
};
