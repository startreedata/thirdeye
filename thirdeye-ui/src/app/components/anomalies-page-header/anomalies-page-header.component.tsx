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
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";
import {
    AppRouteRelative,
    getAnomaliesAllPath,
} from "../../utils/routes/routes.util";
import { Crumb } from "../breadcrumbs/breadcrumbs.interfaces";
import { PageHeader } from "../page-header/page-header.component";

export const AnomaliesPageHeader: FunctionComponent = () => {
    const { search } = useLocation();
    const { pathname } = useLocation();
    const { t } = useTranslation();
    const crumbs: Crumb[] = [
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
        <PageHeader
            showCreateButton
            breadcrumbs={crumbs}
            subNavigation={[
                {
                    link: `${AppRouteRelative.ANOMALIES_LIST}${search}`,
                    label: t("label.anomalies-list"),
                },
                {
                    link: `${AppRouteRelative.ANOMALIES_METRICS_REPORT}${search}`,
                    label: t("label.metrics-report"),
                },
            ]}
            subNavigationSelected={
                pathname.indexOf(AppRouteRelative.ANOMALIES_LIST) > -1 ? 0 : 1
            }
            title={t("label.anomalies")}
        />
    );
};
