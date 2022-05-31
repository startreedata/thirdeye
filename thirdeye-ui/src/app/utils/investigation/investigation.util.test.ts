import { Investigation, SavedStateKeys } from "../../rest/dto/rca.interfaces";
import {
    createNewInvestigation,
    determineInvestigationIDFromSearchParams,
    getFromSavedInvestigationOrDefault,
    INVESTIGATION_ID_QUERY_PARAM,
} from "./investigation.util";

describe("Investigation Util", () => {
    it("getFromSavedInvestigationOrDefault return default value if key is missing", () => {
        const result = getFromSavedInvestigationOrDefault(
            { uiMetadata: {} } as Investigation,
            SavedStateKeys.QUERY_SEARCH_STRING,
            "defaultString"
        );

        expect(result).toEqual("defaultString");
    });

    it("getFromSavedInvestigationOrDefault return default value if investigation is null", () => {
        const result = getFromSavedInvestigationOrDefault(
            null,
            SavedStateKeys.QUERY_SEARCH_STRING,
            "defaultString"
        );

        expect(result).toEqual("defaultString");
    });

    it("createNewInvestigation returns object with expected fields", () => {
        const result = createNewInvestigation(123);

        expect(result).toEqual({
            uiMetadata: {},
            anomaly: { id: 123 },
        });
    });

    it("determineInvestigationIDFromSearchParams should return null if id missing", () => {
        const result = determineInvestigationIDFromSearchParams(
            new URLSearchParams()
        );

        expect(result).toBeNull();
    });

    it("determineInvestigationIDFromSearchParams should return id as number if it exists", () => {
        const result = determineInvestigationIDFromSearchParams(
            new URLSearchParams([[INVESTIGATION_ID_QUERY_PARAM, "456"]])
        );

        expect(result).toEqual(456);
    });
});
