import { Typography, useTheme } from "@material-ui/core";
import { Legend, LegendItem, LegendLabel, scaleOrdinal } from "@visx/visx";
import classnames from "classnames";
import { kebabCase } from "lodash";
import React, { FunctionComponent, ReactNode, useState } from "react";
import { useTranslation } from "react-i18next";
import { Dimension } from "../../../../utils/material-ui-util/dimension-util";
import { Palette } from "../../../../utils/material-ui-util/palette-util";
import { AlertEvaluationTimeSeriesPlot } from "../alert-evaluation-time-series.interfaces";
import { AlertEvaluationTimeSeriesLegendProps } from "./alert-evaluation-time-series-legend.interfaces";
import { useAlertEvaluationTimeSeriesLegendStyles } from "./alert-evaluation-time-series-legend.styles";

export const AlertEvaluationTimeSeriesLegend: FunctionComponent<AlertEvaluationTimeSeriesLegendProps> = (
    props: AlertEvaluationTimeSeriesLegendProps
) => {
    const alertEvaluationTimeSeriesLegendClasses = useAlertEvaluationTimeSeriesLegendStyles();
    const [upperAndLowerBoundVisible, setUpperAndLowerBoundVisible] = useState(
        true
    );
    const [currentVisible, setCurrentVisible] = useState(true);
    const [baselineVisible, setBaselineVisible] = useState(true);
    const [anomaliesVisible, setAnomaliesVisible] = useState(true);
    const theme = useTheme();
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
            Palette.COLOR_VISUALIZATION_STROKE_DEFAULT,
            Palette.COLOR_VISUALIZATION_STROKE_BASELINE,
            theme.palette.primary.main,
            theme.palette.error.main,
        ],
    });

    const getLegendItemVisibility = (
        alertEvaluationTimeSeries: AlertEvaluationTimeSeriesPlot
    ): boolean => {
        switch (alertEvaluationTimeSeries) {
            case AlertEvaluationTimeSeriesPlot.UPPER_AND_LOWER_BOUND: {
                return upperAndLowerBoundVisible;
            }
            case AlertEvaluationTimeSeriesPlot.CURRENT: {
                return currentVisible;
            }
            case AlertEvaluationTimeSeriesPlot.BASELINE: {
                return baselineVisible;
            }
            case AlertEvaluationTimeSeriesPlot.ANOMALIES: {
                return anomaliesVisible;
            }
            default: {
                return false;
            }
        }
    };

    const toggleLegendItemVisibility = (
        alertEvaluationTimeSeries: AlertEvaluationTimeSeriesPlot
    ): void => {
        switch (alertEvaluationTimeSeries) {
            case AlertEvaluationTimeSeriesPlot.UPPER_AND_LOWER_BOUND: {
                setUpperAndLowerBoundVisible(
                    (upperAndLowerBoundVisible) => !upperAndLowerBoundVisible
                );

                break;
            }
            case AlertEvaluationTimeSeriesPlot.CURRENT: {
                setCurrentVisible((currentVisible) => !currentVisible);

                break;
            }
            case AlertEvaluationTimeSeriesPlot.BASELINE: {
                setBaselineVisible((baselineVisible) => !baselineVisible);

                break;
            }
            case AlertEvaluationTimeSeriesPlot.ANOMALIES: {
                setAnomaliesVisible((anomaliesVisible) => !anomaliesVisible);

                break;
            }
        }

        props.onChange && props.onChange(alertEvaluationTimeSeries);
    };

    return (
        // Legend
        <Legend direction="row" scale={legendOrdinalScale}>
            {(labels): ReactNode => (
                <div
                    className={
                        alertEvaluationTimeSeriesLegendClasses.legendContainer
                    }
                >
                    {labels &&
                        labels.map(
                            (label, index): ReactNode => {
                                return (
                                    <LegendItem
                                        className={classnames(
                                            alertEvaluationTimeSeriesLegendClasses.legendItem,
                                            getLegendItemVisibility(
                                                label.text as AlertEvaluationTimeSeriesPlot
                                            )
                                                ? ""
                                                : alertEvaluationTimeSeriesLegendClasses.legendItemDisabled
                                        )}
                                        key={index}
                                        onClick={(): void => {
                                            toggleLegendItemVisibility(
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
                                                getLegendItemVisibility(
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
                                            className={
                                                alertEvaluationTimeSeriesLegendClasses.legendItemText
                                            }
                                        >
                                            {
                                                <Typography variant="body2">
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
                            }
                        )}
                </div>
            )}
        </Legend>
    );
};
