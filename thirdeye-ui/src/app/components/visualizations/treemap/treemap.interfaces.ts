export interface TreemapProps {
    name: string;
    treemapData: TreemapData[];
    onDimensionClickHandler?: (treeMapNodeId: string) => void;
}

export interface TreemapData {
    id: string;
    parent: string | null;
    size: number;
}

type ShowTooltipParams = {
    tooltipLeft: number;
    tooltipTop: number;
    tooltipData: {
        name: string;
        baseline: number;
        current: number;
        change: number;
    };
};

export type TreemapPropsInternal = {
    width: number;
    height: number;
    showTooltip: (params: ShowTooltipParams) => void;
    hideTooltip: () => void;
} & TreemapProps;
