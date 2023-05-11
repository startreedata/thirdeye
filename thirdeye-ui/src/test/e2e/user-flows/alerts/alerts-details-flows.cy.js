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
import { ANOMALY_LIST_TEST_IDS } from "../../../../app/components/anomaly-list-v1/anomaly-list-v1.interfaces";
import { DEFAULT_ALERT_NAME } from "../../support/load-alert-and-anomalies.command";

const CHECKBOX_SELECTOR =
    ".BaseTable__body [role='row'] input[type='checkbox']";

describe("alert details flows", () => {
    beforeEach(() => {
        // Clear out any existing alerts
        cy.request({
            method: "DELETE",
            url: "http://localhost:7004/api/alerts/all",
        });
        // Clear out any existing data sources
        cy.resetDatasets();
        cy.loadDatasource();
        cy.loadAlertAndAnomalies();

        cy.visit("http://localhost:7004/alerts");
    });

    it("user can click into alert from table and explore around the details page", () => {
        cy.getByDataTestId(TEST_IDS.TABLE)
            .find("a")
            .contains(DEFAULT_ALERT_NAME)
            .click();

        cy.selectDateRangeFromPicker(
            {
                month: 12,
                year: 2020,
                date: 20,
            },
            {
                month: 12,
                year: 2020,
                date: 23,
            }
        );

        cy.get("a").contains("View 1 Anomaly").click();

        cy.get(".BaseTable__body")
            .find("div[role='row']")
            .should("have.length", 1);
    });

    it("user can click into alert from table and explore around the anomalies sub page from details page", () => {
        cy.getByDataTestId(TEST_IDS.TABLE)
            .find("a")
            .contains(DEFAULT_ALERT_NAME)
            .click();

        cy.get("a")
            .contains(/View \d* Anomalies/)
            .click();

        // User can see anomalies table with anomalies
        // Due to lazy loading rows check for greater than 5
        cy.get(".BaseTable__body")
            .find("div[role='row']")
            .should("have.length.gt", 5);

        // User can change date range and data will refresh
        // There is one anomaly for the date range below
        cy.selectDateRangeFromPicker(
            {
                month: 12,
                year: 2020,
                date: 20,
            },
            {
                month: 12,
                year: 2020,
                date: 23,
            }
        );

        cy.get(".BaseTable__body")
            .find("div[role='row']")
            .should("have.length", 1);
    });

    it("user can delete alert via options menu", () => {
        cy.getByDataTestId(TEST_IDS.TABLE)
            .find("a")
            .contains(DEFAULT_ALERT_NAME)
            .click();

        cy.get("button").contains("Options").click();
        cy.get("li").contains("Delete Alert").click();

        // Click confirm button in the dialog
        cy.get("[role='dialog']").contains("Confirm").click();

        // Since there is only one alert, the table should no longer exist after deleting
        cy.getByDataTestId(TEST_IDS.TABLE).should("not.exist");
    });

    it("user can delete anomalies from the anomalies sub page", () => {
        cy.getByDataTestId(TEST_IDS.TABLE)
            .find("a")
            .contains(DEFAULT_ALERT_NAME)
            .click();

        // The anomalies fixture has 21 anomalies
        cy.get("a").contains("View 21 Anomalies").click();

        // User can see anomalies table with anomalies
        // Due to lazy loading rows check for greater than 5
        cy.get(".BaseTable__body")
            .find("div[role='row']")
            .should("have.length.gt", 5);

        cy.get(CHECKBOX_SELECTOR).first().click();
        cy.getByDataTestId(ANOMALY_LIST_TEST_IDS.DELETE_BUTTON).click();
        cy.get("[role='dialog']").contains("Confirm").click();
        cy.get(".MuiAlert-message")
            .contains("Anomaly deleted successfully")
            .should("be.visible");
    });

    it("user can duplicate alert via options menu", () => {
        cy.getByDataTestId(TEST_IDS.TABLE)
            .find("a")
            .contains(DEFAULT_ALERT_NAME)
            .click();

        cy.get("button").contains("Options").click();
        cy.get("li").contains("Duplicate Alert").click();
        cy.url().should("include", "/alerts/create/copy");
    });

    it("user can edit alert via options menu", () => {
        cy.getByDataTestId(TEST_IDS.TABLE)
            .find("a")
            .contains(DEFAULT_ALERT_NAME)
            .click();

        cy.get("button").contains("Options").click();
        cy.get("li").contains("Edit Alert").click();
        // Should match something like `http://localhost:7004/alerts/30521/update`
        cy.url().should("match", /.*\/alerts\/\d.*\/update/);
    });

    it("user can reset alert via options menu", () => {
        cy.getByDataTestId(TEST_IDS.TABLE)
            .find("a")
            .contains(DEFAULT_ALERT_NAME)
            .click();

        cy.get("button").contains("Options").click();
        cy.get("li").contains("Reset anomalies for Alert").click();

        // Click confirm button in the dialog
        cy.get("[role='dialog']").contains("Confirm").click();
        cy.get(".notification-display-v1")
            .contains(
                'Anomalies deleted and detection algorithm currently running for "UnitCost_SUM_mean-variance-rule-testing". Page will automatically reload when complete.'
            )
            .should("be.visible");
    });
});
