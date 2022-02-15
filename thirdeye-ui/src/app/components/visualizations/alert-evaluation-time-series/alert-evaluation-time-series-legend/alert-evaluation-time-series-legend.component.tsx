import { Grid, Typography, useTheme } from "@material-ui/core";
import { Legend, LegendItem, LegendLabel, scaleOrdinal } from "@visx/visx";
import classnames from "classnames";
import { kebabCase } from "lodash";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { Dimension } from "../../../../utils/material-ui/dimension.util";
import { Palette } from "../../../../utils/material-ui/palette.util";
import { AlertEvaluationTimeSeriesPlotLine } from "../alert-evaluation-time-series/alert-evaluation-time-series.interfaces";
import { AlertEvaluationTimeSeriesLegendProps } from "./alert-evaluation-time-series-legend.interfaces";
import { useAlertEvaluationTimeSeriesLegendStyles } from "./alert-evaluation-time-series-legend.styles";

export const AlertEvaluationTimeSeriesLegend: FunctionComponent<
    AlertEvaluationTimeSeriesLegendProps
> = (props: AlertEvaluationTimeSeriesLegendProps) => {
    const alertEvaluationTimeSeriesLegendClasses =
        useAlertEvaluationTimeSeriesLegendStyles();
    const theme = useTheme();
    const { t } = useTranslation();

    const legendScale = useMemo(() => {
        return scaleOrdinal({
            domain: [
                AlertEvaluationTimeSeriesPlotLine.CURRENT,
                AlertEvaluationTimeSeriesPlotLine.BASELINE,
                AlertEvaluationTimeSeriesPlotLine.UPPER_AND_LOWER_BOUND,
                AlertEvaluationTimeSeriesPlotLine.ANOMALIES,
            ],
            range: [
                Palette.COLOR_VISUALIZATION_STROKE_CURRENT,
                Palette.COLOR_VISUALIZATION_STROKE_BASELINE,
                Palette.COLOR_VISUALIZATION_STROKE_UPPER_AND_LOWER_BOUND,
                Palette.COLOR_VISUALIZATION_STROKE_ANOMALY,
            ],
        });
    }, []);

    const getLegendItemState = (
        alertEvaluationTimeSeriesPlotLine: AlertEvaluationTimeSeriesPlotLine
    ): boolean => {
        switch (alertEvaluationTimeSeriesPlotLine) {
            case AlertEvaluationTimeSeriesPlotLine.CURRENT: {
                return props.current;
            }
            case AlertEvaluationTimeSeriesPlotLine.BASELINE: {
                return props.baseline;
            }
            case AlertEvaluationTimeSeriesPlotLine.UPPER_AND_LOWER_BOUND: {
                return props.upperAndLowerBound;
            }
            case AlertEvaluationTimeSeriesPlotLine.ANOMALIES: {
                return props.anomalies;
            }
            default: {
                return false;
            }
        }
    };

    return (
        <Legend scale={legendScale}>
            {(labels) => (
                <Grid container justify="space-between" spacing={0}>
                    {labels &&
                        labels.map((label, index) => (
                            <Grid item key={index}>
                                <LegendItem
                                    className={classnames(
                                        alertEvaluationTimeSeriesLegendClasses.legendItem,
                                        {
                                            // When parent container width is roughly equal to
                                            // screen width xs, apply sufficient right margin to
                                            // the last legend item so that it wraps to new line
                                            [alertEvaluationTimeSeriesLegendClasses.legendItemWrapped]:
                                                label.text ===
                                                    AlertEvaluationTimeSeriesPlotLine.ANOMALIES &&
                                                props.parentWidth &&
                                                props.parentWidth <
                                                    theme.breakpoints.width(
                                                        "sm"
                                                    ),
                                        }
                                    )}
                                    onClick={() =>
                                        props.onChange &&
                                        props.onChange(
                                            label.text as AlertEvaluationTimeSeriesPlotLine
                                        )
                                    }
                                >
                                    {/* Glyph */}
                                    <svg
                                        height={
                                            Dimension.HEIGHT_VISUALIZATION_LEGEND_GLYPH
                                        }
                                        opacity={
                                            getLegendItemState(
                                                label.text as AlertEvaluationTimeSeriesPlotLine
                                            )
                                                ? 1
                                                : 0.5
                                        }
                                        width={
                                            Dimension.WIDTH_VISUALIZATION_LEGEND_GLYPH
                                        }
                                    >
                                        <rect
                                            fill={label.value}
                                            height={
                                                Dimension.HEIGHT_VISUALIZATION_LEGEND_GLYPH
                                            }
                                            width={
                                                Dimension.WIDTH_VISUALIZATION_LEGEND_GLYPH
                                            }
                                        />
                                    </svg>

                                    {/* Label */}
                                    <LegendLabel
                                        className={classnames(
                                            alertEvaluationTimeSeriesLegendClasses.legendLabel,
                                            {
                                                [alertEvaluationTimeSeriesLegendClasses.legendLabelDisabled]:
                                                    !getLegendItemState(
                                                        label.text as AlertEvaluationTimeSeriesPlotLine
                                                    ),
                                            }
                                        )}
                                    >
                                        <Typography variant="subtitle2">
                                            {t(
                                                `label.${kebabCase(label.text)}`
                                            )}
                                        </Typography>
                                    </LegendLabel>
                                </LegendItem>
                            </Grid>
                        ))}
                </Grid>
            )}
        </Legend>
    );
};
