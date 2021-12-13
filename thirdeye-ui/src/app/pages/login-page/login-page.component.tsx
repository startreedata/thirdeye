import {
    AppLoadingIndicatorV1,
    AuthExceptionCodeV1,
    isBlockingAuthExceptionV1,
    PageHeaderTextV1,
    PageHeaderV1,
    PageV1,
    useAuthProviderV1,
} from "@startree-ui/platform-ui";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";

export const LoginPage: FunctionComponent = () => {
    const [exceptionCode, setExceptionCode] = useState("");
    const { authExceptionCode, login } = useAuthProviderV1();
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { t } = useTranslation();
    const { enqueueSnackbar } = useSnackbar();

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
            enqueueSnackbar(
                t("message.authentication-error", {
                    exceptionCode: exceptionCode,
                })
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
