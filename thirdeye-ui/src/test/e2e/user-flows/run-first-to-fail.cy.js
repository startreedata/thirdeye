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

describe("run first to fail for github action env", () => {
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
        cy.loadAlertAndAnomalies(false);
    });
});
