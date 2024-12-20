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

export class AnomalyViewPage extends BasePage {
    readonly page: Page;
    anomaly: any;
    anomalyAlertId: any;
    anomalyAlert: any;
    allAnomalies: any;
    enumerationItem: any;

    constructor(page: Page) {
        super(page);
        this.page = page;
    }

    async resolveListAPIs() {
        const [allANomaliesResponse] = await Promise.all([
            this.page.waitForResponse(
                (response) =>
                    response.url().includes("/api/anomalies?") &&
                    response.status() === 200
            ),
        ]);
        this.allAnomalies = await allANomaliesResponse.json();
    }

    async resolveViewApis() {
        const allPromises = [
            this.page.waitForResponse(
                (response) =>
                    response
                        .url()
                        .includes(`/api/anomalies/${this.anomaly.id}`) &&
                    response.status() === 200
            ),
            this.page.waitForResponse(
                (response) =>
                    response
                        .url()
                        .includes(`/api/alerts/${this.anomaly.alert.id}`) &&
                    response.status() === 200
            ),
            this.page.waitForResponse(
                (response) =>
                    response.url().includes("/api/alerts/insights") &&
                    response.status() === 200
            ),
            this.page.waitForResponse(
                (response) =>
                    response.url().includes("/api/alerts/evaluate") &&
                    response.status() === 200
            ),
            this.page.waitForResponse(
                (response) =>
                    response
                        .url()
                        .includes(
                            `/api/rca/investigations?anomaly.id=${this.anomaly.id}`
                        ) && response.status() === 200
            ),
        ];
        if (this.anomaly.enumerationItem?.id) {
            allPromises.push(
                this.page.waitForResponse(
                    (response) =>
                        response
                            .url()
                            .includes(
                                `/api/enumeration-items/${this.anomaly.enumerationItem?.id}`
                            ) && response.status() === 200
                )
            );
        }
        const [
            anomalyApiResponse,
            alertApiResponse,
            insightApiResponse,
            evaluateApiResponse,
            investigateApiResponse,
            enumerationItemApiResponse,
        ] = await Promise.all(allPromises);
        this.anomalyAlert = await alertApiResponse.json();
        if (enumerationItemApiResponse) {
            this.enumerationItem = await enumerationItemApiResponse.json();
        }
    }

    async goToAnomalyView() {
        await this.page.goto(
            "http://localhost:7004/anomalies/all/range/anomalies-list?*"
        );
        await this.resolveListAPIs();
        const topAnomaly = this.allAnomalies.sort(
            (a, b) => b.startTime - a.startTime
        )[0];
        this.anomaly = topAnomaly;
        const tableRows = this.page.locator(".BaseTable__row");
        const firstAnomaly = tableRows.nth(0);
        const anomalyColumn = firstAnomaly.locator(">div").nth(1).locator("a");

        await anomalyColumn.click();
        await this.resolveViewApis();
        await this.page.waitForURL(
            `http://localhost:7004/anomalies/${topAnomaly.id}/v2/view/validate?*`
        );
    }

    async checkHeaderAndBreadcrumbs() {
        const breadrumbsElem = this.page.locator(
            '[data-testId="breadcrumbs"] > nav > ol > li'
        );
        await expect(breadrumbsElem.nth(0)).toHaveText("Anomalies");
        await expect(breadrumbsElem.nth(1)).toHaveText("/");
        let alertNameBreadcrumb = this.anomalyAlert.name;
        if (this.anomaly.enumerationItem.id) {
            alertNameBreadcrumb = `${alertNameBreadcrumb} (${this.enumerationItem.name})`;
        }
        await expect(breadrumbsElem.nth(2)).toHaveText(alertNameBreadcrumb);

        const anchorForBreadcrumbToAnomalyList = breadrumbsElem
            .nth(0)
            .locator("a");
        expect(anchorForBreadcrumbToAnomalyList).toHaveAttribute(
            "href",
            /^\/anomalies\/all/
        );

        const anchorForBreadcrumbToAnomalyAlert = breadrumbsElem
            .nth(2)
            .locator("a");
        const alertId = this.anomaly.alert.id;
        const alertHref = new RegExp(`^/alerts/${alertId.toString()}`);
        expect(anchorForBreadcrumbToAnomalyAlert).toHaveAttribute(
            "href",
            alertHref
        );

        await expect(breadrumbsElem.nth(4)).toHaveText(
            this.anomaly.id.toString()
        );

        await expect(this.page.locator("h4").nth(0)).toHaveText(
            `Anomaly #${this.anomaly.id}`
        );

        const deleteAnomalyBtn = this.page.getByTestId("delete-anomaly-btn");
        expect(deleteAnomalyBtn).toHaveCount(1);

        const anomalyMetric = this.page.getByTestId("anomaly-metric");
        const aggregationFunction =
            this.anomalyAlert.templateProperties.aggregationFunction;
        const metric = this.anomaly.metadata.metric.name;
        const dataset = this.anomaly.metadata.dataset.name;
        expect(anomalyMetric).toHaveText(
            `Metric:${dataset}:${aggregationFunction}(${metric})`
        );
    }

    async checkSummary() {
        const anomalySummaryData = this.page.locator(
            '[data-testId="anomaly-summary"]'
        );
        await expect(anomalySummaryData.locator(">div")).toHaveCount(5);
        const anomalyStart = anomalySummaryData.getByTestId("anomaly-start");
        expect(anomalyStart.getByTestId("label")).toHaveText("Anomaly start");
        const anomalyEnd = anomalySummaryData.getByTestId("anomaly-end");
        expect(anomalyEnd.getByTestId("label")).toHaveText("Anomaly end");
        const anomalyDuration =
            anomalySummaryData.getByTestId("anomaly-duration");
        expect(anomalyDuration.getByTestId("label")).toHaveText(
            "Anomaly duration"
        );
        const anomalyDeviation =
            anomalySummaryData.getByTestId("anomaly-deviation");
        expect(anomalyDeviation.getByTestId("label")).toHaveText(
            "Deviation (Current / Predicted)"
        );
    }

    async checkConfirmAnomalyActions() {
        await expect(
            this.page.getByTestId("feedback-collector-text")
        ).toHaveText(
            "Confirm anomalyCompare with previous time period to confirm this is an anomaly"
        );
        await expect(this.page.getByTestId("not-an-anomaly")).toHaveText(
            "No, this is not an anomaly"
        );
        await expect(this.page.getByTestId("is-an-anomaly")).toHaveText(
            "Yes, this is an anomaly"
        );
        await expect(this.page.getByTestId("investigate-anomaly")).toHaveText(
            "Investigate Anomaly"
        );
        await expect(this.page.getByTestId("add-previous-period")).toHaveText(
            "Add previous period to compare"
        );
    }
}
