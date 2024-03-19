/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import Box from "@material-ui/core/Box";
import Divider from "@material-ui/core/Divider";
import Typography from "@material-ui/core/Typography";
import SortByAlphaIcon from "@material-ui/icons/SortByAlpha";
import BarChartIcon from "@material-ui/icons/BarChart";
import { HierarchyNode } from "d3-hierarchy";
import React, { FunctionComponent, useEffect, useState } from "react";
import { Treemap } from "../../visualizations/treemap/treemap.component";
import { TreemapData } from "../../visualizations/treemap/treemap.interfaces";
import { DimensionHeatmapTooltip } from "./heat-map-tooltip/dimension-heatmap-tooltip.component";
import {
    AnomalyBreakdownComparisonData,
    AnomalyBreakdownComparisonDataByDimensionColumn,
    AnomalyFilterOption,
    DimensionDisplayData,
    HeatMapProps,
} from "./heat-map.interfaces";
import {
    SortOrder,
    formatComparisonData,
    formatTreemapData,
} from "./heat-map.utils";
import { ToggleButton, ToggleButtonGroup } from "@material-ui/lab";
import { useTranslation } from "react-i18next";

export const HeatMap: FunctionComponent<HeatMapProps> = ({
    heatMapData,
    anomalyFilters,
    dimensionsInOrder,
    onFilterChange,
    onHeightChange,
}) => {
    const { t } = useTranslation();

    const [breakdownComparisonData, setBreakdownComparisonData] = useState<
        AnomalyBreakdownComparisonDataByDimensionColumn[] | null
    >(null);

    const [sortOrder, setSortOrder] = useState<SortOrder>(
        SortOrder.Contribution
    );

    useEffect(() => {
        if (!heatMapData) {
            setBreakdownComparisonData(null);

            return;
        }

        setBreakdownComparisonData(
            formatComparisonData(
                heatMapData,
                sortOrder === SortOrder.Contribution
                    ? dimensionsInOrder
                    : undefined
            )
        );
    }, [heatMapData, dimensionsInOrder, sortOrder]);

    const handleNodeClick = (
        tileData: HierarchyNode<TreemapData<AnomalyBreakdownComparisonData>>,
        dimensionColumn: string
    ): void => {
        if (!tileData) {
            return;
        }

        const resultantFilters: AnomalyFilterOption[] = [
            ...anomalyFilters,
            {
                key: dimensionColumn,
                value: tileData.data.id,
            },
        ];
        onFilterChange([...resultantFilters]);
    };

    const colorChangeValueAccessor = (
        node: TreemapData<AnomalyBreakdownComparisonData>
    ): number => {
        if (node.extraData) {
            return node.extraData.contributionDiff * 100;
        }

        return node.size;
    };

    const handleHeightChange = (height: number): void => {
        onHeightChange((height + 10) * (breakdownComparisonData?.length || 1));
    };

    return (
        <>
            <Box pt={2}>
                <Divider />
                <Box
                    alignItems="center"
                    bgcolor="grey.100"
                    display="flex"
                    justifyContent="space-between"
                    px={1}
                >
                    <Typography variant="h6">
                        {t("label.heatmap-of-dimension-distribution")}
                    </Typography>
                    <Box flexGrow={1} />
                    <Typography variant="body2">
                        {t("label.sort-by")}:
                    </Typography>
                    <Box mr={1} />
                    <ToggleButtonGroup
                        exclusive
                        size="small"
                        style={{
                            backgroundColor: "white",
                        }}
                        value={sortOrder}
                        onChange={(_e, newVal) =>
                            newVal && setSortOrder(newVal)
                        }
                    >
                        <ToggleButton
                            size="small"
                            value={SortOrder.Alphabetical}
                        >
                            <SortByAlphaIcon
                                color={
                                    sortOrder === SortOrder.Alphabetical
                                        ? "primary"
                                        : "inherit"
                                }
                                fontSize="small"
                            />
                            <Box mr={1} />
                            <Typography
                                color={
                                    sortOrder === SortOrder.Alphabetical
                                        ? "primary"
                                        : "initial"
                                }
                                variant="body2"
                            >
                                {SortOrder.Alphabetical}
                            </Typography>
                        </ToggleButton>
                        <ToggleButton
                            size="small"
                            value={SortOrder.Contribution}
                        >
                            <BarChartIcon
                                color={
                                    sortOrder === SortOrder.Contribution
                                        ? "primary"
                                        : "inherit"
                                }
                                fontSize="small"
                            />
                            <Box mr={1} />
                            <Typography
                                color={
                                    sortOrder === SortOrder.Contribution
                                        ? "primary"
                                        : "initial"
                                }
                                variant="body2"
                            >
                                {SortOrder.Contribution}
                            </Typography>
                        </ToggleButton>
                    </ToggleButtonGroup>
                </Box>
                {React.Children.toArray(
                    breakdownComparisonData &&
                        breakdownComparisonData.map((data) => (
                            <>
                                <Divider />
                                <Box px={1}>
                                    <Treemap<
                                        AnomalyBreakdownComparisonData &
                                            DimensionDisplayData
                                    >
                                        colorChangeValueAccessor={
                                            colorChangeValueAccessor
                                        }
                                        name={data.column}
                                        tooltipElement={DimensionHeatmapTooltip}
                                        treemapData={formatTreemapData(
                                            data,
                                            data.column
                                        )}
                                        onDimensionClickHandler={(node) =>
                                            handleNodeClick(node, data.column)
                                        }
                                        onHeightChange={handleHeightChange}
                                    />
                                </Box>
                            </>
                        ))
                )}
            </Box>
        </>
    );
};
