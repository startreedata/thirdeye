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
import { useTheme } from "@material-ui/core";
import { Group } from "@visx/group";
import { Text } from "@visx/text";
import { color } from "d3-color";
import { HierarchyNode, HierarchyRectangularNode } from "d3-hierarchy";
import fontColorContrast from "font-color-contrast";
import React from "react";
import { getShortText } from "../../../utils/anomalies/anomalies.util";
import { checkIfOtherDimension } from "../../../utils/visualization/visualization.util";
import { TreemapData, TreemapRectProps } from "./treemap.interfaces";
import { useTreemapStyles } from "./treemap.styles";

function TreemapRect<Data>({
    node,
    colorScale,
    onDimensionClickHandler,
    colorChangeValueAccessor,
    shouldTruncateText,
    onMouseMove,
    onMouseLeave,
    margin,
}: TreemapRectProps<Data>): JSX.Element {
    const treemapClasses = useTreemapStyles();
    const theme = useTheme();

    const handleMouseClick = (
        node:
            | HierarchyRectangularNode<HierarchyNode<TreemapData<Data>>>
            | undefined
    ): void => {
        if (!node || (node.data.id && checkIfOtherDimension(node?.data?.id))) {
            return;
        }

        onDimensionClickHandler &&
            node.data &&
            onDimensionClickHandler(node.data);
    };

    const nodeWidth = Math.max(node.x1 - node.x0 - 1, 0);
    const nodeHeight = Math.max(node.y1 - node.y0 - 1, 0);

    let colorValue = -1;

    const isOtherDimension = checkIfOtherDimension(node.data.id);

    if (!isOtherDimension) {
        if (node.data.data) {
            colorValue = colorChangeValueAccessor(node.data.data);
        } else if (node.value) {
            colorValue = node.value;
        }
    }

    const rectBackgroundColor = colorScale(colorValue);

    let fontLabelColor = theme.palette.text.primary;

    // MUI Palette colors are in RGB while other options are in HEX
    const parsedColor = color(rectBackgroundColor);
    if (parsedColor) {
        fontLabelColor = fontColorContrast(parsedColor.hex(), 0.6);
    }

    return (
        <Group
            className={
                onDimensionClickHandler ? treemapClasses.clickable : undefined
            }
            data-testid={`treemap-group-${node.id}`}
            left={node.x0 + margin.left}
            top={node.y0 + margin.top}
            onClick={() => handleMouseClick(node)}
            onMouseLeave={onMouseLeave}
            onMouseMove={(event) => onMouseMove(event, node)}
        >
            {node.depth === 1 && (
                <>
                    <rect
                        fill={rectBackgroundColor}
                        height={nodeHeight}
                        width={nodeWidth}
                    />
                    <Text
                        className={
                            isOtherDimension
                                ? treemapClasses.headingOtherDimension
                                : treemapClasses.heading
                        }
                        fill={fontLabelColor}
                        textAnchor="start"
                        verticalAnchor="middle"
                        x={10}
                        y={nodeHeight / 2}
                    >
                        {shouldTruncateText
                            ? getShortText(
                                  node.data.data.label,
                                  nodeWidth,
                                  nodeHeight
                              )
                            : node.data.data.label}
                    </Text>
                </>
            )}
        </Group>
    );
}

export { TreemapRect };
