import { MetricCardData } from "../entity-cards/metric-card/metric-card.interfaces";

export interface MetricsListProps {
    metricCardDatas: MetricCardData[] | null;
    onDelete?: (metricsCardData: MetricCardData) => void;
}
