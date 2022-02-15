import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import {
    AppLoadingIndicatorV1,
    AuthExceptionCodeV1,
    NotificationTypeV1,
    PageHeaderTextV1,
    PageHeaderV1,
    PageV1,
    useAuthProviderV1,
    useNotificationProviderV1,
} from "../../components/platform-ui/components";
import { isBlockingAuthExceptionV1 } from "../../components/platform-ui/utils";

export const LoginPage: FunctionComponent = () => {
    const [exceptionCode, setExceptionCode] = useState("");
    const { authExceptionCode, login } = useAuthProviderV1();
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        if (
            isBlockingAuthExceptionV1(authExceptionCode as AuthExceptionCodeV1)
        ) {
            // Blocking auth exception
            setExceptionCode(authExceptionCode);

            return;
        }

        login();
    }, [authExceptionCode]);

    useEffect(() => {
        if (isBlockingAuthExceptionV1(exceptionCode as AuthExceptionCodeV1)) {
            // Display blocking auth exception
            notify(
                NotificationTypeV1.Error,
                t("message.authentication-error", {
                    exceptionCode: exceptionCode,
                }),
                true
            );
        }
    }, [exceptionCode]);

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    // Loading indicator
    if (!isBlockingAuthExceptionV1(exceptionCode as AuthExceptionCodeV1)) {
        return <AppLoadingIndicatorV1 />;
    }

    return (
        <PageV1>
            <PageHeaderV1>
                <PageHeaderTextV1>
                    {t("label.authentication-error")}
                </PageHeaderTextV1>
            </PageHeaderV1>
        </PageV1>
    );
};
