import { Grid, Typography } from "@material-ui/core";
import { TooltipWithBounds } from "@visx/visx";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { formatDateAndTime } from "../../../../utils/date-time/date-time.util";
import { formatLargeNumber } from "../../../../utils/number/number.util";
import { TooltipProps } from "./tooltip.interfaces";
import { useTooltipStyles } from "./tooltip.styles";

export const Tooltip: FunctionComponent<TooltipProps> = (
    props: TooltipProps
) => {
    const tooltipClasses = useTooltipStyles();
    const { t } = useTranslation();

    return (
        <div className={tooltipClasses.tooltipContainer}>
            {props.children}

            {/* Tooltip */}
            {props.alertEvaluationTimeSeriesTooltipPoint && (
                <TooltipWithBounds
                    className={tooltipClasses.tooltip}
                    left={props.tooltipLeft}
                    top={props.tooltipTop}
                >
                    <Grid container direction="column" spacing={0}>
                        {/* Time */}
                        <Grid item className={tooltipClasses.header} sm={12}>
                            <Grid
                                container
                                alignItems="center"
                                justify="center"
                            >
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
                            <Grid item sm={12}>
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
                            <Grid item sm={12}>
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
                            props.alertEvaluationTimeSeriesTooltipPoint
                                .upperBound
                        ) && (
                            <Grid item sm={12}>
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
                            props.alertEvaluationTimeSeriesTooltipPoint
                                .lowerBound
                        ) && (
                            <Grid item sm={12}>
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
                    </Grid>
                </TooltipWithBounds>
            )}
        </div>
    );
};
