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
import { Box, Grid, Tooltip, Typography } from "@material-ui/core";
import InfoIconOutlined from "@material-ui/icons/InfoOutlined";
import { useQuery } from "@tanstack/react-query";
import { AxiosError } from "axios";
import { DateTime } from "luxon";
import {
    default as React,
    FunctionComponent,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useSearchParams } from "react-router-dom";
import { ReactComponent as SetupCompleteLogo } from "../../../assets/images/thirdeye-startree-setup-complete.svg";
import { ActiveAlertsCount } from "../../components/home-page/active-alerts-count/active-alerts-count.component";
import { AlertAccuracy } from "../../components/home-page/alert-accuracy/alert-accuracy.component";
import { AnomaliesPendingFeedbackCount } from "../../components/home-page/anomalies-pending-feedback-count/anomalies-pending-feedback-count.component";
import { AnomaliesReportedCount } from "../../components/home-page/anomalies-reported-count/anomalies-reported-count.component";
import { AnomalyRangeDropdown } from "../../components/home-page/anomaly-range-dropdown/anomaly-range-dropdown.component";
import { EntitySearch } from "../../components/home-page/entity-search/entity-search.component";
import { RecentAnomalies } from "../../components/home-page/recent-anomalies/recent-anomalies.component";
import { RecommendedDocumentation } from "../../components/home-page/recommended-documentation/recommended-documentation.component";
import { TotalSubscriptionGroupCount } from "../../components/home-page/total-subscription-group-count/total-subscription-group-count.component";
import { TrendingAnomalies } from "../../components/home-page/trending-anomalies/trending-anomalies.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    PageHeaderActionsV1,
    PageV1,
    useDialogProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { getAllAlerts } from "../../rest/alerts/alerts.rest";
import { getAnomalyStats } from "../../rest/anomalies/anomalies.rest";
import { getAppAnalytics } from "../../rest/app-analytics/app-analytics.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { AnomalyStats } from "../../rest/dto/anomaly.interfaces";
import { AppAnalytics } from "../../rest/dto/app-analytics.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { getAllSubscriptionGroups } from "../../rest/subscription-groups/subscription-groups.rest";
import { QUERY_PARAM_KEYS } from "../../utils/constants/constants.util";
import {
    AppRoute,
    generateDateRangeDaysFromNow,
    getAlertsAllPath,
} from "../../utils/routes/routes.util";
import { useUserPreferences } from "../../utils/user-preferences/user-preferences";
import { UserPreferencesKeys } from "../../utils/user-preferences/user-preferences.interfaces";
import { useHomePageStyles } from "./home-page.styles";

