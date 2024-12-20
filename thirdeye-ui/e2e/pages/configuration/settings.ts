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

export class ConfigurationSettingsPage extends BasePage {
    readonly page: Page;
    settings: any;
    constructor(page: Page) {
        super(page);
        this.page = page;
    }

    async resolveApis() {
        const settingApiResponse = await this.page.waitForResponse(
            (response) =>
                response.url().includes("/api/workspace-configuration") &&
                response.status() === 200
        );
        this.settings = await settingApiResponse.json();
    }

    async gotToSettingsPage() {
        await this.page.goto("http://localhost:7004/configuration/namespace");
        await this.resolveApis();
    }

    async viewSetting() {
        const jsonEditor = this.page.locator(".CodeMirror");
        await jsonEditor.click();

        const editorvalue = await this.page.evaluate(() => {
            const editor = document.querySelector(".CodeMirror")?.CodeMirror;
            return JSON.parse(editor.getValue());
        });
        await expect(editorvalue).toEqual(this.settings);
    }

    async updateSetting() {
        const jsonEditor = this.page.locator(".CodeMirror");
        await jsonEditor.click();
        await this.page.evaluate(() => {
            const editor = document.querySelector(".CodeMirror")?.CodeMirror;
            editor.setValue(`{
          "id": 1,
          "auth": {
            "namespace": null
          },
          "timeConfiguration": {
            "timezone": "Asia/Kolkata",
            "dateTimePattern": "MMM dd, yyyy HH:mm",
            "minimumOnboardingStartTime": 946684800000
          },
          "templateConfiguration": {
            "sqlLimitStatement": 100000000
          }
        }`);
        });
        const updateBtn = this.page.getByTestId("update-settings");
        await expect(updateBtn).toHaveText("Update");
        await updateBtn.click();
        await this.page.waitForResponse(
            (response) =>
                response.url().includes("/api/workspace-configuration") &&
                response.status() === 200
        );
    }

    async resetSetting() {
        const resetBtn = this.page.getByTestId("reset-settings");
        await expect(resetBtn).toHaveText("Reset");
        await resetBtn.click();
        await this.page.waitForResponse(
            (response) =>
                response.url().includes("/api/workspace-configuration") &&
                response.status() === 200
        );
    }
}
