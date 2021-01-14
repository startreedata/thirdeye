import { validateEmail, validateJSON } from "./validation-util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key: string): string => {
        return key;
    }),
}));

describe("Validation Util", () => {
    test("validateEmail should return appropriate validation result for invalid email", () => {
        expect(validateEmail("")).toEqual({
            valid: false,
            message: "message.email-required",
        });
        expect(validateEmail("testInvalidEmail")).toEqual({
            valid: false,
            message: "message.invalid-email",
        });
    });

    test("validateEmail should return appropriate validation result for valid email", () => {
        expect(validateEmail("test@valid.email")).toEqual({
            valid: true,
        });
        expect(validateEmail(" test@valid.email ")).toEqual({
            valid: true,
        });
    });

    test("validateJSON should return appropriate validation result for invalid JSON", () => {
        expect(validateJSON((null as unknown) as string)).toEqual({
            valid: false,
            message: "message.json-input-required",
        });
        expect(validateJSON("")).toEqual({
            valid: false,
            message: "message.json-input-required",
        });
        expect(validateJSON("testInvalidJson")).toEqual({
            valid: false,
            message: "message.invalid-json",
        });
    });

    test("validateJSON should return appropriate validation result for valid JSON", () => {
        expect(
            validateJSON(`{
            "testKey1": {
                "testKey2": "testStringValue"
            },
            "testKey3": [3]
        }`)
        ).toEqual({
            valid: true,
        });
        expect(
            validateJSON(` {
            "testKey1": {
                "testKey2": "testStringValue"
            },
            "testKey3": [3]
        } `)
        ).toEqual({
            valid: true,
        });
    });
});
