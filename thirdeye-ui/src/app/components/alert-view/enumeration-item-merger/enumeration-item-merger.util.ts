// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { isEqual } from "lodash";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { DetectionEvaluation } from "../../../rest/dto/detection.interfaces";
import { EnumerationItem } from "../../../rest/dto/enumeration-item.interfaces";
import { DetectionEvaluationForRender } from "./enumeration-item-merger.interfaces";

const NON_ENUMERATION_ITEM_ID = -1;

export const mergeContent = (
    anomalies: Anomaly[],
    detectionEvaluations: DetectionEvaluation[],
    enumerationItems: EnumerationItem[]
): DetectionEvaluationForRender[] => {
    let notFoundIdCounter = -1;

    // Bucket all anomalies by enumeration item id
    const enumerationIdToAnomalies: { [key: string]: Anomaly[] } = {};
    anomalies.forEach((anomaly) => {
        const bucketName = anomaly.enumerationItem
            ? anomaly.enumerationItem.id
            : NON_ENUMERATION_ITEM_ID;
        const bucket = enumerationIdToAnomalies[bucketName] || [];
        bucket.push(anomaly);
        enumerationIdToAnomalies[bucketName] = bucket;
    });

    /**
     * For each object in detectionEvaluations, find the
     * corresponding enumeration item so we can use the id to find get
     * persisted anomalies for
     */
    const enumerationIdToAnomaliesEvaluations: {
        [key: string]: {
            anomalies: Anomaly[];
            evaluation: DetectionEvaluation;
        };
    } = {};

    detectionEvaluations.forEach((evaluation) => {
        if (evaluation.enumerationItem) {
            const correspondingEnumerationItem = enumerationItems?.find(
                (enumerationItemCandidate) => {
                    return isEqual(
                        enumerationItemCandidate.params,
                        evaluation.enumerationItem?.params
                    );
                }
            );

            if (correspondingEnumerationItem) {
                const enumerationItemId = correspondingEnumerationItem.id;
                enumerationIdToAnomaliesEvaluations[enumerationItemId] = {
                    anomalies:
                        enumerationIdToAnomalies[enumerationItemId] || [],
                    evaluation,
                };
            } else {
                /**
                 * If this part of the code is reached, this means the
                 * corresponding enumeration item was not persisted or
                 * anomalies given did not include this evaluation result
                 *
                 * -2 to -infinity designate evaluations that were in the
                 * result of "/evaluation" but not tied back to an anomaly
                 * or enumeration item not persisted
                 */
                notFoundIdCounter--;
                enumerationIdToAnomaliesEvaluations[
                    notFoundIdCounter.toString()
                ] = {
                    anomalies: [],
                    evaluation,
                };
            }
        } else {
            // -1 will designate the non enumeration item evaluation result
            enumerationIdToAnomaliesEvaluations[NON_ENUMERATION_ITEM_ID] = {
                anomalies:
                    enumerationIdToAnomalies[NON_ENUMERATION_ITEM_ID] || [],
                evaluation,
            };
        }
    });

    // Replace the anomalies from the evaluation with the persisted anomalies
    return Object.entries(enumerationIdToAnomaliesEvaluations).map(
        ([id, { anomalies, evaluation }]) => {
            const clone: DetectionEvaluationForRender = {
                ...evaluation,
                firstAnomalyTs: Number.MIN_SAFE_INTEGER,
                lastAnomalyTs: Number.MAX_SAFE_INTEGER,
            };

            clone.anomalies = anomalies;
            clone.firstAnomalyTs = Math.min(
                ...anomalies.map((a) => a.startTime)
            );
            clone.lastAnomalyTs = Math.max(
                ...anomalies.map((a) => a.startTime)
            );

            if (Number(id) > 0) {
                clone.enumerationId = Number(id);
            }

            return clone;
        }
    );
};
