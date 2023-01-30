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
import { ActionStatus } from "../../../rest/actions.interfaces";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { EnumerationItem } from "../../../rest/dto/enumeration-item.interfaces";
import { Event } from "../../../rest/dto/event.interfaces";
import { AnomalyFilterOption } from "../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.interfaces";

export interface AnomalyTimeSeriesCardProps {
    anomaly: Anomaly | null;
    events: Event[];
    timeSeriesFiltersSet: AnomalyFilterOption[][];
    onRemoveBtnClick: (idx: number) => void;
    isLoading: boolean;
    onEventSelectionChange: (events: Event[]) => void;
    getEnumerationItemRequest: ActionStatus;
    enumerationItem: EnumerationItem | null;
    timezone: string | undefined;
}
