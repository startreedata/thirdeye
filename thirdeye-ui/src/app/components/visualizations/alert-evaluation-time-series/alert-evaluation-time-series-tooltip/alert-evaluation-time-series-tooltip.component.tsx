import { Grid, Typography } from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { getAnomalyName } from "../../../../utils/anomalies/anomalies.util";
import { formatDateAndTime } from "../../../../utils/date-time/date-time.util";
import { formatLargeNumber } from "../../../../utils/number/number.util";
import { AlertEvaluationTimeSeriesTooltipProps } from "./alert-evaluation-time-series-tooltip.interfaces";
import { useAlertEvaluationTimeSeriesTooltipStyles } from "./alert-evaluation-time-series-tooltip.styles";

export const AlertEvaluationTimeSeriesTooltip: FunctionComponent<AlertEvaluationTimeSeriesTooltipProps> = (
    props: AlertEvaluationTimeSeriesTooltipProps
) => {
    const alertEvaluationTimeSeriesTooltipClasses = useAlertEvaluationTimeSeriesTooltipStyles();
    const { t } = useTranslation();

    return (
        <>
            {props.alertEvaluationTimeSeriesTooltipPoint && (
                <Grid container direction="column" spacing={0}>
                    {/* Time */}
                    <Grid
                        item
                        className={
                            alertEvaluationTimeSeriesTooltipClasses.header
                        }
                    >
                        <Grid container alignItems="center" justify="center">
                            <Grid item>
                                <Typography variant="overline">
                                    {formatDateAndTime(
                                        props
                                            .alertEvaluationTimeSeriesTooltipPoint
                                            .current
                                    )}
                                </Typography>
                            </Grid>
                        </Grid>
                    </Grid>

                    {/* Current */}
                    {isFinite(
                        props.alertEvaluationTimeSeriesTooltipPoint.current
                    ) && (
                        <Grid item>
                            <Grid
                                container
                                alignItems="center"
                                justify="space-between"
                            >
                                <Grid item>
                                    <Typography variant="subtitle2">
                                        {t("label.current")}
                                    </Typography>
                                </Grid>

                                <Grid item>
                                    <Typography variant="overline">
                                        {formatLargeNumber(
                                            props
                                                .alertEvaluationTimeSeriesTooltipPoint
                                                .current
                                        )}
                                    </Typography>
                                </Grid>
                            </Grid>
                        </Grid>
                    )}

                    {/* Baseline */}
                    {isFinite(
                        props.alertEvaluationTimeSeriesTooltipPoint.expected
                    ) && (
                        <Grid item>
                            <Grid
                                container
                                alignItems="center"
                                justify="space-between"
                            >
                                <Grid item>
                                    <Typography variant="subtitle2">
                                        {t("label.baseline")}
                                    </Typography>
                                </Grid>

                                <Grid item>
                                    <Typography variant="overline">
                                        {formatLargeNumber(
                                            props
                                                .alertEvaluationTimeSeriesTooltipPoint
                                                .expected
                                        )}
                                    </Typography>
                                </Grid>
                            </Grid>
                        </Grid>
                    )}

                    {/* Upper bound */}
                    {isFinite(
                        props.alertEvaluationTimeSeriesTooltipPoint.upperBound
                    ) && (
                        <Grid item>
                            <Grid
                                container
                                alignItems="center"
                                justify="space-between"
                            >
                                <Grid item>
                                    <Typography variant="subtitle2">
                                        {t("label.upper-bound")}
                                    </Typography>
                                </Grid>

                                <Grid item>
                                    <Typography variant="overline">
                                        {formatLargeNumber(
                                            props
                                                .alertEvaluationTimeSeriesTooltipPoint
                                                .upperBound
                                        )}
                                    </Typography>
                                </Grid>
                            </Grid>
                        </Grid>
                    )}

                    {/* Lower bound */}
                    {isFinite(
                        props.alertEvaluationTimeSeriesTooltipPoint.lowerBound
                    ) && (
                        <Grid item>
                            <Grid
                                container
                                alignItems="center"
                                justify="space-between"
                            >
                                <Grid item>
                                    <Typography variant="subtitle2">
                                        {t("label.lower-bound")}
                                    </Typography>
                                </Grid>

                                <Grid item>
                                    <Typography variant="overline">
                                        {formatLargeNumber(
                                            props
                                                .alertEvaluationTimeSeriesTooltipPoint
                                                .lowerBound
                                        )}
                                    </Typography>
                                </Grid>
                            </Grid>
                        </Grid>
                    )}

                    {/* Anomalies */}
                    {!isEmpty(
                        props.alertEvaluationTimeSeriesTooltipPoint.anomalies
                    ) && (
                        <>
                            {/* Anomaly */}
                            <Grid
                                item
                                className={
                                    alertEvaluationTimeSeriesTooltipClasses.header
                                }
                            >
                                <Grid
                                    container
                                    alignItems="center"
                                    justify="center"
                                >
                                    <Grid item>
                                        <Typography variant="overline">
                                            {getAnomalyName(
                                                props
                                                    .alertEvaluationTimeSeriesTooltipPoint
                                                    .anomalies[0]
                                            )}
                                        </Typography>
                                    </Grid>
                                </Grid>
                            </Grid>

                            {/* Anomaly start time */}
                            <Grid item>
                                <Grid
                                    container
                                    alignItems="center"
                                    justify="space-between"
                                >
                                    <Grid item>
                                        <Typography variant="subtitle2">
                                            {t("label.start")}
                                        </Typography>
                                    </Grid>

                                    <Grid item>
                                        <Typography variant="overline">
                                            {formatDateAndTime(
                                                props
                                                    .alertEvaluationTimeSeriesTooltipPoint
                                                    .anomalies[0].startTime
                                            )}
                                        </Typography>
                                    </Grid>
                                </Grid>
                            </Grid>

                            {/* Anomaly end time */}
                            <Grid item>
                                <Grid
                                    container
                                    alignItems="center"
                                    justify="space-between"
                                >
                                    <Grid item>
                                        <Typography variant="subtitle2">
                                            {t("label.end")}
                                        </Typography>
                                    </Grid>

                                    <Grid item>
                                        <Typography variant="overline">
                                            {formatDateAndTime(
                                                props
                                                    .alertEvaluationTimeSeriesTooltipPoint
                                                    .anomalies[0].endTime
                                            )}
                                        </Typography>
                                    </Grid>
                                </Grid>
                            </Grid>

                            {/* More */}
                            {props.alertEvaluationTimeSeriesTooltipPoint
                                .anomalies.length > 1 && (
                                <Grid
                                    item
                                    className={
                                        alertEvaluationTimeSeriesTooltipClasses.header
                                    }
                                >
                                    <Grid
                                        container
                                        alignItems="center"
                                        justify="flex-end"
                                    >
                                        <Grid item>
                                            <Typography variant="subtitle2">
                                                {t("label.more-count", {
                                                    count:
                                                        props
                                                            .alertEvaluationTimeSeriesTooltipPoint
                                                            .anomalies.length -
                                                        1,
                                                })}
                                            </Typography>
                                        </Grid>
                                    </Grid>
                                </Grid>
                            )}
                        </>
                    )}
                </Grid>
            )}
        </>
    );
};
