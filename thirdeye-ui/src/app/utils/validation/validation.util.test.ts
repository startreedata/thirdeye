import { validateEmail, validateJSON } from "./validation.util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

describe("Validation Util", () => {
    it("validateEmail should return appropriate validation result for invalid email", () => {
        expect(validateEmail((null as unknown) as string)).toEqual({
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
        expect(validateJSON((null as unknown) as string)).toEqual({
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
