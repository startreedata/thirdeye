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
import { ALERT_CREATION_NAVIGATE_DROPDOWN_TEST_IDS } from "../../../../app/components/alert-wizard-v3/navigate-alert-creation-flows-dropdown/navigate-alert-creation-flows-dropdown.interface";
import { SETUP_DETAILS_TEST_IDS } from "../../../../app/pages/alerts-create-guided-page/setup-details/setup-details-page.interface";
import { DEFAULT_ALERT_CONFIG } from "../../support/load-alert-and-anomalies.command";

describe("alert create flows", () => {
    beforeEach(() => {
        // Clear out any existing alerts
        cy.resetAlerts();
        // Clear out any existing subscription groups
        cy.request({
            method: "DELETE",
            url: "http://localhost:7004/api/subscription-groups/all",
        });
        // Clear out any existing data sources
        cy.resetDatasets();
        cy.loadDatasource();

        cy.visit("http://localhost:7004/alerts");
    });

    it("user can create alert from the simple flow", () => {
        cy.get("a").contains("Create").click();

        cy.get("#startree-mean-variance-basic-btn").click();

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

        // Take user to filters page and can fiddle around
        cy.get("#next-bottom-bar-btn").click();
        cy.get("button").contains("Filters & sensitivity").click();
        cy.getByDataTestId("timeOfWeek-switch").click();
        [
            "MONDAY",
            "TUESDAY",
            "WEDNESDAY",
            "THURSDAY",
            "FRIDAY",
            "SATURDAY",
        ].forEach((day) => {
            cy.getByDataTestId("optionedselect-daysOfWeek")
                .find(".MuiAutocomplete-popupIndicator[title='Open']")
                .click();
            cy.get("li").contains(day).click();
        });
        cy.get("button").contains("Reload preview").click();
        cy.get("h5").contains("Total anomalies detected:").should("exist");

        // Take user to alert details page
        cy.get("#next-bottom-bar-btn").click();
        cy.getByDataTestId(SETUP_DETAILS_TEST_IDS.NAME_INPUT).type("-custom");
        cy.getByDataTestId(SETUP_DETAILS_TEST_IDS.DESCRIPTION_INPUT).type(
            "foo bar description"
        );
        cy.getByDataTestId(SETUP_DETAILS_TEST_IDS.CONFIGURATION_SWITCH).click();
        cy.get(".MuiTab-root")
            .contains("Create a new notification group for this alert")
            .click();
        cy.get(".MuiBox-root").contains("Add Email").click();
        cy.get("input[name='name']").type("e2e-subscription-group");
        cy.get("textarea").first().type("test@test.ai");

        // Ensure working data is returns by intercepting and checking contents
        cy.intercept("http://localhost:7004/api/subscription-groups").as(
            "createSubscriptionGroup"
        );

        // Click create an alert button
        cy.get("#next-bottom-bar-btn").click();

        // User can add subscription group
        cy.wait("@createSubscriptionGroup")
            .its("response.body[0].name")
            .should("equal", "e2e-subscription-group");

        // User is take to the view page
        cy.url().should("match", /.*\/alerts\/\d.*\/view/);
        // The custom name should be displayed
        cy.get("h4")
            .contains("UnitCost_SUM_mean-variance-rule-custom")
            .should("exist");
        // Description should show
        cy.get("h6").contains("foo bar description").should("exist");
    });

    it("user can create alert from the advanced flow", () => {
        cy.get("a").contains("Create").click();

        cy.getByDataTestId(
            ALERT_CREATION_NAVIGATE_DROPDOWN_TEST_IDS.DROPDOWN_CONTAINER
        ).click();
        cy.get("li").contains("Advanced").click();

        cy.getByDataTestId("name-input-container")
            .find("input")
            .type(DEFAULT_ALERT_CONFIG.name);

        const inputKeys = [
            "dataSource",
            "dataset",
            "aggregationColumn",
            "aggregationFunction",
            "sensitivity",
        ];

        inputKeys.forEach((inputKey) => {
            cy.getByDataTestId(`input-${inputKey}`).clear();
            cy.getByDataTestId(`input-${inputKey}`).type(
                DEFAULT_ALERT_CONFIG.templateProperties[inputKey]
            );
        });

        // Click on button to initiate API call for chart data
        cy.getByDataTestId(PREVIEW_CHART_TEST_IDS.PREVIEW_BUTTON).click();

        // Ensure working data is returns by intercepting and checking contents
        cy.intercept("http://localhost:7004/api/alerts").as("createAlert");
        cy.get("#next-bottom-bar-btn").click();
        cy.wait("@createAlert");

        // User is take to the view page
        cy.url().should("match", /.*\/alerts\/\d.*\/view/);
    });

    it("user can create alert from the json editor flow", () => {
        cy.get("a").contains("Create").click();

        cy.getByDataTestId(
            ALERT_CREATION_NAVIGATE_DROPDOWN_TEST_IDS.DROPDOWN_CONTAINER
        ).click();
        cy.get("li").contains("JSON Editor").click();

        // eslint-disable-next-line jest/valid-expect-in-promise
        cy.get(".CodeMirror")
            .first()
            .then((editor) => {
                editor[0].CodeMirror.setValue(
                    JSON.stringify(DEFAULT_ALERT_CONFIG)
                );
            });

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

        // Ensure working data is returns by intercepting and checking contents
        cy.intercept("http://localhost:7004/api/alerts").as("createAlert");
        cy.get("#next-bottom-bar-btn").click();
        cy.wait("@createAlert");

        // User is take to the view page
        cy.url().should("match", /.*\/alerts\/\d.*\/view/);
    });

    it("user can switch among the editors and the same alert configuration state is maintained", () => {
        cy.get("a").contains("Create").click();

        cy.getByDataTestId(
            ALERT_CREATION_NAVIGATE_DROPDOWN_TEST_IDS.DROPDOWN_CONTAINER
        ).click();
        cy.get("li").contains("JSON Editor").click();

        const customizedAlertConfig = {
            ...DEFAULT_ALERT_CONFIG,
            templateProperties: {
                ...DEFAULT_ALERT_CONFIG.templateProperties,
                lookback: "P14D",
                seasonalityPeriod: "P1D",
            },
        };

        // eslint-disable-next-line jest/valid-expect-in-promise
        cy.get(".CodeMirror")
            .first()
            .then((editor) => {
                editor[0].CodeMirror.setValue(
                    JSON.stringify(customizedAlertConfig)
                );
            });

        // Switching to Simple mode retains same values
        cy.getByDataTestId(
            ALERT_CREATION_NAVIGATE_DROPDOWN_TEST_IDS.DROPDOWN_CONTAINER
        ).click();
        cy.get("li").contains("Simple").click();
        cy.getByDataTestId("datasource-select")
            .find("input[value='USStoreSalesOrderData']")
            .should("exist");
        cy.getByDataTestId("metric-select")
            .find("input[value='UnitCost']")
            .should("exist");
        cy.getByDataTestId("seasonalityPeriod-container")
            .find("input[value='P1D']")
            .should("exist");
        cy.getByDataTestId("lookback-container")
            .find("input[value='P14D']")
            .should("exist");

        // Switching to Advanced mode retains same values
        cy.getByDataTestId(
            ALERT_CREATION_NAVIGATE_DROPDOWN_TEST_IDS.DROPDOWN_CONTAINER
        ).click();
        cy.get("li").contains("Advanced").click();
        const inputKeys = [
            "dataSource",
            "dataset",
            "aggregationColumn",
            "aggregationFunction",
            "sensitivity",
        ];
        inputKeys.forEach((inputKey) => {
            cy.getByDataTestId(`input-${inputKey}`).should(
                "have.value",
                DEFAULT_ALERT_CONFIG.templateProperties[inputKey]
            );
        });

        // Can still create
        cy.get("#next-bottom-bar-btn").click();
        // User is taken to the view page
        cy.url().should("match", /.*\/alerts\/\d.*\/view/);
    });
});
