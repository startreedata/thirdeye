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
import {
    Box,
    Grid,
    Typography,
    useMediaQuery,
    useTheme,
} from "@material-ui/core";
import {
    default as React,
    FunctionComponent,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import { ReactComponent as SetupCompleteLogo } from "../../../assets/images/thirdeye-startree-setup-complete.svg";
import { ActiveAlertsCount } from "../../components/home-page/active-alerts-count/active-alerts-count.component";
import { AlertAccuracy } from "../../components/home-page/alert-accuracy/alert-accuracy.component";
import { AnomaliesPendingFeedbackCount } from "../../components/home-page/anomalies-pending-feedback-count/anomalies-pending-feedback-count.component";
import { AnomaliesReportedCount } from "../../components/home-page/anomalies-reported-count/anomalies-reported-count.component";
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
import { useGetAlerts } from "../../rest/alerts/alerts.actions";
import { useGetAnomalies } from "../../rest/anomalies/anomaly.actions";
import { useGetAppAnalytics } from "../../rest/app-analytics/app-analytics.action";
import { useGetSubscriptionGroups } from "../../rest/subscription-groups/subscription-groups.actions";
import { QUERY_PARAM_KEYS } from "../../utils/constants/constants.util";
import { useUserPreferences } from "../../utils/user-preferences/user-preferences";
import { UserPreferencesKeys } from "../../utils/user-preferences/user-preferences.interfaces";
import { useHomePageStyles } from "./home-page.styles";

export const HomePage: FunctionComponent = () => {
    const { t } = useTranslation();
    const { showDialog } = useDialogProviderV1();
    const [searchParams, setSearchParams] = useSearchParams();
    const theme = useTheme();
    const style = useHomePageStyles();
    const screenWidthSmUp = useMediaQuery(theme.breakpoints.up("md"));
    const { appAnalytics, getAppAnalytics, status } = useGetAppAnalytics();
    const { anomalies, getAnomalies } = useGetAnomalies();
    const {
        subscriptionGroups,
        getSubscriptionGroups,
        status: getSubscriptionGroupsStatus,
    } = useGetSubscriptionGroups();
    const { alerts, getAlerts, status: getAlertsStatus } = useGetAlerts();
    const { setPreference, getPreference } = useUserPreferences();

    const [shouldHideDocumentation, setShouldHideDocumentation] = useState(
        getPreference(UserPreferencesKeys.SHOW_DOCUMENTATION_RESOURCES) ?? false
    );

    useEffect(() => {
        getAppAnalytics();
        getSubscriptionGroups();
        getAlerts();
        getAnomalies();
    }, []);

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
                            {t("label.congratulation-!")}
                        </Typography>
                        <Box py={1}>
                            <SetupCompleteLogo />
                        </Box>
                        <Typography color="primary" variant="h6">
                            {t("message.the-setup-is-completed")}
                        </Typography>
                        <Typography color="textSecondary" variant="subtitle1">
                            {t(
                                "message.now-you-can-start-using-thirdeye-as-a-professional"
                            )}
                        </Typography>
                    </Box>
                ),
                headerText: t("message.setup-finished-exclamation"),
                okButtonText: t("label.close"),
                hideCancelButton: true,
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
                customActions={
                    <PageHeaderActionsV1>
                        <Box width={screenWidthSmUp ? 500 : "100%"}>
                            <EntitySearch
                                alerts={alerts}
                                anomalies={anomalies}
                                subscriptionGroups={subscriptionGroups}
                            />
                        </Box>
                    </PageHeaderActionsV1>
                }
                subtitle={t(
                    "message.alerts-monitor-your-kpis-and-anomalies-help-you-to-find-outliers-in-the-kpis"
                )}
                title={t("message.kpis-monitor-and-outliers")}
            />
            <PageContentsGridV1>
                <Grid item sm={12} xs={12}>
                    <Grid container alignItems="stretch">
                        <Grid item sm={4} xs={12}>
                            <PageContentsCardV1 fullHeight>
                                <ActiveAlertsCount
                                    alerts={alerts}
                                    getAlertsStatus={getAlertsStatus}
                                />
                            </PageContentsCardV1>
                        </Grid>
                        <Grid item sm={4} xs={12}>
                            <PageContentsCardV1 fullHeight>
                                <AlertAccuracy
                                    appAnalytics={appAnalytics}
                                    classes={{
                                        noDataIndicator: style.noDataIndicator,
                                    }}
                                    getAppAnalyticsStatus={status}
                                />
                            </PageContentsCardV1>
                        </Grid>
                        <Grid item sm={4} xs={12}>
                            <PageContentsCardV1 fullHeight>
                                <TotalSubscriptionGroupCount
                                    classes={{
                                        noDataIndicator: style.noDataIndicator,
                                    }}
                                    getSubscriptionGroupsStatus={
                                        getSubscriptionGroupsStatus
                                    }
                                    subscriptionGroups={subscriptionGroups}
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
                    <Typography variant="h5">
                        {t("label.recent-entity", {
                            entity: t("label.anomalies"),
                        })}
                    </Typography>

                    {/* TODO: Anomalies range dropdown */}
                </Grid>

                <Grid item sm={8} xs={12}>
                    <Grid container alignItems="stretch">
                        <Grid item sm={12} xs={12}>
                            <PageContentsCardV1 fullHeight>
                                <TrendingAnomalies />
                            </PageContentsCardV1>
                        </Grid>
                    </Grid>
                </Grid>

                <Grid item sm={4} xs={12}>
                    <Grid container direction="column">
                        <Grid item>
                            <PageContentsCardV1 fullHeight>
                                <AnomaliesReportedCount
                                    appAnalytics={appAnalytics}
                                    classes={{
                                        noDataIndicator: style.noDataIndicator,
                                    }}
                                    getAppAnalyticsStatus={status}
                                />
                            </PageContentsCardV1>
                        </Grid>
                        <Grid item>
                            <PageContentsCardV1 fullHeight>
                                <AnomaliesPendingFeedbackCount
                                    appAnalytics={appAnalytics}
                                    classes={{
                                        noDataIndicator: style.noDataIndicator,
                                    }}
                                    getAppAnalyticsStatus={status}
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
