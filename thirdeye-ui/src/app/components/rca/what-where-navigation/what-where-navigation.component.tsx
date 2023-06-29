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
import { Box, Tab, Tabs } from "@material-ui/core";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { Link, useLocation, useSearchParams } from "react-router-dom";
import { AppRouteRelative } from "../../../utils/routes/routes.util";

export const WhatWhereNavigation: FunctionComponent = () => {
    const { t } = useTranslation();
    const location = useLocation();
    const [searchParams] = useSearchParams();

    const stepItems = [
        {
            matcher: (path: string) =>
                path.includes(AppRouteRelative.RCA_TOP_CONTRIBUTORS),
            navLink: `${AppRouteRelative.RCA_TOP_CONTRIBUTORS}`,
            text: t("label.top-contributors"),
        },
        {
            matcher: (path: string) =>
                path.endsWith(AppRouteRelative.RCA_HEATMAP),
            navLink: AppRouteRelative.RCA_HEATMAP,
            text: t("label.heatmap"),
        },
        {
            matcher: (path: string) =>
                path.endsWith(AppRouteRelative.RCA_DIMENSION_ANALYSIS),
            navLink: AppRouteRelative.RCA_DIMENSION_ANALYSIS,
            text: t("label.dimension-analysis-drills"),
        },
    ];

    const currentPage = useMemo(() => {
        return stepItems.find((candidate) => {
            return candidate.matcher(location.pathname);
        });
    }, [location]);

    return (
        <Box pt={2}>
            <Tabs value={currentPage?.navLink}>
                {stepItems.map((tableConfig) => {
                    return (
                        <Tab
                            component={Link}
                            key={tableConfig.navLink}
                            label={tableConfig.text}
                            to={`${
                                tableConfig.navLink
                            }?${searchParams.toString()}`}
                            value={tableConfig.navLink}
                        />
                    );
                })}
            </Tabs>
        </Box>
    );
};
