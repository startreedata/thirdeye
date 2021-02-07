import { Typography } from "@material-ui/core";
import { Legend, LegendItem, LegendLabel, scaleOrdinal } from "@visx/visx";
import classnames from "classnames";
import { kebabCase } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { Dimension } from "../../../../utils/material-ui/dimension.util";
import { Palette } from "../../../../utils/material-ui/palette.util";
import { AlertEvaluationTimeSeriesPlot } from "../alert-evaluation-time-series.interfaces";
import { AlertEvaluationTimeSeriesLegendProps } from "./alert-evaluation-time-series-legend.interfaces";
import { useAlertEvaluationTimeSeriesLegendStyles } from "./alert-evaluation-time-series-legend.styles";

export const AlertEvaluationTimeSeriesLegend: FunctionComponent<AlertEvaluationTimeSeriesLegendProps> = (
    props: AlertEvaluationTimeSeriesLegendProps
) => {
    const alertEvaluationTimeSeriesLegendClasses = useAlertEvaluationTimeSeriesLegendStyles();
    const { t } = useTranslation();

    // Legend scale
    const legendOrdinalScale = scaleOrdinal({
        domain: [
            AlertEvaluationTimeSeriesPlot.CURRENT,
            AlertEvaluationTimeSeriesPlot.BASELINE,
            AlertEvaluationTimeSeriesPlot.UPPER_AND_LOWER_BOUND,
            AlertEvaluationTimeSeriesPlot.ANOMALIES,
        ],
        range: [
            Palette.COLOR_VISUALIZATION_STROKE_CURRENT,
            Palette.COLOR_VISUALIZATION_STROKE_BASELINE,
            Palette.COLOR_VISUALIZATION_STROKE_UPPER_AND_LOWER_BOUND,
            Palette.COLOR_VISUALIZATION_STROKE_ANOMALIES,
        ],
    });

    const getLegendItemState = (
        alertEvaluationTimeSeries: AlertEvaluationTimeSeriesPlot
    ): boolean => {
        switch (alertEvaluationTimeSeries) {
            case AlertEvaluationTimeSeriesPlot.CURRENT: {
                return props.current;
            }
            case AlertEvaluationTimeSeriesPlot.BASELINE: {
                return props.baseline;
            }
            case AlertEvaluationTimeSeriesPlot.UPPER_AND_LOWER_BOUND: {
                return props.upperAndLowerBound;
            }
            case AlertEvaluationTimeSeriesPlot.ANOMALIES: {
                return props.anomalies;
            }
            default: {
                return false;
            }
        }
    };

    return (
        <Legend direction="row" scale={legendOrdinalScale}>
            {(labels) => (
                <div
                    className={alertEvaluationTimeSeriesLegendClasses.container}
                >
                    {labels &&
                        labels.map((label, index) => {
                            return (
                                <LegendItem
                                    className={
                                        alertEvaluationTimeSeriesLegendClasses.legendItem
                                    }
                                    key={index}
                                    onClick={(): void => {
                                        props.onChange &&
                                            props.onChange(
                                                label.text as AlertEvaluationTimeSeriesPlot
                                            );
                                    }}
                                >
                                    {/* Glyph */}
                                    <svg
                                        height={
                                            Dimension.HEIGHT_VISUALIZATION_LEGEND_GLYPH
                                        }
                                        opacity={
                                            getLegendItemState(
                                                label.text as AlertEvaluationTimeSeriesPlot
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
                                            alertEvaluationTimeSeriesLegendClasses.legendItemLabel,
                                            getLegendItemState(
                                                label.text as AlertEvaluationTimeSeriesPlot
                                            )
                                                ? ""
                                                : alertEvaluationTimeSeriesLegendClasses.legendItemLabelDisabled
                                        )}
                                    >
                                        {
                                            <Typography variant="subtitle2">
                                                {t(
                                                    `label.${kebabCase(
                                                        label.text
                                                    )}`
                                                )}
                                            </Typography>
                                        }
                                    </LegendLabel>
                                </LegendItem>
                            );
                        })}
                </div>
            )}
        </Legend>
    );
};
