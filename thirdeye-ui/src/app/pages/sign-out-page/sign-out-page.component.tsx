import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { useAuth } from "../../components/auth-provider/auth-provider.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { logout } from "../../rest/auth/auth.rest";

export const SignOutPage: FunctionComponent = () => {
    const { signOut } = useAuth();
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
        performSignOut();
    }, []);

    const performSignOut = (): void => {
        // Sign out and let authentication state force reload
        logout().finally(() => signOut());
    };

    return (
        <PageContents hideHeader title={t("label.sign-out")}>
            <LoadingIndicator />
        </PageContents>
    );
};
