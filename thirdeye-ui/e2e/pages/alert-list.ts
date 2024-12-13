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

    constructor(page: Page) {
        super(page);
        this.page = page;
    }

    async resolveAPIs() {
        const [alertApiResponse, subscriptionApiResponse] = await Promise.all([
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
        ]);

        this.alertResponseData = await alertApiResponse.json();
        this.subscriptionResponseData = await subscriptionApiResponse.json();
    }

    async goToAlertPage() {
        await this.page.goto("http://localhost:7004/alerts/all");
    }

    async checkHeader() {
        await expect(this.page.locator("h4")).toHaveText("Alerts");
        const createButton = this.page.getByTestId("create-alert-dropdown");
        await expect(createButton).toHaveText("Create");
        await createButton.click();
        const dropdownOptions = this.page.locator(
            "[data-testid^=dropdown-option]"
        );
        expect(dropdownOptions).toHaveCount(3);
        await expect(dropdownOptions.nth(0)).toHaveText("Easy Alert");
        await expect(dropdownOptions.nth(1)).toHaveText("Advanced Alert");
        await expect(dropdownOptions.nth(2)).toHaveText("JSON Alert");
    }

    async checkTableActionButtons() {
        const toolbarElems = this.page.locator(
            '[data-testId="table-toolbar"] > div'
        );
        expect(toolbarElems).toHaveCount(2);

        const tableActions = this.page.locator(
            '[data-testId="alert-list-actions"] button'
        );
        expect(tableActions).toHaveCount(4);
        await expect(tableActions.nth(0)).toHaveText("Duplicate");
        await expect(tableActions.nth(0)).toBeDisabled();
        await expect(tableActions.nth(1)).toHaveText("Edit");
        await expect(tableActions.nth(1)).toBeDisabled();
        await expect(tableActions.nth(2)).toHaveText("Delete");
        await expect(tableActions.nth(2)).toBeDisabled();
        await expect(tableActions.nth(3)).toHaveText("Reset");
        await expect(tableActions.nth(3)).toBeDisabled();

        expect(
            toolbarElems.locator('[data-testId="search-input"]')
        ).toBeTruthy();
    }

    async checkTable() {
        const table = this.page.locator(".BaseTable__table");
        const tableHeader = this.page.locator(".BaseTable__header-row");
        const tableBody = this.page.locator(".BaseTable__body");

        const headerElem = tableHeader.locator(">div");
        expect(headerElem).toHaveCount(6);
        expect(headerElem.nth(1)).toHaveText("Alert Name");
        expect(headerElem.nth(2)).toHaveText("Created by");
        expect(headerElem.nth(3)).toHaveText("Accuracy");
        expect(headerElem.nth(4)).toHaveText("Active");
        expect(headerElem.nth(5)).toHaveText("Created");

        const tableRows = await this.page.locator(".BaseTable__row").all();
        const alerts = this.alertResponseData.reverse();
        // const allRows = await tableRows.all();
        const totalAlerts = this.alertResponseData.length;
        const rowsTocheck =
            totalAlerts === 0 ? 0 : totalAlerts > 7 ? 7 : totalAlerts;
        for (let i = 0; i < rowsTocheck; i++) {
            const allCoulmns = await tableRows[i].locator(">div").all();
            const alert = alerts[i];
            for (let k = 1; k < 6; k++) {
                k === 1 && (await expect(allCoulmns[1]).toHaveText(alert.name));
                k === 2 &&
                    (await expect(allCoulmns[2]).toHaveText(
                        alert.owner.principal
                    ));
                // k === 3 && (await expect(allCoulmns[3]).toHaveText(''));
                // k === 4 && (await expect(allCoulmns[4]).toHaveText(''));
                // k === 4 && (await expect(allCoulmns[5]).toHaveText(''));
            }
        }
    }

    async performSearch() {
        const searchInput = this.page.getByPlaceholder("Search Alerts");
        searchInput.fill("views");
        const tableRows = await this.page.locator(".BaseTable__row").all();
        for (let i = 0; i < tableRows.length; i++) {
            const allCoulmns = await tableRows[i].locator(">div").all();
            allCoulmns[1] &&
                (await expect(allCoulmns[1]).toContainText("views"));
        }
    }

    async duplicateAlert() {
        const firstAlert = this.page.locator(".BaseTable__row").nth(0);
        firstAlert.locator(">div").nth(0).click();
        const tableActions = this.page.locator(
            '[data-testId="alert-list-actions"] button'
        );
        tableActions.nth(0).click();
        const topAlertId = this.alertResponseData.reverse()[0].id;
        await this.page.waitForURL(
            `http://localhost:7004/alerts/create/copy/${topAlertId}`
        );
        await this.page.waitForURL(
            `http://localhost:7004/alerts/create/copy/${topAlertId}/json-editor?*`
        );

        const jsonEditor = this.page.locator(".CodeMirror");
        await jsonEditor.click();

        await this.page.evaluate(() => {
            const editor = document.querySelector(".CodeMirror")?.CodeMirror;
            editor.setValue(
                '{"name": "Clicks_SUM_mean-variance-rule-dup","description": "","template": {"name": "startree-mean-variance"},"templateProperties": {"dataSource": "pinot","dataset": "AdCampaignData","aggregationColumn": "Clicks","aggregationFunction": "SUM","monitoringGranularity": "P1D","timezone": "UTC","queryFilters": "","sensitivity": "-6","lookback": "P21D"},"cron": "0 0 5 ? * MON-FRI *","auth": {"namespace": null}}'
            );
        });

        await this.page.locator("#next-bottom-bar-btn").click({ force: true });
        await this.page.waitForURL("http://localhost:7004/alerts/*/view?*");
    }

    async editAlert() {
        const firstAlert = this.page.locator(".BaseTable__row").nth(0);
        firstAlert.locator(">div").nth(0).click();
        const tableActions = this.page.locator(
            '[data-testId="alert-list-actions"] button'
        );
        tableActions.nth(1).click();

        const topAlertId = this.alertResponseData.reverse()[0].id;
        await this.page.waitForURL(
            `http://localhost:7004/alerts/create/copy/${topAlertId}`
        );
        await this.page.waitForURL(
            `http://localhost:7004/alerts/${topAlertId}/update/json-editor?*`
        );

        const jsonEditor = this.page.locator(".CodeMirror");
        await jsonEditor.click();

        await this.page.evaluate(() => {
            const editor = document.querySelector(".CodeMirror")?.CodeMirror;
            editor.setValue(
                '{"name": "Clicks_SUM_mean-variance-rule-dup-edit","description": "","template": {"name": "startree-mean-variance"},"templateProperties": {"dataSource": "pinot","dataset": "AdCampaignData","aggregationColumn": "Clicks","aggregationFunction": "SUM","monitoringGranularity": "P1D","timezone": "UTC","queryFilters": "","sensitivity": "-6","lookback": "P21D"},"cron": "0 0 5 ? * MON-FRI *","auth": {"namespace": null}}'
            );
        });

        const saveButton = this.page.locator("#next-bottom-bar-btn");
        expect(saveButton).toBeDisabled();
        expect(saveButton).toHaveText(
            "Preview alert in chart before submitting"
        );

        const insightApiRequest = this.page.waitForRequest(
            "/api/alerts/insights"
        );
        const evaluateApiRequest = this.page.waitForRequest(
            "/api/alerts/evaluate"
        );

        const insightApiResponse = this.page.waitForResponse(
            "/api/alerts/insights"
        );
        const evaluateApiResponse = this.page.waitForResponse(
            "/api/alerts/evaluate"
        );

        const insightRequest = await insightApiRequest;
        const insightResponse = await insightApiResponse;

        const evaluateRequest = await evaluateApiRequest;
        const evaluateResponse = await evaluateApiResponse;

        await this.page.getByTestId("preview-chart-button").click();
    }

    async resetAlert() {}

    async deleteAlert() {
        const firstAlert = this.page.locator(".BaseTable__row").nth(0);
        firstAlert.locator(">div").nth(0).click();
        const tableActions = this.page.locator(
            '[data-testId="alert-list-actions"] button'
        );
        tableActions.nth(2).click();
        await expect(
            this.page.locator('[data-testId="delete-alert-dialog-content"]')
        ).toHaveText("");
        const actionBttons = this.page.locator(
            '[data-testId="delete-alert-dialog-actions"]'
        );
        expect(actionBttons).toHaveCount(2);
        expect(actionBttons.nth(0)).toHaveText("Cancel");
        expect(actionBttons.nth(1)).toHaveText("Confirm");
        actionBttons.nth(0).click();
        expect(
            this.page.locator('[data-testId="delete-alert-dialog-content"]')
        ).toBeFalsy();
        tableActions.nth(2).click();
        actionBttons.nth(1).click();
    }
}
