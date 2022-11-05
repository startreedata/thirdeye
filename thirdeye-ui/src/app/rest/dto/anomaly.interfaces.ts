/*
 * Copyright 2022 StarTree Inc
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
import { Alert } from "./alert.interfaces";
import { Metric } from "./metric.interfaces";

export interface Anomaly {
    id: number;
    startTime: number;
    endTime: number;
    avgCurrentVal: number;
    avgBaselineVal: number;
    score: number;
    weight: number;
    impactToGlobal: number;
    sourceType: AnomalyResultSource;
    created: number;
    notified: boolean;
    message: string;
    alert: Alert;
    metric: Metric;
    children: Anomaly[];
    type: AnomalyType;
    severity: AnomalySeverity;
    child: boolean;
    feedback?: AnomalyFeedback;
    metadata: AnomalyMetadata;
    enumerationItem?: {
        id: number;
    };
    anomalyLabels?: {
        name: string;
        sourcePostProcessor: string;
        sourceNodeName: string;
        ignore: boolean;
    }[];
}

export interface AnomalyFeedback {
    id?: number;
    type: AnomalyFeedbackType;
    comment: string;
}

export enum AnomalyType {
    DEVIATION = "DEVIATION",
    TREND_CHANGE = "TREND_CHANGE",
    DATA_SLA = "DATA_SLA",
}

export enum AnomalySeverity {
    CRITICAL = "CRITICAL",
    HIGH = "HIGH",
    MEDIUM = "MEDIUM",
    LOW = "LOW",
    DEFAULT = "DEFAULT",
}

export enum AnomalyResultSource {
    DEFAULT_ANOMALY_DETECTION = "DEFAULT_ANOMALY_DETECTION",
    ANOMALY_REPLAY = "ANOMALY_REPLAY",
    USER_LABELED_ANOMALY = "USER_LABELED_ANOMALY",
}

export enum AnomalyFeedbackType {
    ANOMALY = "ANOMALY",
    ANOMALY_EXPECTED = "ANOMALY_EXPECTED",
    NOT_ANOMALY = "NOT_ANOMALY",
    ANOMALY_NEW_TREND = "ANOMALY_NEW_TREND",
    NO_FEEDBACK = "NO_FEEDBACK",
}

export interface AnomalyMetadata {
    dataset?: { name: string };
    metric?: { name: string };
}
