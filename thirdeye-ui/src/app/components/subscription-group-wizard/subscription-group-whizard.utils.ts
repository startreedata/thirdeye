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
