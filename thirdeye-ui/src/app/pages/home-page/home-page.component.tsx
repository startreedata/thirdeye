import { Grid, useTheme } from "@material-ui/core";
import {
    PageContentsGridV1,
    PageHeaderActionsV1,
    PageHeaderTextV1,
    PageHeaderV1,
    PageV1,
    TileButtonIconV1,
    TileButtonTextV1,
    TileButtonV1,
} from "@startree-ui/platform-ui";
import { default as React, FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { ReactComponent as AlertIcon } from "../../../assets/images/alert.svg";
import { ReactComponent as AnomalyIcon } from "../../../assets/images/anomaly.svg";
import { ReactComponent as ConfigurationIcon } from "../../../assets/images/configuration.svg";
import { ReactComponent as MetricIcon } from "../../../assets/images/metric.svg";
import { ReactComponent as SubscriptionGroupIcon } from "../../../assets/images/subscription-group.svg";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { CreateMenuButton } from "../../components/create-menu-button.component/create-menu-button.component";
import {
    getAlertsPath,
    getAnomaliesPath,
    getConfigurationPath,
    getDatasetsPath,
    getDatasourcesPath,
    getMetricsPath,
    getSubscriptionGroupsPath,
} from "../../utils/routes/routes.util";

export const HomePage: FunctionComponent = () => {
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const theme = useTheme();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    return (
        <PageV1>
            <PageHeaderV1>
                <PageHeaderTextV1>{t("label.home")}</PageHeaderTextV1>

                <PageHeaderActionsV1>
                    {/* Create options button */}
                    <CreateMenuButton />
                </PageHeaderActionsV1>
            </PageHeaderV1>

            <PageContentsGridV1>
                <Grid container>
                    {/* Alerts */}
                    <Grid item>
                        <TileButtonV1 href={getAlertsPath()}>
                            <TileButtonIconV1>
                                <AlertIcon fill={theme.palette.primary.main} />
                            </TileButtonIconV1>
                            <TileButtonTextV1>
                                {t("label.alerts")}
                            </TileButtonTextV1>
                        </TileButtonV1>
                    </Grid>

                    {/* Anomalies */}
                    <Grid item>
                        <TileButtonV1 href={getAnomaliesPath()}>
                            <TileButtonIconV1>
                                <AnomalyIcon
                                    fill={theme.palette.primary.main}
                                />
                            </TileButtonIconV1>
                            <TileButtonTextV1>
                                {t("label.anomalies")}
                            </TileButtonTextV1>
                        </TileButtonV1>
                    </Grid>

                    {/* Configuration */}
                    <Grid item>
                        <TileButtonV1 href={getConfigurationPath()}>
                            <TileButtonIconV1>
                                <ConfigurationIcon
                                    fill={theme.palette.primary.main}
                                />
                            </TileButtonIconV1>
                            <TileButtonTextV1>
                                {t("label.configuration")}
                            </TileButtonTextV1>
                        </TileButtonV1>
                    </Grid>

                    {/* Subscription groups */}
                    <Grid item>
                        <TileButtonV1 href={getSubscriptionGroupsPath()}>
                            <TileButtonIconV1>
                                <SubscriptionGroupIcon
                                    fill={theme.palette.primary.main}
                                />
                            </TileButtonIconV1>
                            <TileButtonTextV1>
                                {t("label.subscription-groups")}
                            </TileButtonTextV1>
                        </TileButtonV1>
                    </Grid>

                    {/* Datasets */}
                    <Grid item>
                        <TileButtonV1 href={getDatasetsPath()}>
                            <TileButtonIconV1>
                                <SubscriptionGroupIcon
                                    fill={theme.palette.primary.main}
                                />
                            </TileButtonIconV1>
                            <TileButtonTextV1>
                                {t("label.datasets")}
                            </TileButtonTextV1>
                        </TileButtonV1>
                    </Grid>

                    {/* Datasources */}
                    <Grid item>
                        <TileButtonV1 href={getDatasourcesPath()}>
                            <TileButtonIconV1>
                                <SubscriptionGroupIcon
                                    fill={theme.palette.primary.main}
                                />
                            </TileButtonIconV1>
                            <TileButtonTextV1>
                                {t("label.datasources")}
                            </TileButtonTextV1>
                        </TileButtonV1>
                    </Grid>

                    {/* Metrics */}
                    <Grid item>
                        <TileButtonV1 href={getMetricsPath()}>
                            <TileButtonIconV1>
                                <MetricIcon fill={theme.palette.primary.main} />
                            </TileButtonIconV1>
                            <TileButtonTextV1>
                                {t("label.metrics")}
                            </TileButtonTextV1>
                        </TileButtonV1>
                    </Grid>
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
