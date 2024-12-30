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
export class ConfigurationSubscriptionGroupPage extends BasePage {
    readonly page: Page;
    subscriptiongroups: any;

    constructor(page: Page) {
        super(page);
        this.page = page;
    }

    async resolveApis() {
        const subGroupsApiResponse = await this.page.waitForResponse(
            (response) =>
                response.url().includes("/api/subscription-groups") &&
                response.status() === 200
        );
        this.subscriptiongroups = await subGroupsApiResponse.json();
    }

    async goToSubscriptionGroupsTab() {
        await this.page.goto(
            "http://localhost:7004/configuration/subscription-groups/all"
        );
        await this.resolveApis();
    }

    async checkTableActions() {
        const editBtn = this.page.getByTestId(
            "subscription-groups-list-edit-button"
        );
        const deleteBtn = this.page.getByTestId(
            "subscription-groups-list-delete-button"
        );
        await expect(editBtn).toBeDisabled();
        await expect(deleteBtn).toBeDisabled();
        await expect(editBtn).toHaveText("Edit");
        await expect(deleteBtn).toHaveText("Delete");
        await expect(
            this.page.getByPlaceholder("Search Subscription Groups")
        ).toHaveCount(1);
    }

    async checkTable() {
        const subscriptionGroups = this.subscriptiongroups;
        const rowsData = subscriptionGroups.map((group) => {
            const obj: any = {};
            obj.name = group.name;
            obj.activeChannels = group.specs.map((spec) => spec.type);
            obj.schedule = group.cron;
            const allAlertIds = group.alertAssociations.map(
                (alert) => alert.alert.id
            );
            const uniqAlerts: any = [];
            allAlertIds.forEach((alert) => {
                if (!uniqAlerts.includes(alert)) {
                    uniqAlerts.push(alert);
                }
            });
            obj.alerts = uniqAlerts;
            obj.dimensions = group.alertAssociations.filter((alert) =>
                Object.keys(alert).includes("enumerationItem")
            );
            return obj;
        });
        const columnsHeaders = this.page.locator(
            ".MuiDataGrid-columnHeaderWrapper > div"
        );
        await expect(columnsHeaders).toHaveCount(6);
        await expect(columnsHeaders.nth(1)).toHaveText("Group Name");
        await expect(columnsHeaders.nth(2)).toHaveText("Active channels");
        await expect(columnsHeaders.nth(3)).toHaveText("Subscribed Alerts");
        await expect(columnsHeaders.nth(4)).toHaveText("Subscribed Dimensions");
        await expect(columnsHeaders.nth(5)).toHaveText("Schedule");

        const rows = await this.page.locator(".MuiDataGrid-row").all();

        for (let i = 0; i < rows.length; i++) {
            const row = rows[i];
            const columnData = row.locator(">div");
            await expect(columnData.nth(1)).toHaveText(rowsData[i].name);
            const channels = rowsData[i].activeChannels.length;
            if (rowsData[i].activeChannels.length) {
                const iconSvgs = columnData.nth(2).locator("svg");
                await expect(iconSvgs).toHaveCount(channels);
            }
            await expect(columnData.nth(3)).toHaveText(
                rowsData[i].alerts.length.toString()
            );
            await expect(columnData.nth(4)).toHaveText(
                rowsData[i].dimensions.length.toString()
            );
            await expect(columnData.nth(5)).toHaveText(rowsData[i].schedule);
        }
    }

