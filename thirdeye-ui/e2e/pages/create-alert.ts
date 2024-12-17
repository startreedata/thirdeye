/*
 * Copyright 2024 StarTree Inc
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
import { expect, Page } from "@playwright/test";
import { sortBy } from "lodash";
import { BasePage } from "./base";

const generateOptions = (): any[] => {
    return [
        {
            title: "Threshold",
            description: "Threshold algorithm description",
            alertTemplate: "startree-threshold",
            alertTemplateForMultidimension: "startree-threshold-dx",
            alertTemplateForMultidimensionQuery: "startree-threshold-query-dx",
            alertTemplateForPercentile: "startree-threshold-percentile",
            exampleImage: "ThresholdScreenshot",
        },
        {
            title: "Mean Variance Rule",
            description: "Mean variance rule algorithm description",
            alertTemplate: "startree-mean-variance",
            alertTemplateForMultidimension: "startree-mean-variance-dx",
            alertTemplateForMultidimensionQuery:
                "startree-mean-variance-query-dx",
            alertTemplateForPercentile: "startree-mean-variance-percentile",
            exampleImage: "MeanVarianceScreenshot",
        },
        {
            title: "Percentage Rule",
            description: "Percentage rule algorithm description",
            alertTemplate: "startree-percentage-rule",
            alertTemplateForMultidimension: "startree-percentage-rule-dx",
            alertTemplateForMultidimensionQuery:
                "startree-percentage-rule-query-dx",
            alertTemplateForPercentile: "startree-percentage-percentile",
            exampleImage: "PercentageRuleScreenshot",
        },
        {
            title: "Absolute Change Rule",
            description: "Absolute change rule algorithm description",
            alertTemplate: "startree-absolute-rule",
            alertTemplateForMultidimension: "startree-absolute-rule-dx",
            alertTemplateForMultidimensionQuery:
                "startree-absolute-rule-query-dx",
            alertTemplateForPercentile: "startree-absolute-percentile",
            exampleImage: "AbsoluteScreenshot",
        },
        {
            title: "Startree-ETS",
            description: "Startree ETS algorithm description",
            alertTemplate: "startree-ets",
            alertTemplateForMultidimension: "startree-ets-dx",
            alertTemplateForMultidimensionQuery: "startree-ets-query-dx",
            alertTemplateForPercentile: "startree-ets-percentile",
            exampleImage: "ETSScreenshot",
        },
        {
            title: "Matrix Profile",
            description:
                "The matrix profile method is a direct anomaly detection method",
            alertTemplate: "startree-matrix-profile",
            alertTemplateForMultidimension: "startree-matrix-profile-dx",
            alertTemplateForMultidimensionQuery:
                "startree-matrix-profile-query-dx",
            alertTemplateForPercentile: "startree-matrix-profile-percentile",
            exampleImage: "MatrixProfileScreenshot",
        },
    ];
};

export class CreateAlertPage extends BasePage {
    readonly page: Page;
    datasetsResponseData: any;
    metricsResponseData: any;
    dataSourcesResponseData: any;
    evaluateResponseData: any;
    recommendResponseData: any;

    constructor(page: Page) {
        super(page);
        this.page = page;
    }

    async resolveApis() {
        const [datasetsApiResponse, metricsApiResponse, dataSourcesResponse] =
            await Promise.all([
                this.page.waitForResponse(
                    (response) =>
                        response.url().includes("/api/datasets") &&
                        response.status() === 200
                ),
                this.page.waitForResponse(
                    (response) =>
                        response.url().includes("/api/metrics") &&
                        response.status() === 200
                ),
                this.page.waitForResponse(
                    (response) =>
                        response.url().includes("/api/data-sources") &&
                        response.status() === 200
                ),
            ]);
        const datasetInfo = await datasetsApiResponse.json();
        this.datasetsResponseData = sortBy(datasetInfo, [
            (d) => d.name.toLowerCase(),
        ]);
        this.metricsResponseData = await metricsApiResponse.json();
        this.dataSourcesResponseData = await dataSourcesResponse.json();
    }

    async resolveRecommendApis() {
        const [recommendApiResponse] = await Promise.all([
            this.page.waitForResponse(
                (response) =>
                    response.url().includes("/api/alerts/recommend") &&
                    response.status() === 200
            ),
        ]);

        this.recommendResponseData = await recommendApiResponse.json();
    }

    async resolveEvaluateApis() {
        const [evaluateApiResponse] = await Promise.all([
            this.page.waitForResponse(
                (response) =>
                    response.url().includes("/api/alerts/evaluate") &&
                    response.status() === 200
            ),
        ]);

        this.evaluateResponseData = await evaluateApiResponse.json();
    }

    async resolveMetricsCohortsApis() {
        const [cohortsApiResponse] = await Promise.all([
            this.page.waitForResponse(
                (response) =>
                    response.url().includes("/api/rca/metrics/cohorts") &&
                    response.status() === 200
            ),
        ]);
        const data = await cohortsApiResponse.json();
        return data;
    }

    async selectDatasetAndMetric() {
        await this.page.getByText("Dataset Select a dataset to").click();
        await this.page
            .getByRole("option", { name: this.datasetsResponseData[0].name })
            .click();
        await this.page
            .getByTestId("metric-select")
            .locator("div")
            .nth(1)
            .click();
        const metrics = this.metricsResponseData?.find(
            (m) => m?.dataset?.name === this.datasetsResponseData[0].name
        );
        await this.page.getByRole("option", { name: metrics.name }).click();
    }

    async selectStaticFields(isMultiDimensional = false, isSQLQuery = false) {
        await this.page.locator("div").filter({ hasText: /^SUM$/ }).click();
        await this.page.getByLabel("SUM").check();
        await this.page.getByPlaceholder("Select granularity").click();
        await this.page.getByRole("option", { name: "Daily" }).click();
        if (!isMultiDimensional) {
            await this.page
                .locator("div")
                .filter({ hasText: /^Single metric$/ })
                .click();
            return;
        }
        await this.page.getByLabel("Multiple dimensions").check();
        if (isSQLQuery) {
            await this.page.getByLabel("SQL Query").check();
            return;
        }
        await this.page.getByLabel("Dimension recommender").check();
    }

    async selectDetectionAlgorithm(isMultiDimensional = false) {
        await this.page.getByPlaceholder("Select an algorithm").click();
        const availableOptions = generateOptions();
        const algorithmOption = availableOptions.find(
            (option) =>
                option.alertTemplate ===
                    this.recommendResponseData?.recommendations[0]?.alert
                        .template?.name ||
                (isMultiDimensional &&
                    option.alertTemplateForMultidimension ===
                        this.recommendResponseData?.recommendations[0]?.alert
                            .template?.name)
        );
        await this.page
            .getByRole("heading", {
                name: algorithmOption
                    ? `${algorithmOption.title} option 1`
                    : availableOptions[0].title,
            })
            .click();
    }

    async goToCreateAlertPage() {
        await this.page.goto("http://localhost:7004/#access_token=''");
        await this.page.waitForSelector("h4:has-text('StarTree ThirdEye')", {
            timeout: 10000,
            state: "visible",
        });
        await this.page.goto(
            "http://localhost:7004/alerts/create/new/new-user/easy-alert/"
        );
    }

    async checkHeader() {
        await expect(this.page.locator("h5")).toHaveText("Alert wizard");
    }

    async clickLoadChartButton() {
        await this.page.getByRole("button", { name: "Load chart" }).click();
    }

    async createAlert() {
        const switchElement = this.page.locator(".MuiSwitch-input");
        await switchElement.click();
        await this.page.getByRole("button", { name: "Create alert" }).click();
        await this.page
            .getByTestId("alert-name-input")
            .getByRole("textbox")
            .click();
        await this.page
            .getByTestId("alert-name-input")
            .getByRole("textbox")
            .fill(`Impressions_SUM_star-tree-ets_dx-${Date.now()}`);
        await this.page.getByRole("button", { name: "Create alert" }).click();
    }

    async addDimensions() {
        await this.page.getByRole("button", { name: "Add dimensions" }).click();
        await this.page.getByPlaceholder("Select dimensions").click();
        await this.page
            .getByRole("option", {
                name: this.datasetsResponseData[0]?.dimensions[0],
            })
            .click();
        await this.page.waitForTimeout(1000);
        const button = this.page.locator("button", {
            hasText: "Generate dimensions to monitor",
        });
        await button.click();
        const data = await this.resolveMetricsCohortsApis();
        await this.page
            .getByRole("row", {
                name: `${this.datasetsResponseData[0]?.dimensions[0]}='${
                    data?.results[0].dimensionFilters[
                        this.datasetsResponseData[0]?.dimensions[0]
                    ]
                }'`,
            })
            .getByRole("checkbox")
            .check();
        await this.page
            .getByRole("button", { name: "Add selected dimensions" })
            .click();
    }

    async addSQLQuery() {
        const textarea = await this.page.locator("textarea:first-of-type");
        const placeholderText = await textarea.getAttribute("placeholder");
        if (placeholderText) {
            textarea.fill(placeholderText);
        }
        await this.page
            .getByRole("button", { name: "Run enumerations" })
            .click();
    }
}
