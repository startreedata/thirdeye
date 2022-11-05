import { AxiosError } from "axios";
import { getErrorMessages } from "./rest.util";

describe("rest util", () => {
    it("should return empty array for null value as input", () => {
        expect(getErrorMessages(null as unknown as AxiosError)).toStrictEqual(
            []
        );
    });

    it("should return empty array for empty object as input", () => {
        expect(getErrorMessages({} as AxiosError)).toStrictEqual([]);
    });

    it("should return empty array for empty response value as input", () => {
        expect(getErrorMessages({ response: {} } as AxiosError)).toStrictEqual(
            []
        );
    });

    it("should return empty array for empty response data value as input", () => {
        expect(
            getErrorMessages({ response: { data: {} } } as AxiosError)
        ).toStrictEqual([]);
    });

    it("should return empty array for empty errors array", () => {
        expect(
            getErrorMessages({ response: { data: { list: [] } } } as AxiosError)
        ).toStrictEqual([]);
    });

    it("should return empty array for list with empty error object", () => {
        expect(
            getErrorMessages({
                response: { data: { list: [{}] } },
            } as AxiosError)
        ).toStrictEqual([]);
    });

    it("should return empty array for error without msg property", () => {
        expect(
            getErrorMessages({
                response: { data: { list: [{ code: "testCode" }] } },
            } as AxiosError)
        ).toStrictEqual([]);
    });

    it("should return errors from list of errors object", () => {
        expect(
            getErrorMessages({
                response: {
                    data: { list: [{ code: "testCode", msg: "testMessage" }] },
                },
            } as AxiosError)
        ).toStrictEqual(["testMessage"]);
    });

    it("should return errors from list of errors object even if code is missing", () => {
        expect(
            getErrorMessages({
                response: {
                    data: { list: [{ msg: "testMessage" }] },
                },
            } as AxiosError)
        ).toStrictEqual(["testMessage"]);
    });

    it("should return all the errors from list of errors object", () => {
        expect(
            getErrorMessages({
                response: {
                    data: {
                        list: [
                            { code: "testCode1", msg: "testMessage1" },
                            { code: "testCode2", msg: "testMessage2" },
                            { code: "testCode3", msg: "testMessage3" },
                        ],
                    },
                },
            } as AxiosError)
        ).toStrictEqual(["testMessage1", "testMessage2", "testMessage3"]);
    });
});
