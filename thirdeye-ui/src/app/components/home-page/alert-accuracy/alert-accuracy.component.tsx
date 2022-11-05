import { Box, Grid, Typography } from "@material-ui/core";
import InfoOutlinedIcon from "@material-ui/icons/InfoOutlined";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { SkeletonV1, TooltipV1 } from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { AlertAccuracyProps } from "./alert-accuracy.interfaces";

export const AlertAccuracy: FunctionComponent<AlertAccuracyProps> = ({
    appAnalytics,
    getAppAnalyticsStatus,
}) => {
    const { t } = useTranslation();

    const precision = useMemo(() => {
        if (!appAnalytics) {
            return null;
        }
        const totalAnomaliesLabeledAsAnomaly =
            appAnalytics.anomalyStats.feedbackStats.ANOMALY +
            appAnalytics.anomalyStats.feedbackStats.ANOMALY_EXPECTED +
            appAnalytics.anomalyStats.feedbackStats.ANOMALY_NEW_TREND;

        // If there are no anomalies with feedback return n/a
        if (appAnalytics.anomalyStats.countWithFeedback === 0) {
            return "n/a";
        }

        return `${Math.round(
            (totalAnomaliesLabeledAsAnomaly /
                appAnalytics.anomalyStats.countWithFeedback) *
                100
        )}%`;
    }, [appAnalytics]);

    return (
        <Grid container alignItems="center" justifyContent="space-between">
            <Grid item>
                <Grid container justifyContent="center">
                    <Grid item>{t("label.accuracy")}</Grid>
                    <Grid item>
                        <TooltipV1
                            delay={0}
                            placement="top"
                            title={t("message.anomaly-precision-tooltip")}
                        >
                            <Typography variant="subtitle1">
                                <InfoOutlinedIcon fontSize="small" />
                            </Typography>
                        </TooltipV1>
                    </Grid>
                </Grid>
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
                        {appAnalytics && precision !== null && (
                            <span>{precision}</span>
                        )}
                    </Typography>
                </LoadingErrorStateSwitch>
            </Grid>
        </Grid>
    );
};
