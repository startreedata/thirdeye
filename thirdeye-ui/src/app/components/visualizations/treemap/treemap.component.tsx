import { Grid, Typography, useTheme } from "@material-ui/core";
import {
    hierarchy,
    Treemap as VisxTreemap,
    treemapSquarify,
} from "@visx/hierarchy";
import {
    Group,
    ParentSize,
    scaleLinear,
    stratify,
    Text,
    useTooltip,
} from "@visx/visx";
import { HierarchyNode, HierarchyRectangularNode } from "d3-hierarchy";
import React, { FunctionComponent, MouseEvent } from "react";
import { getShortText } from "../../../utils/anomalies/anomalies.util";
import { DimensionHeatmapTooltip } from "../dimensions-heatmap/dimensions-heatmap.component";
import { DimensionHeatmapTooltipPoint } from "../dimensions-heatmap/dimentions-heatmap.interfaces";
import { TooltipWithBounds } from "../tooltip-with-bounds/tooltip-with-bounds.component";
import {
    TreemapData,
    TreemapProps,
    TreemapPropsInternal,
} from "./treemap.interfaces";
import { useTreemapStyles } from "./treemap.styles";

const RIGHT_BOUNDS_PADDING = 30;
const margin = {
    left: 10,
    top: 10,
    right: 0,
    bottom: 0,
};

export const Treemap: FunctionComponent<TreemapProps> = (
    props: TreemapProps
) => {
    const {
        tooltipTop,
        tooltipLeft,
        tooltipData,
        showTooltip,
        hideTooltip,
    } = useTooltip<DimensionHeatmapTooltipPoint>();

    return (
        <TooltipWithBounds
            left={tooltipLeft}
            open={Boolean(tooltipData)}
            title={
                <DimensionHeatmapTooltip
                    dimensionHeatmapTooltipPoint={tooltipData}
                />
            }
            top={tooltipTop}
        >
            <Grid container alignItems="center">
                <Grid item sm={2}>
                    <Typography variant="subtitle1">{props.name}</Typography>
                </Grid>
                <Grid item sm={10}>
                    <ParentSize>
                        {({ width }) => (
                            <TreemapInternal
                                {...props}
                                height={60}
                                hideTooltip={hideTooltip}
                                showTooltip={showTooltip}
                                width={width}
                            />
                        )}
                    </ParentSize>
                </Grid>
            </Grid>
        </TooltipWithBounds>
    );
};

const TreemapInternal: FunctionComponent<TreemapPropsInternal> = ({
    width,
    height,
    ...props
}: TreemapPropsInternal) => {
    const xMax = Math.abs(width) - margin.left - margin.right;
    const yMax = Math.abs(height) - margin.top - margin.bottom;
    const treemapClasses = useTreemapStyles();
    const theme = useTheme();

    const data = stratify<TreemapData>()
        .id((d) => d.id)
        .parentId((d) => d.parent)(props.treemapData)
        .sum((d) => Math.abs(d.size) || 0);

    const colorScale = scaleLinear<string>({
        domain: [0, Math.max(...props.treemapData.map((d) => d.size || 0))],
        range: [theme.palette.error.light, theme.palette.success.light],
    });

    const root = hierarchy(data).sort(
        (a, b) => (b.value || 0) - (a.value || 0)
    );

    const handleMouseLeave = (): void => {
        props.hideTooltip();
    };

    const handleMouseMove = (
        event: MouseEvent<SVGGElement>,
        node: HierarchyRectangularNode<HierarchyNode<TreemapData>> | undefined
    ): void => {
        const clickedOnRect = event.target as SVGGElement;

        if (!node || !clickedOnRect.ownerSVGElement) {
            return;
        }

        const rightBound =
            clickedOnRect.ownerSVGElement.width.baseVal.value -
            RIGHT_BOUNDS_PADDING;

        props.showTooltip({
            tooltipLeft: Math.min(rightBound, event.nativeEvent.offsetX),
            tooltipTop: event.nativeEvent.offsetY,
            tooltipData: {
                name: node.data.id || "",
                baseline: 100,
                current: 100,
                change: 100,
            },
        });
    };

    const handleMouseClick = (
        node: HierarchyRectangularNode<HierarchyNode<TreemapData>> | undefined
    ): void => {
        if (!node || !node.data.id) {
            return;
        }
        props.onDimensionClickHandler &&
            props.onDimensionClickHandler(node.data.id);
    };

    return (
        <svg height={height} width={width}>
            <VisxTreemap<typeof data>
                round
                root={root}
                size={[xMax, yMax]}
                tile={treemapSquarify}
                top={margin.top}
            >
                {(treemap) => (
                    <Group>
                        {treemap
                            .descendants()
                            .reverse()
                            .map((node, i) => {
                                const nodeWidth = node.x1 - node.x0 - 1;
                                const nodeHeight = node.y1 - node.y0 - 1;
                                const rect = (
                                    <rect
                                        fill={colorScale(node.value || 0)}
                                        height={Math.max(
                                            node.y1 - node.y0 - 1,
                                            0
                                        )}
                                        width={Math.max(
                                            node.x1 - node.x0 - 1,
                                            0
                                        )}
                                    />
                                );

                                return (
                                    <Group
                                        className={
                                            props.onDimensionClickHandler
                                                ? treemapClasses.clickable
                                                : undefined
                                        }
                                        key={`node-${i}`}
                                        left={node.x0 + margin.left}
                                        top={node.y0 + margin.top}
                                        onClick={() => handleMouseClick(node)}
                                        onMouseLeave={handleMouseLeave}
                                        onMouseMove={(event) =>
                                            handleMouseMove(event, node)
                                        }
                                    >
                                        {node.depth === 1 && (
                                            <>
                                                {rect}
                                                <Text
                                                    className={
                                                        treemapClasses.heading
                                                    }
                                                    textAnchor="middle"
                                                    verticalAnchor="middle"
                                                    x={nodeWidth / 2}
                                                    y={nodeHeight / 2}
                                                >
                                                    {getShortText(
                                                        node.data.id || "",
                                                        nodeWidth,
                                                        nodeHeight
                                                    )}
                                                </Text>
                                            </>
                                        )}
                                    </Group>
                                );
                            })}
                    </Group>
                )}
            </VisxTreemap>
        </svg>
    );
};
