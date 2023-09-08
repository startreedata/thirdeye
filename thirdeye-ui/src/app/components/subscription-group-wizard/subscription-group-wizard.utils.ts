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

import CronValidator from "cron-expression-validator";
import { isEmpty } from "lodash";
import {
    AlertAssociation,
    SubscriptionGroup,
} from "../../rest/dto/subscription-group.interfaces";
import { getAssociationId } from "./alerts-dimensions/alerts-dimensions.utils";
import { specTypeToUIConfig } from "./subscription-group-details/recipient-details/groups-editor/groups-editor.utils";
import { Association } from "./subscription-group-wizard.interfaces";

export function validateSubscriptionGroup(
    subscriptionGroup: SubscriptionGroup
): boolean {
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

export const getAssociations = (
    subscriptionGroup: SubscriptionGroup
): Association[] => {
    const { alertAssociations, alerts = [] } = subscriptionGroup;

    let associations: Association[] = [];

    // Use the newer alertAssociations if present
    if (alertAssociations && !isEmpty(alertAssociations)) {
        associations = alertAssociations.map(({ alert, enumerationItem }) => ({
            alertId: alert.id,
            ...(enumerationItem?.id && {
                enumerationId: enumerationItem?.id,
            }),
            id: getAssociationId({
                alertId: alert.id,
                enumerationId: enumerationItem?.id,
            }),
        }));
    } else {
        // Or else, check the @deprecated `alerts` for data
        associations = alerts.map((alert) => ({
            alertId: alert.id,
            id: getAssociationId({
                alertId: alert.id,
            }),
        }));
    }

    return associations;
};

/**
 *  If there are dimensions selected, then remove the alert level association
 */
export const cleanUpAssociations = (
    alertAssociations: AlertAssociation[]
): AlertAssociation[] => {
    const alertLevelAssociationById: {
        [key: number]: AlertAssociation;
    } = {};
    const enumerationItemsAssociationById: {
        [key: number]: AlertAssociation[];
    } = {};
    const alertsSubscribedTo = new Set<number>();

    alertAssociations.forEach((association) => {
        alertsSubscribedTo.add(association.alert.id);

        if (
            association.enumerationItem === undefined ||
            association.enumerationItem.id === undefined
        ) {
            alertLevelAssociationById[association.alert.id] = association;
        } else {
            const bucket =
                enumerationItemsAssociationById[association.alert.id] || [];
            bucket.push(association);
            enumerationItemsAssociationById[association.alert.id] = bucket;
        }
    });

    let newAssociationList: AlertAssociation[] = [];
    alertsSubscribedTo.forEach((id) => {
        if (enumerationItemsAssociationById[id]) {
            newAssociationList = newAssociationList.concat(
                enumerationItemsAssociationById[id]
            );
        } else {
            newAssociationList.push(alertLevelAssociationById[id]);
        }
    });

    return newAssociationList;
};
