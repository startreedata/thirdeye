import React, { FunctionComponent } from "react";
import { AnomalyFilterOption } from "../../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.interfaces";

export interface TreemapProps<Data> {
    name: string;
    treemapData: TreemapData<Data>[];
    onDimensionClickHandler?: (treeMapNode: AnomalyFilterOption) => void;
    height?: number;
    tooltipElement?: React.FunctionComponent<TreemapData<Data>>;
    colorChangeValueAccessor?: (node: TreemapData<Data>) => number;
}

export interface TreemapData<Data> {
    id: string;
    parent: string | null;
    size: number;
    extraData?: Data;
}

export type TreemapPropsInternal<Data> = {
    width: number;
    height: number;
    tooltipElement: FunctionComponent<TreemapData<Data>>;
} & TreemapProps<Data>;
