/*
 * Copyright 2023 StarTree Inc
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
import { Icon } from "@iconify/react";
import { Box, Button } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { Link as RouterLink, useLocation } from "react-router-dom";
import { PageHeaderActionsV1 } from "../../platform/components";
import {
    AppRouteRelative,
    getAnomaliesAllPath,
    getAnomaliesCreatePath,
} from "../../utils/routes/routes.util";
import { Crumb } from "../breadcrumbs/breadcrumbs.interfaces";
import { anomaliesBasicHelpCards } from "../help-drawer-v1/help-drawer-card-contents.utils";
import { HelpDrawerV1 } from "../help-drawer-v1/help-drawer-v1.component";
import { PageHeader } from "../page-header/page-header.component";

export const AnomaliesPageHeader: FunctionComponent = () => {
    const { search, pathname } = useLocation();
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

    const selectedSubNavigation: number =
        [
            { path: AppRouteRelative.ANOMALIES_LIST, index: 0 },
            { path: AppRouteRelative.METRICS_REPORT, index: 1 },
        ].find((s) => pathname.includes(s.path))?.index || 0;

    return (
        <>
            <PageHeader
                breadcrumbs={crumbs}
                customActions={
                    <PageHeaderActionsV1>
                        <HelpDrawerV1
                            cards={anomaliesBasicHelpCards}
                            title={`${t("label.need-help")}?`}
                            trigger={(handleOpen) => (
                                <Button
                                    color="primary"
                                    size="small"
                                    variant="outlined"
                                    onClick={handleOpen}
                                >
                                    <Box component="span" mr={1}>
                                        {t("label.need-help")}
                                    </Box>
                                    <Box component="span" display="flex">
                                        <Icon
                                            fontSize={24}
                                            icon="mdi:question-mark-circle-outline"
                                        />
                                    </Box>
                                </Button>
                            )}
                        />

                        <Button
                            color="primary"
                            component={RouterLink}
                            data-testId="create-anomaly-btn"
                            to={getAnomaliesCreatePath()}
                        >
                            {t("label.create")}
                        </Button>
                    </PageHeaderActionsV1>
                }
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
                subNavigationSelected={selectedSubNavigation}
                title={t("label.anomalies")}
            />
        </>
    );
};
