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
import { Alert } from "../../rest/dto/alert.interfaces";
import { EnumerationItem } from "../../rest/dto/enumeration-item.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";

export enum AnomalyFilterQueryStringKey {
    ALERT = "alert",
    METRIC = "metric",
    DATASET = "dataset",
    SUBSCRIPTION_GROUP = "subscription",
}

export interface AnomalyFiltersSelectionProps {
    subscriptionGroupData: SubscriptionGroup[] | null;
    alertsData: Alert[] | null;
    enumerationItemsData: EnumerationItem[] | null;
}

export const ANOMALY_FILTERS_TEST_IDS = {
    MODIFY_BTN: "add-btn",
    CLEAR_BTN: "clear-btn",
    CONFIRM_BTN: "confirm-btn",
    ALERTS_TABLE: "alerts-filter-table",
    SUBSCRIPTION_GROUP_TABLE: "subscription-group-filters-table",
};
