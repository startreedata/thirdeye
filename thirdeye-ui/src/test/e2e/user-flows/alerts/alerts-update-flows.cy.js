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
import { PREVIEW_CHART_TEST_IDS } from "../../../../app/components/alert-wizard-v2/alert-template/preview-chart/preview-chart.interfaces";
import { ALERT_CREATION_NAVIGATE_DROPDOWN_TEST_IDS } from "../../../../app/components/alert-wizard-v3/navigate-alert-creation-flows-dropdown/navigate-alert-creation-flows-dropdown.interface";
import { DEFAULT_ALERT_CONFIG } from "../../support/load-alert-and-anomalies.command";
const CHECKBOX_SELECTOR =
    ".BaseTable__body [role='row'] input[type='checkbox']";

describe("alert update flows", () => {
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
        cy.loadAlertAndAnomalies();

        cy.visit("http://localhost:7004/alerts");
    });

    it("user can update alert from the json editor flow", () => {
        cy.getByDataTestId(TEST_IDS.TABLE).find(CHECKBOX_SELECTOR).click();
        cy.getByDataTestId(TEST_IDS.EDIT_BUTTON).click();

        // eslint-disable-next-line jest/valid-expect-in-promise
        cy.get(".CodeMirror")
            .first()
            .then((editor) => {
                const alertConfig = JSON.parse(editor[0].CodeMirror.getValue());
                alertConfig.templateProperties.aggregationFunction = "avg";
                alertConfig.templateProperties.seasonalityPeriod = "P1D";
                editor[0].CodeMirror.setValue(JSON.stringify(alertConfig));
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
            .should("have.length.gt", 1);

        // Click the update button
        cy.get("#next-bottom-bar-btn").click();
        // User is take to the view page
        cy.url().should("match", /.*\/alerts\/\d.*\/view/);
    });

    it("user can update from advanced flow", () => {
        cy.getByDataTestId(TEST_IDS.TABLE).find(CHECKBOX_SELECTOR).click();
        cy.getByDataTestId(TEST_IDS.EDIT_BUTTON).click();
        cy.getByDataTestId(
            ALERT_CREATION_NAVIGATE_DROPDOWN_TEST_IDS.DROPDOWN_CONTAINER
        ).click();
        cy.get("li").contains("Advanced").click();

        cy.getByDataTestId("name-input-container").type("-custom");
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

        // Click the update button
        cy.get("#next-bottom-bar-btn").click();
        // User is take to the view page
        cy.url().should("match", /.*\/alerts\/\d.*\/view/);
    });
});
