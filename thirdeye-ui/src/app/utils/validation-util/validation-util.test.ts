import i18n from "i18next";
import { validateEmail } from "./validation-util";

jest.mock("i18next");

describe("Validation Util", () => {
    beforeAll(() => {
        i18n.t = jest.fn().mockImplementation((key: string): string => {
            return key;
        });
    });

    test("validateEmail shall return appropriate validation result for invalid email", () => {
        let validationResult = validateEmail("");

        expect(validationResult).toBeDefined();
        expect(validationResult.valid).toBeFalsy();
        expect(validationResult.message).toEqual("message.field-required");

        validationResult = validateEmail("testInvalidEmail");

        expect(validationResult).toBeDefined();
        expect(validationResult.valid).toBeFalsy();
        expect(validationResult.message).toEqual("message.invalid-email");
    });

    test("validateEmail shall return appropriate validation result for valid email", () => {
        let validationResult = validateEmail("test@valid.email");

        expect(validationResult).toBeDefined();
        expect(validationResult.valid).toBeTruthy();
        expect(validationResult.message).toEqual("");

        validationResult = validateEmail(" test@valid.email ");

        expect(validationResult).toBeDefined();
        expect(validationResult.valid).toBeTruthy();
        expect(validationResult.message).toEqual("");
    });
});
