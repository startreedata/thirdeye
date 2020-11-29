import { isValidNumberId } from "./params-util";

describe("Params Util", () => {
    test("isValidNumberId shall validate empty string appropriately", () => {
        expect(isValidNumberId("")).toBeFalsy();
    });

    test("isValidNumberId shall validate random string appropriately", () => {
        expect(isValidNumberId("testString")).toBeFalsy();
    });

    test("isValidNumberId shall validate number string appropriately", () => {
        expect(isValidNumberId("1")).toBeTruthy();
    });
});
