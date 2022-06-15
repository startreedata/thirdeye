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

import { validateEmail, validateJSON } from "./validation.util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

describe("Validation Util", () => {
    it("validateEmail should return appropriate validation result for invalid email", () => {
        expect(validateEmail(null as unknown as string)).toEqual({
            valid: false,
            message: "message.email-required",
        });
        expect(validateEmail("")).toEqual({
            valid: false,
            message: "message.email-required",
        });
        expect(validateEmail("testEmail")).toEqual({
            valid: false,
            message: "message.invalid-email",
        });
    });

    it("validateEmail should return appropriate validation result for valid email", () => {
        expect(validateEmail("test@email.com")).toEqual({
            valid: true,
        });
        expect(validateEmail(" test@email.com ")).toEqual({
            valid: true,
        });
    });

    it("validateJSON should return appropriate validation result for invalid JSON", () => {
        expect(validateJSON(null as unknown as string)).toEqual({
            valid: false,
            message: "message.json-input-required",
        });
        expect(validateJSON("")).toEqual({
            valid: false,
            message: "message.json-input-required",
        });
        expect(validateJSON("testJson")).toEqual({
            valid: false,
            message: "message.invalid-json",
        });
    });

    it("validateJSON should return appropriate validation result for valid JSON", () => {
        expect(
            validateJSON(`{
            "testKey1": {
                "testKey2": "testValue2"
            },
            "testKey3": [3]
        }`)
        ).toEqual({
            valid: true,
        });
        expect(
            validateJSON(` {
            "testKey1": {
                "testKey2": "testValue2"
            },
            "testKey3": [3]
        } `)
        ).toEqual({
            valid: true,
        });
    });
});
