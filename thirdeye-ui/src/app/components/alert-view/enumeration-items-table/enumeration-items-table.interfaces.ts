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
import type { DataGridSortOrderV1 } from "../../../platform/components";
import type { AlertStats } from "../../../rest/dto/alert.interfaces";
import type { DetectionEvaluationForRender } from "../enumeration-item-merger/enumeration-item-merger.interfaces";

export interface EnumerationItemsTableProps {
    detectionEvaluations: DetectionEvaluationForRender[];
    expanded: string[];
    onExpandedChange: (newExpanded: string[]) => void;
    alertId: number;
    sortOrder: DataGridSortOrderV1;
    initialSearchTerm: string;
    onSearchTermChange: (newTerm: string) => void;
    onSortOrderChange: (newOrder: DataGridSortOrderV1) => void;
    alertsStats?: Record<number, AlertStats | null> | null;
}
