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

export class ConfigurationEventsPage extends BasePage {
    readonly page: Page;
    events: any;
    constructor(page: Page) {
        super(page);
        this.page = page;
    }

    async resolveApis() {
        const eventsResponse = await this.page.waitForResponse(
            (response) =>
                response.url().includes("/api/events") &&
                response.status() === 200
        );
        this.events = await eventsResponse.json();
    }

    async gotoEventstab() {
        await this.page.goto(
            "http://localhost:7004/configuration/events/all/range"
        );
        await this.resolveApis();
    }

    async checkTableActions() {
        const deleteBtn = this.page
            .getByTestId("table-toolbar")
            .locator("button")
            .nth(0);
        await expect(deleteBtn).toBeDisabled();
        await expect(deleteBtn).toHaveText("Delete");
        await expect(this.page.getByPlaceholder("Search Event")).toHaveCount(1);
    }

    async checkTable() {
        const events = this.events;
        const columnsHeaders = this.page.locator(
            ".BaseTable__header-row > div"
        );
        await expect(columnsHeaders).toHaveCount(5);
        await expect(columnsHeaders.nth(1)).toHaveText("Name");
        await expect(columnsHeaders.nth(2)).toHaveText("Type");
        await expect(columnsHeaders.nth(3)).toHaveText("Start");
        await expect(columnsHeaders.nth(4)).toHaveText("End");

        const rows = await this.page.locator(".BaseTable__row").all();

        for (let i = 0; i < rows.length; i++) {
            const row = rows[i];
            const columnData = row.locator(">div");
            await expect(columnData.nth(1)).toHaveText(events[i].name);
            await expect(columnData.nth(2)).toHaveText(events[i].type);
        }
    }

    async createEvent() {
        await this.page.getByTestId("create-menu-button").click();
        const dropdownOptions = this.page.locator(
            "[data-testid^=dropdown-option]"
        );
        await dropdownOptions.nth(5).click();
        await this.page.waitForURL(
            "http://localhost:7004/configuration/events/create"
        );
        await expect(this.page.locator("h4")).toHaveText("Create Event");
        await expect(
            this.page.getByTestId("event-properties-title")
        ).toHaveText("Event Properties");
        const propertiesInput = this.page
            .getByTestId("event-properties-form")
            .locator(">div");
        await expect(propertiesInput).toHaveCount(4);

        const firstInput = propertiesInput.nth(0);
        expect(firstInput.getByTestId("label")).toHaveText("Name");
        await firstInput.locator("input").fill("e2e event");

        const secondInput = propertiesInput.nth(1);
        expect(secondInput.getByTestId("label")).toHaveText("Type");
        await secondInput.locator("input").fill("e2e type");

        const thirdInput = propertiesInput.nth(2);
        expect(thirdInput.getByTestId("label")).toHaveText("Start time");

        const fourthInput = propertiesInput.nth(3);
        expect(fourthInput.getByTestId("label")).toHaveText("End time");

        await this.page.getByTestId("add-metadata-entry").click();

        const propName1 = this.page
            .getByTestId("property-name-0")
            .locator("input");
        const propValue1 = this.page
            .getByTestId("property-value-0")
            .locator("input");
        await propName1.fill("metadata1");
        await propValue1.fill("metadata1value");

        const propName2 = this.page
            .getByTestId("property-name-1")
            .locator("input");
        const propValue2 = this.page
            .getByTestId("property-value-1")
            .locator("input");
        await propName2.fill("metadata2");
        await propValue2.fill("metadata2value");

        const createBtn = this.page.getByTestId("create-event-btn");
        await expect(createBtn).toHaveText("Create Event");
        await createBtn.click();
        const eventApi = await this.page.waitForResponse(
            (response) =>
                response.url().includes("/api/events") &&
                response.status() === 200
        );
        const eventApiResponse = await eventApi.json();
        await this.page.waitForURL(
            `http://localhost:7004/configuration/events/view/id/${eventApiResponse[0].id}`
        );
    }

    async deleteEvent() {
        const event = this.events.find((ev) => ev.name === "e2e event");
        const searchInput = this.page.getByPlaceholder("Search Event");
        await searchInput.fill("e2e event");
        const firstRow = this.page.locator(".BaseTable__row").nth(0);
        await expect(firstRow.locator(">div").nth(1)).toHaveText("e2e event");
        await firstRow.locator(">div").nth(0).click();
        const deleteBtn = this.page
            .getByTestId("table-toolbar")
            .locator("button")
            .nth(0);
        await deleteBtn.click();
        await expect(this.page.getByTestId("dialoag-content")).toHaveText(
            "Are you sure you want to delete e2e event?"
        );
        const modalActions = this.page
            .getByTestId("dialoag-actions")
            .locator("button");
        await expect(modalActions).toHaveCount(2);
        await expect(modalActions.nth(0)).toHaveText("Cancel");
        await expect(modalActions.nth(1)).toHaveText("Confirm");
        await modalActions.nth(1).click();
        await this.page.waitForResponse(
            (response) =>
                response.url().includes(`/api/events/${event.id}`) &&
                response.status() === 200
        );
        await expect(this.page.getByTestId("notfication-container")).toHaveText(
            "Event deleted successfully"
        );
    }
}