export const HomePage: FunctionComponent = () => {
    const [anomalyStartTime, setAnomalyStartTime] = useState<number>(
        generateDateRangeDaysFromNow(7)[0]
    );
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { showDialog } = useDialogProviderV1();
    const [searchParams, setSearchParams] = useSearchParams();
    const style = useHomePageStyles();

    const getAppAnalyticsQuery = useQuery<AppAnalytics, AxiosError>({
        queryKey: ["appAnalytics"],
        queryFn: () => {
            return getAppAnalytics();
        },
    });
    const getAlertsQuery = useQuery<Alert[], AxiosError>({
        queryKey: ["alerts"],
        queryFn: () => {
            return getAllAlerts();
        },
    });
    const getSubscriptionGroupsQuery = useQuery<
        SubscriptionGroup[],
        AxiosError
    >({
        queryKey: ["subscriptiongroups"],
        queryFn: () => {
            return getAllSubscriptionGroups();
        },
    });
    const getAnomalyStatsQuery = useQuery<AnomalyStats, AxiosError>({
        queryKey: ["anomalyStats"],
        queryFn: () => {
            return getAnomalyStats({
                startTime: anomalyStartTime,
                endTime: DateTime.local().endOf("hour").toMillis(),
            });
        },
    });
    const { setPreference, getPreference } = useUserPreferences();

    const [shouldHideDocumentation, setShouldHideDocumentation] = useState(
        getPreference(UserPreferencesKeys.SHOW_DOCUMENTATION_RESOURCES) ?? false
    );

    useEffect(() => {
        if (getAlertsQuery.data && getAlertsQuery.data.length === 0) {
            navigate(AppRoute.WELCOME);
        }
    }, [getAlertsQuery.data]);

    useEffect(() => {
        getAnomalyStatsQuery.refetch();
    }, [anomalyStartTime]);

    const handleHideDocumentationClick = (): void => {
        setShouldHideDocumentation(true);
        setPreference(UserPreferencesKeys.SHOW_DOCUMENTATION_RESOURCES, true);
    };

    useEffect(() => {
        if (
            searchParams.get(QUERY_PARAM_KEYS.SHOW_FIRST_ALERT_SUCCESS) ===
            "true"
        ) {
            showDialog({
                type: DialogType.ALERT,
                contents: (
                    <Box textAlign="center">
                        <Typography color="primary" variant="h5">
                            {t("label.congratulations-!")}
                        </Typography>
                        <Box py={1}>
                            <SetupCompleteLogo />
                        </Box>
                        <Typography color="primary" variant="h6">
                            {t("message.the-setup-is-completed")}
                        </Typography>
                        <Typography color="textSecondary" variant="subtitle1">
                            {t("message.you-can-now-start-using-thirdeye")}
                        </Typography>
                    </Box>
                ),
                headerText: t("message.setup-finished-exclamation"),
                okButtonText: t("label.close"),
                cancelButtonText: t("label.view-all-entities", {
                    entity: t("label.alerts"),
                }),
                onCancel: () => {
                    navigate(getAlertsAllPath());
                },
                onOk: () => {
                    searchParams.delete(
                        QUERY_PARAM_KEYS.SHOW_FIRST_ALERT_SUCCESS
                    );
                    setSearchParams(searchParams, { replace: true });
                },
            });
        }
    }, []);

    return (
        <PageV1>
            <PageHeader
                transparentBackground
                actionsGridContainProps={{ xs: 12, sm: 12, md: 12, lg: 3 }}
                customActions={
                    <PageHeaderActionsV1>
                        <Box width="100%">
                            <EntitySearch
                                alerts={getAlertsQuery.data || []}
                                subscriptionGroups={
                                    getSubscriptionGroupsQuery.data || []
                                }
                            />
                        </Box>
                    </PageHeaderActionsV1>
                }
                headerGridContainerProps={{
                    xs: 12,
                    sm: 12,
                    md: true,
                    lg: true,
                }}
                subtitle={
                    <Grid container alignItems="center">
                        <Grid item>
                            {t("message.effortless-alert-configuration")}
                        </Grid>
                        <Grid item>
                            <Tooltip
                                arrow
                                interactive
                                placement="top"
                                title={
                                    <Typography variant="caption">
                                        {t(
                                            "message.experience-streamlined-monitoring"
                                        )}
                                    </Typography>
                                }
                            >
                                <InfoIconOutlined
                                    color="secondary"
                                    fontSize="small"
                                />
                            </Tooltip>
                        </Grid>
                    </Grid>
                }
                title={t(
                    "message.automated-metrics-monitoring-and-anomaly-detection"
                )}
            />
            <PageContentsGridV1>
                <Grid item sm={12} xs={12}>
                    <Grid container alignItems="stretch">
                        <Grid item sm={4} xs={12}>
                            <PageContentsCardV1 fullHeight>
                                <ActiveAlertsCount
                                    alertsQuery={getAlertsQuery}
                                />
                            </PageContentsCardV1>
                        </Grid>
                        <Grid item sm={4} xs={12}>
                            <PageContentsCardV1 fullHeight>
                                <AlertAccuracy
                                    appAnalyticsQuery={getAppAnalyticsQuery}
                                    classes={{
                                        noDataIndicator: style.noDataIndicator,
                                    }}
                                />
                            </PageContentsCardV1>
                        </Grid>
                        <Grid item sm={4} xs={12}>
                            <PageContentsCardV1 fullHeight>
                                <TotalSubscriptionGroupCount
                                    classes={{
                                        noDataIndicator: style.noDataIndicator,
                                    }}
                                    subscriptionGroupsQuery={
                                        getSubscriptionGroupsQuery
                                    }
                                />
                            </PageContentsCardV1>
                        </Grid>
                    </Grid>
                </Grid>

                {!shouldHideDocumentation && (
                    <Grid item xs={12}>
                        <PageContentsCardV1>
                            <RecommendedDocumentation
                                onHideDocumentationClick={
                                    handleHideDocumentationClick
                                }
                            />
                        </PageContentsCardV1>
                    </Grid>
                )}

                <Grid item sm={12} xs={12}>
                    <Grid container justifyContent="space-between">
                        <Grid item sm={8} xs={12}>
                            <Typography variant="h5">
                                {t("label.recent-entity", {
                                    entity: t("label.anomalies"),
                                })}
                            </Typography>
                        </Grid>

                        <Grid item sm={4} xs={12}>
                            <AnomalyRangeDropdown
                                anomalyStartTime={anomalyStartTime}
                                setAnomalyStartTime={setAnomalyStartTime}
                            />
                        </Grid>
                    </Grid>
                </Grid>

                <Grid item sm={8} xs={12}>
                    <Grid container alignItems="stretch">
                        <Grid item sm={12} xs={12}>
                            <PageContentsCardV1 fullHeight>
                                <TrendingAnomalies
                                    startTime={anomalyStartTime}
                                />
                            </PageContentsCardV1>
                        </Grid>
                    </Grid>
                </Grid>

                <Grid item sm={4} xs={12}>
                    <Grid container direction="column">
                        <Grid item>
                            <PageContentsCardV1 fullHeight>
                                <AnomaliesReportedCount
                                    anomalyStatsQuery={getAnomalyStatsQuery}
                                    classes={{
                                        noDataIndicator: style.noDataIndicator,
                                    }}
                                />
                            </PageContentsCardV1>
                        </Grid>
                        <Grid item>
                            <PageContentsCardV1 fullHeight>
                                <AnomaliesPendingFeedbackCount
                                    anomalyStatsQuery={getAnomalyStatsQuery}
                                    classes={{
                                        noDataIndicator: style.noDataIndicator,
                                    }}
                                />
                            </PageContentsCardV1>
                        </Grid>
                    </Grid>
                </Grid>

                <Grid item xs={12}>
                    <RecentAnomalies />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
