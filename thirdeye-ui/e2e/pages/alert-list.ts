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

export class AlertListPage extends BasePage {
    readonly page: Page;
    alertResponseData: any;
    subscriptionResponseData: any;
    anomalyResponseData: any;

    constructor(page: Page) {
        super(page);
        this.page = page;
    }

    async goToAlertPage() {
        await this.page.goto("http://localhost:7004/alerts/all");
    }

    async checkHeader() {
        await expect(this.page.locator("h4")).toHaveText("Alerts");
    }
}
