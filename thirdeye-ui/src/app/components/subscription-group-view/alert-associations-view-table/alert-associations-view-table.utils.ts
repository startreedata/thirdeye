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

import { UiSubscriptionGroupAlert } from "../../../rest/dto/ui-subscription-group.interfaces";
import {
    getAssociationId,
    getEnumerationItemName,
} from "../../subscription-group-wizard-new/alerts-dimensions/alerts-dimensions.utils";
import { UiAssociation } from "./alert-associations-view-table.interface";

export const getUiAssociation = (
    uiSubscriptionGroupAlerts: UiSubscriptionGroupAlert[],
    t: (v: string, args?: Record<string, string>) => string
): UiAssociation[] => {
    const uiAssociations: UiAssociation[] = [];

    (uiSubscriptionGroupAlerts || []).forEach((alert) => {
        if (!alert.enumerationItems) {
            uiAssociations.push({
                id: getAssociationId({ alertId: alert.id }),
                alertId: alert.id,
                alertName: alert.name,
                enumerationName: t("label.overall-entity", {
                    entity: t("label.alert"),
                }),
            });

            return;
        }

        alert.enumerationItems.forEach((enumerationItem) => {
            uiAssociations.push({
                id: getAssociationId({
                    alertId: alert.id,
                    enumerationId: enumerationItem.id,
                }),
                alertId: alert.id,
                alertName: alert.name,
                enumerationId: enumerationItem.id,
                enumerationName: getEnumerationItemName(enumerationItem),
            });
        });
    });

    return uiAssociations;
};
