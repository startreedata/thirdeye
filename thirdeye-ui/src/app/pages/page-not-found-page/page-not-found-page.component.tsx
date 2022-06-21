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
import { Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import {
    PageContentsGridV1,
    PageHeaderTextV1,
    PageHeaderV1,
    PageNotFoundIndicatorV1,
    PageV1,
} from "../../platform/components";

export const PageNotFoundPage: FunctionComponent = () => {
    const { t } = useTranslation();

    return (
        <PageV1>
            <PageHeaderV1>
                <PageHeaderTextV1>{t("label.page-not-found")}</PageHeaderTextV1>
            </PageHeaderV1>

            <PageContentsGridV1 fullHeight>
                <Grid item xs={12}>
                    <PageNotFoundIndicatorV1
                        headerText={t("label.404")}
                        messageText={t("message.page-not-found")}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
