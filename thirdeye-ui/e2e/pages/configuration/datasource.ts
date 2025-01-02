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

export class ConfigurationDataSourcePage extends BasePage {
    readonly page: Page;
    datasources: any;
    datasourcesValidations: any = [];

    constructor(page: Page) {
        super(page);
        this.page = page;
    }

    async resolveApis() {
        const [datasourceApiResponse] = await Promise.all([
            this.page.waitForResponse(
                (response) =>
                    response.url().includes("/api/data-sources") &&
                    response.status() === 200
            ),
        ]);
        this.datasources = await datasourceApiResponse.json();
        const validatePromises: Promise<Response>[] = [];
        for (let i = 0; i < this.datasources.length; i++) {
            validatePromises.push(
                this.page.waitForResponse(
                    (response) =>
                        response
                            .url()
                            .includes(
                                `/api/data-sources/validate?id=${this.datasources[i].id}`
                            ) && response.status() === 200
                )
            );
        }

        const datasourcesValidationsResponse = await Promise.all(
            validatePromises
        );
        for (let i = 0; i < this.datasources.length; i++) {
            this.datasourcesValidations.push(
                await datasourcesValidationsResponse[i].json()
            );
        }
    }
    async gotToDataSourceTabPage() {
        await this.page.goto("http://localhost:7004/configuration/");
        await this.page.waitForURL(
            "http://localhost:7004/configuration/datasources/all"
        );
        await this.resolveApis();
    }

    async checkTableActions() {
        const editBtn = this.page.getByTestId("datasource-list-edit-button");
        const deleteBtn = this.page.getByTestId(
            "datasource-list-delete-button"
        );
        expect(editBtn).toBeDisabled();
        expect(deleteBtn).toBeDisabled();
        expect(editBtn).toHaveText("Edit");
        expect(deleteBtn).toHaveText("Delete");
        expect(this.page.getByPlaceholder("Search Datasources")).toHaveCount(1);
    }

    async checkTable() {
        const datasources = this.datasources;
        const validations = this.datasourcesValidations;
        const columnsHeaders = this.page.locator(
            ".MuiDataGrid-columnHeaderWrapper > div"
        );
        expect(columnsHeaders).toHaveCount(4);
        expect(columnsHeaders.nth(1)).toHaveText("Name");
        expect(columnsHeaders.nth(2)).toHaveText("Type");
        expect(columnsHeaders.nth(3)).toHaveText("Health Status");

        const rows = await this.page.locator(".MuiDataGrid-row").all();

        for (let i = 0; i < rows.length; i++) {
            const row = rows[i];
            const columnData = row.locator(">div");
            expect(columnData.nth(1)).toHaveText(datasources[i].name);
            expect(columnData.nth(2)).toHaveText(datasources[i].type);
            if (validations[i].code === "OK") {
                const svg = columnData.nth(3).locator("svg");
                expect(svg).toHaveAttribute("color", "#50AE55");
            }
        }
    }

    async viewDataSourceConfig() {
        const datasources = this.datasources;
        const firstDataSource = datasources[0];
        const firstRow = this.page.locator(".MuiDataGrid-row").nth(0);
        const nameCol = firstRow.locator(">div").nth(1).locator("a");
        await nameCol.click();
        await this.page.waitForURL(
            `http://localhost:7004/configuration/datasources/view/id/${firstDataSource.id}`
        );
        await expect(this.page.locator("h4")).toHaveText(firstDataSource.name);
        const jsonEditor = this.page.locator(".CodeMirror");
        await jsonEditor.click();

        const editorValue = await this.page.evaluate(() => {
            const editor = document.querySelector(".CodeMirror")?.CodeMirror;
            return JSON.parse(editor.getValue());
        });
        expect(editorValue).toEqual(firstDataSource);
    }

