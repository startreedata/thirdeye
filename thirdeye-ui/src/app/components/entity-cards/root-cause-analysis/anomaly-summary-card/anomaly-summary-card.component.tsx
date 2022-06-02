import { Card, CardContent, Grid } from "@material-ui/core";
import ArrowDownwardIcon from "@material-ui/icons/ArrowDownward";
import ArrowUpwardIcon from "@material-ui/icons/ArrowUpward";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import {
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../../../platform/components";
import { ActionStatus } from "../../../../rest/actions.interfaces";
import { useGetAlert } from "../../../../rest/alerts/alerts.actions";
import { useCommonStyles } from "../../../../utils/material-ui/common.styles";
import { NoDataIndicator } from "../../../no-data-indicator/no-data-indicator.component";
import { AnomalySummaryCardProps } from "./anomaly-summary-card.interfaces";
import { useAnomalySummaryCardStyles } from "./anomaly-summary-card.styles";

export const AnomalySummaryCard: FunctionComponent<AnomalySummaryCardProps> = (
    props
) => {
    const { alert, getAlert, status, errorMessages } = useGetAlert();
    const anomalySummaryCardStyles = useAnomalySummaryCardStyles();
    const commonClasses = useCommonStyles();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const { uiAnomaly } = props;
    let metricName;

    useEffect(() => {
        if (uiAnomaly && uiAnomaly.alertId) {
            getAlert(uiAnomaly.alertId);
        }
    }, [uiAnomaly]);

    useEffect(() => {
        if (status === ActionStatus.Error) {
            isEmpty(errorMessages)
                ? notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.alert"),
                      })
                  )
                : errorMessages.map((msg) =>
                      notify(NotificationTypeV1.Error, msg)
                  );
        }
    }, [status, errorMessages]);

    if (alert && alert.templateProperties) {
        if (
            alert.templateProperties.aggregationColumn &&
            alert.templateProperties.aggregationFunction
        ) {
            metricName = `${alert.templateProperties.aggregationFunction}(${alert.templateProperties.aggregationColumn})`;
        } else if (props.uiAnomaly && props.uiAnomaly.metricName) {
            metricName = props.uiAnomaly.metricName;
        }
    }

    return (
        <Card className={props.className} variant="outlined">
            <CardContent>
                {uiAnomaly && (
                    <Grid container spacing={4}>
                        {/* Metric */}
                        <Grid item lg={3} sm={6} xs={12}>
                            <div className={anomalySummaryCardStyles.valueText}>
                                {status === ActionStatus.Working && (
                                    <span>Loading ...</span>
                                )}
                                {status === ActionStatus.Done && metricName}
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
                        <Grid container item lg={4} sm={6} xs={12}>
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
                                    {t("label.baseline")}
                                </div>
                            </Grid>
                        </Grid>

                        {/* Start */}
                        <Grid item lg={4} sm={6} xs={12}>
                            <div className={anomalySummaryCardStyles.valueText}>
                                {uiAnomaly.startTime}
                            </div>
                            <div className={anomalySummaryCardStyles.label}>
                                {t("label.start")}
                            </div>
                        </Grid>

                        {/* Duration */}
                        <Grid item lg sm={6} xs={12}>
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
