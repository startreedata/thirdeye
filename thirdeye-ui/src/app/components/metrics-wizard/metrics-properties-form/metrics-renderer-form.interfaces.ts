import { Dataset, LogicalMetric } from "../../../rest/dto/metric.interfaces";

export interface MetricsPropertiesFormProps {
    id: string;
    metric?: LogicalMetric;
    datasets: Dataset[];
    onSubmit?: (metric: LogicalMetric) => void;
}
