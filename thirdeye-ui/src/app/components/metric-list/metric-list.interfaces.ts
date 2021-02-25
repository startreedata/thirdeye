import { MetricCardData } from "../entity-cards/metric-card/metric-card.interfaces";

export interface MetricListProps {
    hideSearchBar?: boolean;
    metricCardDatas: MetricCardData[] | null;
    onDelete?: (metricsCardData: MetricCardData) => void;
}
