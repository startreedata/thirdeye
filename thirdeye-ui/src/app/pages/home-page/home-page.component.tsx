import { Grid, useTheme } from "@material-ui/core";
import { default as React, FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { ReactComponent as AlertIcon } from "../../../assets/images/alert.svg";
import { ReactComponent as AnomalyIcon } from "../../../assets/images/anomaly.svg";
import { ReactComponent as ConfigurationIcon } from "../../../assets/images/configuration.svg";
import { ReactComponent as MetricIcon } from "../../../assets/images/metric.svg";
import { ReactComponent as SubscriptionGroupIcon } from "../../../assets/images/subscription-group.svg";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { ButtonTile } from "../../components/button-tile/button-tile.component";
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
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    const handleAlertsClick = (): void => {
        history.push(getAlertsPath());
    };

    const handleAnomaliesClick = (): void => {
        history.push(getAnomaliesPath());
    };

    const handleConfigurationClick = (): void => {
        history.push(getConfigurationPath());
    };

    const handleSubscriptionGroupsClick = (): void => {
        history.push(getSubscriptionGroupsPath());
    };

    const handleDatasetsClick = (): void => {
        history.push(getDatasetsPath());
    };

    const handleDatasourcesClick = (): void => {
        history.push(getDatasourcesPath());
    };

    const handleMetricsClick = (): void => {
        history.push(getMetricsPath());
    };

    return (
        <PageContents centered hideAppBreadcrumbs title={t("label.home")}>
            <Grid
                container
                alignItems="center"
                className={homePageClasses.homePage}
                justify="center"
            >
                <Grid container justify="center" spacing={4}>
                    {/* Alerts */}
                    <Grid item>
                        <ButtonTile
                            icon={AlertIcon}
                            iconColor={theme.palette.primary.main}
                            text={t("label.alerts")}
                            onClick={handleAlertsClick}
                        />
                    </Grid>

                    {/* Anomalies */}
                    <Grid item>
                        <ButtonTile
                            icon={AnomalyIcon}
                            iconColor={theme.palette.primary.main}
                            text={t("label.anomalies")}
                            onClick={handleAnomaliesClick}
                        />
                    </Grid>

                    {/* Configuration */}
                    <Grid item>
                        <ButtonTile
                            icon={ConfigurationIcon}
                            iconColor={theme.palette.primary.main}
                            text={t("label.configuration")}
                            onClick={handleConfigurationClick}
                        />
                    </Grid>

                    {/* Subscription groups */}
                    <Grid item>
                        <ButtonTile
                            icon={SubscriptionGroupIcon}
                            iconColor={theme.palette.primary.main}
                            text={t("label.subscription-groups")}
                            onClick={handleSubscriptionGroupsClick}
                        />
                    </Grid>

                    {/* Datasets */}
                    <Grid item>
                        <ButtonTile
                            icon={SubscriptionGroupIcon}
                            iconColor={theme.palette.primary.main}
                            text={t("label.datasets")}
                            onClick={handleDatasetsClick}
                        />
                    </Grid>

                    {/* Datasources */}
                    <Grid item>
                        <ButtonTile
                            icon={SubscriptionGroupIcon}
                            iconColor={theme.palette.primary.main}
                            text={t("label.datasources")}
                            onClick={handleDatasourcesClick}
                        />
                    </Grid>

                    {/* Metrics */}
                    <Grid item>
                        <ButtonTile
                            icon={MetricIcon}
                            iconColor={theme.palette.primary.main}
                            text={t("label.metrics")}
                            onClick={handleMetricsClick}
                        />
                    </Grid>
                </Grid>
            </Grid>
        </PageContents>
    );
};
