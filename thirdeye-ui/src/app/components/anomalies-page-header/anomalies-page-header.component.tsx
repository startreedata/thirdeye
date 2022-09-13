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
import { Box } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";
import {
    PageHeaderActionsV1,
    PageHeaderTabsV1,
    PageHeaderTabV1,
    PageHeaderTextV1,
    PageHeaderV1,
} from "../../platform/components";
import {
    AppRouteRelative,
    getAnomaliesAllPath,
} from "../../utils/routes/routes.util";
import { Breadcrumbs } from "../breadcrumbs/breadcrumbs.component";
import { Crumb } from "../breadcrumbs/breadcrumbs.interfaces";
import { CreateMenuButton } from "../create-menu-button.component/create-menu-button.component";

export const AnomaliesPageHeader: FunctionComponent = () => {
    const { search } = useLocation();
    const { pathname } = useLocation();
    const { t } = useTranslation();
    const crumbs: Crumb[] = [
        {
            label: t("label.home"),
            link: "/",
        },
        {
            label: t("label.anomalies"),
            link: getAnomaliesAllPath(),
        },
    ];

    if (pathname.indexOf(AppRouteRelative.ANOMALIES_METRICS_REPORT) > -1) {
        crumbs.push({
            label: t("label.metrics-report"),
        });
    } else if (pathname.indexOf(AppRouteRelative.ANOMALIES_LIST) > -1) {
        crumbs.push({
            label: t("label.anomalies-list"),
        });
    }

    return (
        <PageHeaderV1>
            <Box paddingBottom={2}>
                <Breadcrumbs crumbs={crumbs} />

                <PageHeaderTextV1>{t("label.anomalies")}</PageHeaderTextV1>
            </Box>

            <PageHeaderActionsV1>
                {/* Create options button */}
                <CreateMenuButton />
            </PageHeaderActionsV1>
            <PageHeaderTabsV1
                selectedIndex={
                    pathname.indexOf(AppRouteRelative.ANOMALIES_LIST) > -1
                        ? 0
                        : 1
                }
            >
                <PageHeaderTabV1
                    href={`${AppRouteRelative.ANOMALIES_LIST}${search}`}
                >
                    {t("label.anomalies-list")}
                </PageHeaderTabV1>
                <PageHeaderTabV1
                    href={`${AppRouteRelative.ANOMALIES_METRICS_REPORT}${search}`}
                >
                    {t("label.metrics-report")}
                </PageHeaderTabV1>
            </PageHeaderTabsV1>
        </PageHeaderV1>
    );
};
