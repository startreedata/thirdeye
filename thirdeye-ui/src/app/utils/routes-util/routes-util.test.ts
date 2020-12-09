import * as paramsUtil from "../params-util/params-util";
import {
    createPathWithTimeRangeQueryString,
    getAlertsAllPath,
    getAlertsCreatePath,
    getAlertsDetailPath,
    getAlertsPath,
    getAlertsUpdatePath,
    getAnomaliesAllPath,
    getAnomaliesDetailPath,
    getAnomaliesPath,
    getBasePath,
    getConfigurationPath,
    getHomePath,
    getSignInPath,
    getSignOutPath,
} from "./routes-util";

jest.mock("../params-util/params-util");

describe("Routes Util with time range query string", () => {
    beforeAll(() => {
        jest.spyOn(paramsUtil, "getTimeRangeQueryString").mockImplementation(
            (): string => {
                return "testTimeRangeQueryString";
            }
        );
    });

    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
    });

    test("getBasePath shall return appropriate path with time range query string", () => {
        expect(getBasePath()).toEqual("/?testTimeRangeQueryString");
    });

    test("getHomePath shall return appropriate path with time range query string", () => {
        expect(getHomePath()).toEqual("/home?testTimeRangeQueryString");
    });

    test("getAlertsPath shall return appropriate path with time range query string", () => {
        expect(getAlertsPath()).toEqual("/alerts?testTimeRangeQueryString");
    });

    test("getAlertsAllPath shall return appropriate path with time range query string", () => {
        expect(getAlertsAllPath()).toEqual(
            "/alerts/all?testTimeRangeQueryString"
        );
    });

    test("getAlertsDetailPath shall return appropriate path with time range query string", () => {
        expect(getAlertsDetailPath(1)).toEqual(
            "/alerts/id/1?testTimeRangeQueryString"
        );
    });

    test("getAlertsCreatePath shall return appropriate path with time range query string", () => {
        expect(getAlertsCreatePath()).toEqual(
            "/alerts/create?testTimeRangeQueryString"
        );
    });

    test("getAlertsUpdatePath shall return appropriate path with time range query string", () => {
        expect(getAlertsUpdatePath(1)).toEqual(
            "/alerts/update/id/1?testTimeRangeQueryString"
        );
    });

    test("getAnomaliesPath shall return appropriate path with time range query string", () => {
        expect(getAnomaliesPath()).toEqual(
            "/anomalies?testTimeRangeQueryString"
        );
    });

    test("getAnomaliesAllPath shall return appropriate path with time range query string", () => {
        expect(getAnomaliesAllPath()).toEqual(
            "/anomalies/all?testTimeRangeQueryString"
        );
    });

    test("getAnomaliesDetailPath shall return appropriate path with time range query string", () => {
        expect(getAnomaliesDetailPath(1)).toEqual(
            "/anomalies/id/1?testTimeRangeQueryString"
        );
    });

    test("getConfigurationPath shall return appropriate path with time range query string", () => {
        expect(getConfigurationPath()).toEqual(
            "/configuration?testTimeRangeQueryString"
        );
    });

    test("getSignInPath shall return appropriate path with time range query string", () => {
        expect(getSignInPath()).toEqual("/signIn?testTimeRangeQueryString");
    });

    test("getSignOutPath shall return appropriate path with time range query string", () => {
        expect(getSignOutPath()).toEqual("/signOut?testTimeRangeQueryString");
    });

    test("createPathWithTimeRangeQueryString shall return input path with time range query string", () => {
        expect(createPathWithTimeRangeQueryString("/testPath")).toEqual(
            "/testPath?testTimeRangeQueryString"
        );
    });
});
