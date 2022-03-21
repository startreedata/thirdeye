import { Grid, useTheme } from "@material-ui/core";
import {
    ButtonTileIconV1,
    ButtonTileTextV1,
    ButtonTileV1,
    DimensionV1,
    PageV1,
} from "@startree-ui/platform-ui";
import { default as React, FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { ReactComponent as AlertIcon } from "../../../assets/images/alert.svg";
import { ReactComponent as AnomalyIcon } from "../../../assets/images/anomaly.svg";
import { ReactComponent as ConfigurationIcon } from "../../../assets/images/configuration.svg";
import { ReactComponent as MetricIcon } from "../../../assets/images/metric.svg";
import { ReactComponent as SubscriptionGroupIcon } from "../../../assets/images/subscription-group.svg";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import {
    getAlertsPath,
    getAnomaliesPath,
    getConfigurationPath,
    getDatasetsPath,
    getDatasourcesPath,
    getMetricsPath,
    getSubscriptionGroupsPath,
} from "../../utils/routes/routes.util";
import { useHomePageStyles } from "./home-page.styles";

export const HomePage: FunctionComponent = () => {
    const homePageClasses = useHomePageStyles();
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const theme = useTheme();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    return (
        <PageV1>
            <PageContents centered hideAppBreadcrumbs title={t("label.home")}>
                <Grid
                    container
                    alignItems="center"
                    className={homePageClasses.homePage}
                    justify="center"
                >
                    <Grid
                        container
                        justify="center"
                        spacing={DimensionV1.PageGridSpacing}
                    >
                        {/* Alerts */}
                        <Grid item>
                            <ButtonTileV1 href={getAlertsPath()}>
                                <ButtonTileIconV1>
                                    <AlertIcon
                                        fill={theme.palette.primary.main}
                                    />
                                </ButtonTileIconV1>
                                <ButtonTileTextV1>
                                    {t("label.alerts")}
                                </ButtonTileTextV1>
                            </ButtonTileV1>
                        </Grid>

                        {/* Anomalies */}
                        <Grid item>
                            <ButtonTileV1 href={getAnomaliesPath()}>
                                <ButtonTileIconV1>
                                    <AnomalyIcon
                                        fill={theme.palette.primary.main}
                                    />
                                </ButtonTileIconV1>
                                <ButtonTileTextV1>
                                    {t("label.anomalies")}
                                </ButtonTileTextV1>
                            </ButtonTileV1>
                        </Grid>

                        {/* Configuration */}
                        <Grid item>
                            <ButtonTileV1 href={getConfigurationPath()}>
                                <ButtonTileIconV1>
                                    <ConfigurationIcon
                                        fill={theme.palette.primary.main}
                                    />
                                </ButtonTileIconV1>
                                <ButtonTileTextV1>
                                    {t("label.configuration")}
                                </ButtonTileTextV1>
                            </ButtonTileV1>
                        </Grid>

                        {/* Subscription groups */}
                        <Grid item>
                            <ButtonTileV1 href={getSubscriptionGroupsPath()}>
                                <ButtonTileIconV1>
                                    <SubscriptionGroupIcon
                                        fill={theme.palette.primary.main}
                                    />
                                </ButtonTileIconV1>
                                <ButtonTileTextV1>
                                    {t("label.subscription-groups")}
                                </ButtonTileTextV1>
                            </ButtonTileV1>
                        </Grid>

                        {/* Datasets */}
                        <Grid item>
                            <ButtonTileV1 href={getDatasetsPath()}>
                                <ButtonTileIconV1>
                                    <SubscriptionGroupIcon
                                        fill={theme.palette.primary.main}
                                    />
                                </ButtonTileIconV1>
                                <ButtonTileTextV1>
                                    {t("label.datasets")}
                                </ButtonTileTextV1>
                            </ButtonTileV1>
                        </Grid>

                        {/* Datasources */}
                        <Grid item>
                            <ButtonTileV1 href={getDatasourcesPath()}>
                                <ButtonTileIconV1>
                                    <SubscriptionGroupIcon
                                        fill={theme.palette.primary.main}
                                    />
                                </ButtonTileIconV1>
                                <ButtonTileTextV1>
                                    {t("label.datasources")}
                                </ButtonTileTextV1>
                            </ButtonTileV1>
                        </Grid>

                        {/* Metrics */}
                        <Grid item>
                            <ButtonTileV1 href={getMetricsPath()}>
                                <ButtonTileIconV1>
                                    <MetricIcon
                                        fill={theme.palette.primary.main}
                                    />
                                </ButtonTileIconV1>
                                <ButtonTileTextV1>
                                    {t("label.metrics")}
                                </ButtonTileTextV1>
                            </ButtonTileV1>
                        </Grid>
                    </Grid>
                </Grid>
            </PageContents>
        </PageV1>
    );
};
