import React from "react";
import { AnomalyFilterOption } from "../../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.interfaces";

export interface TreemapProps<Data> {
    name: string;
    treemapData: TreemapData<Data>[];
    onDimensionClickHandler?: (treeMapNode: AnomalyFilterOption) => void;
    height?: number;
    tooltipElement?: React.FunctionComponent<TreemapData<Data>>;
    colorChangeFactor?: string;
}

export interface TreemapData<Data> {
    id: string;
    parent: string | null;
    size: number;
    extraData?: Data;
}

type ShowTooltipParams<Data> = {
    tooltipLeft: number;
    tooltipTop: number;
    tooltipData: Data;
};

export type TreemapPropsInternal<Data> = {
    width: number;
    height: number;
    showTooltip: (params: ShowTooltipParams<TreemapData<Data>>) => void;
    hideTooltip: () => void;
} & TreemapProps<Data>;
