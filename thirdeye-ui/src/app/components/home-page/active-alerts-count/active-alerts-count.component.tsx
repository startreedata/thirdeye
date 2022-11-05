import { Box, Grid, Link, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { Link as RouterLink } from "react-router-dom";
import { SkeletonV1 } from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import {
    getAlertsAllPath,
    getAlertsCreatePath,
} from "../../../utils/routes/routes.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { ActiveAlertsCountProps } from "./active-alerts-count.interfaces";

export const ActiveAlertsCount: FunctionComponent<ActiveAlertsCountProps> = ({
    getAlertsStatus,
    alerts,
}) => {
    const { t } = useTranslation();

    return (
        <Grid container alignItems="center" justifyContent="space-between">
            <Grid item>
                <Box>{t("label.active-alerts")}</Box>
                <Box>
                    <Link component={RouterLink} to={getAlertsCreatePath()}>
                        {t("label.create-entity", {
                            entity: t("label.alert"),
                        })}
                    </Link>
                </Box>
                <Box>
                    <Link component={RouterLink} to={getAlertsAllPath()}>
                        {t("label.view-all-alerts")}
                    </Link>
                </Box>
            </Grid>
            <Grid item>
                <LoadingErrorStateSwitch
                    errorState={
                        <NoDataIndicator>
                            {t("message.experienced-issues-fetching-data")}
                        </NoDataIndicator>
                    }
                    isError={getAlertsStatus === ActionStatus.Error}
                    isLoading={getAlertsStatus === ActionStatus.Working}
                    loadingState={
                        <Box width={100}>
                            <Typography variant="h2">
                                <SkeletonV1 animation="pulse" />
                            </Typography>
                        </Box>
                    }
                >
                    <Typography variant="h2">
                        {alerts &&
                            alerts.filter((alert) => alert.active).length}
                    </Typography>
                </LoadingErrorStateSwitch>
            </Grid>
        </Grid>
    );
};
