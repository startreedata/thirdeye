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
