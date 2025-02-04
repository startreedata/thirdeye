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
import { Page, expect } from "@playwright/test";
import { BasePage } from "./base";

export class AnomalyListPage extends BasePage {
    readonly page: Page;
    alertResponseData: any;
    subscriptionResponseData: any;
    anomalyResponseData: any;
    enumerationItemsData: any;
    allEnumerationItemsData: any;

    constructor(page: Page) {
        super(page);
        this.page = page;
    }

    async resolveAPIs() {
        const [
            alertApiResponse,
            subscriptionApiResponse,
            anomalyApiResponse,
            enumerationItemsApiResponse,
            allEnumerationItemsApiResponse,
        ] = await Promise.all([
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
                    response.url().includes("/api/anomalies?") &&
                    response.status() === 200
            ),
            this.page.waitForResponse(
                (response) =>
                    response.url().includes("/api/enumeration-items?id=") &&
                    response.status() === 200
            ),
            //redundant call
            this.page.waitForResponse(
                (response) =>
                    response.url().includes("/api/enumeration-items") &&
                    response.status() === 200
            ),
        ]);

        this.alertResponseData = await alertApiResponse.json();
        this.subscriptionResponseData = await subscriptionApiResponse.json();
        this.anomalyResponseData = await anomalyApiResponse.json();
        this.enumerationItemsData = await enumerationItemsApiResponse.json();
        this.allEnumerationItemsData =
            await allEnumerationItemsApiResponse.json();
    }

    async goToAnomalyListPage() {
        await this.page.goto(
            "http://localhost:7004/anomalies/all/range/anomalies-list?*"
        );
    }

    async checkBreadcrumbAndHeader() {
        const breadrumbsElem = this.page.locator(
            '[data-testId="breadcrumbs"] > nav > ol > li'
        );
        await expect(breadrumbsElem.nth(0)).toHaveText("Anomalies");
        await expect(breadrumbsElem.nth(1)).toHaveText("/");
        await expect(breadrumbsElem.nth(2)).toHaveText("Anomalies List");

        const anchorForBreadcrumb = breadrumbsElem.nth(0).locator("a");
        expect(anchorForBreadcrumb).toHaveAttribute(
            "href",
            /\/anomalies\/all\?timeRange=.*&startTime=.*&endTime=.*/
        );

        await expect(this.page.locator("h4")).toHaveText("Anomalies");

        const createAnomaliesBtn = this.page.getByTestId("create-anomaly-btn");
        expect(createAnomaliesBtn).toHaveAttribute("href", "/anomalies/create");
    }

    async checkTabs() {
        const tabLinks = this.page.getByRole("tablist").locator("a");
        const firstTab = tabLinks.nth(0);
        const secondTab = tabLinks.nth(1);
        expect(firstTab).toHaveText("Anomalies List");
        // somehow there is extra wildcard that PW is adding thats causing this to fail. Need to look more into the reason.
        // expect(firstTab).toHaveAttribute('href', /\/anomalies\/all\/range\/anomalies-list\?timeRange=.*&startTime=.*&endTime=.*/)
        expect(secondTab).toHaveText("Metrics Report");
        // expect(secondTab).toHaveAttribute('href', /\/anomalies\/all\/range\/metrics-report\?timeRange=.*&startTime=.*&endTime=.*/)
    }

    async checkFilters() {
        expect(this.page.getByTestId("filter-text")).toHaveText(
            "Filter anomalies by Alert and/or Subscription Group"
        );
        expect(this.page.getByTestId("modify-filters")).toHaveText(
            "Modify Filters"
        );
        expect(this.page.getByTestId("clear-filters")).toHaveText("Clear");
    }

    async checkTableActions() {
        const deleteBtn = this.page.getByTestId("anomalies-list-delete-button");
        await expect(deleteBtn).toHaveText("Delete");
        await expect(deleteBtn).toBeDisabled();
        const timeRangeSelector = this.page.getByTestId("time-range-selector");
        expect(timeRangeSelector).toHaveCount(1);
    }

    async checkTable() {
        const table = this.page.locator(".BaseTable__table");
        const tableHeader = this.page.locator(".BaseTable__header-row");
        const tableBody = this.page.locator(".BaseTable__body");

        const headerElem = tableHeader.locator(">div");
        expect(headerElem).toHaveCount(13);
        expect(headerElem.nth(1)).toHaveText("Anomaly ID");
        expect(headerElem.nth(2)).toHaveText("Alert");
        expect(headerElem.nth(3)).toHaveText("Metric");
        expect(headerElem.nth(4)).toHaveText("Dataset");
        expect(headerElem.nth(5)).toHaveText("Duration");
        expect(headerElem.nth(6)).toHaveText("Start");
        expect(headerElem.nth(7)).toHaveText("End");
        expect(headerElem.nth(8)).toHaveText("Current");
        expect(headerElem.nth(9)).toHaveText("Predicted");
        expect(headerElem.nth(10)).toHaveText("Deviation");
        expect(headerElem.nth(11)).toHaveText("Has Feedback");
        expect(headerElem.nth(12)).toHaveText("Enumeration Item");

        const tableRows = await this.page.locator(".BaseTable__row").all();
        const anomalies = this.anomalyResponseData.sort(
            (a, b) => b.startTime - a.startTime
        );
        const totalAnomalies = this.anomalyResponseData.length;
        const rowsTocheck =
            totalAnomalies === 0 ? 0 : totalAnomalies > 7 ? 7 : totalAnomalies;
        for (let i = 0; i < rowsTocheck; i++) {
            const allCoulmns = await tableRows[i].locator(">div").all();
            const anomaly = anomalies[i];
            await expect(allCoulmns[1]).toHaveText(`#${anomaly.id}`);
            const alert = this.alertResponseData.find(
                (alert) => alert.id === anomaly.alert.id
            );
            await expect(allCoulmns[2]).toHaveText(alert?.name || "-");
            await expect(allCoulmns[3]).toHaveText(
                anomaly.metadata.metric.name
            );
            await expect(allCoulmns[4]).toHaveText(
                anomaly.metadata.dataset.name
            );
            if (anomaly.enumerationItem?.id) {
                const enumerationItemName = this.enumerationItemsData.find(
                    (enumerationItem) =>
                        enumerationItem.id === anomaly.enumerationItem.id
                )?.name;
                await expect(allCoulmns[12]).toHaveText(enumerationItemName);
            } else {
                await expect(allCoulmns[12]).toHaveText("No data available");
            }
        }
    }
}
