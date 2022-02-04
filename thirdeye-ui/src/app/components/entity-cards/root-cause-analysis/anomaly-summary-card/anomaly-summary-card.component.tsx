import { Card, CardContent, Grid } from "@material-ui/core";
import ArrowDownwardIcon from "@material-ui/icons/ArrowDownward";
import ArrowUpwardIcon from "@material-ui/icons/ArrowUpward";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { ActionStatus } from "../../../../rest/actions.interfaces";
import { useGetAlert } from "../../../../rest/alerts/alerts.actions";
import { useCommonStyles } from "../../../../utils/material-ui/common.styles";
import { NoDataIndicator } from "../../../no-data-indicator/no-data-indicator.component";
import { AnomalySummaryCardProps } from "./anomaly-summary-card.interfaces";
import { useAnomalySummaryCardStyles } from "./anomaly-summary-card.styles";

export const AnomalySummaryCard: FunctionComponent<AnomalySummaryCardProps> = (
    props: AnomalySummaryCardProps
) => {
    const { alert, getAlert, status } = useGetAlert();
    const anomalySummaryCardStyles = useAnomalySummaryCardStyles();
    const commonClasses = useCommonStyles();
    const { t } = useTranslation();
    const { uiAnomaly } = props;

    useEffect(() => {
        if (uiAnomaly && uiAnomaly.alertId) {
            getAlert(uiAnomaly.alertId);
        }
    }, [uiAnomaly]);

    return (
        <Card variant="outlined">
            <CardContent>
                {uiAnomaly && (
                    <Grid container spacing={4}>
                        {/* Alert */}
                        <Grid item md={3} xs={6}>
                            <div className={anomalySummaryCardStyles.valueText}>
                                {status === ActionStatus.Working && (
                                    <span>Loading ...</span>
                                )}
                                {status === ActionStatus.Done &&
                                    alert &&
                                    alert.templateProperties.metric}
                            </div>
                            <div className={anomalySummaryCardStyles.label}>
                                {t("label.metric")}{" "}
                                {status === ActionStatus.Done && alert && (
                                    <span>
                                        from{" "}
                                        <strong>
                                            {alert.templateProperties.dataset}
                                        </strong>
                                    </span>
                                )}
                            </div>
                        </Grid>

                        {/* Current and Predicted */}
                        <Grid container item md={4} xs={6}>
                            <Grid item>
                                <div
                                    className={
                                        anomalySummaryCardStyles.valueText
                                    }
                                >
                                    {uiAnomaly.current}
                                </div>
                                <div className={anomalySummaryCardStyles.label}>
                                    {t("label.current")}
                                </div>
                            </Grid>
                            <Grid item>
                                <Grid
                                    container
                                    className={
                                        uiAnomaly.negativeDeviation
                                            ? commonClasses.decreased
                                            : commonClasses.increased
                                    }
                                    spacing={0}
                                >
                                    <Grid item>{uiAnomaly.deviation}</Grid>
                                    <Grid item>
                                        {uiAnomaly.negativeDeviation && (
                                            <ArrowDownwardIcon fontSize="small" />
                                        )}
                                        {!uiAnomaly.negativeDeviation && (
                                            <ArrowUpwardIcon fontSize="small" />
                                        )}
                                    </Grid>
                                </Grid>
                            </Grid>
                            <Grid item>
                                <div
                                    className={
                                        anomalySummaryCardStyles.valueText
                                    }
                                >
                                    {uiAnomaly.predicted}
                                </div>
                                <div className={anomalySummaryCardStyles.label}>
                                    {t("label.predicted")}
                                </div>
                            </Grid>
                        </Grid>

                        {/* Start */}
                        <Grid item md={4} xs={6}>
                            <div className={anomalySummaryCardStyles.valueText}>
                                {uiAnomaly.startTime}
                            </div>
                            <div className={anomalySummaryCardStyles.label}>
                                {t("label.start")}
                            </div>
                        </Grid>

                        {/* Duration */}
                        <Grid item md xs={6}>
                            <div className={anomalySummaryCardStyles.valueText}>
                                {uiAnomaly.duration}
                            </div>
                            <div className={anomalySummaryCardStyles.label}>
                                {t("label.duration")}
                            </div>
                        </Grid>
                    </Grid>
                )}

                {/* No data available */}
                {!uiAnomaly && <NoDataIndicator />}
            </CardContent>
        </Card>
    );
};
