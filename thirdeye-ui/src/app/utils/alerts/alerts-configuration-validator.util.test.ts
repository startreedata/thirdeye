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
import { MetadataProperty } from "../../rest/dto/alert-template.interfaces";
import { validateTemplateProperties } from "./alerts-configuration-validator.util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

describe("Alerts Configuration Validator Util", () => {
    it("validateTemplateProperties should return error for optioned property if a value does not exist for single", () => {
        const result = validateTemplateProperties(
            [OPTIONED_SINGLE],
            {
                optionedSingleProperty: "4",
            },
            MOCK_TRANSLATION
        );

        expect(result).toEqual([
            {
                key: "optionedSingleProperty",
                msg: MOCK_MSG,
            },
        ]);
    });

    it("validateTemplateProperties should return error for optioned property if a value does not exist", () => {
        const result = validateTemplateProperties(
            [OPTIONED_MULTISELECT],
            {
                optionedMultiselectProperty: ["4", "1"],
            },
            MOCK_TRANSLATION
        );

        expect(result).toEqual([
            {
                key: "optionedMultiselectProperty",
                msg: MOCK_MSG,
            },
        ]);
    });

    it("validateTemplateProperties should return error for optioned property if not array value", () => {
        const result = validateTemplateProperties(
            [OPTIONED_MULTISELECT],
            {
                optionedMultiselectProperty: "4",
            },
            MOCK_TRANSLATION
        );

        expect(result).toEqual([
            {
                key: "optionedMultiselectProperty",
                msg: MOCK_MSG,
            },
        ]);
    });

    it("validateTemplateProperties should return error for array jsonType if value is not array", () => {
        const result = validateTemplateProperties(
            [ARRAY],
            {
                arrayProperty: "4",
            },
            MOCK_TRANSLATION
        );

        expect(result).toEqual([
            {
                key: "arrayProperty",
                msg: MOCK_MSG,
            },
        ]);
    });

    it("validateTemplateProperties should return error for boolean jsonType if value is not boolean", () => {
        const result = validateTemplateProperties(
            [BOOLEAN],
            {
                booleanProperty: "4",
            },
            MOCK_TRANSLATION
        );

        expect(result).toEqual([
            {
                key: "booleanProperty",
                msg: MOCK_MSG,
            },
        ]);
    });

    it("validateTemplateProperties should return error for object jsonType if value is not object", () => {
        const result = validateTemplateProperties(
            [OBJECT],
            {
                objectProperty: "4",
            },
            MOCK_TRANSLATION
        );

        expect(result).toEqual([
            {
                key: "objectProperty",
                msg: MOCK_MSG,
            },
        ]);
    });

    it("validateTemplateProperties should return no errors for valid values", () => {
        const result = validateTemplateProperties(
            [OPTIONED_SINGLE, OPTIONED_MULTISELECT, ARRAY, BOOLEAN, OBJECT],
            {
                optionedSingleProperty: "1",
                optionedMultiselectProperty: ["1"],
                arrayProperty: ["1", "2"],
                booleanProperty: false,
                objectProperty: {
                    hello: "world",
                },
                fieldNotIncluded: "foobar",
            },
            MOCK_TRANSLATION
        );

        expect(result).toEqual([]);
    });

    it("validateTemplateProperties should return no errors for dimension exploration values", () => {
        const result = validateTemplateProperties(
            [OPTIONED_SINGLE, OPTIONED_MULTISELECT, ARRAY, BOOLEAN, OBJECT],
            {
                optionedSingleProperty: "${foo}",
                optionedMultiselectProperty: "${foo}",
                arrayProperty: "${foo}",
                booleanProperty: "${foo}",
                objectProperty: "${foo}",
                fieldNotIncluded: "${foo}",
            },
            MOCK_TRANSLATION
        );

        expect(result).toEqual([]);
    });
});

const OPTIONED_SINGLE = {
    name: "optionedSingleProperty",
    options: ["1", "2", "3"],
    multiselect: false,
    defaultIsNull: false,
};

const OPTIONED_MULTISELECT = {
    name: "optionedMultiselectProperty",
    options: ["1", "2", "3"],
    multiselect: true,
    defaultIsNull: false,
};

const ARRAY = {
    name: "arrayProperty",
    multiselect: false,
    defaultIsNull: false,
    jsonType: "ARRAY" as MetadataProperty["jsonType"],
};

const BOOLEAN = {
    name: "booleanProperty",
    multiselect: false,
    defaultIsNull: false,
    jsonType: "BOOLEAN" as MetadataProperty["jsonType"],
};

const OBJECT = {
    name: "objectProperty",
    multiselect: false,
    defaultIsNull: false,
    jsonType: "OBJECT" as MetadataProperty["jsonType"],
};

const MOCK_MSG = "error msg";
const MOCK_TRANSLATION = (): string => MOCK_MSG;
