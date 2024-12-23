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
            console.log("request.headers", request.headers());
            console.log("request.headersa", request.headers().Authorization);
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

        const response = await this.page.request.post(
            "/api/data-sources/onboard-dataset/",
            {
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    Authorization: this.auth,
                },
                form: {
                    dataSourceId: 156,
                    datasetName: "AdCampaignData",
                },
            }
        );
        // const res = await response
        console.log("res", response);
    }
}
