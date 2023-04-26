/*
 * Copyright 2023 StarTree Inc
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

import { TEST_IDS } from "../../../../app/components/subscription-group-list-v1/subscription-group-list-v1.interfaces";

const CHECKBOX_SELECTOR =
    ".BaseTable__body [role='row'] input[type='checkbox']";
const DEFAULT_SUBSCRIPTION_GROUP_NAME = "default-test-subscription-group";

describe("configuration subscription groups pages", () => {
    beforeEach(() => {
        cy.resetDatasets();
        cy.loadDatasource();
        // Clear out any existing subscription groups
        cy.request({
            method: "DELETE",
            url: "http://localhost:7004/api/subscription-groups/all",
        });
        // Clear out any existing alerts
        cy.request({
            method: "DELETE",
            url: "http://localhost:7004/api/alerts/all",
        });
        cy.request({
            method: "POST",
            url: "http://localhost:7004/api/alerts",
            json: [
                {
                    name: "UnitCost_SUM_mean-variance-rule-testing",
                    description: "",
                    cron: "0 0 5 ? * MON-FRI *",
                    template: { name: "startree-mean-variance" },
                    templateProperties: {
                        dataSource: "mypinot",
                        dataset: "USStoreSalesOrderData",
                        aggregationColumn: "UnitCost",
                        aggregationFunction: "SUM",
                        seasonalityPeriod: "P7D",
                        lookback: "P28D",
                        monitoringGranularity: "P1D",
                        sensitivity: "-11",
                        timezone: "UTC",
                    },
                },
            ],
        }).then(({ body }) => {
            // Make sure the associated alert is valid
            cy.request({
                method: "POST",
                url: "http://localhost:7004/api/subscription-groups",
                json: [
                    {
                        name: DEFAULT_SUBSCRIPTION_GROUP_NAME,
                        cron: "0 */5 * * * ?",
                        alertAssociations: [{ alert: { id: body[0].id } }],
                        specs: [
                            {
                                type: "email-sendgrid",
                                params: {
                                    apiKey: "${SENDGRID_API_KEY}",
                                    emailRecipients: {
                                        from: "thirdeye-alerts@startree.ai",
                                        to: [],
                                    },
                                },
                            },
                        ],
                    },
                ],
            });
        });

        cy.visit("http://localhost:7004/configuration/subscription-groups");
    });

    it("user can filter subscription groups in table", () => {
        const searchInputSelector =
            "input[placeholder='Search Subscription Groups']";

        // There should be 1 alert (table should show something)
        cy.getByDataTestId(TEST_IDS.TABLE)
            .find(".BaseTable__body [role='row']")
            .contains(DEFAULT_SUBSCRIPTION_GROUP_NAME)
            .should("be.visible");

        // If filtering for the subscription group, the matching
        // subscription group should be visible
        cy.get(searchInputSelector).type("test");
        // Make sure input value is propagated after debounce
        // eslint-disable-next-line cypress/no-unnecessary-waiting
        cy.wait(500);
        cy.getByDataTestId(TEST_IDS.TABLE)
            .find(".BaseTable__body [role='row']")
            .contains(DEFAULT_SUBSCRIPTION_GROUP_NAME)
            .should("be.visible");

        // If filtering for the subscription group, non matching
        // string should hide subscription group
        cy.get(searchInputSelector).type("shouldnotshow");
        // Make sure input value is propagated after debounce
        // eslint-disable-next-line cypress/no-unnecessary-waiting
        cy.wait(500);
        cy.getByDataTestId(TEST_IDS.TABLE)
            .find(".BaseTable__body [role='row']")
            .should("not.exist");
    });

    it("user can edit subscription groups", () => {
        cy.getByDataTestId(TEST_IDS.TABLE).find(CHECKBOX_SELECTOR).click();

        cy.getByDataTestId(TEST_IDS.EDIT_BUTTON).click();

        // Should match something like
        // `http://localhost:7004/configuration/datasources/update/id/30521`
        cy.url().should("contain", "/configuration/subscription-groups/");
    });

    it("user can delete subscription groups", () => {
        cy.getByDataTestId(TEST_IDS.TABLE).find(CHECKBOX_SELECTOR).click();

        cy.getByDataTestId(TEST_IDS.DELETE_BUTTON).click();

        // Click confirm button in the dialog
        cy.get("[role='dialog']").contains("Confirm").click();

        // Since there is only one datasource, the table should no longer exist after deleting
        cy.getByDataTestId(TEST_IDS.TABLE).should("not.exist");
    });

    it("user can create subscription group with an alert", () => {
        cy.getByDataTestId("create-menu-button").click();

        cy.get("ul.MuiMenu-list")
            .find("li[role='menuitem']")
            .contains("Create Subscription Group")
            .click();

        cy.get("input[name='name']").type("test-subscription-group");
        cy.getByDataTestId("email-sendgrid").click();
        cy.getByDataTestId("email-list-input").type("test@test.ai");
        cy.get("button").contains("Next").click();
        cy.get("input[type='checkbox']").click();
        cy.get("button").contains("Save").click();

        // User should be taken to the subscription group views page for the
        // newly created subscription group
        cy.get("h4").contains("test-subscription-group").should("be.visible");
    });

    it("user goes to edit page from subscription group details page", () => {
        cy.getByDataTestId(TEST_IDS.TABLE)
            .find("a")
            .contains(DEFAULT_SUBSCRIPTION_GROUP_NAME)
            .click();
        cy.get("button").contains("Edit").click();

        // Should match something like `/configuration/subscription-groups/44485/update/details`
        cy.url().should(
            "match",
            /.*\/configuration\/subscription-groups\/\d.*\/update/
        );
    });

    it("user can delete subscription group from details page", () => {
        cy.getByDataTestId(TEST_IDS.TABLE)
            .find("a")
            .contains(DEFAULT_SUBSCRIPTION_GROUP_NAME)
            .click();
        cy.get("button").contains("Delete").click();
        // Click confirm button in the dialog
        cy.get("[role='dialog']").contains("Confirm").click();
        cy.getByDataTestId(TEST_IDS.TABLE).should("not.exist");
    });
});
