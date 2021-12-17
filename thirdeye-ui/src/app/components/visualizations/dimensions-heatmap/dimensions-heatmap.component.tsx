import { Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { formatLargeNumber } from "../../../utils/number/number.util";
import { SafariMuiGridFix } from "../../safari-mui-grid-fix/safari-mui-grid-fix.component";
import { useAlertEvaluationTimeSeriesTooltipStyles } from "../alert-evaluation-time-series/alert-evaluation-time-series-tooltip/alert-evaluation-time-series-tooltip.styles";
import { DimensionHeatmapTooltipProps } from "./dimentions-heatmap.interfaces";

export const DimensionHeatmapTooltip: FunctionComponent<DimensionHeatmapTooltipProps> = (
    props: DimensionHeatmapTooltipProps
) => {
    const alertEvaluationTimeSeriesTooltipClasses = useAlertEvaluationTimeSeriesTooltipStyles();
    const { t } = useTranslation();

    return (
        <>
            {props.dimensionHeatmapTooltipPoint && (
                <Grid
                    container
                    className={
                        alertEvaluationTimeSeriesTooltipClasses.alertEvaluationTimeSeriesTooltip
                    }
                    direction="column"
                    spacing={0}
                >
                    {/* Name of the Dimension */}
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
                                    {props.dimensionHeatmapTooltipPoint.name}
                                </Typography>
                            </Grid>
                        </Grid>
                    </Grid>

                    {/* Current */}
                    {isFinite(props.dimensionHeatmapTooltipPoint.current) && (
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
                                {formatLargeNumber(
                                    props.dimensionHeatmapTooltipPoint.current
                                )}
                            </Typography>
                        </Grid>
                    )}

                    {/* Baseline */}
                    {isFinite(props.dimensionHeatmapTooltipPoint.baseline) && (
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
                                {formatLargeNumber(
                                    props.dimensionHeatmapTooltipPoint.baseline
                                )}
                            </Typography>
                        </Grid>
                    )}

                    {/* Change*/}
                    {isFinite(props.dimensionHeatmapTooltipPoint.change) && (
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
                                {formatLargeNumber(
                                    props.dimensionHeatmapTooltipPoint.change
                                )}
                            </Typography>
                        </Grid>
                    )}

                    <SafariMuiGridFix />
                </Grid>
            )}
        </>
    );
};
