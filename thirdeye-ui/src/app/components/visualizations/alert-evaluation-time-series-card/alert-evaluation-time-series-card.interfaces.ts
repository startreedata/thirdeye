import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { DetectionEvaluation } from "../../../rest/dto/detection.interfaces";

export interface AlertEvaluationTimeSeriesCardProps {
    alertEvaluationTimeSeriesHeight: number;
    detectionEvaluation: DetectionEvaluation | null;
    onRefresh?: (start?: number, end?: number) => void;
    isLoading?: boolean;
    header?: React.ReactElement;
    anomalies: Anomaly[];
    disableNavigation?: boolean;
}

export interface ViewAnomalyHeaderProps {
    anomaly: Anomaly | null;
    onRefresh?: (start?: number, end?: number) => void;
}

export interface CreateAlertHeaderProps {
    onRefresh?: (start?: number, end?: number) => void;
    title: string;
}
