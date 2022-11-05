/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Grid } from "@material-ui/core";
import { default as React, FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { RecentFailures } from "../../components/admin-page/recent-failures/recent-failures.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { PageContentsGridV1, PageV1 } from "../../platform/components";

export const AdminPage: FunctionComponent = () => {
    const { t } = useTranslation();

    return (
        <PageV1>
            <PageHeader
                transparentBackground
                title={t("label.admin-dashboard")}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <RecentFailures />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
