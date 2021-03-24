import { Grid, useTheme } from "@material-ui/core";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { ReactComponent as MetricIcon } from "../../../assets/images/metric.svg";
import { ReactComponent as SubscriptionGroupIcon } from "../../../assets/images/subscription-group.svg";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { ButtonTile } from "../../components/button-tile/button-tile.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import {
    getMetricsPath,
    getSubscriptionGroupsPath,
} from "../../utils/routes/routes.util";
import { useConfigurationPageStyles } from "./configuration-page.styles";

export const ConfigurationPage: FunctionComponent = () => {
    const configurationPageClasses = useConfigurationPageStyles();
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const theme = useTheme();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    const handleSubscriptionGroupsClick = (): void => {
        history.push(getSubscriptionGroupsPath());
    };

    const handleMetricsClick = (): void => {
        history.push(getMetricsPath());
    };

    return (
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
                        <ButtonTile
                            icon={SubscriptionGroupIcon}
                            iconColor={theme.palette.primary.main}
                            text={t("label.subscription-groups")}
                            onClick={handleSubscriptionGroupsClick}
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
