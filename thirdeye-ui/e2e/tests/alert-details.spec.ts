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
import { AlertDetailsPage } from "../pages/alert-detail";

test("View anomalies for alerts", async ({ page }) => {
    const alertDetailsPage = new AlertDetailsPage(page);
    await alertDetailsPage.goToAlertDetailsPage();
    await alertDetailsPage.resolveApis();
    await alertDetailsPage.checkHeader();
    await alertDetailsPage.openFirstAlert();
    await alertDetailsPage.checkAlertHeader();
    const anomalyCount = await alertDetailsPage.checkAnomaliesCount();
    if (anomalyCount === "0") {
        return;
    }
    await alertDetailsPage.openAnomalies();
    await alertDetailsPage.resolveAnomaliesApis();
    await alertDetailsPage.openAnomalieFromTable();
    await alertDetailsPage.assertPageComponents();
});

test("Active and Deactivate alerts", async ({ page }) => {
    const alertDetailsPage = new AlertDetailsPage(page);
    await alertDetailsPage.goToAlertDetailsPage();
    await alertDetailsPage.resolveApis();
    await alertDetailsPage.checkHeader();
    await alertDetailsPage.openFirstAlert();
    await alertDetailsPage.checkAlertHeader();
    const anomalyCount = await alertDetailsPage.checkAnomaliesCount();
    if (anomalyCount === "0") {
        return;
    }
    await alertDetailsPage.checkAlertIsActiveOrDeactive(true);
    await alertDetailsPage.activeDeactiveAlert(false);
    await alertDetailsPage.resolveApis();
    await alertDetailsPage.checkAlertIsActiveOrDeactive(false);
    await alertDetailsPage.activeDeactiveAlert(true);
    await alertDetailsPage.resolveApis();
    await alertDetailsPage.checkAlertIsActiveOrDeactive(true);
});
