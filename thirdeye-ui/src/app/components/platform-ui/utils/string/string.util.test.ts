// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { parseBooleanV1 } from "./string.util";

describe("String Util", () => {
    it("parseBooleanV1 should return false for invalid string", () => {
        expect(parseBooleanV1((null as unknown) as string)).toBeFalsy();
    });

    it("parseBooleanV1 should return false for empty string", () => {
        expect(parseBooleanV1("")).toBeFalsy();
    });

    it("parseBooleanV1 should return false for random string", () => {
        expect(parseBooleanV1("testString")).toBeFalsy();
    });

    it("parseBooleanV1 should return true for 'true'", () => {
        expect(parseBooleanV1("true")).toBeTruthy();
    });

    it("parseBooleanV1 should return true for padded ' true '", () => {
        expect(parseBooleanV1(" true ")).toBeTruthy();
    });
});
