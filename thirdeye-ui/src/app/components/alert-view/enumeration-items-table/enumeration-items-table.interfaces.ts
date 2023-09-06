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
import type { DataGridSortOrderV1 } from "../../../platform/components";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { EnumerationItem } from "../../../rest/dto/enumeration-item.interfaces";

export interface EnumerationItemsTableProps {
    anomalies: Anomaly[];
    enumerationsItems: EnumerationItem[];
    alertId: number;
    startTime: number;
    endTime: number;

    expanded: string[];
    onExpandedChange: (newExpanded: string[]) => void;
    sortOrder: DataGridSortOrderV1;
    sortKey: string;
    initialSearchTerm: string;
    onSearchTermChange: (newTerm: string) => void;
    onSortKeyChange: (newKey: string) => void;
    onSortOrderChange: (newOrder: DataGridSortOrderV1) => void;
}

export interface EnumerationItemsWithAnomalies {
    enumerationItem: EnumerationItem;
    anomalies: Anomaly[];

    lastAnomalyTs: number;
}
