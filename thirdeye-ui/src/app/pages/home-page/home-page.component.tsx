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
import {
    default as React,
    FunctionComponent,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { ActiveAlertsCount } from "../../components/home-page/active-alerts-count/active-alerts-count.component";
import { AlertAccuracy } from "../../components/home-page/alert-accuracy/alert-accuracy.component";
import { AnomaliesPendingFeedbackCount } from "../../components/home-page/anomalies-pending-feedback-count/anomalies-pending-feedback-count.component";
import { AnomaliesReportedCount } from "../../components/home-page/anomalies-reported-count/anomalies-reported-count.component";
import { RecentAnomalies } from "../../components/home-page/recent-anomalies/recent-anomalies.component";
import { RecommendedDocumentation } from "../../components/home-page/recommended-documentation/recommended-documentation.component";
import { TotalSubscriptionGroupCount } from "../../components/home-page/total-subscription-group-count/total-subscription-group-count.component";
import { TrendingAnomalies } from "../../components/home-page/trending-anomalies/trending-anomalies.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
} from "../../platform/components";
import { useGetAppAnalytics } from "../../rest/app-analytics/app-analytics.action";
import { useUserPreferences } from "../../utils/user-preferences/user-preferences";
import { UserPreferencesKeys } from "../../utils/user-preferences/user-preferences.interfaces";

export const HomePage: FunctionComponent = () => {
    const { t } = useTranslation();
    const { appAnalytics, getAppAnalytics, status } = useGetAppAnalytics();
    const { setPreference, getPreference } = useUserPreferences();
    const [shouldHideDocumentation, setShouldHideDocumentation] = useState(
        getPreference(UserPreferencesKeys.SHOW_DOCUMENTATION_RESOURCES) ?? false
    );

    useEffect(() => {
        getAppAnalytics();
    }, []);

    const handleHideDocumentationClick = (): void => {
        setShouldHideDocumentation(true);
        setPreference(UserPreferencesKeys.SHOW_DOCUMENTATION_RESOURCES, true);
    };

    return (
        <PageV1>
            <PageHeader transparentBackground title={t("label.overview")} />
            <PageContentsGridV1>
                <Grid item sm={8} xs={12}>
                    <Grid container alignItems="stretch">
                        <Grid item sm={12} xs={12}>
                            <PageContentsCardV1 fullHeight>
                                <TrendingAnomalies />
                            </PageContentsCardV1>
                        </Grid>

                        <Grid item sm={6} xs={12}>
                            <PageContentsCardV1 fullHeight>
                                <ActiveAlertsCount />
                            </PageContentsCardV1>
                        </Grid>

                        <Grid item sm={6} xs={12}>
                            <PageContentsCardV1 fullHeight>
                                <AlertAccuracy
                                    appAnalytics={appAnalytics}
                                    getAppAnalyticsStatus={status}
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
                                    appAnalytics={appAnalytics}
                                    getAppAnalyticsStatus={status}
                                />
                            </PageContentsCardV1>
                        </Grid>
                        <Grid item>
                            <PageContentsCardV1 fullHeight>
                                <AnomaliesPendingFeedbackCount
                                    appAnalytics={appAnalytics}
                                    getAppAnalyticsStatus={status}
                                />
                            </PageContentsCardV1>
                        </Grid>
                        <Grid item>
                            <PageContentsCardV1 fullHeight>
                                <TotalSubscriptionGroupCount />
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

                <Grid item xs={12}>
                    <RecentAnomalies />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