    async createSubscriptionGroup() {
        await this.page.getByTestId("create-menu-button").click();
        const dropdownOptions = this.page.locator(
            "[data-testid^=dropdown-option]"
        );
        await dropdownOptions.nth(4).click();

        const [alertApi] = await Promise.all([
            this.page.waitForResponse(
                (response) =>
                    response.url().includes("/api/alerts") &&
                    response.status() === 200
            ),
            this.page.waitForURL(
                "http://localhost:7004/configuration/subscription-groups/create/details"
            ),
        ]);
        const alertData = (await alertApi.json()).reverse();

        await expect(this.page.locator("h4")).toHaveText(
            "Create Subscription Group"
        );
        const breadrumbsElem = this.page.locator(
            '[data-testId="breadcrumbs"] > nav > ol > li'
        );
        await expect(breadrumbsElem.nth(0)).toHaveText("Configuration");
        await expect(breadrumbsElem.nth(1)).toHaveText("/");
        await expect(breadrumbsElem.nth(2)).toHaveText("Subscription Groups");
        await expect(breadrumbsElem.nth(3)).toHaveText("/");
        await expect(breadrumbsElem.nth(4)).toHaveText("Create");

        const tabs = this.page.locator('[data-testid="tabs"] a');
        await expect(tabs.nth(0)).toHaveText("Group Details");
        await expect(tabs.nth(1)).toHaveText("Alerts & dimensions");

        const headerElems = this.page
            .getByTestId("details-card-header")
            .locator("span");
        await expect(headerElems.nth(0)).toHaveText("Group Details");
        await expect(headerElems.nth(1)).toHaveText(
            "Add details that define the group's purpose and update frequency"
        );

        const nameInput = this.page.getByTestId("group-name").locator("input");
        await nameInput.fill("e2e subscription group");

        const recepientCardHeaderElem = this.page
            .getByTestId("recepient-details-card-header")
            .locator("span");
        await expect(recepientCardHeaderElem.nth(0)).toHaveText(
            "Recipient Details"
        );
        await expect(recepientCardHeaderElem.nth(1)).toHaveText(
            "Configure notification channels that will receive updates from this subscription group"
        );

        const emailBtn = this.page.getByTestId("email-sendgrid");
        const slackBtn = this.page.getByTestId("slack");
        const webhookBtn = this.page.getByTestId("webhook");
        const pagerdutyBtn = this.page.getByTestId("pagerduty");
        await expect(emailBtn).toHaveCount(1);
        await expect(slackBtn).toHaveCount(1);
        await expect(webhookBtn).toHaveCount(1);
        await expect(pagerdutyBtn).toHaveCount(1);

        await emailBtn.click();
        const emailInput = this.page
            .getByTestId("email-list-input")
            .locator("textarea")
            .nth(0);
        await emailInput.fill("test@email.com");

        await slackBtn.click();
        const slackUrlInput = this.page
            .getByTestId("slack-input-container")
            .locator("input");
        const slackAlertownerInput = this.page
            .getByTestId("slack-owner-container")
            .locator("input");
        await slackUrlInput.fill("https://hooks.slack.com/services/T12");
        await slackAlertownerInput.fill("test");

        await webhookBtn.click();
        const webhookUrlInput = this.page
            .getByTestId("webhook-input-container")
            .locator("input");
        await webhookUrlInput.fill("https://hooks.com/services/T12");

        await pagerdutyBtn.click();
        const pagerdutyUrlInput = this.page
            .getByTestId("pager-duty-input-container")
            .locator("input");
        await pagerdutyUrlInput.fill("ad2a75369ae4400");

        const nextBtn = this.page.locator("#next-bottom-bar-btn");
        await expect(nextBtn).toHaveText("Next");
        await nextBtn.click();

        this.page.waitForURL(
            "http://localhost:7004/configuration/subscription-groups/create/alert-dimensions"
        );
        const tabHeader = this.page.getByTestId("add-alerts-to-group");
        const mainHeading = tabHeader.locator("h5");
        await expect(mainHeading).toHaveText("Alerts & dimensions");
        const subHeading = tabHeader.locator("h6");
        await expect(subHeading).toHaveText(
            "Add individual alerts and dimensions that will send updates via this subscription group"
        );

        const alertTypesOptions = this.page.locator(
            '[data-testId="alert-types"] > label'
        );
        await expect(alertTypesOptions.nth(0)).toHaveText("All");
        await expect(alertTypesOptions.nth(1)).toHaveText("Simple alerts");
        await expect(alertTypesOptions.nth(2)).toHaveText(
            "Dimension exploration alerts"
        );

        const table = this.page.locator("table");

        const tableHeaders = table.locator("thead th");
        await expect(tableHeaders).toHaveCount(6);

        await expect(tableHeaders.nth(1)).toHaveText("Subscribe");
        await expect(tableHeaders.nth(2)).toHaveText("Alert Name");
        await expect(tableHeaders.nth(3)).toHaveText("Dimensions");
        await expect(tableHeaders.nth(4)).toHaveText("Subscribed To");
        await expect(tableHeaders.nth(5)).toHaveText("Created");

        const tableRows = table.locator("tbody");
        expect(tableRows).toHaveCount(alertData.length);
        const allRows = await tableRows.all();
        for (let i = 0; i < 2; i++) {
            const allCoulmns = await allRows[i].locator("tr td").all();
            const alertCol = await allRows[i].locator("tr th");
            const alert = alertData[i];
            await expect(alertCol.nth(0)).toHaveText(alert.name);
            const enumitems = alert.templateProperties.enumerationItems?.length;
            const enumQuery = alert.templateProperties.enumeratorQuery;
            if (enumQuery) {
            } else if (enumitems) {
                await expect(allCoulmns[2]).toHaveText(enumitems.toString());
            } else {
                await expect(allCoulmns[2]).toHaveText("-");
            }
        }

        const simpleAlertIdx = alertData.findIndex(
            (alert) =>
                !Object.keys(alert.templateProperties).includes(
                    "enumerationItems"
                )
        );
        const dxAlertIdx = alertData.findIndex(
            (alert) =>
                Object.keys(alert.templateProperties).includes(
                    "enumerationItems"
                ) && alert.templateProperties.enumerationItems.length
        );

        const simpleAlertRow = allRows[simpleAlertIdx].locator("tr td");
        await simpleAlertRow.nth(1).locator("input").click();

        const dxAlertRow = allRows[dxAlertIdx].locator("tr td");
        await dxAlertRow.nth(1).locator("input").click();

        const payloadObj = [
            {
                name: "e2e subscription group",
                cron: "0 */5 * * * ?",
                alertAssociations: [
                    {
                        alert: {
                            id: 739402,
                        },
                    },
                    {
                        alert: {
                            id: 739041,
                        },
                    },
                ],
                specs: [
                    {
                        type: "email-sendgrid",
                        params: {
                            apiKey: "${SENDGRID_API_KEY}",
                            emailRecipients: {
                                from: "thirdeye-alerts@startree.ai",
                                to: ["test@email.com"],
                            },
                        },
                    },
                    {
                        type: "slack",
                        params: {
                            webhookUrl: "https://hooks.slack.com/services/T12",
                            notifyResolvedAnomalies: false,
                            sendOneMessagePerAnomaly: false,
                            textConfiguration: {
                                owner: "test",
                                mentionMemberIds: [],
                            },
                        },
                    },
                    {
                        type: "webhook",
                        params: {
                            url: "https://hooks.com/services/T12",
                        },
                    },
                    {
                        type: "pagerduty",
                        params: {
                            eventsIntegrationKey: "ad2a75369ae4400",
                        },
                    },
                ],
            },
        ];
        const saveBtn = this.page.locator("#next-bottom-bar-btn");
        const response = this.page.waitForResponse(
            (response) =>
                response.url().includes("/api/subscription-groups") &&
                response.status() === 200
        );
        saveBtn.click();
        await response;
    }

