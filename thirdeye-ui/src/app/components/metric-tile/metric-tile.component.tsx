import { ButtonBase, Paper, Typography } from "@material-ui/core";
import classnames from "classnames";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { MetricTileProps } from "./metric-tile.interfaces";
import { useMetricTileStyles } from "./metric-tile.styles";

export const MetricTile: FunctionComponent<MetricTileProps> = (
    props: MetricTileProps
) => {
    const metricTileClasses = useMetricTileStyles();
    const { t } = useTranslation();

    return (
        <Paper
            className={classnames({
                [metricTileClasses.metricTile]: !props.compact,
                [metricTileClasses.metricTileCompact]: props.compact,
            })}
            elevation={2}
        >
            <ButtonBase
                className={metricTileClasses.metricTileContents}
                data-testid="metricContainer"
                disabled={!props.clickable}
                onClick={props.onClick}
            >
                {/* Metric value */}
                <div className={metricTileClasses.metricValueContainer}>
                    <Typography
                        noWrap
                        className={props.metricValueClassName}
                        data-testid="metricValueContainer"
                        variant={props.compact ? "h5" : "h3"}
                    >
                        {props.metricValue || t("label.no-data-marker")}
                    </Typography>
                </div>

                {/* Metric name */}
                {props.metricName && (
                    <div className={metricTileClasses.metricNameContainer}>
                        <Typography
                            className={classnames(
                                metricTileClasses.metricName,
                                props.metricNameClassName
                            )}
                            data-testid="metricNameContainer"
                            variant="subtitle1"
                        >
                            {props.metricName}
                        </Typography>
                    </div>
                )}
            </ButtonBase>
        </Paper>
    );
};