    async createDatasoucre() {
        await this.page.getByTestId("create-menu-button").click();
        const dropdownOptions = this.page.locator(
            "[data-testid^=dropdown-option]"
        );
        await dropdownOptions.nth(0).click();
        await this.page.waitForURL(
            "http://localhost:7004/configuration/datasources/create"
        );
        await expect(this.page.locator("h4")).toHaveText("Create Datasource");
        const newDatasource = `{
        "name": "newdatasource",
        "type": "pinot",
        "defaultQueryOptions": {
            "timeoutMs": "30000"
        },
        "properties": {
            "zookeeperUrl": "pinot-zookeeper-headless.managed.svc.cluster.local:2181",
            "brokerUrl": "pinot-pinot-broker-headless.managed.svc.cluster.local:8095",
            "clusterName": "pinot",
            "controllerConnectionScheme": "https",
            "controllerHost": "pinot-pinot-controller-headless.managed.svc.cluster.local",
            "controllerPort": 9000,
            "oauth": {
            "enabled": true,
            "tokenFilePath": "/var/run/secrets/kubernetes.io/serviceaccount/token"
            }
        },
        "auth": {
            "namespace": null
        }
        }`;
        await this.page.evaluate((newDatasource) => {
            const editor = document.querySelector(".CodeMirror")?.CodeMirror;
            editor.setValue(newDatasource);
        }, newDatasource);

        const createBtn = this.page.getByTestId("create-datasource-btn");
        expect(createBtn).toHaveText("Create Datasource");
        const waitForCreateResponse = this.page.waitForResponse(
            (response) =>
                response.url().includes("/api/data-sources") &&
                response.status() === 200
        );
        createBtn.click();
        const response = await waitForCreateResponse;
        const createdDatasource = (await response.json())[0];

        await this.page.waitForURL(
            `http://localhost:7004/configuration/datasources/view/id/${createdDatasource.id}`
        );
    }

    async editDatasource() {
        const datasources = this.datasources;
        const datasourceToEdit = datasources[datasources.length - 1];
        const datasourceRow = this.page
            .locator(".MuiDataGrid-row")
            .nth(datasources.length - 1);
        const checkbox = datasourceRow.locator(">div").nth(0);
        await checkbox.click();
        const editBtn = this.page.getByTestId("datasource-list-edit-button");
        await editBtn.click();
        this.page.waitForURL(
            `configuration/datasources/update/id/${datasourceToEdit.id}`
        );
        await expect(this.page.locator("h4")).toHaveText("Update Datasource");
        // Name is not allowed to update. What is allowed to update?
        // const editedDatasource = `{
        //   "name": "newdatasource",
        //   "type": "pinot",
        //   "defaultQueryOptions": {
        //     "timeoutMs": "30000"
        //   },
        //   "properties": {
        //     "zookeeperUrl": "pinot-zookeeper-headless.managed.svc.cluster.local:2181",
        //     "brokerUrl": "pinot-pinot-broker-headless.managed.svc.cluster.local:8095",
        //     "clusterName": "pinot",
        //     "controllerConnectionScheme": "https",
        //     "controllerHost": "pinot-pinot-controller-headless.managed.svc.cluster.local",
        //     "controllerPort": 9000,
        //     "oauth": {
        //       "enabled": true,
        //       "tokenFilePath": "/var/run/secrets/kubernetes.io/serviceaccount/token"
        //     }
        //   },
        //   "auth": {
        //     "namespace": null
        //   }
        // }`
        // await this.page.evaluate((editedDatasource) => {
        //   const editor = document.querySelector(".CodeMirror")?.CodeMirror;
        //   editor.setValue(editedDatasource);
        // }, editedDatasource);

        const updateBtn = this.page.getByTestId("create-datasource-btn");
        await expect(updateBtn).toHaveText("Update Datasource");
        const apiRequest = this.page.waitForRequest(
            (request) =>
                request.url().includes("/api/data-sources") &&
                request.method() === "PUT"
        );
        const apiResponse = this.page.waitForResponse(
            (response) =>
                response.url().includes("/api/data-sources") &&
                response.status() === 200
        );
        await updateBtn.click();
        const request = await apiRequest;
        const payload = JSON.parse(request.postData()!)[0];
        expect(payload).toEqual(datasourceToEdit);
        await apiResponse;
    }

    async deleteDatasource() {
        const datasources = this.datasources;
        if (datasources.length > 1) {
            const datasourceRow = this.page
                .locator(".MuiDataGrid-row")
                .nth(datasources.length - 1);
            const checkbox = datasourceRow.locator(">div").nth(0);
            await checkbox.click();
            const deleteBtn = this.page.getByTestId(
                "datasource-list-delete-button"
            );
            await deleteBtn.click();
            await expect(this.page.getByTestId("dialoag-content")).toHaveText(
                `Are you sure you want to delete ${
                    datasources[datasources.length - 1].name
                }?`
            );
            const actionButtons = this.page.locator(
                '[data-testId="dialoag-actions"] > button'
            );
            await expect(actionButtons.nth(0)).toHaveText("Cancel");
            await expect(actionButtons.nth(1)).toHaveText("Confirm");
            await actionButtons.nth(1).click();
            await expect(
                this.page.getByTestId("notfication-container")
            ).toHaveText("Datasource deleted successfully");
        }
    }
}
