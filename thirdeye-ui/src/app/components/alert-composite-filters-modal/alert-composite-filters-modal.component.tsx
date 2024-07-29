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
import { Box, Button, Grid } from "@material-ui/core";
import { isEqual } from "lodash";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useOutletContext } from "react-router-dom";
import { AlertCreatedGuidedPageOutletContext } from "../../pages/alerts-create-guided-page/alerts-create-guided-page.interfaces";
import { useNotificationProviderV1 } from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { EnumerationItemConfig } from "../../rest/dto/alert.interfaces";
import { MetricAggFunction } from "../../rest/dto/metric.interfaces";
import { CohortResult } from "../../rest/dto/rca.interfaces";
import { useGetCohort } from "../../rest/rca/rca.actions";
import { GetCohortParams } from "../../rest/rca/rca.interfaces";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import {
    SessionStorageKeys,
    useSessionStorage,
} from "../../utils/storage/use-session-storage";
import { CohortsTable } from "../cohort-detector/cohorts-table/cohorts-table.component";
import { CohortTableRowData } from "../cohort-detector/cohorts-table/cohorts-table.interfaces";
import {
    getCohortTableRowFromData,
    NAME_JOIN_KEY,
} from "../cohort-detector/cohorts-table/cohorts-table.utils";
import { DatasetDetails } from "../cohort-detector/dataset-details/dataset-details.component";
import { Modal } from "../modal/modal.component";
import { AlertCompositeFiltersModalProps } from "./alert-composite-filters-modal.interfaces";

export const AlertCompositeFiltersModal: FunctionComponent<AlertCompositeFiltersModalProps> =
    ({ onUpdateCompositeFiltersChange, onCancel }) => {
        const { t } = useTranslation();
        const { notify } = useNotificationProviderV1();

        const { cohortsResponse, getCohorts, status, errorMessages } =
            useGetCohort();

        const { alert, onAlertPropertyChange, setIsMultiDimensionAlert } =
            useOutletContext<AlertCreatedGuidedPageOutletContext>();

        const [selectedCohorts, setSelectedCohorts] = useState<CohortResult[]>(
            []
        );

        const [queryValue, setQueryValue] = useSessionStorage<string>(
            SessionStorageKeys.QueryFilterOnAlertFlow,
            ""
        );

        useEffect(() => {
            setIsMultiDimensionAlert(true);
        }, []);

        useEffect(() => {
            notifyIfErrors(
                status,
                errorMessages,
                notify,
                t("message.error-while-fetching", {
                    entity: t("label.dimensions-data"),
                })
            );
        }, [status]);

        const handleSearchButtonClick = (
            getCohortsParams: GetCohortParams
        ): void => {
            const params = { ...getCohortsParams };

            if (params.aggregationFunction === MetricAggFunction.COUNT) {
                params.roundOffThreshold = true;
            }

            getCohorts(params);
        };

        const handleCohortsSelectionChange = (
            cohorts: CohortResult[]
        ): void => {
            setSelectedCohorts(cohorts);
        };

        const initiallySelectedCohorts = useMemo<CohortTableRowData[]>(() => {
            if (!(status === ActionStatus.Done && cohortsResponse)) {
                return [];
            }

            // Extract all the queryFilters in alert
            const selectedEnumerationItemsInAlert = (
                (alert.templateProperties.enumerationItems ||
                    []) as EnumerationItemConfig[]
            ).map((item) => item.params.queryFilters);

            // Parse the stringified `queryFilter=` to an object
            // `dimensionFilters` format for precise comparison
            const selectedDimensionFilters = selectedEnumerationItemsInAlert
                .map((query) =>
                    query
                        .split(NAME_JOIN_KEY)
                        .map((dim) => dim.split("="))
                        .filter((v) => v.filter((g) => g.length > 0))
                        .filter((v) => v.length > 0 && v[0].length > 0)
                        .map(([k, v]) => ({ [k]: v.slice(1, -1) }))
                )
                .map((entries) => Object.assign({}, ...entries));

            // Get the table rows from the cohorts list
            const cohortDimensionsTableRow = cohortsResponse?.results
                ? cohortsResponse?.results.map(getCohortTableRowFromData)
                : [];

            // Filter for the cohorts that match any enumeration item in alert
            const cohortsInAlert = cohortDimensionsTableRow.filter(
                (dimension) =>
                    selectedDimensionFilters.some((selected) =>
                        isEqual(selected, dimension.dimensionFilters)
                    )
            );

            return cohortsInAlert;
        }, [alert, status, cohortsResponse]);

        const handleCreateBtnClick = (): void => {
            const enumerationItemConfiguration = selectedCohorts.map(
                (cohort) => {
                    const criteria = Object.keys(cohort.dimensionFilters).map(
                        (k) => {
                            return `${k}='${cohort.dimensionFilters[k]}'`;
                        }
                    );
                    const joined = criteria.join(" AND ");

                    return {
                        params: {
                            queryFilters: ` AND ${joined}`,
                        },
                    };
                }
            );

            let queryFilters = "${queryFilters}";

            // Append the custom query filter if exists
            if (queryValue) {
                queryFilters = `${queryFilters} AND ${queryValue}`;
            }

            onAlertPropertyChange({
                templateProperties: {
                    ...alert.templateProperties,
                    queryFilters,
                    enumerationItems: enumerationItemConfiguration,
                },
            });
            onUpdateCompositeFiltersChange({
                ...alert.templateProperties,
                queryFilters,
                enumerationItems: enumerationItemConfiguration,
            });
            onCancel();
        };

        return (
            <Modal
                initiallyOpen
                maxWidth="md"
                title={t("label.add-dimensions")}
                onCancel={onCancel}
            >
                <Grid item xs={12}>
                    <DatasetDetails
                        initialSelectedAggregationFunc={
                            alert.templateProperties
                                .aggregationFunction as MetricAggFunction
                        }
                        initialSelectedDataset={
                            alert.templateProperties.dataset as string
                        }
                        initialSelectedDatasource={
                            alert.templateProperties.dataSource as string
                        }
                        initialSelectedMetric={
                            alert.templateProperties.aggregationColumn as string
                        }
                        queryValue={queryValue}
                        setQueryValue={setQueryValue}
                        submitButtonLabel={t(
                            "message.generate-dimensions-to-monitor"
                        )}
                        subtitle={t(
                            "message.automatically-detects-dimensions-based-on-your-selection"
                        )}
                        title={t("label.dimensions-recommender")}
                        onSearchButtonClick={handleSearchButtonClick}
                    />
                </Grid>
                <Grid item xs={12}>
                    <CohortsTable
                        cohortsData={cohortsResponse}
                        getCohortsRequestStatus={status}
                        initiallySelectedCohorts={initiallySelectedCohorts}
                        onSelectionChange={handleCohortsSelectionChange}
                    >
                        <Box textAlign="right">
                            <Button
                                color="primary"
                                disabled={selectedCohorts.length === 0}
                                onClick={handleCreateBtnClick}
                            >
                                {t("label.add-selected-dimensions")}
                            </Button>
                        </Box>
                    </CohortsTable>
                </Grid>
            </Modal>
        );
    };
