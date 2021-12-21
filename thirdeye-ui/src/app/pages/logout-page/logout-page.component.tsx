import {
    AppLoadingIndicatorV1,
    useAuthProviderV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { PageContents } from "../../components/page-contents/page-contents.component";

export const LogoutPage: FunctionComponent = () => {
    const { logout } = useAuthProviderV1();
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
        logout();
    }, []);

    return (
        <PageContents hideHeader title={t("label.logout")}>
            <AppLoadingIndicatorV1 />
        </PageContents>
    );
};
