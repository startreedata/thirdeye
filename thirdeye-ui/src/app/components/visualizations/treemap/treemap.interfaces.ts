export interface TreemapProps {
    showTooltip?: boolean;
    name: string;
    treemapData: TreemapData[];
}

export interface TreemapData {
    id: string;
    parent: string | null;
    size: number;
}

export type TreemapPropsInternal = {
    width: number;
    height: number;
} & TreemapProps;
