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

export class ConfigurationPage extends BasePage {
    readonly page: Page;

    constructor(page: Page) {
        super(page);
        this.page = page;
    }

    async resolveApis() {
        await Promise.all([
            this.page.waitForResponse(
                (response) =>
                    response.url().includes("/api/data-sources") &&
                    response.status() === 200
            ),
        ]);
    }
    async gotToConfigPage() {
        await this.page.goto("http://localhost:7004/configuration/");
        await this.page.waitForURL(
            "http://localhost:7004/configuration/datasources/all"
        );
    }

    async checkHeader() {
        await expect(this.page.locator("h4")).toHaveText("Configuration");
        const createBtn = this.page.getByTestId("create-menu-button");
        await expect(createBtn).toHaveText("Create");
        await createBtn.click();
        const dropdownOptions = this.page.locator(
            "[data-testid^=dropdown-option]"
        );
        expect(dropdownOptions).toHaveCount(7);
        await expect(dropdownOptions.nth(0)).toHaveText("Create Datasource");
        await expect(dropdownOptions.nth(1)).toHaveText("Onboard Dataset");
        await expect(dropdownOptions.nth(2)).toHaveText(
            "Create Alert Template"
        );
        await expect(dropdownOptions.nth(3)).toHaveText("Create Alert");
        await expect(dropdownOptions.nth(4)).toHaveText(
            "Create Subscription Group"
        );
        await expect(dropdownOptions.nth(5)).toHaveText("Create Event");
        await expect(dropdownOptions.nth(6)).toHaveText(
            "Report missed Anomaly"
        );
    }

    async checkTabs() {
        const tabLinks = this.page.getByTestId("tabs").locator("a");
        expect(tabLinks).toHaveCount(7);
        await expect(tabLinks.nth(0)).toHaveText("Datasources");
        await expect(tabLinks.nth(0)).toHaveClass(/Mui-selected/);
        await expect(tabLinks.nth(1)).toHaveText("Datasets");
        await expect(tabLinks.nth(2)).toHaveText("Metrics");
        await expect(tabLinks.nth(3)).toHaveText("Alert Templates");
        await expect(tabLinks.nth(4)).toHaveText("Subscription Groups");
        await expect(tabLinks.nth(5)).toHaveText("Events");
        await expect(tabLinks.nth(6)).toHaveText("Settings");
    }
}
