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
    getSubscriptionGroupsAllPath,
    getSubscriptionGroupsCreatePath,
    getSubscriptionGroupsDetailPath,
    getSubscriptionGroupsPath,
    getSubscriptionGroupsUpdatePath,
} from "./routes-util";

jest.mock("../params-util/params-util");

describe("Routes Util", () => {
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

    test("getBasePath shall return appropriate path with current query string", () => {
        expect(getBasePath()).toEqual("/?testQueryString");
    });

    test("getHomePath shall return appropriate path with current query string", () => {
        expect(getHomePath()).toEqual("/home?testQueryString");
    });

    test("getAlertsPath shall return appropriate path with current query string", () => {
        expect(getAlertsPath()).toEqual("/alerts?testQueryString");
    });

    test("getAlertsAllPath shall return appropriate path with current query string", () => {
        expect(getAlertsAllPath()).toEqual("/alerts/all?testQueryString");
    });

    test("getAlertsDetailPath shall return appropriate path with current query string", () => {
        expect(getAlertsDetailPath(1)).toEqual(
            "/alerts/detail/id/1?testQueryString"
        );
    });

    test("getAlertsCreatePath shall return appropriate path with current query string", () => {
        expect(getAlertsCreatePath()).toEqual("/alerts/create?testQueryString");
    });

    test("getAlertsUpdatePath shall return appropriate path with current query string", () => {
        expect(getAlertsUpdatePath(1)).toEqual(
            "/alerts/update/id/1?testQueryString"
        );
    });

    test("getAnomaliesPath shall return appropriate path with current query string", () => {
        expect(getAnomaliesPath()).toEqual("/anomalies?testQueryString");
    });

    test("getAnomaliesAllPath shall return appropriate path with current query string", () => {
        expect(getAnomaliesAllPath()).toEqual("/anomalies/all?testQueryString");
    });

    test("getAnomaliesDetailPath shall return appropriate path with current query string", () => {
        expect(getAnomaliesDetailPath(1)).toEqual(
            "/anomalies/detail/id/1?testQueryString"
        );
    });

    test("getConfigurationPath shall return appropriate path with current query string", () => {
        expect(getConfigurationPath()).toEqual(
            "/configuration?testQueryString"
        );
    });

    test("getSubscriptionGroupsPath shall return appropriate path with current query string", () => {
        expect(getSubscriptionGroupsPath()).toEqual(
            "/configuration/subscriptionGroups?testQueryString"
        );
    });

    test("getSubscriptionGroupsAllPath shall return appropriate path with current query string", () => {
        expect(getSubscriptionGroupsAllPath()).toEqual(
            "/configuration/subscriptionGroups/all?testQueryString"
        );
    });

    test("getSubscriptionGroupsDetailPath shall return appropriate path with current query string", () => {
        expect(getSubscriptionGroupsDetailPath(1)).toEqual(
            "/configuration/subscriptionGroups/detail/id/1?testQueryString"
        );
    });

    test("getSubscriptionGroupsCreatePath shall return appropriate path with current query string", () => {
        expect(getSubscriptionGroupsCreatePath()).toEqual(
            "/configuration/subscriptionGroups/create?testQueryString"
        );
    });

    test("getSubscriptionGroupsUpdatePath shall return appropriate path with current query string", () => {
        expect(getSubscriptionGroupsUpdatePath(1)).toEqual(
            "/configuration/subscriptionGroups/update/id/1?testQueryString"
        );
    });

    test("getSignInPath shall return appropriate path with current query string", () => {
        expect(getSignInPath()).toEqual("/signIn?testQueryString");
    });

    test("getSignOutPath shall return appropriate path with current query string", () => {
        expect(getSignOutPath()).toEqual("/signOut?testQueryString");
    });

    test("createPathWithTimeRangeQueryString shall return input path with current query string", () => {
        expect(createPathWithRecognizedQueryString("/testPath")).toEqual(
            "/testPath?testQueryString"
        );
    });
});
