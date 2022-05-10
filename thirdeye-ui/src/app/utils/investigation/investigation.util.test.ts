import { Investigation, SavedStateKeys } from "../../rest/dto/rca.interfaces";
import {
    createNewInvestigation,
    getFromSavedInvestigationOrDefault,
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
});
