import { HierarchyNode, HierarchyRectangularNode } from "d3-hierarchy";
import { ScaleLinear } from "d3-scale";
import React, { FunctionComponent, MouseEvent } from "react";

export interface TreemapProps<Data> {
    name: string;
    shouldTruncateText?: boolean;
    treemapData: TreemapData<Data>[];
    onDimensionClickHandler?: (
        treeMapNode: HierarchyNode<TreemapData<Data>>
    ) => void;
    height?: number;
    tooltipElement?: React.FunctionComponent<TreemapData<Data>>;
    colorChangeValueAccessor?: (node: TreemapData<Data>) => number;
}

export interface TreemapData<Data> {
    id: string;
    parent: string | null;
    size: number;
    label: string;
    extraData?: Data;
}

export type TreemapPropsInternal<Data> = {
    width: number;
    height: number;
    tooltipElement: FunctionComponent<TreemapData<Data>>;
} & TreemapProps<Data>;

export type TreemapRectProps<Data> = {
    colorChangeValueAccessor: (node: TreemapData<Data>) => number;
    shouldTruncateText?: boolean;
    onDimensionClickHandler?: (
        treeMapNode: HierarchyNode<TreemapData<Data>>
    ) => void;
    node: HierarchyRectangularNode<HierarchyNode<TreemapData<Data>>>;
    colorScale: ScaleLinear<string, string, never>;
    onMouseMove: (
        event: MouseEvent<SVGGElement>,
        node:
            | HierarchyRectangularNode<HierarchyNode<TreemapData<Data>>>
            | undefined
    ) => void;
    onMouseLeave: () => void;
    margin: {
        left: number;
        right: number;
        top: number;
        bottom: number;
    };
};
