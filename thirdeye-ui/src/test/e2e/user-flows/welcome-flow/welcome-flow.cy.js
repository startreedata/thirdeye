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
import { PREVIEW_CHART_TEST_IDS } from "../../../../app/components/alert-wizard-v2/alert-template/preview-chart/preview-chart.interfaces";
import { SELECT_ALERT_CATEGORY_TEST_IDS } from "../../../../app/pages/alerts-create-guided-page/select-alert-category/select-alert-category-page.interface";
import { ONBOARD_DATASETS_TEST_IDS } from "../../../../app/pages/welcome-page/create-datasource/onboard-datasets/onboard-datasets-page.interface";
import { ADD_NEW_DATASOURCE } from "../../../../app/pages/welcome-page/create-datasource/onboard-datasource/onboard-datasource-page.utils";

describe("newly launched ThirdEye welcome flow", () => {
    beforeEach(() => {
        // Cypress starts out with a blank slate for each test
        // so we must tell it to visit our website with the `cy.visit()` command.
        // Since we want to visit the same URL at the start of all our tests,
        // we include it in our beforeEach function so that it runs before each test

        // Clear out any existing alerts
        cy.resetAlerts();
        // Clear out any existing data sources
        cy.resetDatasets();
    });

    it("user is taken to the welcome landing page", () => {
        cy.visit("http://localhost:7004/");
        // User should be taken to the welcome landing page when there
        // are no datasets onboarded
        cy.url().should("eq", "http://localhost:7004/welcome/landing");

        // configure data button should be clickable
        cy.getByDataTestId("configure-data-btn").should("not.have.class", "");

        // create alert button should be disabled if not data is configured
        cy.getByDataTestId("create-alert-btn").should(
            "have.class",
            "Mui-disabled"
        );
    });

    it("user can setup a data source", () => {
        cy.visit("http://localhost:7004/welcome");

        // Click on `Configure Data` button should take user to onboard data source page
        cy.getByDataTestId("configure-data-btn").click();
        cy.url().should(
            "eq",
            "http://localhost:7004/welcome/onboard-datasource/datasource"
        );
        // Click on "Add new Pinot datasource" radio option
        cy.get(`[value='${ADD_NEW_DATASOURCE}']`).click();
        // Click next to onboard datasets
        cy.get("#next-bottom-bar-btn").click();
        // User should be taken to the onboard datasets page
        // `mypinot` is the default name of the datasource
        cy.url().should(
            "eq",
            "http://localhost:7004/welcome/onboard-datasource/mypinot/datasets"
        );
        // Ensure checkboxes exist for the datasets
        cy.getByDataTestId(ONBOARD_DATASETS_TEST_IDS.DATASETS_OPTIONS_CONTAINER)
            .find("input[type='checkbox']")
            .should("have.length.gt", 2);

        // Setup the intercept before clicking the Onboard button
        cy.intercept("/api/datasets").as("datasets");

        // Next button is labeled "Onboard Datasets"
        cy.get("#next-bottom-bar-btn").click();

        // create alert button should be visible
        cy.getByDataTestId("create-alert-btn").should(
            "not.have.class",
            "Mui-disabled"
        );

        cy.wait("@datasets").its("response.body").should("have.length.gt", 2);
    });

    it("user can setup an alert", () => {
        cy.loadDatasource();

        cy.visit("http://localhost:7004/welcome");

        cy.getByDataTestId("create-alert-btn").click();
        cy.getByDataTestId(
            SELECT_ALERT_CATEGORY_TEST_IDS.BASIC_ALERT_BTN
        ).click();

        /** Select metric page **/
        // Ensure preview button is disabled
        cy.getByDataTestId("preview-chart-button").should(
            "have.class",
            "Mui-disabled"
        );

        // Open the dataset autocomplete dropdown
        cy.getByDataTestId("datasource-select").click();
        cy.getByDataTestId("usstoresalesorderdata-datasource-option").click();

        // Open the metric autocomplete dropdown
        cy.getByDataTestId("metric-select").click();
        cy.get('.MuiAutocomplete-popper li[data-option-index="2"]').click();

        // Ensure preview button is enabled
        cy.getByDataTestId(PREVIEW_CHART_TEST_IDS.PREVIEW_BUTTON).should(
            "not.have.class",
            "Mui-disabled"
        );

        // Ensure working data is returns by intercepting and checking contents
        cy.intercept("http://localhost:7004/api/alerts/evaluate").as(
            "getChartData"
        );

        // Click on button to initiate API call for chart data
        cy.getByDataTestId(PREVIEW_CHART_TEST_IDS.PREVIEW_BUTTON).click();

        cy.wait("@getChartData")
            .its(
                "response.body.detectionEvaluations.output_AnomalyDetectorResult_0.anomalies"
            )
            .should("have.length.gt", 10);

        cy.get("#next-bottom-bar-btn").click();

        /** Select alert type page **/
        // Click the 4th option
        cy.getByDataTestId("threshold-select-btn").click();
        // Ensure option is labeled as selected
        cy.getByDataTestId("threshold-select-btn")
            .contains("Selected")
            .should("exist");
        cy.getByDataTestId("mean-variance-rule-select-btn").click();
        cy.getByDataTestId("mean-variance-rule-select-btn")
            .contains("Selected")
            .should("exist");
        cy.get("#next-bottom-bar-btn").click();

        /** Tune alert page **/
        // Ensure preview button is enabled
        cy.getByDataTestId(PREVIEW_CHART_TEST_IDS.PREVIEW_BUTTON).should(
            "not.have.class",
            "Mui-disabled"
        );
        // Click on button to initiate API call for chart data
        cy.getByDataTestId(PREVIEW_CHART_TEST_IDS.PREVIEW_BUTTON).click();

        cy.wait("@getChartData")
            .its(
                "response.body.detectionEvaluations.output_AnomalyDetectorResult_0.anomalies"
            )
            .should("have.length.gt", 10);

        // Take user to filters page
        cy.get("#next-bottom-bar-btn").click();

        /** Filters page **/
        // Take user to alert details page
        cy.get("#next-bottom-bar-btn").click();

        /** Details and notifications page **/
        // create the alert
        cy.get("#next-bottom-bar-btn").click();

        // User is take to home page where modal flag is in url
        cy.url().should(
            "eq",
            "http://localhost:7004/home?showFirstAlertSuccess=true"
        );
        cy.get("h2").contains("Setup finished!").should("be.visible");
    });
});
