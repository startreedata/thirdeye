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
    PageHeaderTextV1,
    PageHeaderV1,
    PageV1,
    useAuthProviderV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";

export const LogoutPage: FunctionComponent = () => {
    const { logout } = useAuthProviderV1();
    const { t } = useTranslation();

    useEffect(() => {
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
