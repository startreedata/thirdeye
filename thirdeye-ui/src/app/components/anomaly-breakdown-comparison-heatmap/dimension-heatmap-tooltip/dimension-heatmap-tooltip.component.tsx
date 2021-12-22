import { Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { formatLargeNumber } from "../../../utils/number/number.util";
import { SafariMuiGridFix } from "../../safari-mui-grid-fix/safari-mui-grid-fix.component";
import { TreemapData } from "../../visualizations/treemap/treemap.interfaces";
import { AnomalyBreakdownComparisonData } from "../anomaly-breakdown-comparison-heatmap.interfaces";
import { useDimensionHeatmapTooltipStyles } from "./dimension-heatmap-tooltip.styles";

export const DimensionHeatmapTooltip: FunctionComponent<
    TreemapData<AnomalyBreakdownComparisonData>
> = (props: TreemapData<AnomalyBreakdownComparisonData>) => {
    const dimensionHeatmapTooltipStyles = useDimensionHeatmapTooltipStyles();
    const { t } = useTranslation();

    if (!props.extraData) {
        return <span />;
    }

    return (
        <>
            <Grid container direction="column" spacing={0}>
                {/* Name of the Dimension */}
                <Grid item>
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
                            <Typography variant="overline">
                                {props.id}
                            </Typography>
                        </Grid>
                    </Grid>
                </Grid>

                <Grid
                    item
                    className={dimensionHeatmapTooltipStyles.spaceBottom}
                    xs={12}
                >
                    <table>
                        <thead>
                            <tr>
                                <th
                                    className={
                                        dimensionHeatmapTooltipStyles.tableCell
                                    }
                                />
                                <th
                                    className={
                                        dimensionHeatmapTooltipStyles.tableCell
                                    }
                                >
                                    {t("label.metric-value")}{" "}
                                    <small
                                        className={
                                            dimensionHeatmapTooltipStyles.smallText
                                        }
                                    >
                                        (of total)
                                    </small>
                                </th>
                                <th
                                    className={
                                        dimensionHeatmapTooltipStyles.tableCell
                                    }
                                >
                                    {t("label.%-contribution")}
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <th
                                    className={
                                        dimensionHeatmapTooltipStyles.tableCellLabel
                                    }
                                >
                                    {t("label.current")}
                                </th>
                                <td
                                    className={
                                        dimensionHeatmapTooltipStyles.tableCellData
                                    }
                                >
                                    {formatLargeNumber(props.extraData.current)}
                                    <small
                                        className={
                                            dimensionHeatmapTooltipStyles.smallText
                                        }
                                    >
                                        {" "}
                                        (of{" "}
                                        {formatLargeNumber(
                                            props.extraData.currentTotalCount
                                        )}
                                        )
                                    </small>
                                </td>
                                <td
                                    className={
                                        dimensionHeatmapTooltipStyles.tableCellData
                                    }
                                >
                                    {formatLargeNumber(
                                        props.extraData.currentPercentage * 100
                                    )}
                                    %
                                </td>
                            </tr>
                            <tr>
                                <th
                                    className={
                                        dimensionHeatmapTooltipStyles.tableCellLabel
                                    }
                                >
                                    {t("label.comparison")}
                                </th>
                                <td
                                    className={
                                        dimensionHeatmapTooltipStyles.tableCellData
                                    }
                                >
                                    {formatLargeNumber(
                                        props.extraData.comparison
                                    )}
                                    <small
                                        className={
                                            dimensionHeatmapTooltipStyles.smallText
                                        }
                                    >
                                        {" "}
                                        (of{" "}
                                        {formatLargeNumber(
                                            props.extraData.comparisonTotalCount
                                        )}
                                        )
                                    </small>
                                </td>
                                <td
                                    className={
                                        dimensionHeatmapTooltipStyles.tableCellData
                                    }
                                >
                                    {formatLargeNumber(
                                        props.extraData.comparisonPercentage *
                                            100
                                    )}
                                    %
                                </td>
                            </tr>
                            <tr>
                                <th
                                    className={
                                        dimensionHeatmapTooltipStyles.tableCellLabel
                                    }
                                >
                                    {t("label.difference")}
                                </th>
                                <td
                                    className={
                                        dimensionHeatmapTooltipStyles.tableCellData
                                    }
                                />
                                <td
                                    className={
                                        dimensionHeatmapTooltipStyles.tableCellData
                                    }
                                >
                                    {formatLargeNumber(
                                        props.extraData.percentageDiff * 100
                                    )}
                                    %
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </Grid>

                <SafariMuiGridFix />
            </Grid>
        </>
    );
};
