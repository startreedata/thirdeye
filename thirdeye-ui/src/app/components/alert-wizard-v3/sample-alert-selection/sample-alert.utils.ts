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

const FOOD_DELIVERY_DATASET_NAME = "FoodDelOrderData";
const E_COMMERCE_DATASET_NAME = "USStoreSalesOrderData";
const STARTREE_ETS = "startree-ets";
const STARTREE_ETS_DX = "startree-ets-dx";

export const generateOptions = (
    t: (id: string) => string,
    datasets: Dataset[],
    alertTemplates: AlertTemplate[]
): SampleAlertOption[] => {
    const foodDeliveryDataset = datasets.find(
        (d) => d.name === FOOD_DELIVERY_DATASET_NAME
    );
    const ecommerceDataset = datasets.find(
        (d) => d.name === E_COMMERCE_DATASET_NAME
    );

    const availableOptions = [];

    if (
        foodDeliveryDataset &&
        alertTemplates.find((a) => a.name === STARTREE_ETS)
    ) {
        availableOptions.push({
            title: t("label.food-delivery-sample"),
            description: t("message.food-delivery-sample-description"),
            alertConfiguration: {
                name: "Startree-ETS-food-delivery-sample-alert",
                description: t("message.food-delivery-sample-description"),
                template: {
                    name: STARTREE_ETS,
                },
                templateProperties: {
                    dataSource: foodDeliveryDataset.dataSource.name,
                    dataset: FOOD_DELIVERY_DATASET_NAME,
                    timeColumn: foodDeliveryDataset.timeColumn.name,
                    timeColumnFormat: foodDeliveryDataset.timeColumn.format,
                    timezone: foodDeliveryDataset.timeColumn.timezone,
                    aggregationFunction: "SUM",
                    seasonalityPeriod: "P7D",
                    lookback: "P90D",
                    monitoringGranularity: "P1D",
                    sensitivity: "4",
                    aggregationColumn: "order_price",
                },
                cron: "0 0 0 1/1 * ? *",
            },
        });
    }

    if (
        ecommerceDataset &&
        alertTemplates.find((a) => a.name === STARTREE_ETS_DX)
    ) {
        availableOptions.push({
            title: t("label.e-commerce-sample"),
            description: t("message.e-commerce-sample-description"),
            alertConfiguration: {
                name: "Startree-ETS-multidimension-ecommerce-sample-alert",
                description: t("message.e-commerce-sample-description"),
                template: {
                    name: STARTREE_ETS_DX,
                },
                templateProperties: {
                    dataSource: ecommerceDataset.dataSource.name,
                    dataset: E_COMMERCE_DATASET_NAME,
                    timeColumn: ecommerceDataset.timeColumn.name,
                    timeColumnFormat: ecommerceDataset.timeColumn.format,
                    timezone: ecommerceDataset.timeColumn.timezone,
                    aggregationFunction: "SUM",
                    seasonalityPeriod: "P7D",
                    lookback: "P120D",
                    monitoringGranularity: "P1D",
                    sensitivity: "3",
                    aggregationColumn: "UnitCost",
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

    return availableOptions;
};
