/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import {
    AppLoadingIndicatorV1,
    AuthExceptionCodeV1,
    AuthExceptionCodeV1Label,
    isBlockingAuthExceptionV1,
    NotificationTypeV1,
    PageHeaderTextV1,
    PageHeaderV1,
    PageV1,
    useAuthProviderV1,
    useNotificationProviderV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

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
            <PageHeaderV1>
                <PageHeaderTextV1>
                    {t("label.authentication-error")}
                </PageHeaderTextV1>
            </PageHeaderV1>
        </PageV1>
    );
};
