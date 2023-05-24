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

export const DEFAULT_ALERT_NAME = "UnitCost_SUM_mean-variance-rule-testing";
export const DEFAULT_ALERT_CONFIG = {
    name: DEFAULT_ALERT_NAME,
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
        sensitivity: "-18",
        timezone: "UTC",
    },
};

Cypress.Commands.add("loadAlertAndAnomalies", (failOnStatusCode = true) => {
    if (process.env.TE_DEV_PROXY_SERVER !== undefined) {
        throw new Error(
            "TE_DEV_PROXY_SERVER is set. Failing in case it is linked to a dev server"
        );
    }

    cy.request({
        method: "POST",
        url: "http://localhost:7004/api/alerts",
        json: [DEFAULT_ALERT_CONFIG],
        failOnStatusCode: failOnStatusCode,
    }).then(({ body }) => {
        if (!body || !body[0]) {
            return;
        }

        // Note that these get cleared out when the schedule runs a job
        cy.fixture("anomalies-for-us-store-sales-order.json").then(
            (anomalies) => {
                const mockAnomalies = anomalies.map((anomaly) => {
                    anomaly.alert = {
                        id: body[0].id,
                    };
                    anomaly.metadata = {
                        dataset: { name: body[0].templateProperties.dataset },
                        metric: {
                            name: body[0].templateProperties.aggregationColumn,
                        },
                    };

                    return anomaly;
                });
                cy.request({
                    method: "POST",
                    url: "http://localhost:7004/api/anomalies",
                    json: mockAnomalies,
                });
            }
        );
    });
});
