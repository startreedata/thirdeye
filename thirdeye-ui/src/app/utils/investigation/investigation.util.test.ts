// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
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
