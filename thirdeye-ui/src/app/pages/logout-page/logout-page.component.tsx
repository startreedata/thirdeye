import {
    AppLoadingIndicatorV1,
    PageHeaderTextV1,
    PageHeaderV1,
    PageV1,
    useAuthProviderV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";

export const LogoutPage: FunctionComponent = () => {
    const { logout } = useAuthProviderV1();
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
        logout();
    }, []);

    return (
        <PageV1>
            <PageHeaderV1>
                <PageHeaderTextV1>{t("label.logout")}</PageHeaderTextV1>
            </PageHeaderV1>

            <AppLoadingIndicatorV1 />
        </PageV1>
    );
};
