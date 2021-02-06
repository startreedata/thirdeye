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
} from "./routes.util";

jest.mock("../params/params.util", () => ({
    getRecognizedQueryString: jest.fn().mockReturnValue("testQueryString"),
}));

describe("Routes Util", () => {
    test("getBasePath should return appropriate path with appropriate query string", () => {
        expect(getBasePath()).toEqual("/?testQueryString");
    });

    test("getHomePath should return appropriate path with appropriate query string", () => {
        expect(getHomePath()).toEqual("/home?testQueryString");
    });

    test("getAlertsPath should return appropriate path with appropriate query string", () => {
        expect(getAlertsPath()).toEqual("/alerts?testQueryString");
    });

    test("getAlertsAllPath should return appropriate path with appropriate query string", () => {
        expect(getAlertsAllPath()).toEqual("/alerts/all?testQueryString");
    });

    test("getAlertsDetailPath should return appropriate path with appropriate query string", () => {
        expect(getAlertsDetailPath(1)).toEqual(
            "/alerts/detail/id/1?testQueryString"
        );
    });

    test("getAlertsCreatePath should return appropriate path with appropriate query string", () => {
        expect(getAlertsCreatePath()).toEqual("/alerts/create?testQueryString");
    });

    test("getAlertsUpdatePath should return appropriate path with appropriate query string", () => {
        expect(getAlertsUpdatePath(1)).toEqual(
            "/alerts/update/id/1?testQueryString"
        );
    });

    test("getAnomaliesPath should return appropriate path with appropriate query string", () => {
        expect(getAnomaliesPath()).toEqual("/anomalies?testQueryString");
    });

    test("getAnomaliesAllPath should return appropriate path with appropriate query string", () => {
        expect(getAnomaliesAllPath()).toEqual("/anomalies/all?testQueryString");
    });

    test("getAnomaliesDetailPath should return appropriate path with appropriate query string", () => {
        expect(getAnomaliesDetailPath(1)).toEqual(
            "/anomalies/detail/id/1?testQueryString"
        );
    });

    test("getConfigurationPath should return appropriate path with appropriate query string", () => {
        expect(getConfigurationPath()).toEqual(
            "/configuration?testQueryString"
        );
    });

    test("getSubscriptionGroupsPath should return appropriate path with appropriate query string", () => {
        expect(getSubscriptionGroupsPath()).toEqual(
            "/configuration/subscriptionGroups?testQueryString"
        );
    });

    test("getSubscriptionGroupsAllPath should return appropriate path with appropriate query string", () => {
        expect(getSubscriptionGroupsAllPath()).toEqual(
            "/configuration/subscriptionGroups/all?testQueryString"
        );
    });

    test("getSubscriptionGroupsDetailPath should return appropriate path with appropriate query string", () => {
        expect(getSubscriptionGroupsDetailPath(1)).toEqual(
            "/configuration/subscriptionGroups/detail/id/1?testQueryString"
        );
    });

    test("getSubscriptionGroupsCreatePath should return appropriate path with appropriate query string", () => {
        expect(getSubscriptionGroupsCreatePath()).toEqual(
            "/configuration/subscriptionGroups/create?testQueryString"
        );
    });

    test("getSubscriptionGroupsUpdatePath should return appropriate path with appropriate query string", () => {
        expect(getSubscriptionGroupsUpdatePath(1)).toEqual(
            "/configuration/subscriptionGroups/update/id/1?testQueryString"
        );
    });

    test("getSignInPath should return appropriate path with appropriate query string", () => {
        expect(getSignInPath()).toEqual("/signIn?testQueryString");
    });

    test("getSignOutPath should return appropriate path with appropriate query string", () => {
        expect(getSignOutPath()).toEqual("/signOut?testQueryString");
    });

    test("createPathWithTimeRangeQueryString should return path with appropriate query string", () => {
        expect(createPathWithRecognizedQueryString("/testPath")).toEqual(
            "/testPath?testQueryString"
        );
    });
});
