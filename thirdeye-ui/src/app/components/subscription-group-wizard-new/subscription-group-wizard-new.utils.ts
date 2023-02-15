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

import CronValidator from "cron-expression-validator";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { getAssociationId } from "./alerts-dimensions/alerts-dimensions.utils";
import { specTypeToUIConfig } from "./subscription-group-details/recipient-details/groups-editor/groups-editor.utils";
import { Association } from "./subscription-group-wizard-new.interface";

export function validateSubscriptionGroup(
    subscriptionGroup: SubscriptionGroup
): boolean {
    // TODO
    const isValid = [
        CronValidator.isValidCronExpression(subscriptionGroup.cron),
        subscriptionGroup.name !== "",
        subscriptionGroup.specs
            ? subscriptionGroup.specs.every((spec) =>
                  specTypeToUIConfig[spec.type].validate(spec)
              )
            : true,
    ].every((v) => v === true);

    return isValid;
}

export const SelectedTab = "selectedTab";

export const getAssociations = (
    subscriptionGroup: SubscriptionGroup
): Association[] => {
    const { alertAssociations, alerts = [] } = subscriptionGroup;

    const associations: Association[] = alertAssociations?.length
        ? alertAssociations.map(({ alert, enumerationItem }) => ({
              alertId: alert.id,
              ...(enumerationItem?.id && {
                  enumerationId: enumerationItem?.id,
              }),
              id: getAssociationId({
                  alertId: alert.id,
                  enumerationId: enumerationItem?.id,
              }),
          }))
        : alerts.map((alert) => ({
              alertId: alert.id,
              id: getAssociationId({
                  alertId: alert.id,
              }),
          }));

    return associations;
};
