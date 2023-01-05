/*
 * Copyright 2022 StarTree Inc
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

import { AlertTemplate } from "../../../rest/dto/alert-template.interfaces";
import { Dataset } from "../../../rest/dto/dataset.interfaces";
import { SampleAlertOption } from "./sample-alert-selection.interfaces";

const PAGE_VIEWS_DATASET_NAME = "CleanPageViewsData";
const E_COMMERCE_DATASET_NAME = "USStoreSalesOrderData";
const ADS_DATASET_NAME = "AdCampaignData";
const STARTREE_PERCENTAGE_RULE = "startree-percentage-rule";
const STARTREE_ETS_DX = "startree-ets-dx";

export const generateOptions = (
    t: (id: string) => string,
    datasets: Dataset[],
    alertTemplates: AlertTemplate[]
): SampleAlertOption[] => {
    const availableOptions = [];

    const pageViewDataset = datasets.find(
        (d) => d.name === PAGE_VIEWS_DATASET_NAME
    );

    if (
        pageViewDataset &&
        alertTemplates.find((a) => a.name === STARTREE_PERCENTAGE_RULE)
    ) {
        availableOptions.push({
            title: t("message.monitor-website-traffic-for-consumer-products"),
            description: t(
                "message.monitor-number-of-page-views-across-multiple"
            ),
            recipeLink:
                "https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye/how-tos/thirdeye_recipes/consumer_product_user_experience",
            alertConfiguration: {
                name: "Startree-percentage-rule-pageviews-sample-alert",
                description: t(
                    "message.monitor-number-of-page-views-across-multiple"
                ),
                template: {
                    name: STARTREE_PERCENTAGE_RULE,
                },
                templateProperties: {
                    dataSource: pageViewDataset.dataSource.name,
                    dataset: PAGE_VIEWS_DATASET_NAME,
                    aggregationFunction: "sum",
                    lookback: "P28D",
                    monitoringGranularity: "P1D",
                    aggregationColumn: "views",
                    baselineOffset: "PT168H",
                    percentageChange: "0.2",
                    rcaAggregationFunction: "sum",
                    coldStartIgnore: "true",
                },
                cron: "0 0 5 ? * * *",
            },
        });
    }

    const ecommerceDataset = datasets.find(
        (d) => d.name === E_COMMERCE_DATASET_NAME
    );
    if (
        ecommerceDataset &&
        alertTemplates.find((a) => a.name === STARTREE_ETS_DX)
    ) {
        availableOptions.push({
            title: t("message.monitor-store-sales-for-ecommerce-retail-stores"),
            description: t(
                "message.monitor-number-of-orders-across-multiple-dimension"
            ),
            recipeLink:
                "https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye/how-tos/thirdeye_recipes/eCommerce_store_sales",
            alertConfiguration: {
                name: "USStoreSalesOrderData-seasonal-dx-sample-alert",
                description: t(
                    "message.monitor-number-of-orders-across-multiple-dimension"
                ),
                template: {
                    name: STARTREE_ETS_DX,
                },
                templateProperties: {
                    dataSource: ecommerceDataset.dataSource.name,
                    dataset: E_COMMERCE_DATASET_NAME,
                    aggregationFunction: "sum",
                    aggregationColumn: "OrderQuantity",
                    seasonalityPeriod: "P7D",
                    lookback: "P120D",
                    monitoringGranularity: "P1D",
                    sensitivity: "3",
                    rcaAggregationFunction: "sum",
                    queryFilters: "${queryFilters}",
                    enumerationItems: [
                        {
                            name: "overall",
                            params: {
                                queryFilters: "",
                            },
                        },
                        {
                            params: {
                                queryFilters: " AND SalesRegion = 'Midwest'",
                            },
                        },
                        {
                            params: {
                                queryFilters: " AND SalesRegion = 'West'",
                            },
                        },
                        {
                            params: {
                                queryFilters:
                                    " AND SalesRegion = 'West' AND SalesChannel = 'In-Store'",
                            },
                        },
                        {
                            params: {
                                queryFilters: " AND SalesRegion = 'Northeast'",
                            },
                        },
                    ],
                },
                cron: "0 0 0 1/1 * ? *",
            },
        });
    }
    const adsDataset = datasets.find((d) => d.name === ADS_DATASET_NAME);
    if (adsDataset && alertTemplates.find((a) => a.name === STARTREE_ETS_DX)) {
        availableOptions.push({
            title: t("message.monitor-ad-campaign-performance"),
            description: t(
                "message.monitor-number-of-clicks-across-multiple-dimension"
            ),
            alertConfiguration: {
                name: "AdCampaignData-seasonal-dx-sample-alert",
                description: t(
                    "message.monitor-number-of-clicks-across-multiple-dimension"
                ),
                template: {
                    name: STARTREE_ETS_DX,
                },
                templateProperties: {
                    dataSource: adsDataset.dataSource.name,
                    dataset: ADS_DATASET_NAME,
                    aggregationFunction: "sum",
                    seasonalityPeriod: "P7D",
                    lookback: "P20D",
                    monitoringGranularity: "P1D",
                    sensitivity: "1",
                    aggregationColumn: "Clicks",
                    queryFilters: "${queryFilters}",
                    enumerationItems: [
                        {
                            name: "Overall",
                            params: {
                                queryFilters: "",
                            },
                        },
                        {
                            name: "DoubleClick-Canada",
                            params: {
                                queryFilters:
                                    " AND Country='Canada' AND Exchange='DoubleClick'",
                            },
                        },
                        {
                            name: "DoubleClick-US",
                            params: {
                                queryFilters:
                                    " AND Country='USA' AND Exchange='DoubleClick'",
                            },
                        },
                    ],
                },
                cron: "0 0 5 ? * * *",
            },
        });
    }

    return availableOptions;
};
