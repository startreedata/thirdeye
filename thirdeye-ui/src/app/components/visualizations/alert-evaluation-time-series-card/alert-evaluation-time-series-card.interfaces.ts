import { AlertEvaluation } from "../../../rest/dto/alert.interfaces";
import { VisualizationCardCommonProps } from "../visualization-card/visualization-card.interfaces";

export interface AlertEvaluationTimeSeriesCardProps
    extends VisualizationCardCommonProps {
    alertEvaluation: AlertEvaluation | null;
}
