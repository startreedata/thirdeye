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
import { Box, Divider } from "@material-ui/core";
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
    formatComparisonData,
    formatDimensionOptions,
    formatTreemapData,
} from "./heat-map.utils";

export const HeatMap: FunctionComponent<HeatMapProps> = ({
    heatMapData,
    anomalyFilters,
    onFilterChange,
    onHeightChange,
}) => {
    const [breakdownComparisonData, setBreakdownComparisonData] = useState<
        AnomalyBreakdownComparisonDataByDimensionColumn[] | null
    >(null);
    const [anomalyFilterOptions, setAnomalyFilterOptions] = useState<
        AnomalyFilterOption[]
    >([]);

    useEffect(() => {
        if (!heatMapData) {
            setBreakdownComparisonData(null);

            return;
        }

        if (anomalyFilterOptions.length === 0) {
            setAnomalyFilterOptions(formatDimensionOptions(heatMapData));
        }

        setBreakdownComparisonData(formatComparisonData(heatMapData));
    }, [heatMapData]);

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
                {React.Children.toArray(
                    breakdownComparisonData &&
                        breakdownComparisonData.map((data) => (
                            <>
                                <Divider />
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
                            </>
                        ))
                )}
            </Box>
        </>
    );
};
