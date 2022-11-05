import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    AppLoadingIndicatorV1,
    PageHeaderTextV1,
    PageV1,
    useAuthProviderV1,
} from "../../platform/components";

export const LogoutPage: FunctionComponent = () => {
    const { logout } = useAuthProviderV1();
    const { t } = useTranslation();

    useEffect(() => {
        logout();
    }, []);

    return (
        <PageV1>
            <PageHeader>
                <PageHeaderTextV1>{t("label.logout")}</PageHeaderTextV1>
            </PageHeader>

            <AppLoadingIndicatorV1 />
        </PageV1>
    );
};
