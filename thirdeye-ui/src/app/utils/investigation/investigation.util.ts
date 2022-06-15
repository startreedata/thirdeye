///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///

import { Investigation, SavedStateKeys } from "../../rest/dto/rca.interfaces";

export const INVESTIGATION_ID_QUERY_PARAM = "investigationId";

export function getFromSavedInvestigationOrDefault<ExpectedReturnType>(
    investigation: Investigation | null,
    key: SavedStateKeys,
    defaultValue: ExpectedReturnType
): ExpectedReturnType {
    if (
        investigation &&
        investigation.uiMetadata &&
        investigation.uiMetadata[key]
    ) {
        return investigation.uiMetadata[key] as unknown as ExpectedReturnType;
    }

    return defaultValue;
}

export function createNewInvestigation(anomalyId: number): Investigation {
    return {
        uiMetadata: {},
        anomaly: { id: anomalyId },
    } as Investigation;
}

export function determineInvestigationIDFromSearchParams(
    searchParams: URLSearchParams
): number | null {
    const investigationIdFromQueryParams = searchParams.get(
        INVESTIGATION_ID_QUERY_PARAM
    );

    return investigationIdFromQueryParams
        ? Number(investigationIdFromQueryParams)
        : null;
}
