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

export class AlertDetailsPage extends BasePage {
    readonly page: Page;
    alertsApiResponseData: any;
    anomaliesApiResponseData: any;
    anomalyApiResponseData: any;
    evaluateResponseData: any;

    constructor(page: Page) {
        super(page);
        this.page = page;
    }

    async resolveApis() {
        const [alertsApiResponse] = await Promise.all([
            this.page.waitForResponse(
                (response) =>
                    response.url().includes("/api/alerts") &&
                    response.status() === 200
            ),
        ]);

        this.alertsApiResponseData = await alertsApiResponse.json();
    }

    async resolveAnomaliesApis() {
        const [anomaliesApiResponse] = await Promise.all([
            this.page.waitForResponse(
                (response) =>
                    response.url().includes("/api/anomalies") &&
                    response.status() === 200
            ),
        ]);

        this.anomaliesApiResponseData = await anomaliesApiResponse.json();
    }

    async resolveAnomalyApis() {
        const [anomalyApiResponse] = await Promise.all([
            this.page.waitForResponse(
                (response) =>
                    response
                        .url()
                        .includes(
                            `/api/anomalies/${this.anomaliesApiResponseData[0].id}`
                        ) && response.status() === 200
            ),
        ]);

        this.anomalyApiResponseData = await anomalyApiResponse.json();
    }

    async resolveInvestigateAnomalyPageApis() {
        const [anomalyApiResponse] = await Promise.all([
            this.page.waitForResponse(
                (response) =>
                    response.url().includes(`/api/rca/metrics/heatmap`) &&
                    response.status() === 200
            ),
            this.page.waitForResponse(
                (response) =>
                    response.url().includes(`/api/rca/dim-analysis`) &&
                    response.status() === 200
            ),
            this.page.waitForResponse(
                (response) =>
                    response.url().includes(`/api/alerts/evaluate`) &&
                    response.status() === 200
            ),
        ]);
    }

    async goToAlertDetailsPage() {
        await this.page.goto("http://localhost:7004/#access_token=''");
        await this.page.waitForSelector("h4:has-text('StarTree ThirdEye')", {
            timeout: 10000,
            state: "visible",
        });
        await this.page.goto("http://localhost:7004/alerts/all");
    }

    async checkHeader() {
        await expect(this.page.locator("h4")).toHaveText("Alerts");
    }

    async checkAlertHeader() {
        await expect(this.page.locator("h4")).toHaveText(
            this.alertsApiResponseData[this.alertsApiResponseData.length - 1]
                ?.name
        );
    }

    async checkAlertIsActiveOrDeactive(isActive = true) {
        await expect(this.page.locator("h6").nth(1)).toHaveText(
            isActive ? "Alert is active" : "Alert is inactive"
        );
    }

    async activeDeactiveAlert(activate = true) {
        await this.page.getByRole("button", { name: "Options" }).click();
        await this.page
            .getByRole("menuitem", {
                name: activate ? "Activate Alert" : "Deactivate Alert",
            })
            .click();
    }

    async openAnomalies() {
        await this.page
            .locator("span")
            .filter({ hasText: "Anomalies" })
            .first()
            .click();
    }

    async checkAnomaliesCount() {
        const node = this.page
            .locator("span")
            .filter({ hasText: "Anomalies" })
            .first()
            .isDisabled();
        return node;
    }
    async openFirstAlert() {
        await this.page
            .locator("a")
            .filter({
                hasText:
                    this.alertsApiResponseData[
                        this.alertsApiResponseData.length - 1
                    ]?.name,
            })
            .click();
    }

    async openAnomalieFromTable() {
        await this.page
            .locator("a")
            .filter({
                hasText:
                    this.anomaliesApiResponseData[
                        this.anomaliesApiResponseData.length - 1
                    ]?.id,
            })
            .click();
    }

    async openInvestigateAnomalyPage() {
        await this.page
            .getByRole("button", { name: "Investigate Anomaly" })
            .click();
    }

    async assertPageComponents() {
        await expect(this.page.locator("h4").first()).toHaveText(
            /^Anomaly #\d+(\s\(\w+\))?$/
        );
        expect(
            this.page.locator("li").filter({ hasText: "Anomalies" })
        ).toBeDefined();
        expect(
            this.page.locator("li").filter({
                hasText:
                    this.alertsApiResponseData[
                        this.alertsApiResponseData.length - 1
                    ]?.name,
            })
        ).toBeDefined();
        expect(
            this.page.locator("li").filter({
                hasText:
                    this.anomaliesApiResponseData[
                        this.anomaliesApiResponseData.length - 1
                    ]?.id,
            })
        ).toBeDefined();
        await expect(this.page.locator("h4").nth(1)).toHaveText(
            "Confirm anomaly"
        );
        expect(
            this.page.locator("p").filter({ hasText: "Anomaly start" })
        ).toBeDefined();
        expect(
            this.page.locator("p").filter({ hasText: "Anomaly end" })
        ).toBeDefined();
        expect(
            this.page.locator("p").filter({ hasText: "Anomaly duration" })
        ).toBeDefined();
        expect(
            this.page.locator("p").filter({ hasText: "Seasonality" })
        ).toBeDefined();
        expect(
            this.page
                .locator("p")
                .filter({ hasText: "Deviation (Current / Predicted)" })
        ).toBeDefined();
    }

    async assertInvestigatePageComponents() {
        await expect(this.page.locator("h4").first()).toHaveText(
            "Investigation (not saved)"
        );

        expect(
            this.page.locator("li").filter({
                hasText:
                    this.anomaliesApiResponseData[
                        this.anomaliesApiResponseData.length - 1
                    ]?.id,
            })
        ).toBeDefined();
        expect(
            this.page.locator("p").filter({ hasText: "Anomaly start" })
        ).toBeDefined();
        expect(
            this.page.locator("p").filter({ hasText: "Anomaly end" })
        ).toBeDefined();
        expect(
            this.page.locator("p").filter({ hasText: "Anomaly duration" })
        ).toBeDefined();
        expect(
            this.page.locator("p").filter({ hasText: "Seasonality" })
        ).toBeDefined();
        expect(
            this.page
                .locator("p")
                .filter({ hasText: "Deviation (Current / Predicted)" })
        ).toBeDefined();
        await expect(this.page.locator("h4").nth(1)).toHaveText(
            "What went wrong and where?"
        );
        await expect(this.page.locator("h4").nth(2)).toHaveText(
            "Investigation preview"
        );
        expect(
            this.page
                .locator("span")
                .filter({ hasText: "What went wrong and where?" })
        ).toBeDefined();
        expect(
            this.page
                .locator("span")
                .filter({ hasText: "An event could have caused it?" })
        ).toBeDefined();
        expect(
            this.page
                .locator("span")
                .filter({ hasText: "Review investigation & share" })
        ).toBeDefined();
        expect(
            this.page.locator("h5").filter({ hasText: "Top Contributors" })
        ).toBeDefined();
        expect(
            this.page
                .locator("h5")
                .filter({ hasText: "Heatmap & Dimension Drills" })
        ).toBeDefined();
        expect(
            this.page
                .locator("h5")
                .filter({ hasText: "Heatmap & Dimension Drills" })
        ).toBeDefined();
    }
}
