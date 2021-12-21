import { TreemapData } from "../../components/visualizations/treemap/treemap.interfaces";

export interface AnomaliesViewPageParams {
    id: string;
}

export interface UiAnomalyBreakdown {
    label: string;
    treeMapData: TreemapData[];
}

export enum AnomalyBreakdownAPIOffsetValues {
    CURRENT = "current",
    ONE_WEEK_AGO = "wo1w",
    TWO_WEEKS_AGO = "wo2w",
    THREE_WEEKS_AGO = "wo3w",
}
