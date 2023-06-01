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
export enum UserPreferencesKeys {
    SHOW_DOCUMENTATION_RESOURCES = "showHomeDocumentationResources",
    ANOMALIES_LIST_DEFAULT_ALERT_FILTERS = "anomaliesListDefaultAlertFilter",
    ANOMALIES_LIST_DEFAULT_SUBSCRIPTION_FILTERS = "anomaliesListDefaultSubscriptionFilter",
}

export interface UserPreferences {
    showHomeDocumentationResources?: boolean;
    anomaliesListDefaultAlertFilter?: number[];
    anomaliesListDefaultSubscriptionFilter?: number[];
}

export type GetPreferenceFunction = (
    key: keyof UserPreferences
) => number[] | string | boolean | null | undefined;

export interface UseUserPreferencesHook {
    setPreference: (
        key: keyof UserPreferences,
        value: string | boolean | number[]
    ) => void;
    getPreference: GetPreferenceFunction;
    localPreferences: UserPreferences;
}
