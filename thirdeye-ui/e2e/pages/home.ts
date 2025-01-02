/*
 * Copyright 2024 StarTree Inc
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
import { expect, Page } from "@playwright/test";
import { BasePage } from "./base";
// import { getUiAnomaly } from '../../src/app/utils/anomalies/anomalies.util'

export class HomePage extends BasePage {
    readonly page: Page;
    alertResponseData: any;
    subscriptionResponseData: any;
    anomalyResponseData: any;

    constructor(page: Page) {
        super(page);
        this.page = page;
    }

    async resolveApis() {
        const [alertApiResponse, subscriptionApiResponse, anomalyResponse] =
            await Promise.all([
                this.page.waitForResponse(
                    (response) =>
                        response.url().includes("/api/alerts") &&
                        response.status() === 200
                ),
                this.page.waitForResponse(
                    (response) =>
                        response.url().includes("/api/subscription-groups") &&
                        response.status() === 200
                ),
                this.page.waitForResponse(
                    (response) =>
                        response.url().includes("/api/anomalies") &&
                        response.status() === 200
                ),
            ]);

        this.alertResponseData = await alertApiResponse.json();
        this.subscriptionResponseData = await subscriptionApiResponse.json();
        this.anomalyResponseData = await anomalyResponse.json();
    }

    async checkHeadingSection() {
        await expect(this.page.locator("h4")).toHaveText("StarTree ThirdEye");
        await expect(this.page.locator("h6").first()).toHaveText(
            "Automated Metrics Monitoring and Anomaly Detection"
        );
    }

    async checkSummaryCards() {
        await expect(this.page.getByTestId("alert-summary-header")).toHaveText(
            "Monitor & Detect anomalies"
        );
        const alertSummaryContent = this.page.getByTestId(
            "alert-summary-content"
        );
        const alertSummaryInfo = alertSummaryContent.getByTestId("alert-count");
        expect(alertSummaryInfo).toHaveText(
            `${this.alertResponseData.length}Active alerts`
        );
        const createAlertLink = alertSummaryContent.getByTestId("alert-create");
        expect(createAlertLink).toHaveText("Create Alert");

        await expect(
            this.page.getByTestId("subscription-summary-header")
        ).toHaveText("Subscribe to alerts");
        const subscriptionSummaryContent = this.page.getByTestId(
            "subscription-summary-content"
        );
        const subscriptionSummaryInfo =
            subscriptionSummaryContent.getByTestId("subscription-count");
        expect(subscriptionSummaryInfo).toHaveText(
            `${this.subscriptionResponseData.length}Active groups`
        );
        const createSubscriptionLink = subscriptionSummaryContent.getByTestId(
            "subscription-create"
        );
        expect(createSubscriptionLink).toHaveText("Setup notifications");

        await expect(this.page.getByTestId("admin-summary-header")).toHaveText(
            "Dashboards"
        );
        const adminSummaryContent = this.page.getByTestId(
            "admin-summary-content"
        );
        expect(adminSummaryContent).toHaveText("View Detection Failures");
    }

    async checkActiveAlerts() {
        const tableContainer = this.page.getByTestId("active-alerts");
        tableContainer.locator("h5", { hasText: "Latest active alerts" });

        const tableHeaders = tableContainer.locator("table thead th");
        await expect(tableHeaders).toHaveCount(3);

        await expect(tableHeaders.nth(0)).toHaveText("Alert Name");
        await expect(tableHeaders.nth(1)).toHaveText("Status");
        await expect(tableHeaders.nth(2)).toHaveText("Created");

        const topAlerts = this.alertResponseData.reverse().slice(0, 5);
        const totalAlertsToShow = topAlerts.length;

        const tableRows = tableContainer.locator("table tbody tr");
        expect(tableRows).toHaveCount(totalAlertsToShow);
        const allRows = await tableRows.all();
        for (let i = 0; i < totalAlertsToShow; i++) {
            const allCoulmns = await allRows[i].locator("td").all();
            const alert = topAlerts[i];
            for (let k = 0; k < 3; k++) {
                k === 0 && (await expect(allCoulmns[0]).toHaveText(alert.name));
                k === 1 &&
                    (await expect(allCoulmns[1]).toHaveText(
                        alert.active ? "Healthy" : "Unhealthy"
                    ));
            }
        }
    }

    async checkSubscriptionGroups() {
        const tableContainer = this.page.getByTestId("subscription-groups");
        tableContainer.locator("h5", { hasText: "Latest Subscription Groups" });

        const tableHeaders = tableContainer.locator("table thead th");
        await expect(tableHeaders).toHaveCount(2);

        await expect(tableHeaders.nth(0)).toHaveText("Subscription name");
        await expect(tableHeaders.nth(1)).toHaveText("Created");

        const topSubsciptionGroups = this.subscriptionResponseData
            .reverse()
            .slice(0, 5);
        const totalSubscriptionGroupsToShow = topSubsciptionGroups.length;

        const tableRows = tableContainer.locator("table tbody tr");
        expect(tableRows).toHaveCount(totalSubscriptionGroupsToShow);
        const allRows = await tableRows.all();
        for (let i = 0; i < totalSubscriptionGroupsToShow; i++) {
            const allCoulmns = await allRows[i].locator("td").all();
            const subscriptionGroup = topSubsciptionGroups[i];
            for (let k = 0; k < 2; k++) {
                k === 0 &&
                    (await expect(allCoulmns[0]).toHaveText(
                        subscriptionGroup.name
                    ));
                // k === 1 && await expect(allCoulmns[1]).toHaveText(subscriptionGroup.created)
            }
        }
    }

    async checkRecentAnomalies() {
        const tableContainer = this.page.getByTestId("recent-anomalies");
        tableContainer.locator("h5", { hasText: "Recent Anomalies" });

        const tableHeaders = tableContainer.locator("table thead th");
        await expect(tableHeaders).toHaveCount(8);

        await expect(tableHeaders.nth(0)).toHaveText("Anomaly Id");
        await expect(tableHeaders.nth(1)).toHaveText("Alert Name");
        await expect(tableHeaders.nth(2)).toHaveText("Metric");
        await expect(tableHeaders.nth(3)).toHaveText("Started");
        await expect(tableHeaders.nth(4)).toHaveText("Ended");
        await expect(tableHeaders.nth(5)).toHaveText("Deviation");

        const topAnomalies = this.anomalyResponseData.reverse().slice(0, 10);
        const totalAnomaliesToShow = topAnomalies.length;

        const tableRows = tableContainer.locator("table tbody tr");
        expect(tableRows).toHaveCount(totalAnomaliesToShow);
        const allRows = await tableRows.all();
        // for(let i=0;i<totalAnomaliesToShow;i++) {
        //   const allCoulmns = await allRows[i].locator('td').all()
        //   const anomaly = getUiAnomaly(topAnomalies[i])
        //   for(let k=0;k<6;k++) {
        //     k === 0 && await expect(allCoulmns[0]).toHaveText(anomaly.id.toString())
        //     k === 1 && await expect(allCoulmns[1]).toHaveText('-')
        //     k === 2 && await expect(allCoulmns[2]).toHaveText(anomaly.metricName)
        //     // k === 3 && await expect(allCoulmns[3]).toHaveText(anomaly.startTime)
        //     // k === 4 && await expect(allCoulmns[4]).toHaveText(anomaly.endTime)
        //     k === 5 && await expect(allCoulmns[5]).toHaveText(anomaly.deviation)
        //   }
        // }
    }
}