    async editSubscriptionGroup() {
        const group = this.subscriptiongroups.find(
            (grp) => grp.name === "e2e subscription group"
        );
        const hasEnumerationItems = group?.alertAssociations.find((alert) =>
            Object.keys(alert).includes("enumerationItem")
        );
        const searchInput = this.page.getByPlaceholder(
            "Search Subscription Groups"
        );
        await searchInput.fill("e2e subscription group");
        const groupRow = this.page.locator(".MuiDataGrid-row").nth(0);
        await expect(groupRow.locator(">div").nth(1)).toHaveText(
            "e2e subscription group"
        );
        const nameColumn = groupRow.locator(">div").nth(1).locator("a");
        await nameColumn.click();
        await this.page.waitForURL(
            `http://localhost:7004/configuration/subscription-groups/${group.id}/view`
        );
        const editBtn = this.page.getByTestId("edit-subscription-group");
        await Promise.all([
            this.page.waitForResponse(
                (response) =>
                    response.url().includes("/api/alerts") &&
                    response.status() === 200
            ),
            this.page.waitForResponse(
                (response) =>
                    response.url().includes("/api/tasks") &&
                    response.status() === 200
            ),
        ]);
        if (hasEnumerationItems) {
            await this.page.waitForResponse(
                (response) =>
                    response.url().includes("/api/enumeration-items") &&
                    response.status() === 200
            );
        }
        await editBtn.click();
        await this.page.waitForURL(
            `http://localhost:7004/configuration/subscription-groups/${group.id}/update/details`
        );
        const nameInput = this.page.getByTestId("group-name").locator("input");
        await nameInput.fill("e2e subscription group update");
        await this.page.locator("#next-bottom-bar-btn").click();
        const updatebtn = this.page.locator("#next-bottom-bar-btn");
        await expect(updatebtn).toHaveText("Update");
        const response = this.page.waitForResponse(
            (response) =>
                response.url().includes("/api/subscription-groups") &&
                response.status() === 200
        );
        await updatebtn.click();
        await response;
    }

    async deleteSubscriptiongroup() {
        const group = this.subscriptiongroups.find(
            (grp) => grp.name === "e2e subscription group update"
        );
        const searchInput = this.page.getByPlaceholder(
            "Search Subscription Groups"
        );
        await searchInput.fill("e2e subscription group update");
        const row = this.page.locator(".MuiDataGrid-row").nth(0);
        await expect(row.locator(">div").nth(1)).toHaveText(
            "e2e subscription group update"
        );
        const chekbox = row.locator(">div").nth(0);
        await chekbox.click();
        const deleteBtn = this.page.getByTestId(
            "subscription-groups-list-delete-button"
        );
        await deleteBtn.click();
        await expect(this.page.getByTestId("dialoag-content")).toHaveText(
            `Are you sure you want to delete ${group.name}?`
        );
        const modalActions = this.page
            .getByTestId("dialoag-actions")
            .locator("button");
        await expect(modalActions).toHaveCount(2);
        await expect(modalActions.nth(0)).toHaveText("Cancel");
        await expect(modalActions.nth(1)).toHaveText("Confirm");
        const response = this.page.waitForResponse(
            (response) =>
                response
                    .url()
                    .includes(`/api/subscription-groups/${group.id}`) &&
                response.status() === 200
        );
        await modalActions.nth(1).click();
        await response;
        await expect(this.page.getByTestId("notfication-container")).toHaveText(
            "Subscription Group deleted successfully"
        );
    }
}
