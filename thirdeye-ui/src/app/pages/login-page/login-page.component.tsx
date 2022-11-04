import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    AppLoadingIndicatorV1,
    AuthExceptionCodeV1,
    NotificationTypeV1,
    PageHeaderTextV1,
    PageV1,
    useAuthProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import {
    AuthExceptionCodeV1Label,
    isBlockingAuthExceptionV1,
} from "../../platform/utils";

export const LoginPage: FunctionComponent = () => {
    const [exceptionCode, setExceptionCode] = useState("");
    const { authExceptionCode, login } = useAuthProviderV1();
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
                    exceptionCode:
                        AuthExceptionCodeV1Label[
                            exceptionCode as AuthExceptionCodeV1
                        ],
                }),
                true
            );
        }
    }, [exceptionCode]);

    // Loading indicator
    if (!isBlockingAuthExceptionV1(exceptionCode as AuthExceptionCodeV1)) {
        return <AppLoadingIndicatorV1 />;
    }

    return (
        <PageV1>
            <PageHeader>
                <PageHeaderTextV1>
                    {t("label.authentication-error")}
                </PageHeaderTextV1>
            </PageHeader>
        </PageV1>
    );
};
