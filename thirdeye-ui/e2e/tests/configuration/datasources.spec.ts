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
import { ConfigurationDataSourcePage } from "../../pages/configuration/datasource";

test.describe.configure({ mode: "serial" });

test("Datasource List", async ({ page }) => {
    const configurationDataSourcePage = new ConfigurationDataSourcePage(page);
    await configurationDataSourcePage.gotToDataSourceTabPage();
    await configurationDataSourcePage.checkTableActions();
    await configurationDataSourcePage.checkTable();
    await configurationDataSourcePage.viewDataSourceConfig();
});

test("Create Datasource", async ({ page }) => {
    const configurationDataSourcePage = new ConfigurationDataSourcePage(page);
    await configurationDataSourcePage.gotToDataSourceTabPage();
    await configurationDataSourcePage.createDatasoucre();
});

test("Edit Datasource", async ({ page }) => {
    const configurationDataSourcePage = new ConfigurationDataSourcePage(page);
    await configurationDataSourcePage.gotToDataSourceTabPage();
    await configurationDataSourcePage.editDatasource();
});

test("Delete Datasource", async ({ page }) => {
    const configurationDataSourcePage = new ConfigurationDataSourcePage(page);
    await configurationDataSourcePage.gotToDataSourceTabPage();
    await configurationDataSourcePage.deleteDatasource();
});
