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
import { CreateAlertPage } from "../pages/create-alert";

test("Create Simple Alert", async ({ page }) => {
    const createAlertPage = new CreateAlertPage(page);
    await createAlertPage.goToCreateAlertPage();
    await createAlertPage.resolveApis();
    await createAlertPage.checkHeader();
    await createAlertPage.selectDatasetAndMetric();
    await createAlertPage.selectStaticFields();
    await createAlertPage.resolveRecommendApis();
    await createAlertPage.selectDetectionAlgorithm();
    await createAlertPage.resolveEvaluateApis();
    await createAlertPage.createAlert();
});

test("Create Multi Dimensions Alert", async ({ page }) => {
    const createAlertPage = new CreateAlertPage(page);
    await createAlertPage.goToCreateAlertPage();
    await createAlertPage.resolveApis();
    await createAlertPage.checkHeader();
    await createAlertPage.selectDatasetAndMetric();
    await createAlertPage.selectStaticFields(true);
    await createAlertPage.addDimensions();
    await createAlertPage.resolveRecommendApis();
    await createAlertPage.selectDetectionAlgorithm(true);
    await createAlertPage.resolveEvaluateApis();
    await createAlertPage.createAlert();
});

test("Create Multi Dimensions SQL Alert", async ({ page }) => {
    const createAlertPage = new CreateAlertPage(page);
    await createAlertPage.goToCreateAlertPage();
    await createAlertPage.resolveApis();
    await createAlertPage.checkHeader();
    await createAlertPage.selectDatasetAndMetric();
    await createAlertPage.selectStaticFields(true, true);
    await createAlertPage.addSQLQuery();
    await createAlertPage.resolveRecommendApis();
    await createAlertPage.selectDetectionAlgorithm(true);
    await createAlertPage.clickLoadChartButton();
    await createAlertPage.resolveEvaluateApis();
    await createAlertPage.createAlert();
});
