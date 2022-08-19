/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import CronValidator from "cron-expression-validator";
import { DataGridSelectionModelV1 } from "../../platform/components";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { specTypeToUIConfig } from "./groups-editor/groups-editor.utils";

export function generateDefaultSelection(
    subscriptionGroup: SubscriptionGroup | undefined | null,
    alerts: Alert[]
): DataGridSelectionModelV1<Alert> {
    if (subscriptionGroup && alerts) {
        const selectedIds = subscriptionGroup.alerts.map(
            (alert: { id: number }) => alert.id
        );
        const selectedAlerts = alerts.filter((alert: Alert) =>
            selectedIds.includes(alert.id)
        );

        return {
            rowKeyValues: selectedIds,
            rowKeyValueMap: new Map(
                selectedAlerts.map((alert: Alert) => [alert.id, alert])
            ),
        };
    }

    return {
        rowKeyValues: [],
        rowKeyValueMap: new Map(),
    };
}

export function validateSubscriptionGroup(
    subscriptionGroup: SubscriptionGroup
): boolean {
    let isValid = CronValidator.isValidCronExpression(subscriptionGroup.cron);

    isValid = isValid && subscriptionGroup.name !== "";

    subscriptionGroup.specs &&
        subscriptionGroup.specs.forEach((spec) => {
            const configurationForSpec = specTypeToUIConfig[spec.type];

            isValid = isValid && configurationForSpec.validate(spec);
        });

    return isValid;
}
