import { DetectionEvaluation } from "./detection.interfaces";
import { User } from "./user.interfaces";

export interface EditableAlert {
    name: string;
    description: string;
    cron: string;
    template?: {
        nodes: Array<
            | AlertDataFetcherNode
            | AlertTimeIndexFillerNode
            | AlertAnomalyDetectorNode
        >;
        metadata?: {
            [index: string]: { [index: string]: string };
        };
    };
    templateProperties: {
        [index: string]: string;
    };
    lastTimestamp?: number;
    active?: boolean;
    owner?: User;
}

export interface Alert extends EditableAlert {
    id: number;
}

export interface AlertDataFetcherNode {
    name: string;
    type: string;
    params: {
        "component.dataSource": string;
        "component.query": string;
    };
    // should usually be empty array
    inputs?: [];
    outputs?: AlertNodeOutput[];
}

export interface AlertTimeIndexFillerNode {
    name: string;
    type: string;
    inputs?: {
        sourcePlanNode: string;
        sourceProperty: string;
    }[];
    outputs?: AlertNodeOutput[];
}

export interface AlertAnomalyDetectorNode {
    name: string;
    type: string;
    params: {
        type: string;
        "component.timezone": string;
        "component.monitoringGranularity": string;
        "component.timestamp": string;
        "component.metric": string;
        "component.max": string;
        "component.min": string;
        "anomaly.metric": string;
    };
    inputs?: {
        targetProperty: string;
        sourcePlanNode: string;
        sourceProperty: string;
    }[];
    outputs?: AlertNodeOutput[];
}

export interface AlertNodeOutput {
    outputKey: string;
    outputName: string;
}

export enum AlertNodeType {
    ANOMALY_DETECTOR = "AnomalyDetector",
    TIME_INDEX_FILLER = "TimeIndexFiller",
    DATA_FETCHER = "DataFetcher",
}

export interface AlertEvaluation {
    alert: Alert;
    detectionEvaluations: { [index: string]: DetectionEvaluation };
    start: number;
    end: number;
    lastTimestamp: number;
}
