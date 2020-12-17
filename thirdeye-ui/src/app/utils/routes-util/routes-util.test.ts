import * as paramsUtil from "../params-util/params-util";
import {
    createPathWithRecognizedQueryString,
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
        jest.spyOn(paramsUtil, "getRecognizedQueryString").mockImplementation(
            (): string => {
                return "testQueryString";
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
        expect(getBasePath()).toEqual("/?testQueryString");
    });

    test("getHomePath shall return appropriate path with time range query string", () => {
        expect(getHomePath()).toEqual("/home?testQueryString");
    });

    test("getAlertsPath shall return appropriate path with time range query string", () => {
        expect(getAlertsPath()).toEqual("/alerts?testQueryString");
    });

    test("getAlertsAllPath shall return appropriate path with time range query string", () => {
        expect(getAlertsAllPath()).toEqual("/alerts/all?testQueryString");
    });

    test("getAlertsDetailPath shall return appropriate path with time range query string", () => {
        expect(getAlertsDetailPath(1)).toEqual("/alerts/id/1?testQueryString");
    });

    test("getAlertsCreatePath shall return appropriate path with time range query string", () => {
        expect(getAlertsCreatePath()).toEqual("/alerts/create?testQueryString");
    });

    test("getAlertsUpdatePath shall return appropriate path with time range query string", () => {
        expect(getAlertsUpdatePath(1)).toEqual(
            "/alerts/update/id/1?testQueryString"
        );
    });

    test("getAnomaliesPath shall return appropriate path with time range query string", () => {
        expect(getAnomaliesPath()).toEqual("/anomalies?testQueryString");
    });

    test("getAnomaliesAllPath shall return appropriate path with time range query string", () => {
        expect(getAnomaliesAllPath()).toEqual("/anomalies/all?testQueryString");
    });

    test("getAnomaliesDetailPath shall return appropriate path with time range query string", () => {
        expect(getAnomaliesDetailPath(1)).toEqual(
            "/anomalies/id/1?testQueryString"
        );
    });

    test("getConfigurationPath shall return appropriate path with time range query string", () => {
        expect(getConfigurationPath()).toEqual(
            "/configuration?testQueryString"
        );
    });

    test("getSignInPath shall return appropriate path with time range query string", () => {
        expect(getSignInPath()).toEqual("/signIn?testQueryString");
    });

    test("getSignOutPath shall return appropriate path with time range query string", () => {
        expect(getSignOutPath()).toEqual("/signOut?testQueryString");
    });

    test("createPathWithTimeRangeQueryString shall return input path with time range query string", () => {
        expect(createPathWithRecognizedQueryString("/testPath")).toEqual(
            "/testPath?testQueryString"
        );
    });
});
