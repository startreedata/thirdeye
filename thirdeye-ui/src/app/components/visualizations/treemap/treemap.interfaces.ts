/*
 * Copyright 2022 StarTree Inc
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
