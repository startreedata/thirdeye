import { Grid, useTheme } from "@material-ui/core";
import {
    PageV1,
    TileButtonIconV1,
    TileButtonTextV1,
    TileButtonV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { ReactComponent as MetricIcon } from "../../../assets/images/metric.svg";
import { ReactComponent as SubscriptionGroupIcon } from "../../../assets/images/subscription-group.svg";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import {
    getDatasetsPath,
    getDatasourcesPath,
    getMetricsPath,
    getSubscriptionGroupsPath,
} from "../../utils/routes/routes.util";
import { useConfigurationPageStyles } from "./configuration-page.styles";

export const ConfigurationPage: FunctionComponent = () => {
    const configurationPageClasses = useConfigurationPageStyles();
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const theme = useTheme();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    return (
        <PageV1>
            <PageContents
                centered
                hideAppBreadcrumbs
                hideTimeRange
                title={t("label.configuration")}
            >
                <Grid
                    container
                    alignItems="center"
                    className={configurationPageClasses.configurationPage}
                    justify="center"
                >
                    <Grid container justify="center" spacing={4}>
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

                        {/* datasets */}
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

                        {/* datasources */}
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
                                    <MetricIcon
                                        fill={theme.palette.primary.main}
                                    />
                                </TileButtonIconV1>
                                <TileButtonTextV1>
                                    {t("label.metrics")}
                                </TileButtonTextV1>
                            </TileButtonV1>
                        </Grid>
                    </Grid>
                </Grid>
            </PageContents>
        </PageV1>
    );
};
