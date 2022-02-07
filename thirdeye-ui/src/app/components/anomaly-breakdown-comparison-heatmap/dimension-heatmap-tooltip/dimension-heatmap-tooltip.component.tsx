import {
    Box,
    Divider,
    Grid,
    List,
    ListItem,
    ListItemText,
} from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { EMPTY_STRING_DISPLAY } from "../../../utils/anomalies/anomalies.util";
import { useCommonStyles } from "../../../utils/material-ui/common.styles";
import { formatLargeNumber } from "../../../utils/number/number.util";
import { SafariMuiGridFix } from "../../safari-mui-grid-fix/safari-mui-grid-fix.component";
import { TreemapData } from "../../visualizations/treemap/treemap.interfaces";
import {
    AnomalyBreakdownComparisonData,
    DimensionDisplayData,
} from "../anomaly-breakdown-comparison-heatmap.interfaces";
import { useDimensionHeatmapTooltipStyles } from "./dimension-heatmap-tooltip.styles";

export const DimensionHeatmapTooltip: FunctionComponent<
    TreemapData<AnomalyBreakdownComparisonData & DimensionDisplayData>
> = (
    props: TreemapData<AnomalyBreakdownComparisonData & DimensionDisplayData>
) => {
    const dimensionHeatmapTooltipStyles = useDimensionHeatmapTooltipStyles();
    const commonClasses = useCommonStyles();
    const { t } = useTranslation();
    let metricValueDiffDisplay;
    let metricValueChangeColorClass;
    let contributionChangeColorClass;

    if (props.extraData) {
        metricValueDiffDisplay = formatLargeNumber(
            props.extraData.metricValueDiff
        );

        if (props.extraData.metricValueDiffPercentage) {
            metricValueDiffDisplay = `(${props.extraData.metricValueDiffPercentage.toFixed(
                2
            )}%) ${metricValueDiffDisplay}`;
        }

        if (props.extraData.metricValueDiff > 0) {
            metricValueChangeColorClass = commonClasses.increased;
        } else if (props.extraData.metricValueDiff < 0) {
            metricValueChangeColorClass = commonClasses.decreased;
        }

        if (props.extraData.contributionDiff > 0) {
            contributionChangeColorClass = commonClasses.increased;
        } else if (props.extraData.contributionDiff < 0) {
            contributionChangeColorClass = commonClasses.decreased;
        }
    }

    if (!props.extraData) {
        return <span />;
    }

    return (
        <>
            <Grid container direction="column" spacing={0}>
                {/* Name of the Dimension */}
                <Grid item xs={12}>
                    <Grid
                        container
                        alignItems="center"
                        justify="center"
                        spacing={0}
                    >
                        <Grid
                            item
                            className={
                                dimensionHeatmapTooltipStyles.spaceBottom
                            }
                        >
                            {props.extraData.columnName}:{" "}
                            {props.id || EMPTY_STRING_DISPLAY}
                        </Grid>
                    </Grid>

                    <Box padding={1}>
                        <Divider light />
                    </Box>
                </Grid>

                <Grid item xs={12}>
                    <List
                        dense
                        disablePadding
                        className={
                            dimensionHeatmapTooltipStyles.dataDisplayList
                        }
                    >
                        <ListItem
                            className={
                                dimensionHeatmapTooltipStyles.dataDisplayItem
                            }
                        >
                            <ListItemText
                                className={
                                    dimensionHeatmapTooltipStyles.dataDisplayText
                                }
                                primary={`${t("label.current")} ${t(
                                    "label.value"
                                )}`}
                            />
                            <ListItemText
                                className={
                                    dimensionHeatmapTooltipStyles.dataDisplayText
                                }
                                primary={`${formatLargeNumber(
                                    props.extraData.current
                                )}`}
                                primaryTypographyProps={{
                                    align: "right",
                                    variant: "subtitle2",
                                }}
                            />
                        </ListItem>

                        <ListItem
                            className={
                                dimensionHeatmapTooltipStyles.dataDisplayItem
                            }
                        >
                            <ListItemText
                                className={
                                    dimensionHeatmapTooltipStyles.dataDisplayText
                                }
                                primary={`${t("label.baseline")} ${t(
                                    "label.value"
                                )}`}
                            />
                            <ListItemText
                                className={
                                    dimensionHeatmapTooltipStyles.dataDisplayText
                                }
                                primary={`${formatLargeNumber(
                                    props.extraData.baseline
                                )}`}
                                primaryTypographyProps={{
                                    align: "right",
                                    variant: "subtitle2",
                                }}
                            />
                        </ListItem>

                        <ListItem
                            className={
                                dimensionHeatmapTooltipStyles.dataDisplayItem
                            }
                        >
                            <ListItemText
                                className={
                                    dimensionHeatmapTooltipStyles.dataDisplayText
                                }
                                primary={`${t("label.change")}`}
                            />
                            <ListItemText
                                className={`${dimensionHeatmapTooltipStyles.dataDisplayText} ${metricValueChangeColorClass}`}
                                primary={metricValueDiffDisplay}
                                primaryTypographyProps={{
                                    align: "right",
                                    variant: "subtitle2",
                                }}
                            />
                        </ListItem>
                    </List>
                </Grid>

                <Grid item xs={12}>
                    <Box padding={1}>
                        <Divider />
                    </Box>
                </Grid>

                <Grid item xs={12}>
                    <List
                        dense
                        disablePadding
                        className={
                            dimensionHeatmapTooltipStyles.dataDisplayList
                        }
                    >
                        <ListItem
                            className={
                                dimensionHeatmapTooltipStyles.dataDisplayItem
                            }
                        >
                            <ListItemText
                                className={
                                    dimensionHeatmapTooltipStyles.dataDisplayText
                                }
                                primary={`${t("label.current")} ${t(
                                    "label.contribution"
                                )}`}
                            />
                            <ListItemText
                                className={
                                    dimensionHeatmapTooltipStyles.dataDisplayText
                                }
                                primary={`${formatLargeNumber(
                                    props.extraData
                                        .currentContributionPercentage * 100
                                )}%`}
                                primaryTypographyProps={{
                                    align: "right",
                                    variant: "subtitle2",
                                }}
                            />
                        </ListItem>

                        <ListItem
                            className={
                                dimensionHeatmapTooltipStyles.dataDisplayItem
                            }
                        >
                            <ListItemText
                                className={
                                    dimensionHeatmapTooltipStyles.dataDisplayText
                                }
                                primary={`${t("label.baseline")} ${t(
                                    "label.contribution"
                                )}`}
                            />
                            <ListItemText
                                className={
                                    dimensionHeatmapTooltipStyles.dataDisplayText
                                }
                                primary={`${formatLargeNumber(
                                    props.extraData
                                        .baselineContributionPercentage * 100
                                )}%`}
                                primaryTypographyProps={{
                                    align: "right",
                                    variant: "subtitle2",
                                }}
                            />
                        </ListItem>

                        <ListItem
                            className={
                                dimensionHeatmapTooltipStyles.dataDisplayItem
                            }
                        >
                            <ListItemText
                                className={
                                    dimensionHeatmapTooltipStyles.dataDisplayText
                                }
                                primary={`${t("label.change")}`}
                            />
                            <ListItemText
                                className={`${dimensionHeatmapTooltipStyles.dataDisplayText} ${contributionChangeColorClass}`}
                                primary={`${formatLargeNumber(
                                    props.extraData.contributionDiff * 100
                                )}%`}
                                primaryTypographyProps={{
                                    align: "right",
                                    variant: "subtitle2",
                                }}
                            />
                        </ListItem>
                    </List>
                </Grid>

                <SafariMuiGridFix />
            </Grid>
        </>
    );
};
