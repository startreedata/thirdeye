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
export const PROMISES: { REJECTED: "rejected"; FULFILLED: "fulfilled" } =
    Object.freeze({
        REJECTED: "rejected",
        FULFILLED: "fulfilled",
    });

export const THIRDEYE_DOC_LINK =
    "https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye";

export const QUERY_PARAM_KEYS: { [key: string]: string } = {
    SHOW_FIRST_ALERT_SUCCESS: "showFirstAlertSuccess",
};

// sensitivity range for ETS and other templates that use confidence-based sensitivity
export const SENSITIVITY_RANGE = {
    LOW: -26,
    HIGH: 14,
    MEDIUM: -6,
};
