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
import type { ReactNode } from "react";
import { ActionStatus } from "../../rest/actions.interfaces";
import type { EnumerationItem } from "../../rest/dto/enumeration-item.interfaces";
import type { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { Alert } from "../../rest/dto/alert.interfaces";

interface EnumerationDataProps {
    showEnumerationItem: boolean;
    enumerationItems: EnumerationItem[] | null;
    enumerationItemsStatus: ActionStatus;
}

export interface AnomalyListV1Props extends Partial<EnumerationDataProps> {
    anomalies: UiAnomaly[] | null;
    onDelete?: (uiAnomalies: UiAnomaly[]) => void;
    searchFilterValue?: string | null;
    onSearchFilterValueChange?: (value: string) => void;
    toolbar?: ReactNode;
    timezone?: string;
    allAlerts?: Alert[] | null;
}

export const ANOMALY_LIST_TEST_IDS = {
    TABLE: "anomalies-list-table",
    DELETE_BUTTON: "anomalies-list-delete-button",
    METRIC_FILTER_CONTAINER: "anomalies-list-metric-filter-container",
    DATASET_FILTER_CONTAINER: "anomalies-list-dataset-filter-container",
    ALERT_FILTER_CONTAINER: "anomalies-list-alert-filter-container",
};
