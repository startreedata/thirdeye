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
import { Alert } from "../../../../../rest/dto/alert.interfaces";
import { Anomaly } from "../../../../../rest/dto/anomaly.interfaces";
import { DetectionEvaluation } from "../../../../../rest/dto/detection.interfaces";
import { TimeRange } from "../../../../../rest/dto/time-range.interfaces";
import { LegendPlacement } from "../../../../alert-wizard-v2/alert-template/preview-chart/preview-chart.interfaces";

export interface EnumerationItemRowProps {
    detectionEvaluation: DetectionEvaluation;
    anomalies: Anomaly[];
    onDeleteClick?: () => void;
    timezone: string | undefined;
    hideTime: boolean;
    showOnlyActivity?: boolean;
    hideDelete?: boolean;
    alert: Alert;
    evaluationTimeRange: TimeRange;
    legendsPlacement?: `${LegendPlacement}`;
}
