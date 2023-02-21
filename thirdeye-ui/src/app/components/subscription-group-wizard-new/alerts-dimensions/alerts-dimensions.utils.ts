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

import { EnumerationItem } from "../../../rest/dto/enumeration-item.interfaces";
import { Association } from "../subscription-group-wizard-new.interface";

// Function type signature overloading helps inform the TS compiler to infer
// types smartly from the parameters themselves. Here, it is important to
// place the narrower type above the more general type for this to work properly
export function getEnumerationItemName(item: EnumerationItem): string;
export function getEnumerationItemName(
    item: EnumerationItem | undefined | null
): string | null;

export function getEnumerationItemName(
    item: EnumerationItem | undefined | null
): string | null {
    if (!item) {
        return null;
    }

    const { params, name } = item;

    if (!params) {
        return name;
    }

    return Object.entries(params)
        .map(([k, v]) => `${k}=${v}`)
        .join(";");
}

export const getAssociationId = ({
    alertId,
    enumerationId,
}: Pick<Association, "alertId" | "enumerationId">): string =>
    `${alertId}${enumerationId ? `-${enumerationId}` : ""}`;
