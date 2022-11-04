import { Anomaly } from "../../../../rest/dto/anomaly.interfaces";
import { DetectionEvaluationForRender } from "../../enumeration-item-merger/enumeration-item-merger.interfaces";

export interface EnumerationItemRowProps {
    alertId: number;
    detectionEvaluation: DetectionEvaluationForRender;
    anomalies: Anomaly[];
    expanded: string[];
    onExpandChange: (isOpen: boolean, name: string) => void;
}
