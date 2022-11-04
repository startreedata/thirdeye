import React from "react";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { DetectionEvaluation } from "../../../rest/dto/detection.interfaces";

export interface EnumerationItemMergerProps {
    anomalies: Anomaly[];
    detectionEvaluations: DetectionEvaluation[];
    children: (
        detectionEvaluations: DetectionEvaluationForRender[]
    ) => React.ReactElement;
}

export interface DetectionEvaluationForRender extends DetectionEvaluation {
    enumerationId?: number;
    firstAnomalyTs: number;
    lastAnomalyTs: number;
}
