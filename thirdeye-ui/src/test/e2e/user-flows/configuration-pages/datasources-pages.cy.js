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

import { TEST_IDS } from "../../../../app/components/datasource-list-v1/datasource-list-v1.interfaces";

const CHECKBOX_SELECTOR =
    ".BaseTable__body [role='row'] input[type='checkbox']";

describe("configuration datasources pages", () => {
    beforeEach(() => {
        // Clear out any existing data sources
        cy.resetDatasets();
        cy.loadDatasource();
        cy.visit("http://localhost:7004/configuration/datasources");
    });

    it("user can filter datasources in table", () => {
        const searchInputSelector = "input[placeholder='Search Datasources']";

        // There should be 1 alert (table should show something)
        cy.getByDataTestId(TEST_IDS.TABLE)
            .find(".BaseTable__body [role='row']")
            .contains("mypinot")
            .should("be.visible");

        // If filtering for the datasource, the matching datasource should be visible
        cy.get(searchInputSelector).type("myp");
        // Make sure input value is propagated after debounce
        // eslint-disable-next-line cypress/no-unnecessary-waiting
        cy.wait(500);
        cy.getByDataTestId(TEST_IDS.TABLE)
            .find(".BaseTable__body [role='row']")
            .contains("mypinot")
            .should("be.visible");

        // If filtering for the datasource, non matching string should hide datasource
        cy.get(searchInputSelector).type("shouldnotshow");
        // Make sure input value is propagated after debounce
        // eslint-disable-next-line cypress/no-unnecessary-waiting
        cy.wait(500);
        cy.getByDataTestId(TEST_IDS.TABLE)
            .find(".BaseTable__body [role='row']")
            .should("not.exist");
    });

    it("user can edit datasource", () => {
        cy.getByDataTestId(TEST_IDS.TABLE).find(CHECKBOX_SELECTOR).click();

        cy.getByDataTestId(TEST_IDS.EDIT_BUTTON).click();

        // Should match something like
        // `http://localhost:7004/configuration/datasources/update/id/30521`
        cy.url().should("contain", "/configuration/datasources/update/id/");
    });

    it("user can delete datasource", () => {
        cy.getByDataTestId(TEST_IDS.TABLE).find(CHECKBOX_SELECTOR).click();

        cy.getByDataTestId(TEST_IDS.DELETE_BUTTON).click();

        // Click confirm button in the dialog
        cy.get("[role='dialog']").contains("Confirm").click();

        // Since there is only one datasource, the table should no longer exist after deleting
        cy.getByDataTestId(TEST_IDS.TABLE).should("not.exist");
    });

    it("user can create datasource", () => {
        cy.getByDataTestId("create-menu-button").click();

        cy.get("ul.MuiMenu-list")
            .find("li[role='menuitem']")
            .contains("Create Datasource")
            .click();

        // eslint-disable-next-line jest/valid-expect-in-promise
        cy.get(".CodeMirror")
            .first()
            .then((editor) => {
                editor[0].CodeMirror.setValue(
                    JSON.stringify({
                        name: "test-pinot",
                        type: "pinot",
                        properties: {
                            zookeeperUrl: "localhost:2123",
                            clusterName: "QuickStartCluster",
                            controllerConnectionScheme: "http",
                            controllerHost: "localhost",
                            controllerPort: 9000,
                            brokerUrl: "localhost:8000",
                        },
                    })
                );
            });

        cy.get("input[type='checkbox']").click();
        cy.get("button").contains("Create Datasource").click();
        cy.get(".notification-display-v1")
            .contains("Datasets onboarded successfully")
            .should("be.visible");
    });
});
