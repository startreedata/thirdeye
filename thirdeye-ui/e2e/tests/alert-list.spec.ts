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
import { test } from "@playwright/test";
import { AlertListPage } from "../pages/alert-list";

let alertListPage;
test.beforeEach(async ({ page }) => {
    alertListPage = new AlertListPage(page);
    await alertListPage.gotoHomePage();
    await alertListPage.goToAlertPage();
    await alertListPage.resolveAPIs();
});

test("Alert List Page", async ({ page }) => {
    await alertListPage.checkHeader();
    await alertListPage.checkTableActionButtons();
    await alertListPage.checkTable();
});

test("Alert List Search", async ({ page }) => {
    await alertListPage.performSearch();
});

test("Duplicate Alert", async ({ page }) => {
    await alertListPage.duplicateAlert();
});

test.skip("Edit Alert", async ({ page }) => {
    await alertListPage.editAlert();
});

test("Reset Alert", async ({ page }) => {
    await alertListPage.resetAlert();
});

test("Delete Alert", async ({ page }) => {
    await alertListPage.deleteAlert();
});
