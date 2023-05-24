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

import { ANOMALY_LIST_TEST_IDS } from "../../../../app/components/anomaly-list-v1/anomaly-list-v1.interfaces";

describe("anomalies list flows", () => {
    beforeEach(() => {
        // Clear out any existing alerts and anomalies
        cy.resetAlerts();
        cy.request({
            method: "DELETE",
            url: "http://localhost:7004/api/anomalies/all",
        });
        // Clear out any existing data sources
        cy.resetDatasets();
        cy.loadDatasource();
        cy.loadAlertAndAnomalies();

        cy.visit("http://localhost:7004/anomalies");
        // Dates need to change for past data to show
        cy.selectDateRangeFromPicker(
            {
                month: 12,
                year: 2019,
                date: 20,
            },
            {
                month: 12,
                year: 2020,
                date: 23,
            }
        );
    });

    it("user sees table and can filter by metric, dataset, and alert", () => {
        cy.getByDataTestId(ANOMALY_LIST_TEST_IDS.TABLE)
            .find(".BaseTable__body [role='row']")
            .should("have.length.gt", 5);

        // Ensure api is called when filters are changed
        cy.intercept("/api/anomalies*").as("anomalies");
        cy.getByDataTestId(
            ANOMALY_LIST_TEST_IDS.ALERT_FILTER_CONTAINER
        ).click();

        cy.get("li").contains("UnitCost").click();
        cy.wait("@anomalies").its("response.body").should("have.length.gt", 5);

        // Ensure clearing also causes api call
        cy.getByDataTestId(ANOMALY_LIST_TEST_IDS.ALERT_FILTER_CONTAINER)
            .find("button.MuiAutocomplete-clearIndicator")
            .click({ force: true });
        cy.wait("@anomalies").its("response.body").should("have.length.gt", 5);

        // Testing filtering by dataset
        cy.getByDataTestId(
            ANOMALY_LIST_TEST_IDS.DATASET_FILTER_CONTAINER
        ).click();

        cy.get("li").contains("USStore").click();
        cy.wait("@anomalies").its("response.body").should("have.length.gt", 5);
        cy.getByDataTestId(ANOMALY_LIST_TEST_IDS.DATASET_FILTER_CONTAINER)
            .find("button.MuiAutocomplete-clearIndicator")
            .click({ force: true });
        cy.wait("@anomalies").its("response.body").should("have.length.gt", 5);

        // Testing filtering by metric
        cy.getByDataTestId(
            ANOMALY_LIST_TEST_IDS.METRIC_FILTER_CONTAINER
        ).click();

        cy.get("li").contains("UnitCost").click();
        cy.wait("@anomalies").its("response.body").should("have.length.gt", 5);
        cy.getByDataTestId(ANOMALY_LIST_TEST_IDS.METRIC_FILTER_CONTAINER)
            .find("button.MuiAutocomplete-clearIndicator")
            .click({ force: true });
        cy.wait("@anomalies").its("response.body").should("have.length.gt", 5);
    });

    it("user sees working metrics report page", () => {
        cy.get("a").contains("Metrics Report").click();

        cy.getByDataTestId("metrics-report-list-tbody")
            .find("tr")
            .should("have.length", 1);

        // Ensure api is called when filters are changed
        cy.intercept("/api/anomalies*").as("anomalies");
        cy.getByDataTestId(
            ANOMALY_LIST_TEST_IDS.ALERT_FILTER_CONTAINER
        ).click();

        cy.get("li").contains("UnitCost").click();
        cy.wait("@anomalies").its("response.body").should("have.length.gt", 5);

        // Ensure clearing also causes api call
        cy.getByDataTestId(ANOMALY_LIST_TEST_IDS.ALERT_FILTER_CONTAINER)
            .find("button.MuiAutocomplete-clearIndicator")
            .click({ force: true });
        cy.wait("@anomalies").its("response.body").should("have.length.gt", 5);

        // Testing filtering by dataset
        cy.getByDataTestId(
            ANOMALY_LIST_TEST_IDS.DATASET_FILTER_CONTAINER
        ).click();

        cy.get("li").contains("USStore").click();
        cy.wait("@anomalies").its("response.body").should("have.length.gt", 5);
        cy.getByDataTestId(ANOMALY_LIST_TEST_IDS.DATASET_FILTER_CONTAINER)
            .find("button.MuiAutocomplete-clearIndicator")
            .click({ force: true });
        cy.wait("@anomalies").its("response.body").should("have.length.gt", 5);

        // Testing filtering by metric
        cy.getByDataTestId(
            ANOMALY_LIST_TEST_IDS.METRIC_FILTER_CONTAINER
        ).click();

        cy.get("li").contains("UnitCost").click();
        cy.wait("@anomalies").its("response.body").should("have.length.gt", 5);
        cy.getByDataTestId(ANOMALY_LIST_TEST_IDS.METRIC_FILTER_CONTAINER)
            .find("button.MuiAutocomplete-clearIndicator")
            .click({ force: true });
        cy.wait("@anomalies").its("response.body").should("have.length.gt", 5);
    });
});
