import { Investigation, SavedStateKeys } from "../../rest/dto/rca.interfaces";

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
