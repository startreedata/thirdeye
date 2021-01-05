import { validateEmail } from "./validation-util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key: string): string => {
        return key;
    }),
}));

describe("Validation Util", () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
    });

    test("validateEmail should return appropriate validation result for invalid email", () => {
        expect(validateEmail("")).toEqual({
            valid: false,
            message: "message.field-required",
        });
        expect(validateEmail("testInvalidEmail")).toEqual({
            valid: false,
            message: "message.invalid-email",
        });
    });

    test("validateEmail should return appropriate validation result for valid email", () => {
        expect(validateEmail("test@valid.email")).toEqual({
            valid: true,
            message: "",
        });
        expect(validateEmail(" test@valid.email ")).toEqual({
            valid: true,
            message: "",
        });
    });
});
