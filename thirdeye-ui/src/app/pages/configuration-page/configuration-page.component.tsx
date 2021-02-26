import { Box, Grid, useTheme } from "@material-ui/core";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { ReactComponent as Metrics } from "../../../assets/images/metric.svg";
import { ReactComponent as SubscriptionGroups } from "../../../assets/images/subscription-group.svg";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { ButtonTile } from "../../components/button-tile/button-tile.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import {
    getMetricsPath,
    getSubscriptionGroupsPath,
} from "../../utils/routes/routes.util";

export const ConfigurationPage: FunctionComponent = () => {
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
            <Box
                alignItems="center"
                display="flex"
                flex={1}
                height="100%"
                justifyContent="center"
                width="100%"
            >
                <Grid container justify="center" spacing={4}>
                    {/* Subscription groups */}
                    <Grid item>
                        <ButtonTile
                            icon={SubscriptionGroups}
                            iconColor={theme.palette.primary.main}
                            text={t("label.subscription-groups")}
                            onClick={handleSubscriptionGroupsClick}
                        />
                    </Grid>

                    {/* Metrics */}
                    <Grid item>
                        <ButtonTile
                            icon={Metrics}
                            iconColor={theme.palette.primary.main}
                            text={t("label.metrics")}
                            onClick={handleMetricsClick}
                        />
                    </Grid>
                </Grid>
            </Box>
        </PageContents>
    );
};
