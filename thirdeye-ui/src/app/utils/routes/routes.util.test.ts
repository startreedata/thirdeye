import {
    createPathWithRecognizedQueryString,
    getAlertsAllPath,
    getAlertsCreatePath,
    getAlertsPath,
    getAlertsUpdatePath,
    getAlertsViewPath,
    getAnomaliesAllPath,
    getAnomaliesPath,
    getAnomaliesViewIndexPath,
    getAnomaliesViewPath,
    getBasePath,
    getConfigurationPath,
    getHomePath,
    getLoginPath,
    getLogoutPath,
    getMetricsAllPath,
    getMetricsPath,
    getMetricsViewPath,
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
    it("getBasePath should return appropriate path with appropriate query string", () => {
        expect(getBasePath()).toEqual("/?testQueryString");
    });

    it("getHomePath should return appropriate path with appropriate query string", () => {
        expect(getHomePath()).toEqual("/home?testQueryString");
    });

    it("getAlertsPath should return appropriate path with appropriate query string", () => {
        expect(getAlertsPath()).toEqual("/alerts?testQueryString");
    });

    it("getAlertsAllPath should return appropriate path with appropriate query string", () => {
        expect(getAlertsAllPath()).toEqual("/alerts/all?testQueryString");
    });

    it("getAlertsViewPath should return appropriate path with appropriate query string for id", () => {
        expect(getAlertsViewPath(1)).toEqual(
            "/alerts/view/id/1?testQueryString"
        );
    });

    it("getAlertsCreatePath should return appropriate path with appropriate query string", () => {
        expect(getAlertsCreatePath()).toEqual("/alerts/create?testQueryString");
    });

    it("getAlertsUpdatePath should return appropriate path with appropriate query string for id", () => {
        expect(getAlertsUpdatePath(1)).toEqual(
            "/alerts/update/id/1?testQueryString"
        );
    });

    it("getAnomaliesPath should return appropriate path with appropriate query string", () => {
        expect(getAnomaliesPath()).toEqual("/anomalies?testQueryString");
    });

    it("getAnomaliesAllPath should return appropriate path with appropriate query string", () => {
        expect(getAnomaliesAllPath()).toEqual("/anomalies/all?testQueryString");
    });

    it("getAnomaliesViewPath should return appropriate path with appropriate query string for id", () => {
        expect(getAnomaliesViewPath(1)).toEqual(
            "/anomalies/view/id/1?testQueryString"
        );
    });

    it("getAnomaliesViewIndexPath should return appropriate path with appropriate query string for id", () => {
        expect(getAnomaliesViewIndexPath(1)).toEqual(
            "/anomalies/view/id/1/index?testQueryString"
        );
    });

    it("getConfigurationPath should return appropriate path with appropriate query string", () => {
        expect(getConfigurationPath()).toEqual(
            "/configuration?testQueryString"
        );
    });

    it("getSubscriptionGroupsPath should return appropriate path with appropriate query string", () => {
        expect(getSubscriptionGroupsPath()).toEqual(
            "/configuration/subscription-groups?testQueryString"
        );
    });

    it("getSubscriptionGroupsAllPath should return appropriate path with appropriate query string", () => {
        expect(getSubscriptionGroupsAllPath()).toEqual(
            "/configuration/subscription-groups/all?testQueryString"
        );
    });

    it("getSubscriptionGroupsViewPath should return appropriate path with appropriate query string for id", () => {
        expect(getSubscriptionGroupsViewPath(1)).toEqual(
            "/configuration/subscription-groups/view/id/1?testQueryString"
        );
    });

    it("getSubscriptionGroupsCreatePath should return appropriate path with appropriate query string", () => {
        expect(getSubscriptionGroupsCreatePath()).toEqual(
            "/configuration/subscription-groups/create?testQueryString"
        );
    });

    it("getSubscriptionGroupsUpdatePath should return appropriate path with appropriate query string for id", () => {
        expect(getSubscriptionGroupsUpdatePath(1)).toEqual(
            "/configuration/subscription-groups/update/id/1?testQueryString"
        );
    });

    it("getMetricsPath should return appropriate path with appropriate query string", () => {
        expect(getMetricsPath()).toEqual(
            "/configuration/metrics?testQueryString"
        );
    });

    it("getMetricsAllPath should return appropriate path with appropriate query string", () => {
        expect(getMetricsAllPath()).toEqual(
            "/configuration/metrics/all?testQueryString"
        );
    });

    it("getMetricsViewPath should return appropriate path with appropriate query string for id", () => {
        expect(getMetricsViewPath(1)).toEqual(
            "/configuration/metrics/view/id/1?testQueryString"
        );
    });

    it("getLoginPath should return appropriate path with appropriate query string", () => {
        expect(getLoginPath()).toEqual("/login?testQueryString");
    });

    it("getLogoutPath should return appropriate path with appropriate query string", () => {
        expect(getLogoutPath()).toEqual("/logout?testQueryString");
    });

    it("createPathWithTimeRangeQueryString should return path with appropriate query string", () => {
        expect(createPathWithRecognizedQueryString("/testPath")).toEqual(
            "/testPath?testQueryString"
        );
    });
});
