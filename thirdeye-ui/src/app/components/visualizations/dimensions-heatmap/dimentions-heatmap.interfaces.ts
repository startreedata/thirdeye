export interface DimensionHeatmapTooltipProps {
    dimensionHeatmapTooltipPoint?: DimensionHeatmapTooltipPoint;
}

export interface DimensionHeatmapTooltipPoint {
    name: string;
    current: number;
    baseline: number;
    change: number;
}
