import { Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { formatLargeNumber } from "../../../utils/number/number.util";
import { SafariMuiGridFix } from "../../safari-mui-grid-fix/safari-mui-grid-fix.component";
import { useAlertEvaluationTimeSeriesTooltipStyles } from "../../visualizations/alert-evaluation-time-series/alert-evaluation-time-series-tooltip/alert-evaluation-time-series-tooltip.styles";
import { TreemapData } from "../../visualizations/treemap/treemap.interfaces";
import { AnomalyBreakdownComparisonData } from "../anomaly-breakdown-comparison-heatmap.interfaces";

export const DimensionHeatmapTooltip: FunctionComponent<
    TreemapData<AnomalyBreakdownComparisonData>
> = (props: TreemapData<AnomalyBreakdownComparisonData>) => {
    const alertEvaluationTimeSeriesTooltipClasses = useAlertEvaluationTimeSeriesTooltipStyles();
    const { t } = useTranslation();

    if (!props.extraData) {
        return <span />;
    }

    return (
        <>
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
                                {props.id}
                            </Typography>
                        </Grid>
                    </Grid>
                </Grid>

                {/* Current */}
                {isFinite(props.extraData.current) && (
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
                            {formatLargeNumber(props.extraData.current)}
                        </Typography>
                    </Grid>
                )}

                {/* Baseline */}
                {isFinite(props.extraData.comparison) && (
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
                            {formatLargeNumber(props.extraData.comparison)}
                        </Typography>
                    </Grid>
                )}

                {/* Change*/}
                {isFinite(props.extraData.percentageDiff) && (
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
                            Contribution Diff
                        </Typography>

                        {/* Value */}
                        <Typography
                            className={
                                alertEvaluationTimeSeriesTooltipClasses.value
                            }
                            variant="overline"
                        >
                            {formatLargeNumber(
                                props.extraData.percentageDiff * 100
                            )}
                            %
                        </Typography>
                    </Grid>
                )}

                <SafariMuiGridFix />
            </Grid>
        </>
    );
};
