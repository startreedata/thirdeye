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

import { Alert } from "../../rest/dto/alert.interfaces";
import {
    AnomalyMetadata,
    AnomalyResultSource,
} from "../../rest/dto/anomaly.interfaces";
import { Metric } from "../../rest/dto/metric.interfaces";

// TODO: Proper interface
export interface EditedAnomaly {
    startTime: number;
    endTime: number;

    // avgCurrentVal: number;
    // avgBaselineVal: number;

    // score: number;
    // weight: number;
    // impactToGlobal: number;
    sourceType: AnomalyResultSource.USER_LABELED_ANOMALY;
    // created: number;
    // notified: boolean;
    // message: string;

    alert: Pick<Alert, "id" | "name">;
    metric: Metric;
    metadata: AnomalyMetadata;
    enumerationItem?: {
        id: number;
    };

    // children: Anomaly[];
    // type: AnomalyType;
    // severity: AnomalySeverity;
    // child: boolean;
    // feedback?: AnomalyFeedback;
}
