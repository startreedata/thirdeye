/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { AnomalyFeedbackType } from "./anomaly.interfaces";
import { DetectionEvaluation } from "./detection.interfaces";
import { AnalysisRunInfo } from "./rca.interfaces";
import { User } from "./user.interfaces";

export type PropertyConfigValueTypes =
    | string
    | number
    | string[] // Array
    | number[] // Array
    | Record<string, unknown> // Object
    | boolean
    | null
    | EnumerationItemConfig[];

export interface TemplatePropertiesObject {
    [index: string]: PropertyConfigValueTypes;
}

export interface EditableAlert {
    id?: number;
    name: string;
    description: string;
    cron: string;
    template?: {
        nodes?: Array<
            | AlertDataFetcherNode
            | AlertTimeIndexFillerNode
            | AlertAnomalyDetectorNode
        >;
        metadata?: { [index: string]: string | { [index: string]: string } };
        name?: string;
    };
    templateProperties: TemplatePropertiesObject;
    lastTimestamp?: number;
    active?: boolean;
    owner?: User;
}

export interface Alert extends EditableAlert {
    id: number;
    created: number;
    updated: number;
}

export interface EvaluatedTemplateMetadata {
    [index: string]: string | { [index: string]: string };
    granularity: string;
    timezone: string;
}

export interface AlertInsight {
    analysisRunInfo: AnalysisRunInfo;
    datasetStartTime: number;
    datasetEndTime: number;
    defaultStartTime: number;
    defaultEndTime: number;
    templateWithProperties: {
        metadata: EvaluatedTemplateMetadata;
    };
    /**
     * Added in https://startree.atlassian.net/browse/TE-1964
     *
     * The field may be null or missing if the backend was not able to infer a good cron
     */
    defaultCron?: string;
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

export interface AlertInEvaluation extends EditableAlert {
    id?: number;
    template: {
        metadata: EvaluatedTemplateMetadata;
        name: string;
    };
}

export interface AlertEvaluation {
    alert: AlertInEvaluation;
    detectionEvaluations: { [index: string]: DetectionEvaluation };
    start: number;
    end: number;
    lastTimestamp: number;
}

export interface EnumerationItemConfig {
    name?: string;
    params: {
        queryFilters: string;
    };
}

export interface AlertStats {
    totalCount: number;
    countWithFeedback: number;
    feedbackStats: Record<keyof typeof AnomalyFeedbackType, number>;
}
