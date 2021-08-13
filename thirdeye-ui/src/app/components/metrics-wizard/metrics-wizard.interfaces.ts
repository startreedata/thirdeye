import { Dataset } from "../../rest/dto/dataset.interfaces";
import { LogicalMetric } from "../../rest/dto/metric.interfaces";

export interface MetricsWizardProps {
    datasets: Dataset[];
    metric?: LogicalMetric;
    showCancel?: boolean;
    onChange?: (metricsWizardStep: MetricsWizardStep) => void;
    onCancel?: () => void;
    onFinish?: (metric: LogicalMetric) => void;
}

export enum MetricsWizardStep {
    METRIC_PROPERTIES,
    REVIEW_AND_SUBMIT,
}
