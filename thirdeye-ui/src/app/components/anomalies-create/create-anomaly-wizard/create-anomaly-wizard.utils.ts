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

import { isNumber } from "lodash";
import { EditableAnomaly } from "../../../pages/anomalies-create-page/anomalies-create-page.interfaces";
import {
    Alert,
    AlertEvaluation,
    EnumerationItemConfig,
} from "../../../rest/dto/alert.interfaces";
import {
    AnomalyResultSource,
    AnomalySeverity,
    AnomalyType,
} from "../../../rest/dto/anomaly.interfaces";
import { Metric } from "../../../rest/dto/metric.interfaces";
import { extractDetectionEvaluation } from "../../../utils/alerts/alerts.util";

export const getEnumerationItemsConfigFromAlert = (
    alert: Alert
): EnumerationItemConfig[] | null => {
    if (
        alert?.templateProperties?.enumerationItems &&
        (alert?.templateProperties?.enumerationItems as EnumerationItemConfig[])
            .length > 0
    ) {
        return alert?.templateProperties
            ?.enumerationItems as EnumerationItemConfig[];
    }

    return null;
};

export const AlertId = "alertId";

export const getIsAnomalyValid = (
    editableAnomaly?: EditableAnomaly | null
): boolean => {
    if (!editableAnomaly) {
        return false;
    }

    const { alert, startTime, endTime } = editableAnomaly;

    // Basic sanity checks for values
    const conditions = [
        isNumber(alert?.id),
        isNumber(startTime),
        isNumber(endTime),
        startTime > 0,
        endTime > 0,
        endTime > startTime,
    ];

    // The anomaly is valid iff all check are valid
    return conditions.every((c) => !!c);
};

export const createEditableAnomaly = ({
    alert,
    enumerationItemId,
    startTime,
    endTime,
    sourceType = AnomalyResultSource.USER_LABELED_ANOMALY,
    severity,
    type,

    // If these values are absent, the anomaly chart component tends to crash
    avgBaselineVal = 0,
    avgCurrentVal = 0,
}: {
    alert: Alert;
    enumerationItemId?: number;
    startTime: number;
    endTime: number;
    sourceType?: AnomalyResultSource;
    severity?: AnomalySeverity;
    type?: AnomalyType;

    avgBaselineVal?: number;
    avgCurrentVal?: number;
}): EditableAnomaly => {
    const { dataset: datasetName, aggregationColumn: metricName } =
        alert.templateProperties as {
            dataset: string;
            aggregationColumn: string;
        };

    const editableAnomaly: EditableAnomaly = {
        alert,
        ...(enumerationItemId && {
            enumerationItem: { id: enumerationItemId },
        }),
        startTime,
        endTime,
        sourceType,
        metric: {
            name: metricName,
        } as Metric,
        metadata: {
            metric: {
                name: metricName,
            },
            dataset: {
                name: datasetName,
            },
        },
        ...(severity && { severity }),
        ...(type && { type }),

        // TODO: Needed?
        score: 0.0,
        weight: 0.0,
        impactToGlobal: 0.0,

        avgBaselineVal,
        avgCurrentVal,
    };

    return editableAnomaly;
};

export const getAnomaliesAvgValues = ({
    evaluation,
    startTime,
}: {
    evaluation: AlertEvaluation;
    startTime: number;
}): { avgBaselineVal: number; avgCurrentVal: number } | null => {
    const detectionEvaluation = extractDetectionEvaluation(evaluation)[0];

    if (evaluation) {
        const { timestamp, current, expected } = detectionEvaluation?.data;
        const anomalyStartIndex = timestamp.findIndex((v) => v >= startTime);

        if (anomalyStartIndex) {
            // TODO: Should the avg of all the values in the range be used?
            return {
                avgBaselineVal: current?.[anomalyStartIndex] || 0,
                avgCurrentVal: expected?.[anomalyStartIndex] || 0,
            };
        }
    }

    return null;
};
