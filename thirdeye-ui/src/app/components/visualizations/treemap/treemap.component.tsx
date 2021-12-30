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
import React, { MouseEvent } from "react";
import { getShortText } from "../../../utils/anomalies/anomalies.util";
import { TooltipWithBounds } from "../tooltip-with-bounds/tooltip-with-bounds.component";
import { GenericTreemapTooltip } from "./generic-treemap-tooltip";
import {
    TreemapData,
    TreemapProps,
    TreemapPropsInternal,
} from "./treemap.interfaces";
import { useTreemapStyles } from "./treemap.styles";

const DEFAULT_TREEMAP_HEIGHT = 60;
const RIGHT_BOUNDS_PADDING = 30;
const margin = {
    left: 10,
    top: 10,
    right: 0,
    bottom: 0,
};

function Treemap<Data>({
    height = DEFAULT_TREEMAP_HEIGHT,
    tooltipElement = GenericTreemapTooltip,
    ...props
}: TreemapProps<Data>): JSX.Element {
    const {
        tooltipTop,
        tooltipLeft,
        tooltipData,
        showTooltip,
        hideTooltip,
    } = useTooltip<TreemapData<Data>>();

    return (
        <TooltipWithBounds
            left={tooltipLeft}
            open={Boolean(tooltipData)}
            title={React.createElement<TreemapData<Data>>(
                tooltipElement,
                tooltipData
            )}
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
                                height={height}
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
}

function TreemapInternal<Data>({
    width,
    height,
    ...props
}: TreemapPropsInternal<Data>): JSX.Element {
    const xMax = Math.abs(width) - margin.left - margin.right;
    const yMax = Math.abs(height) - margin.top - margin.bottom;
    const treemapClasses = useTreemapStyles();
    const theme = useTheme();

    const data: HierarchyNode<TreemapData<Data>> = stratify<TreemapData<Data>>()
        .id((d) => d.id)
        .parentId((d) => d.parent)(props.treemapData)
        .sum((d) => Math.abs(d.size) || 0);

    const colorScale = scaleLinear<string>({
        domain: [0, Math.max(...props.treemapData.map((d) => d.size || 0))],
        range: [theme.palette.success.light, theme.palette.error.light],
    });

    const root = hierarchy(data).sort(
        (a, b) => (b.value || 0) - (a.value || 0)
    );

    const handleMouseLeave = (): void => {
        props.hideTooltip();
    };

    const handleMouseMove = (
        event: MouseEvent<SVGGElement>,
        node:
            | HierarchyRectangularNode<HierarchyNode<TreemapData<Data>>>
            | undefined
    ): void => {
        const clickedOnRect = event.target as SVGGElement;

        if (
            !node ||
            !clickedOnRect.ownerSVGElement ||
            !node.data.data.extraData
        ) {
            return;
        }

        const rightBound =
            clickedOnRect.ownerSVGElement.width.baseVal.value -
            RIGHT_BOUNDS_PADDING;

        props.showTooltip({
            tooltipLeft: Math.min(rightBound, event.nativeEvent.offsetX),
            tooltipTop: event.nativeEvent.offsetY,
            tooltipData: node.data.data,
        });
    };

    const handleMouseClick = (
        node:
            | HierarchyRectangularNode<HierarchyNode<TreemapData<Data>>>
            | undefined
    ): void => {
        if (
            !node ||
            (node.data.id && node?.data?.id.toLowerCase() === "other")
        ) {
            return;
        }
        let key = "";
        if (node.data.data.parent) {
            key = node.data.data.parent.toString();
        }
        props.onDimensionClickHandler &&
            props.onDimensionClickHandler({
                key,
                value: node.data.id || "",
            });
    };

    return (
        <svg height={height} width={width}>
            <VisxTreemap<HierarchyNode<TreemapData<Data>>>
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
}

export { Treemap };
