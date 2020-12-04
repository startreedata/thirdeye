import { isValidNumberId } from "./params-util";

describe("Params Util", () => {
    test("isValidNumberId shall validate empty string appropriately", () => {
        expect(isValidNumberId("")).toBeFalsy();
    });

    test("isValidNumberId shall validate random string appropriately", () => {
        expect(isValidNumberId("testString")).toBeFalsy();
    });

    test("isValidNumberId shall validate positive integer number string appropriately", () => {
        expect(isValidNumberId("1")).toBeTruthy();
    });

    test("isValidNumberId shall validate 0 string appropriately", () => {
        expect(isValidNumberId("0")).toBeTruthy();
    });

    test("isValidNumberId shall validate negative integer number string appropriately", () => {
        expect(isValidNumberId("-1")).toBeFalsy();
    });

    test("isValidNumberId shall validate decimal number string appropriately", () => {
        expect(isValidNumberId("1.1")).toBeFalsy();
    });
});
