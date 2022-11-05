import { Box, Grid, Link, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { Link as RouterLink } from "react-router-dom";
import { SkeletonV1 } from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { getAnomaliesAllPath } from "../../../utils/routes/routes.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { AnomaliesPendingFeedbackCountProps } from "./anomlies-pending-feedback-count.interfaces";

export const AnomaliesPendingFeedbackCount: FunctionComponent<
    AnomaliesPendingFeedbackCountProps
> = ({ appAnalytics, getAppAnalyticsStatus }) => {
    const { t } = useTranslation();

    return (
        <Grid container alignItems="center" justifyContent="space-between">
            <Grid item>
                <Box>{t("label.anomalies-pending-feedback")}</Box>
                <Box>
                    <Link component={RouterLink} to={getAnomaliesAllPath()}>
                        {t("label.review-entities", {
                            entity: t("label.anomalies"),
                        })}
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
                    isError={getAppAnalyticsStatus === ActionStatus.Error}
                    isLoading={getAppAnalyticsStatus === ActionStatus.Working}
                    loadingState={
                        <Box width={100}>
                            <Typography variant="h2">
                                <SkeletonV1 animation="pulse" />
                            </Typography>
                        </Box>
                    }
                >
                    <Typography variant="h2">
                        {appAnalytics &&
                            appAnalytics.anomalyStats.totalCount -
                                appAnalytics.anomalyStats.countWithFeedback}
                    </Typography>
                </LoadingErrorStateSwitch>
            </Grid>
        </Grid>
    );
};
