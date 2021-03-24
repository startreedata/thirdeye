import {
    createPathWithRecognizedQueryString,
    getAlertsAllPath,
    getAlertsCreatePath,
    getAlertsPath,
    getAlertsUpdatePath,
    getAlertsViewPath,
    getAnomaliesAllPath,
    getAnomaliesPath,
    getAnomaliesViewPath,
    getBasePath,
    getConfigurationPath,
    getHomePath,
    getMetricsAllPath,
    getMetricsPath,
    getMetricsViewPath,
    getSignInPath,
    getSignOutPath,
    getSubscriptionGroupsAllPath,
    getSubscriptionGroupsCreatePath,
    getSubscriptionGroupsPath,
    getSubscriptionGroupsUpdatePath,
    getSubscriptionGroupsViewPath,
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

    test("getAlertsViewPath should return appropriate path with appropriate query string for id", () => {
        expect(getAlertsViewPath(1)).toEqual(
            "/alerts/view/id/1?testQueryString"
        );
    });

    test("getAlertsCreatePath should return appropriate path with appropriate query string", () => {
        expect(getAlertsCreatePath()).toEqual("/alerts/create?testQueryString");
    });

    test("getAlertsUpdatePath should return appropriate path with appropriate query string for id", () => {
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

    test("getAnomaliesViewPath should return appropriate path with appropriate query string for id", () => {
        expect(getAnomaliesViewPath(1)).toEqual(
            "/anomalies/view/id/1?testQueryString"
        );
    });

    test("getConfigurationPath should return appropriate path with appropriate query string", () => {
        expect(getConfigurationPath()).toEqual(
            "/configuration?testQueryString"
        );
    });

    test("getSubscriptionGroupsPath should return appropriate path with appropriate query string", () => {
        expect(getSubscriptionGroupsPath()).toEqual(
            "/configuration/subscription-groups?testQueryString"
        );
    });

    test("getSubscriptionGroupsAllPath should return appropriate path with appropriate query string", () => {
        expect(getSubscriptionGroupsAllPath()).toEqual(
            "/configuration/subscription-groups/all?testQueryString"
        );
    });

    test("getSubscriptionGroupsViewPath should return appropriate path with appropriate query string for id", () => {
        expect(getSubscriptionGroupsViewPath(1)).toEqual(
            "/configuration/subscription-groups/view/id/1?testQueryString"
        );
    });

    test("getSubscriptionGroupsCreatePath should return appropriate path with appropriate query string", () => {
        expect(getSubscriptionGroupsCreatePath()).toEqual(
            "/configuration/subscription-groups/create?testQueryString"
        );
    });

    test("getSubscriptionGroupsUpdatePath should return appropriate path with appropriate query string for id", () => {
        expect(getSubscriptionGroupsUpdatePath(1)).toEqual(
            "/configuration/subscription-groups/update/id/1?testQueryString"
        );
    });

    test("getMetricsPath should return appropriate path with appropriate query string", () => {
        expect(getMetricsPath()).toEqual(
            "/configuration/metrics?testQueryString"
        );
    });

    test("getMetricsAllPath should return appropriate path with appropriate query string", () => {
        expect(getMetricsAllPath()).toEqual(
            "/configuration/metrics/all?testQueryString"
        );
    });

    test("getMetricsViewPath should return appropriate path with appropriate query string for id", () => {
        expect(getMetricsViewPath(1)).toEqual(
            "/configuration/metrics/view/id/1?testQueryString"
        );
    });

    test("getSignInPath should return appropriate path with appropriate query string", () => {
        expect(getSignInPath()).toEqual("/sign-in?testQueryString");
    });

    test("getSignOutPath should return appropriate path with appropriate query string", () => {
        expect(getSignOutPath()).toEqual("/sign-out?testQueryString");
    });

    test("createPathWithTimeRangeQueryString should return path with appropriate query string", () => {
        expect(createPathWithRecognizedQueryString("/testPath")).toEqual(
            "/testPath?testQueryString"
        );
    });
});
