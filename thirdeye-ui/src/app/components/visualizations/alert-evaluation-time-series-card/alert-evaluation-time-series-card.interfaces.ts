import { AlertEvaluation } from "../../../rest/dto/alert.interfaces";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";

export interface AlertEvaluationTimeSeriesCardProps {
    alertEvaluationTimeSeriesHeight: number;
    alertEvaluationTimeSeriesMaximizedHeight?: number;
    title?: string;
    maximizedTitle?: string;
    alertEvaluation: AlertEvaluation | null;
    error?: boolean;
    helperText?: string;
    hideRefreshButton?: boolean;
    onRefresh?: () => void;
    onAnomalyBarClick?: (anomaly: Anomaly) => void;
}
