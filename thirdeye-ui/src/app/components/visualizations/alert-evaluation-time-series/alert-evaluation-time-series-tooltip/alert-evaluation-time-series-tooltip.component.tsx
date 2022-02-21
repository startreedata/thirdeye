import { Grid, Typography } from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import {
    formatDateAndTimeV1,
    formatLargeNumberV1,
    formatNumberV1,
} from "../../../../platform/utils";
import { getAnomalyName } from "../../../../utils/anomalies/anomalies.util";
import { SafariMuiGridFix } from "../../../safari-mui-grid-fix/safari-mui-grid-fix.component";
import { AlertEvaluationTimeSeriesTooltipProps } from "./alert-evaluation-time-series-tooltip.interfaces";
import { useAlertEvaluationTimeSeriesTooltipStyles } from "./alert-evaluation-time-series-tooltip.styles";

export const AlertEvaluationTimeSeriesTooltip: FunctionComponent<
    AlertEvaluationTimeSeriesTooltipProps
> = (props: AlertEvaluationTimeSeriesTooltipProps) => {
    const alertEvaluationTimeSeriesTooltipClasses =
        useAlertEvaluationTimeSeriesTooltipStyles();
    const { t } = useTranslation();

    return (
        <>
            {props.alertEvaluationTimeSeriesTooltipPoint && (
                <Grid
                    container
                    className={
                        alertEvaluationTimeSeriesTooltipClasses.alertEvaluationTimeSeriesTooltip
                    }
                    direction="column"
                    spacing={0}
                >
                    {/* Time */}
                    <Grid
                        item
                        className={alertEvaluationTimeSeriesTooltipClasses.time}
                    >
                        <Grid
                            container
                            alignItems="center"
                            justify="center"
                            spacing={0}
                        >
                            <Grid item>
                                <Typography variant="overline">
                                    {formatDateAndTimeV1(
                                        props
                                            .alertEvaluationTimeSeriesTooltipPoint
                                            .timestamp
                                    )}
                                </Typography>
                            </Grid>
                        </Grid>
                    </Grid>

                    {/* Current */}
                    {isFinite(
                        props.alertEvaluationTimeSeriesTooltipPoint.current
                    ) && (
                        <Grid
                            item
                            className={
                                alertEvaluationTimeSeriesTooltipClasses.nameValueContents
                            }
                        >
                            {/* Name */}
                            <Typography
                                className={
                                    alertEvaluationTimeSeriesTooltipClasses.name
                                }
                                variant="subtitle2"
                            >
                                {t("label.current")}
                            </Typography>

                            {/* Value */}
                            <Typography
                                className={
                                    alertEvaluationTimeSeriesTooltipClasses.value
                                }
                                variant="overline"
                            >
                                {formatLargeNumberV1(
                                    props.alertEvaluationTimeSeriesTooltipPoint
                                        .current
                                )}
                            </Typography>
                        </Grid>
                    )}

                    {/* Baseline */}
                    {isFinite(
                        props.alertEvaluationTimeSeriesTooltipPoint.expected
                    ) && (
                        <Grid
                            item
                            className={
                                alertEvaluationTimeSeriesTooltipClasses.nameValueContents
                            }
                        >
                            {/* Name */}
                            <Typography
                                className={
                                    alertEvaluationTimeSeriesTooltipClasses.name
                                }
                                variant="subtitle2"
                            >
                                {t("label.baseline")}
                            </Typography>

                            {/* Value */}
                            <Typography
                                className={
                                    alertEvaluationTimeSeriesTooltipClasses.value
                                }
                                variant="overline"
                            >
                                {formatLargeNumberV1(
                                    props.alertEvaluationTimeSeriesTooltipPoint
                                        .expected
                                )}
                            </Typography>
                        </Grid>
                    )}

                    {/* Upper bound */}
                    {isFinite(
                        props.alertEvaluationTimeSeriesTooltipPoint.upperBound
                    ) && (
                        <Grid
                            item
                            className={
                                alertEvaluationTimeSeriesTooltipClasses.nameValueContents
                            }
                        >
                            {/* Name */}
                            <Typography
                                className={
                                    alertEvaluationTimeSeriesTooltipClasses.name
                                }
                                variant="subtitle2"
                            >
                                {t("label.upper-bound")}
                            </Typography>

                            {/* Value */}
                            <Typography
                                className={
                                    alertEvaluationTimeSeriesTooltipClasses.value
                                }
                                variant="overline"
                            >
                                {formatLargeNumberV1(
                                    props.alertEvaluationTimeSeriesTooltipPoint
                                        .upperBound
                                )}
                            </Typography>
                        </Grid>
                    )}

                    {/* Lower bound */}
                    {isFinite(
                        props.alertEvaluationTimeSeriesTooltipPoint.lowerBound
                    ) && (
                        <Grid
                            item
                            className={
                                alertEvaluationTimeSeriesTooltipClasses.nameValueContents
                            }
                        >
                            {/* Name */}
                            <Typography
                                className={
                                    alertEvaluationTimeSeriesTooltipClasses.name
                                }
                                variant="subtitle2"
                            >
                                {t("label.lower-bound")}
                            </Typography>

                            {/* Value */}
                            <Typography
                                className={
                                    alertEvaluationTimeSeriesTooltipClasses.value
                                }
                                variant="overline"
                            >
                                {formatLargeNumberV1(
                                    props.alertEvaluationTimeSeriesTooltipPoint
                                        .lowerBound
                                )}
                            </Typography>
                        </Grid>
                    )}

                    {/* Anomaly */}
                    {!isEmpty(
                        props.alertEvaluationTimeSeriesTooltipPoint.anomalies
                    ) && (
                        <>
                            {/* Anomaly name */}
                            <Grid
                                item
                                className={
                                    alertEvaluationTimeSeriesTooltipClasses.anomaly
                                }
                            >
                                <Grid
                                    container
                                    alignItems="center"
                                    justify="center"
                                    spacing={0}
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

                            {/* Start time */}
                            <Grid
                                item
                                className={
                                    alertEvaluationTimeSeriesTooltipClasses.nameValueContents
                                }
                            >
                                {/* Name */}
                                <Typography
                                    className={
                                        alertEvaluationTimeSeriesTooltipClasses.name
                                    }
                                    variant="subtitle2"
                                >
                                    {t("label.start")}
                                </Typography>

                                {/* Value */}
                                <Typography
                                    className={
                                        alertEvaluationTimeSeriesTooltipClasses.value
                                    }
                                    variant="overline"
                                >
                                    {formatDateAndTimeV1(
                                        props
                                            .alertEvaluationTimeSeriesTooltipPoint
                                            .anomalies[0].startTime
                                    )}
                                </Typography>
                            </Grid>

                            {/* End time */}
                            <Grid
                                item
                                className={
                                    alertEvaluationTimeSeriesTooltipClasses.nameValueContents
                                }
                            >
                                {/* Name */}
                                <Typography
                                    className={
                                        alertEvaluationTimeSeriesTooltipClasses.name
                                    }
                                    variant="subtitle2"
                                >
                                    {t("label.end")}
                                </Typography>

                                {/* Value */}
                                <Typography
                                    className={
                                        alertEvaluationTimeSeriesTooltipClasses.value
                                    }
                                    variant="overline"
                                >
                                    {formatDateAndTimeV1(
                                        props
                                            .alertEvaluationTimeSeriesTooltipPoint
                                            .anomalies[0].endTime
                                    )}
                                </Typography>
                            </Grid>

                            {/* More */}
                            {props.alertEvaluationTimeSeriesTooltipPoint
                                .anomalies.length > 1 && (
                                <Grid
                                    item
                                    className={
                                        alertEvaluationTimeSeriesTooltipClasses.more
                                    }
                                >
                                    <Grid
                                        container
                                        alignItems="center"
                                        justify="flex-end"
                                        spacing={0}
                                    >
                                        <Grid item>
                                            <Typography variant="subtitle2">
                                                {t("label.more-count", {
                                                    count: formatNumberV1(
                                                        props
                                                            .alertEvaluationTimeSeriesTooltipPoint
                                                            .anomalies.length -
                                                            1
                                                    ) as never,
                                                })}
                                            </Typography>
                                        </Grid>
                                    </Grid>
                                </Grid>
                            )}
                        </>
                    )}

                    <SafariMuiGridFix />
                </Grid>
            )}
        </>
    );
};
