export interface MetricTileProps {
    metricValue?: string;
    metricValueClassName?: string;
    metricName?: string;
    metricNameClassName?: string;
    clickable?: boolean;
    compact?: boolean;
    onClick?: () => void;
}
