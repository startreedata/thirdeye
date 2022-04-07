import { AnomalyFeedback } from "../../rest/dto/anomaly.interfaces";

export interface AnomalyFeedbackProps {
    anomalyId: number;
    anomalyFeedback: AnomalyFeedback;
    className?: string;
}
