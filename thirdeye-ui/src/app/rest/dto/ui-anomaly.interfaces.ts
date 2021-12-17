import { TreemapData } from "../../components/visualizations/treemap/treemap.interfaces";

export interface UiAnomaly {
    id: number;
    name: string;
    alertId: number;
    alertName: string;
    current: string;
    predicted: string;
    deviation: string;
    negativeDeviation: boolean;
    duration: string;
    startTime: string;
    endTime: string;
}

export interface UiAnomalyBreakdown {
    label: string;
    treeMapData: TreemapData[];
}
