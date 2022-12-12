/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { DateTime } from "luxon";
import {
    createPathWithRecognizedQueryString,
    generateDateRangeDaysFromNow,
    generateDateRangeMonthsFromNow,
    getAlertsAlertViewPath,
    getAlertsAllPath,
    getAlertsCreateNewAdvancedPath,
    getAlertsCreateNewSimplePath,
    getAlertsCreatePath,
    getAlertsPath,
    getAlertsUpdatePath,
    getAlertTemplatesAllPath,
    getAlertTemplatesCreatePath,
    getAlertTemplatesPath,
    getAlertTemplatesUpdatePath,
    getAlertTemplatesViewPath,
    getAnomaliesAllPath,
    getAnomaliesAllRangePath,
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
    getRootCauseAnalysisForAnomalyInvestigatePath,
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
        expect(getBasePath()).toEqual("/");
    });

    it("getHomePath should return appropriate path with appropriate query string", () => {
        expect(getHomePath()).toEqual("/home");
    });

    it("getAlertsPath should return appropriate path with appropriate query string", () => {
        expect(getAlertsPath()).toEqual("/alerts?testQueryString");
    });

    it("getAlertsAllPath should return appropriate path with appropriate query string", () => {
        expect(getAlertsAllPath()).toEqual("/alerts/all?testQueryString");
    });

    it("getAlertsAlertViewPath should return appropriate path with appropriate query string for id", () => {
        expect(getAlertsAlertViewPath(1)).toEqual(
            "/alerts/1/view?testQueryString"
        );
    });

    it("getAlertsCreatePath should return appropriate path with appropriate query string", () => {
        expect(getAlertsCreatePath()).toEqual("/alerts/create?testQueryString");
    });

    it("getAlertsCreateSimplePath should return appropriate path with appropriate query string", () => {
        expect(getAlertsCreateNewSimplePath()).toEqual(
            "/alerts/create/new/simple?testQueryString"
        );
    });

    it("getAlertsCreateAdvancedPath should return appropriate path with appropriate query string", () => {
        expect(getAlertsCreateNewAdvancedPath()).toEqual(
            "/alerts/create/new/advanced?testQueryString"
        );
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

    it("getAnomaliesAllRangePath should return appropriate path with appropriate query string", () => {
        expect(getAnomaliesAllRangePath()).toEqual(
            "/anomalies/all/range?testQueryString"
        );
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
            "/configuration/subscription-groups/all"
        );
    });

    it("getSubscriptionGroupsViewPath should return appropriate path with appropriate query string for id", () => {
        expect(getSubscriptionGroupsViewPath(1)).toEqual(
            "/configuration/subscription-groups/view/id/1"
        );
    });

    it("getSubscriptionGroupsCreatePath should return appropriate path with appropriate query string", () => {
        expect(getSubscriptionGroupsCreatePath()).toEqual(
            "/configuration/subscription-groups/create"
        );
    });

    it("getSubscriptionGroupsUpdatePath should return appropriate path with appropriate query string for id", () => {
        expect(getSubscriptionGroupsUpdatePath(1)).toEqual(
            "/configuration/subscription-groups/update/id/1"
        );
    });

    it("getMetricsPath should return appropriate path with appropriate query string", () => {
        expect(getMetricsPath()).toEqual("/configuration/metrics");
    });

    it("getMetricsAllPath should return appropriate path with appropriate query string", () => {
        expect(getMetricsAllPath()).toEqual("/configuration/metrics/all");
    });

    it("getMetricsViewPath should return appropriate path with appropriate query string for id", () => {
        expect(getMetricsViewPath(1)).toEqual(
            "/configuration/metrics/view/id/1"
        );
    });

    it("getRootCauseAnalysisForAnomalyPath should return appropriate path with appropriate query string for anomaly id", () => {
        expect(getRootCauseAnalysisForAnomalyPath(1)).toEqual(
            "/root-cause-analysis/anomaly/1"
        );
    });

    it("getRootCauseAnalysisForAnomalyInvestigatePath should return appropriate path with for anomaly id", () => {
        expect(getRootCauseAnalysisForAnomalyInvestigatePath(1)).toEqual(
            "/root-cause-analysis/anomaly/1/investigate"
        );
    });

    it("getLoginPath should return appropriate path with appropriate query string", () => {
        expect(getLoginPath()).toEqual("/login");
    });

    it("getLogoutPath should return appropriate path with appropriate query string", () => {
        expect(getLogoutPath()).toEqual("/logout");
    });

    it("createPathWithTimeRangeQueryString should return path with appropriate query string", () => {
        expect(createPathWithRecognizedQueryString("/testPath")).toEqual(
            "/testPath?testQueryString"
        );
    });

    it("getAlertTemplatesPath should return appropriate path", () => {
        expect(getAlertTemplatesPath()).toEqual(
            "/configuration/alert-templates"
        );
    });

    it("getAlertTemplatesAllPath should return appropriate path", () => {
        expect(getAlertTemplatesAllPath()).toEqual(
            "/configuration/alert-templates/all"
        );
    });

    it("getAlertTemplatesCreatePath should return appropriate path", () => {
        expect(getAlertTemplatesCreatePath()).toEqual(
            "/configuration/alert-templates/create"
        );
    });

    it("getAlertTemplatesViewPath should return appropriate path for id", () => {
        expect(getAlertTemplatesViewPath(1)).toEqual(
            "/configuration/alert-templates/1/view"
        );
    });

    it("getAlertTemplatesUpdatePath should return appropriate path with appropriate for id", () => {
        expect(getAlertTemplatesUpdatePath(1)).toEqual(
            "/configuration/alert-templates/1/update"
        );
    });

    it("generateDateRangeMonthsFromNow should return expected values", () => {
        const range = generateDateRangeMonthsFromNow(
            1,
            // Thursday, May 26, 2022 12:00:00 PM (GMT)
            DateTime.fromMillis(1653566400000),
            "day"
        );

        // about 56 days (April 01 to May 26 => 30 + 26)
        expect(range[1] - range[0]).toEqual(4838399999);
    });

    it("generateDateRangeDaysFromNow should return expected values", () => {
        const range = generateDateRangeDaysFromNow(
            3,
            // Sunday, August 07, 2022 12:00:00 PM
            DateTime.fromMillis(1659853800000),
            "day"
        );

        // about 4 days (Aug 07 - 3 days => Aug 04)
        expect(range[1] - range[0]).toEqual(345599999);
    });
});
