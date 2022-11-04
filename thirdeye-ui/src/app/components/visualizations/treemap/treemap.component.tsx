import { Grid, Typography, useTheme } from "@material-ui/core";
import { Group } from "@visx/group";
import {
    hierarchy,
    stratify,
    Treemap as VisxTreemap,
    treemapSquarify,
} from "@visx/hierarchy";
import { ParentSize } from "@visx/responsive";
import { scaleLinear } from "@visx/scale";
import { useTooltip } from "@visx/tooltip";
import { HierarchyNode, HierarchyRectangularNode } from "d3-hierarchy";
import React, { MouseEvent } from "react";
import { TooltipV1 } from "../../../platform/components";
import { checkIfOtherDimension } from "../../../utils/visualization/visualization.util";
import { TooltipWithBounds } from "../tooltip-with-bounds/tooltip-with-bounds.component";
import { GenericTreemapTooltip } from "./generic-treemap-tooltip";
import { TreemapRect } from "./treemap-rect.component";
import {
    TreemapData,
    TreemapProps,
    TreemapPropsInternal,
} from "./treemap.interfaces";
import { useTreemapStyles } from "./treemap.styles";

// #TODO move to a constants file
const RIGHT_BOUNDS_PADDING = 30;
const DEFAULT_TREEMAP_HEIGHT = 60;
const margin = {
    left: 10,
    top: 10,
    right: 0,
    bottom: 0,
};
const GRAY = "#EEEEEE";
const PURPLE = "#5B6AEC";

function Treemap<Data>({
    height = DEFAULT_TREEMAP_HEIGHT,
    tooltipElement = GenericTreemapTooltip,
    ...props
}: TreemapProps<Data>): JSX.Element {
    const styles = useTreemapStyles();

    return (
        <Grid container alignItems="center">
            <Grid item sm={2}>
                <TooltipV1 placement="top" title={props.name}>
                    <Typography
                        className={styles.textOverflowEllipses}
                        variant="subtitle1"
                    >
                        {props.name}
                    </Typography>
                </TooltipV1>
            </Grid>
            <Grid item sm={10}>
                <ParentSize>
                    {({ width }) => (
                        <TreemapInternal
                            {...props}
                            height={height}
                            tooltipElement={tooltipElement}
                            width={width}
                        />
                    )}
                </ParentSize>
            </Grid>
        </Grid>
    );
}

function TreemapInternal<Data>({
    width,
    height,
    colorChangeValueAccessor = (d) => d.size,
    ...props
}: TreemapPropsInternal<Data>): JSX.Element {
    const xMax = Math.abs(width) - margin.left - margin.right;
    const yMax = Math.abs(height) - margin.top - margin.bottom;
    const theme = useTheme();

    const { tooltipTop, tooltipLeft, showTooltip, tooltipData, hideTooltip } =
        useTooltip<TreemapData<Data>>();

    const data: HierarchyNode<TreemapData<Data>> = stratify<TreemapData<Data>>()
        .id((d) => d.id)
        .parentId((d) => d.parent)(props.treemapData)
        .sum((d) => Math.abs(d.size) || 0);

    const colorValues = props.treemapData.map(
        (d) => colorChangeValueAccessor(d) || 0
    );
    const colorScale = scaleLinear<string>({
        domain: [
            Math.min(...colorValues),
            -25,
            0,
            25,
            Math.max(...colorValues),
        ],
        range: [
            theme.palette.error.main,
            theme.palette.error.main,
            GRAY,
            PURPLE,
            PURPLE,
        ],
    });

    const root = hierarchy(data)
        .sort((a, b) => (b.value || 0) - (a.value || 0))
        .sort((_a, b) => (checkIfOtherDimension(b.data.id) ? -1 : 1));

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

        showTooltip({
            tooltipLeft: Math.min(rightBound, event.nativeEvent.offsetX),
            tooltipTop: event.nativeEvent.offsetY,
            tooltipData: node.data.data,
        });
    };

    return (
        <TooltipWithBounds
            left={tooltipLeft}
            open={Boolean(tooltipData)}
            title={React.createElement<TreemapData<Data>>(
                props.tooltipElement,
                tooltipData
            )}
            top={tooltipTop}
        >
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
                                .map((node, i) => (
                                    <TreemapRect<Data>
                                        colorChangeValueAccessor={
                                            colorChangeValueAccessor
                                        }
                                        colorScale={colorScale}
                                        key={`node-${i}`}
                                        margin={margin}
                                        node={node}
                                        shouldTruncateText={
                                            props.shouldTruncateText
                                        }
                                        onDimensionClickHandler={
                                            props.onDimensionClickHandler
                                        }
                                        onMouseLeave={hideTooltip}
                                        onMouseMove={handleMouseMove}
                                    />
                                ))}
                        </Group>
                    )}
                </VisxTreemap>
            </svg>
        </TooltipWithBounds>
    );
}

export { Treemap };
