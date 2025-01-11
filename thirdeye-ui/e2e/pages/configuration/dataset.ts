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
import { BasePage } from "../base";
import { Response } from "@playwright/test";
export class ConfigurationDataSetPage extends BasePage {
    readonly page: Page;
    datasets: any;
    auth: any;

    constructor(page: Page) {
        super(page);
        this.page = page;
    }

    async resolveApis() {
        const datasetsApiRequest = await this.page.waitForRequest((request) => {
            request.url().includes("/api/datasets");
            this.auth = request.headers["authorization"];
            return true;
        });
        const datasetsApiResponse = await this.page.waitForResponse(
            (response) =>
                response.url().includes("/api/datasets") &&
                response.status() === 200
        );
        this.datasets = await datasetsApiResponse.json();
    }

    async goToDatasetTab() {
        await this.page.goto(
            "http://localhost:7004/configuration/datasets/all"
        );
    }

    async deleteDataset() {
        const datasets = this.datasets.sort((a, b) => {
            if (a.name.toLowerCase() > b.name.toLowerCase()) {
                return 1;
            }
            if (b.name.toLowerCase() > a.name.toLowerCase()) {
                return -1;
            }
            return 0;
        });
        const datasetToDelete = datasets[0];
        const datasetRow = this.page.locator(".MuiDataGrid-row").nth(0);
        const checkbox = datasetRow.locator(">div").nth(0);
        await checkbox.click();
        const deleteBtn = this.page.getByTestId("dataset-list-delete-button");
        await deleteBtn.click();
        await expect(this.page.getByTestId("dialoag-content")).toHaveText(
            `Are you sure you want to delete ${datasetToDelete.name}?`
        );
        const actionButtons = this.page.locator(
            '[data-testId="dialoag-actions"] > button'
        );
        await expect(actionButtons.nth(0)).toHaveText("Cancel");
        await expect(actionButtons.nth(1)).toHaveText("Confirm");
        await actionButtons.nth(1).click();
        await expect(this.page.getByTestId("notfication-container")).toHaveText(
            "Dataset deleted successfully"
        );
    }

    async onBoardDataset() {
        await this.page.getByTestId("create-menu-button").click();
        const dropdownOptions = this.page.locator(
            "[data-testid^=dropdown-option]"
        );
        dropdownOptions.nth(1).click();
        await this.page.waitForURL(
            "http://localhost:7004/configuration/datasets/onboard"
        );
        const onboardedDatasetApi = this.page.waitForResponse(
            (response) =>
                response.url().includes("/api/datasets") &&
                response.status() === 200
        );
        const datasourcesApi = this.page.waitForResponse(
            (response) =>
                response.url().includes("/api/data-sources") &&
                response.status() === 200
        );
        await onboardedDatasetApi;
        await datasourcesApi;

        await expect(this.page.locator("h4")).toHaveText("Onboard Dataset");
        const selectDatasourceInout = this.page.getByPlaceholder(
            "Select a datasource"
        );
        await selectDatasourceInout.click();
        const datasourceOptions = this.page
            .getByRole("presentation")
            .locator("li");
        const selectedDatasourceDatsetsApi = this.page.waitForResponse(
            (responnse) =>
                responnse.url().includes(`/api/data-sources`) &&
                responnse.status() === 200
        );
        await datasourceOptions.nth(0).click();
        await selectedDatasourceDatsetsApi;
        await this.page.waitForTimeout(2000);
        const datasetsToOnboard = await this.page
            .locator('input[type="checkbox"]')
            .all();
        for (let i = 0; i < datasetsToOnboard.length; i++) {
            await datasetsToOnboard[i].click();
        }
        const submitBtn = this.page.locator("#next-bottom-bar-btn");
        const onboardDatasetApi = this.page.waitForResponse(
            (response) =>
                response.url().includes("/api/data-sources/onboard-dataset") &&
                response.status() === 200
        );
        await submitBtn.click();
        await onboardDatasetApi;
    }
}
