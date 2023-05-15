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

import { TEST_IDS } from "../../../../app/components/alert-list-v1/alert-list-v1.interfaces";

const CHECKBOX_SELECTOR =
    ".BaseTable__body [role='row'] input[type='checkbox']";

describe("all alerts page", () => {
    beforeEach(() => {
        // Clear out any existing alerts
        cy.resetAlerts();
        // Clear out any existing data sources
        cy.resetDatasets();
        cy.loadDatasource();

        cy.visit("http://localhost:7004/alerts");
    });

    it("force fail create alert on very test", () => {
        /**
         * When running in docker compose or github action env for:
         * https://github.com/startreedata/thirdeye-e2e-testing
         * This is expected to fail so that subsequent calls work.
         * #TODO root cause failure in backend
         */
        cy.loadAlertAndAnomalies();
    });

    it("user can filter alerts in table", () => {
        /**
         * There is a weird bug where this would fail on initial launch of TE,
         * so we make it fail for the very first test case
         */
        cy.loadAlertAndAnomalies();
        const searchInputSelector = "input[placeholder='Search Alerts']";

        // There should be 1 alert (table should show something)
        cy.getByDataTestId(TEST_IDS.TABLE)
            .find(".BaseTable__body [role='row']")
            .contains("UnitCost_SUM_mean-variance-rule-testing")
            .should("be.visible");

        // If filtering for the alert, the matching alert should be visible
        cy.get(searchInputSelector).type("UnitCost_S");
        // Make sure input value is propagated after debounce
        // eslint-disable-next-line cypress/no-unnecessary-waiting
        cy.wait(500);
        cy.getByDataTestId(TEST_IDS.TABLE)
            .find(".BaseTable__body [role='row']")
            .contains("UnitCost_SUM_mean-variance-rule-testing")
            .should("be.visible");

        // If filtering for the alert, non matching string should hide alert
        cy.get(searchInputSelector).type("shouldnotshow");
        // Make sure input value is propagated after debounce
        // eslint-disable-next-line cypress/no-unnecessary-waiting
        cy.wait(500);
        cy.getByDataTestId(TEST_IDS.TABLE)
            .find(".BaseTable__body [role='row']")
            .should("not.exist");
    });

    it("user can duplicate alert", () => {
        /**
         * There is a weird bug where this would fail on initial launch of TE,
         * so we make it fail for the very first test case
         */
        cy.loadAlertAndAnomalies();
        cy.getByDataTestId(TEST_IDS.TABLE).find(CHECKBOX_SELECTOR).click();

        cy.getByDataTestId(TEST_IDS.DUPLICATE_BUTTON).click();
        cy.url().should("include", "/alerts/create/copy");
    });

    it("user can edit alert", () => {
        /**
         * There is a weird bug where this would fail on initial launch of TE,
         * so we make it fail for the very first test case
         */
        cy.loadAlertAndAnomalies();
        cy.getByDataTestId(TEST_IDS.TABLE).find(CHECKBOX_SELECTOR).click();

        cy.getByDataTestId(TEST_IDS.EDIT_BUTTON).click();

        // Should match something like `http://localhost:7004/alerts/30521/update`
        cy.url().should("match", /.*\/alerts\/\d.*\/update/);
    });

    it("user can reset alert", () => {
        /**
         * There is a weird bug where this would fail on initial launch of TE,
         * so we make it fail for the very first test case
         */
        cy.loadAlertAndAnomalies();
        cy.getByDataTestId(TEST_IDS.TABLE).find(CHECKBOX_SELECTOR).click();

        cy.getByDataTestId(TEST_IDS.RESET_BUTTON).click();
        // Click confirm button in the dialog
        cy.get("[role='dialog']").contains("Confirm").click();
        cy.get(".notification-display-v1")
            .contains(
                'Anomalies deleted and detection algorithm successfully ran for "UnitCost_SUM_mean-variance-rule-testing"'
            )
            .should("be.visible");
    });

    it("user can delete alert", () => {
        /**
         * There is a weird bug where this would fail on initial launch of TE,
         * so we make it fail for the very first test case
         */
        cy.loadAlertAndAnomalies();
        cy.getByDataTestId(TEST_IDS.TABLE).find(CHECKBOX_SELECTOR).click();

        cy.getByDataTestId(TEST_IDS.DELETE_BUTTON).click();

        // Click confirm button in the dialog
        cy.get("[role='dialog']").contains("Confirm").click();

        // Since there is only one alert, the table should no longer exist after deleting
        cy.getByDataTestId(TEST_IDS.TABLE).should("not.exist");
    });
});
