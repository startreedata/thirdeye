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
import { Box, Tab, Tabs, Typography } from "@material-ui/core";
import { ArrowForward } from "@material-ui/icons";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import {
    useLocation,
    useNavigate,
    useParams,
    useSearchParams,
} from "react-router-dom";
import {
    AppRouteRelative,
    getRootCauseAnalysisForAnomalyInvestigateV2StepsPath,
    RCAV2Steps,
} from "../../../utils/routes/routes.util";
import { InvestigationStepsNavigationStyles } from "./investigation-steps-navigation.styles";

export const InvestigationStepsNavigation: FunctionComponent = () => {
    const { t } = useTranslation();
    const location = useLocation();
    const classes = InvestigationStepsNavigationStyles();
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    const params = useParams<Record<"id", string>>();

    const anomalyId = Number(params.id);

    const handleTabChange = (newValue: RCAV2Steps): void => {
        navigate(
            `${getRootCauseAnalysisForAnomalyInvestigateV2StepsPath(
                anomalyId,
                newValue
            )}?${searchParams.toString()}`
        );
    };

    const stepItems = [
        {
            matcher: (path: string) =>
                path.includes(AppRouteRelative.RCA_WHAT_WHERE),
            navLink: AppRouteRelative.RCA_WHAT_WHERE as RCAV2Steps,
            text: t("label.top-contributors"),
        },
        {
            matcher: (path: string) =>
                path.endsWith(AppRouteRelative.RCA_HEATMAP),
            navLink: AppRouteRelative.RCA_HEATMAP as RCAV2Steps,
            text: t("label.heatmap-and-dimension"),
        },
        {
            matcher: (path: string) =>
                path.endsWith(AppRouteRelative.METRICS_DRILL_DOWN),
            navLink: AppRouteRelative.METRICS_DRILL_DOWN as RCAV2Steps,
            text: t("label.metrics-drill-down"),
        },
        {
            matcher: (path: string) =>
                path.endsWith(AppRouteRelative.RCA_EVENTS),
            navLink: AppRouteRelative.RCA_EVENTS as RCAV2Steps,
            text: t("label.events"),
        },
    ];

    const currentPageIdx = useMemo(() => {
        return stepItems.findIndex((candidate) => {
            return candidate.matcher(location.pathname);
        });
    }, [location]);

    const selectedTab = (text: string): React.ReactNode => (
        <Box className={classes.selectedTabContainer}>
            <Typography variant="subtitle1">{text}</Typography>
            <ArrowForward />
        </Box>
    );

    return (
        <Tabs
            className={classes.tabsMenu}
            orientation="vertical"
            value={currentPageIdx}
        >
            {stepItems.map((stepConfig, index) => {
                return (
                    <Tab
                        className={
                            currentPageIdx === index
                                ? classes.selectedTab
                                : classes.tab
                        }
                        key={stepConfig.navLink}
                        label={
                            currentPageIdx === index
                                ? selectedTab(stepConfig.text)
                                : stepConfig.text
                        }
                        value={stepConfig.navLink}
                        onClick={() => handleTabChange(stepConfig.navLink)}
                    />
                );
            })}
        </Tabs>
    );
};
