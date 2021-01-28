import { AlertEvaluation } from "../../../rest/dto/alert.interfaces";

export interface AlertEvaluationTimeSeriesCardProps {
    title?: string;
    showRefreshButton?: boolean;
    alertEvaluation: AlertEvaluation | null;
    onRefresh?: () => void;
}
