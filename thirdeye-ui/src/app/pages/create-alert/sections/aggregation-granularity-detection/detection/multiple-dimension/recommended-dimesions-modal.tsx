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
// external
import React, { useEffect, useState } from "react";
import { Box, Button, CircularProgress, Grid } from "@material-ui/core";
import { useTranslation } from "react-i18next";
import { isEmpty } from "lodash";

// app components
import { DatasetDetails } from "../../../../../../components/cohort-detector/dataset-details/dataset-details-v2.component";
import { CohortsTable } from "../../../../../../components/cohort-detector/cohorts-table-v2/cohorts-table.component";
import { Modal } from "../../../../../../components/modal/modal.component";
import {
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../../../../../platform/components";

// state
import { EnumerationItem, useCreateAlertStore } from "../../../../hooks/state";

// types
import { EditableAlert } from "../../../../../../rest/dto/alert.interfaces";
import { GetCohortParams } from "../../../../../../rest/rca/rca.interfaces";
import { MetricAggFunction } from "../../../../../../rest/dto/metric.interfaces";
import { CohortTableRowData } from "../../../../../../components/cohort-detector/cohorts-table-v2/cohorts-table.interfaces";
import { ActionStatus } from "../../../../../../rest/actions.interfaces";

// apis
import { useGetCohort } from "../../../../../../rest/rca/rca.actions";
import { getAlertRecommendation } from "../../../../../../rest/alerts/alerts.rest";
import { useGetEvaluation } from "../../../../../../rest/alerts/alerts.actions";

// utils
import { notifyIfErrors } from "../../../../../../utils/notifications/notifications.util";
import { getWorkingAlert } from "../../../../utils";

type RecommendedDimesnionsModalProps = {
    onCancel: () => void;
};

const ALERT_TEMPLATE_FOR_EVALUATE_DX = "startree-threshold-dx";

export const RecommendedDimesnionsModal = ({
    onCancel,
}: RecommendedDimesnionsModalProps): JSX.Element => {
    const { t } = useTranslation();
    const {
        selectedMetric,
        selectedDataset,
        editedDatasourceFieldValue,
        aggregationFunction,
        granularity,
        selectedEnumerationItemsCohortsTable,
        setSelectedEnumerationItems,
        setSelectedEnumerationItemsCohortsTable,
        queryFilters,
        setWorkingAlert,
        setAlertRecommendations,
        alertInsight,
        setWorkingAlertEvaluation,
        apiState,
        setApiState,
    } = useCreateAlertStore();
    const [queryValue, setQueryValue] = useState("");
    const { cohortsResponse, getCohorts, status, errorMessages } =
        useGetCohort();
    const {
        getEvaluation,
        evaluation,
        status: evaluationStatus,
        errorMessages: evaluationErrorMessages,
    } = useGetEvaluation();
    const [isLoading, setIsLoading] = useState(false);

    const [localEnumerationItems, setLocalEnumerationItems] = useState<
        EnumerationItem[] | null
    >(null);
    useEffect(() => {
        setApiState({
            ...apiState,
            evaluationState: {
                ...apiState.evaluationState,
                status: evaluationStatus,
            },
        });
    }, [evaluationStatus]);

    const { notify } = useNotificationProviderV1();

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

    useEffect(() => {
        notifyIfErrors(
            evaluationStatus,
            evaluationErrorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.alert-evaluation"),
            })
        );
        if (evaluationStatus === ActionStatus.Error) {
            setIsLoading(false);
        }
        if (evaluationStatus === ActionStatus.Done) {
            localEnumerationItems &&
                setSelectedEnumerationItems(localEnumerationItems);
            setWorkingAlertEvaluation(evaluation);
            onCancel();
        }
    }, [evaluationStatus]);

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
        cohorts: CohortTableRowData[]
    ): void => {
        setSelectedEnumerationItemsCohortsTable(cohorts);
    };

    const handleCreateBtnClick = (): void => {
        setIsLoading(true);
        const enumerationItemConfiguration =
            selectedEnumerationItemsCohortsTable!.map((cohort) => {
                const criteria = Object.keys(cohort.dimensionFilters).map(
                    (k) => {
                        return `${k}='${cohort.dimensionFilters[k]}'`;
                    }
                );
                const joined = criteria.join(" AND ");

                return {
                    params: { queryFilters: ` AND ${joined}` },
                };
            });

        setLocalEnumerationItems(enumerationItemConfiguration);
        let isCustomMetrics = false;
        if (selectedMetric === t("label.custom-metric-aggregation")) {
            isCustomMetrics = true;
        }
        const workingAlertUpdated = getWorkingAlert({
            templateName: ALERT_TEMPLATE_FOR_EVALUATE_DX,
            metric: isCustomMetrics
                ? editedDatasourceFieldValue
                : (selectedMetric as string),
            dataset: selectedDataset!.dataset!,
            aggregationFunction: aggregationFunction || "",
            granularity: granularity!,
            queryFilters,
            min: 0,
            max: 1,
            dxAlertProps: {
                queryFilters: queryFilters,
                enumerationItems: enumerationItemConfiguration,
            },
            isMultiDimensionAlert: true,
        });
        setWorkingAlert(workingAlertUpdated);
        getAlertRecommendation(workingAlertUpdated as EditableAlert)
            .then((recommendations) => {
                setAlertRecommendations(recommendations);
            })
            .catch(() => {
                notify(
                    NotificationTypeV1.Error,
                    t("errors.could-not-compute-detection-recommendations")
                );
            });
        const start = alertInsight?.defaultStartTime;
        const end = alertInsight?.defaultEndTime;
        if (start && end) {
            getEvaluation({
                start,
                end,
                alert: workingAlertUpdated as EditableAlert,
            });
        }
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
                        aggregationFunction as MetricAggFunction
                    }
                    initialSelectedDataset={
                        selectedDataset?.dataset.name as string
                    }
                    initialSelectedDatasource={
                        selectedDataset?.datasource as string
                    }
                    initialSelectedMetric={selectedMetric as string}
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
                    initiallySelectedCohorts={
                        selectedEnumerationItemsCohortsTable
                    }
                    onSelectionChange={handleCohortsSelectionChange}
                >
                    <Box textAlign="right">
                        <Button
                            color="primary"
                            disabled={isEmpty(
                                selectedEnumerationItemsCohortsTable
                            )}
                            startIcon={
                                isLoading ? (
                                    <CircularProgress
                                        color="inherit"
                                        size={20}
                                    />
                                ) : null
                            }
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
