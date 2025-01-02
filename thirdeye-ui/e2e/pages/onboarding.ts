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

export class OnboardingPage extends BasePage {
    readonly page: Page;
    datasourceApiResponse: any;

    constructor(page: Page) {
        super(page);
        this.page = page;
    }

    async resolveApis() {
        const [count, workspaces, datasets] = await Promise.all([
            this.page.waitForResponse(
                (response) =>
                    response.url().includes("/api/alerts/count") &&
                    response.status() === 200
            ),
            this.page.waitForResponse(
                (response) =>
                    response.url().includes("/api/workspaces") &&
                    response.status() === 200
            ),
            this.page.waitForResponse(
                (response) =>
                    response.url().includes("/api/datasets") &&
                    response.status() === 200
            ),
        ]);
    }

    async resolveCreateAlertApis() {
        const [datasetsApiResponse, metricsApiResponse, dataSourcesResponse] =
            await Promise.all([
                this.page.waitForResponse(
                    (response) =>
                        response.url().includes("/api/datasets") &&
                        response.status() === 200
                ),
                this.page.waitForResponse(
                    (response) =>
                        response.url().includes("/api/metrics") &&
                        response.status() === 200
                ),
                this.page.waitForResponse(
                    (response) =>
                        response.url().includes("/api/data-sources") &&
                        response.status() === 200
                ),
            ]);
    }

    async resolveDataSourcesApi() {
        const [datasourceApiResponse] = await Promise.all([
            this.page.waitForResponse(
                (response) =>
                    response.url().includes("/api/data-sources") &&
                    response.status() === 200
            ),
        ]);
        this.datasourceApiResponse = await datasourceApiResponse.json();
    }

    async resolveDataSetApi() {
        const [dataSetApiResponse] = await Promise.all([
            this.page.waitForResponse(
                (response) =>
                    response
                        .url()
                        .includes(
                            `/api/data-sources/${this.datasourceApiResponse[0]?.id}/datasets`
                        ) && response.status() === 200
            ),
        ]);
    }

    async goToWelcomeLanding() {
        await this.page.goto("http://localhost:7004/#access_token=''");
        await this.page.waitForSelector("h4:has-text('StarTree ThirdEye')", {
            timeout: 10000,
            state: "visible",
        });
        await this.page.goto("http://localhost:7004/welcome/landing");
    }

    async checkHeader() {
        await expect(this.page.locator("h4")).toHaveText(
            "Let's create your first setup"
        );
        await expect(this.page.locator("h5")).toHaveText(
            "Complete the following steps."
        );
    }

    async checkConfigureCardHeader() {
        await expect(this.page.locator("h6").first()).toHaveText(
            "Review and configure data"
        );
    }

    async checkConfigurePageHeader() {
        await expect(this.page.locator("h4")).toHaveText(
            "Let's start setting up your data"
        );

        await expect(this.page.locator("h5").first()).toHaveText(
            "Complete the following steps."
        );
        await expect(this.page.locator("h5").nth(1)).toHaveText(
            "Select Datasource"
        );
    }

    async checkCreateAlertHeader() {
        await expect(this.page.locator("h4")).toHaveText("Create Alert");
    }

    async checkCartHeader() {
        await expect(this.page.locator("h6").first()).toHaveText(
            "Review and configure data"
        );
        await expect(this.page.locator("h6").nth(1)).toHaveText(
            "Create my first alert"
        );
    }

    async clickCreateAlertButton() {
        await this.page.getByRole("button", { name: "Create Alert" }).click();
    }

    async clickConfigureDataButton() {
        await this.page.getByRole("button", { name: "Configure Data" }).click();
    }

    async selectDataSources() {
        await this.page.click(
            `span:text("${
                this.datasourceApiResponse
                    ? this.datasourceApiResponse[0]?.name
                    : ""
            }")`
        );
        await this.page.getByRole("button", { name: "Next" }).click();
    }

    async onboardDatasets() {
        await expect(this.page.locator("h5").nth(1)).toHaveText(
            "Onboard datasets for mypinot"
        );

        await this.page
            .getByRole("button", { name: "Onboard Datasets" })
            .click();
    }

    async selectOtherDataSources() {
        await this.page.click(`span:text("Add new Pinot datasource")`);
        await this.page.click(`span:text("mypinot")`);
        await this.page.press(`span:text("mypinot")`, "Digit2");

        await this.page.getByRole("button", { name: "Next" }).click();
    }
}
