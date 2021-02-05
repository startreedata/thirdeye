import { Grid, useTheme } from "@material-ui/core";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { ReactComponent as Metrics } from "../../../assets/images/metrics.svg";
import { ReactComponent as SubscriptionGroups } from "../../../assets/images/subscription-group.svg";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { ButtonTile } from "../../components/button-tile/button-tile.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { getSubscriptionGroupsPath } from "../../utils/routes-util/routes-util";
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

    const onSubscriptionGroupsClick = (): void => {
        history.push(getSubscriptionGroupsPath());
    };

    const onMetricsClick = (): void => {
        // ToDo
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
                className={configurationPageClasses.container}
                justify="center"
            >
                {/* Subscription groups */}
                <Grid item>
                    <ButtonTile
                        icon={SubscriptionGroups}
                        iconColor={theme.palette.primary.main}
                        text={t("label.subscription-groups")}
                        onClick={onSubscriptionGroupsClick}
                    />
                </Grid>

                {/* Metrics */}
                <Grid item>
                    <ButtonTile
                        icon={Metrics}
                        iconColor={theme.palette.primary.main}
                        text={t("label.metrics")}
                        onClick={onMetricsClick}
                    />
                </Grid>
            </Grid>
        </PageContents>
    );
};
