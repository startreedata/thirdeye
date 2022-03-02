import {
    createPathWithRecognizedQueryString,
    getAlertsAllPath,
    getAlertsCreatePath,
    getAlertsPath,
    getAlertsUpdatePath,
    getAlertsViewPath,
    getAnomaliesAllPath,
    getAnomaliesAnomalyPath,
    getAnomaliesAnomalyViewPath,
    getAnomaliesPath,
    getBasePath,
    getConfigurationPath,
    getHomePath,
    getLoginPath,
    getLogoutPath,
    getMetricsAllPath,
    getMetricsPath,
    getMetricsViewPath,
    getRootCauseAnalysisForAnomalyIndexPath,
    getRootCauseAnalysisForAnomalyPath,
    getSubscriptionGroupsAllPath,
    getSubscriptionGroupsCreatePath,
    getSubscriptionGroupsPath,
    getSubscriptionGroupsUpdatePath,
    getSubscriptionGroupsViewPath,
} from "./routes.util";

jest.mock("../params/params.util", () => ({
    getRecognizedQuery: jest.fn().mockReturnValue({
        toString: jest.fn().mockReturnValue("testQueryString"),
    }),
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
        expect(getAlertsViewPath(1)).toEqual("/alerts/1?testQueryString");
    });

    it("getAlertsCreatePath should return appropriate path with appropriate query string", () => {
        expect(getAlertsCreatePath()).toEqual("/alerts/create?testQueryString");
    });

    it("getAlertsUpdatePath should return appropriate path with appropriate query string for id", () => {
        expect(getAlertsUpdatePath(1)).toEqual(
            "/alerts/1/update?testQueryString"
        );
    });

    it("getAnomaliesPath should return appropriate path with appropriate query string", () => {
        expect(getAnomaliesPath()).toEqual("/anomalies?testQueryString");
    });

    it("getAnomaliesAllPath should return appropriate path with appropriate query string", () => {
        expect(getAnomaliesAllPath()).toEqual("/anomalies/all?testQueryString");
    });

    it("getAnomaliesAnomalyPath should return appropriate path with appropriate query string for id", () => {
        expect(getAnomaliesAnomalyPath(1)).toEqual("/anomalies/1");
    });

    it("getAnomaliesAnomalyViewPath should return appropriate path with appropriate query string for id", () => {
        expect(getAnomaliesAnomalyViewPath(1)).toEqual("/anomalies/1/view");
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

    it("getRootCauseAnalysisForAnomalyPath should return appropriate path with appropriate query string for anomaly id", () => {
        expect(getRootCauseAnalysisForAnomalyPath(1)).toEqual(
            "/root-cause-analysis/anomaly/1?testQueryString"
        );
    });

    it("getRootCauseAnalysisForAnomalyIndexPath should return appropriate path with appropriate query string for anomaly id", () => {
        expect(getRootCauseAnalysisForAnomalyIndexPath(1)).toEqual(
            "/root-cause-analysis/anomaly/1/index?testQueryString"
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
