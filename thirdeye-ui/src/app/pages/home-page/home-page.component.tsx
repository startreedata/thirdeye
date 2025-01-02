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
import { Box, Grid, ThemeProvider, Typography } from "@material-ui/core";
import DashboardIcon from "@material-ui/icons/Dashboard";
import PersonAddIcon from "@material-ui/icons/PersonAdd";
import PlaylistAddCheckIcon from "@material-ui/icons/PlaylistAddCheck";
import ReportIcon from "@material-ui/icons/Report";
import { AxiosError } from "axios";
import { default as React, FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useSearchParams } from "react-router-dom";
import { ReactComponent as SetupCompleteLogo } from "../../../assets/images/thirdeye-startree-setup-complete.svg";
import { ActiveAlertsCountV2 } from "../../components/home-page/active-alerts-count-v2/active-alerts-count-v2.component";
import { LatestActiveAlerts } from "../../components/home-page/latest-active-alerts/latest-active-alerts.component";
import { LatestSubscriptionGroups } from "../../components/home-page/latest-subscription-groups/latest-subscription-groups.component";
import { RecentAnomaliesV2 } from "../../components/home-page/recent-anomalies-v2/recent-anomalies-v2.component";
import { RecommendedDocumentationV2 } from "../../components/home-page/recommended-documentation-v2/recommended-documentation-v2.component";
import { TotalSubscriptionGroupCountV2 } from "../../components/home-page/total-subscription-group-count-v2/total-subscription-group-count-v2.component";
import IconLink from "../../components/icon-link/icon-link.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import TitleCard from "../../components/title-card/title-card.component";
import {
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { getAllAlerts } from "../../rest/alerts/alerts.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { getAllSubscriptionGroups } from "../../rest/subscription-groups/subscription-groups.rest";
import { QUERY_PARAM_KEYS } from "../../utils/constants/constants.util";
import {
    AppRoute,
    getAdminPath,
    getAlertsAllPath,
} from "../../utils/routes/routes.util";
import { homePageTheme, useHomePageStyles } from "./home-page.styles";
import { useFetchQuery } from "../../rest/hooks/useFetchQuery";

export const HomePage: FunctionComponent = () => {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { showDialog } = useDialogProviderV1();
    const [searchParams, setSearchParams] = useSearchParams();
    const style = useHomePageStyles();

    const getAlertsQuery = useFetchQuery<Alert[], AxiosError>({
        queryKey: ["alerts"],
        queryFn: () => {
            return getAllAlerts();
        },
    });
    const getSubscriptionGroupsQuery = useFetchQuery<
        SubscriptionGroup[],
        AxiosError
    >({
        queryKey: ["subscriptiongroups"],
        queryFn: () => {
            return getAllSubscriptionGroups();
        },
    });

    useEffect(() => {
        if (
            getAlertsQuery.isFetching === false &&
            getAlertsQuery.isSuccess &&
            getAlertsQuery.data &&
            getAlertsQuery.data.length === 0
        ) {
            navigate(AppRoute.WELCOME);
        }
    }, [
        getAlertsQuery.data,
        getAlertsQuery.isSuccess,
        getAlertsQuery.isFetching,
    ]);

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
        <ThemeProvider theme={homePageTheme}>
            <PageV1 className={style.page}>
                <PageHeader
                    transparentBackground
                    actionsGridContainProps={{ xs: 12, sm: 12, md: 12, lg: 3 }}
                    headerGridContainerProps={{
                        xs: 12,
                        sm: 12,
                        md: true,
                        lg: true,
                    }}
                    subtitle={
                        <Grid container alignItems="center">
                            <Grid item>
                                {t(
                                    "message.automated-metrics-monitoring-and-anomaly-detection"
                                )}
                            </Grid>
                        </Grid>
                    }
                    title={t("message.startree-thirdeye")}
                />
                <PageContentsGridV1>
                    <Grid item sm={4} xs={4}>
                        <TitleCard
                            content={
                                <ActiveAlertsCountV2
                                    alertsQuery={getAlertsQuery}
                                />
                            }
                            datatestId="alert-summary"
                            title={
                                <Grid
                                    container
                                    alignItems="center"
                                    direction="row"
                                    xs={12}
                                >
                                    <Grid
                                        item
                                        alignItems="center"
                                        classes={{
                                            root: style.iconGridContainer,
                                        }}
                                        direction="row"
                                    >
                                        <ReportIcon
                                            classes={{ root: style.icon }}
                                        />
                                    </Grid>
                                    <Grid item>
                                        <Typography variant="h6">
                                            {t(
                                                "label.monitor-and-detect-anomalies"
                                            )}
                                        </Typography>
                                    </Grid>
                                </Grid>
                            }
                        />
                    </Grid>
                    <Grid item sm={4} xs={4}>
                        <TitleCard
                            content={
                                <TotalSubscriptionGroupCountV2
                                    subscriptionGroupsQuery={
                                        getSubscriptionGroupsQuery
                                    }
                                />
                            }
                            datatestId="subscription-summary"
                            title={
                                <Grid
                                    container
                                    alignItems="center"
                                    direction="row"
                                    xs={12}
                                >
                                    <Grid
                                        item
                                        alignItems="center"
                                        classes={{
                                            root: style.iconGridContainer,
                                        }}
                                        direction="row"
                                    >
                                        <PlaylistAddCheckIcon
                                            classes={{ root: style.icon }}
                                        />
                                    </Grid>
                                    <Grid item>
                                        <Typography variant="h6">
                                            {t("label.subscribe-to-alerts")}
                                        </Typography>
                                    </Grid>
                                </Grid>
                            }
                        />
                    </Grid>
                    <Grid item sm={4} xs={4}>
                        <TitleCard
                            content={
                                <IconLink
                                    icon={<PersonAddIcon />}
                                    label={t("label.view-detection-failures")}
                                    route={getAdminPath()}
                                />
                            }
                            datatestId="admin-summary"
                            title={
                                <Grid
                                    container
                                    alignItems="center"
                                    direction="row"
                                    xs={12}
                                >
                                    <Grid
                                        item
                                        alignItems="center"
                                        classes={{
                                            root: style.iconGridContainer,
                                        }}
                                        direction="row"
                                    >
                                        <DashboardIcon
                                            classes={{ root: style.icon }}
                                        />
                                    </Grid>
                                    <Grid item>
                                        <Typography variant="h6">
                                            {t("label.dashboards")}
                                        </Typography>
                                    </Grid>
                                </Grid>
                            }
                        />
                    </Grid>

                    <Grid
                        item
                        className={style.gridSpace}
                        data-testId="active-alerts"
                        sm={6}
                    >
                        <LatestActiveAlerts alertsQuery={getAlertsQuery} />
                    </Grid>
                    <Grid
                        item
                        className={style.gridSpace}
                        data-testId="subscription-groups"
                        sm={6}
                    >
                        <LatestSubscriptionGroups
                            subscriptionGroupsQuery={getSubscriptionGroupsQuery}
                        />
                    </Grid>

                    <Grid
                        item
                        className={style.gridSpace}
                        data-testId="recent-anomalies"
                        xs={12}
                    >
                        <RecentAnomaliesV2 />
                    </Grid>

                    <Grid item xs={12}>
                        <RecommendedDocumentationV2 />
                    </Grid>
                </PageContentsGridV1>
            </PageV1>
        </ThemeProvider>
    );
};
