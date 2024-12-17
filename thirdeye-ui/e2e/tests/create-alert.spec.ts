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
import { expect, test } from "@playwright/test";
import { CreateAlertPage } from "../pages/create-alert";

test("Create Alert Page", async ({ page }) => {
    const createAlertPage = new CreateAlertPage(page);
    await createAlertPage.goToCreateAlertPage();
    await createAlertPage.checkHeader();
    await page.getByText("Dataset Select a dataset to").click();
    await page
        .getByRole("option", { name: "AdCampaignData 6 metrics" })
        .click();
    await page.getByTestId("metric-select").locator("div").nth(1).click();
    await page.getByRole("option", { name: "Impressions" }).click();
    await page.locator("div").filter({ hasText: /^SUM$/ }).click();
    await page.getByLabel("SUM").check();
    await page.getByPlaceholder("Select granularity").click();
    await page.getByRole("option", { name: "Daily" }).click();
    await page
        .locator("div")
        .filter({ hasText: /^Single metric$/ })
        .click();
    await page.getByPlaceholder("Select an algorithm").click();
    await page.getByRole("heading", { name: "StarTree-ETS option 1" }).click();
    await page.getByText("Anomalies", { exact: true }).click();
    await page.getByRole("button", { name: "Create alert" }).click();
    await page.getByRole("button", { name: "Create alert" }).click();

    // Multiple dimensions
    await page.getByText("Dataset Select a dataset to").click();
    await page
        .getByRole("option", { name: "AdCampaignData 6 metrics" })
        .click();
    await page.getByText("Metric Select a metric to").click();
    await page.getByRole("option", { name: "Impressions" }).click();
    await page.locator("div").filter({ hasText: /^SUM$/ }).click();
    await page.getByLabel("SUM").check();
    await page.getByPlaceholder("Select granularity").click();
    await page.getByText("Daily").click();
    await page.getByLabel("Single metric").check();
    await page.getByLabel("Multiple dimensions").check();
    await page.getByLabel("Dimension recommender").check();
    await page.getByRole("button", { name: "Add dimensions" }).click();
    await page.getByPlaceholder("Select dimensions").click();
    await page.getByRole("option", { name: "Exchange" }).click();
    await page
        .getByRole("button", { name: "Generate dimensions to monitor" })
        .click();
    await page
        .getByRole("row", { name: "Exchange='DoubleClick' 350." })
        .getByRole("checkbox")
        .check();
    await page.getByRole("button", { name: "Add selected dimensions" }).click();
    await page.getByPlaceholder("Select an algorithm").click();
    await page.getByRole("heading", { name: "StarTree-ETS option 1" }).click();
    await page.getByRole("button", { name: "Create alert" }).click();
    await page.getByTestId("alert-name-input").getByRole("textbox").click();
    await page
        .getByTestId("alert-name-input")
        .getByRole("textbox")
        .fill("Impressions_SUM_star-tree-ets_dx-456");
    await page.getByRole("button", { name: "Create alert" }).click();
    await expect(page.locator("h4")).toHaveText(
        "Impressions_SUM_star-tree-ets_dx-456"
    );
});
