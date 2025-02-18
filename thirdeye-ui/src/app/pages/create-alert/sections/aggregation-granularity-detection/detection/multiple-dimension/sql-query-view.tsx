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
import React, { useEffect } from "react";
import {
    Box,
    Button,
    CircularProgress,
    Grid,
    TextareaAutosize,
} from "@material-ui/core";
import { useTranslation } from "react-i18next";

// styles
import { multipleDimensionStyle } from "./styles";

// state
import { useCreateAlertStore } from "../../../../hooks/state";

// types
import { EditableAlert } from "../../../../../../rest/dto/alert.interfaces";

// apis
import { getAlertRecommendation } from "../../../../../../rest/alerts/alerts.rest";
import { useGetEvaluation } from "../../../../../../rest/alerts/alerts.actions";

// utils
import { getWorkingAlert } from "../../../../utils";
import { notifyIfErrors } from "../../../../../../utils/notifications/notifications.util";

// app components
import {
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../../../../../platform/components";
import { ActionStatus } from "../../../../../../rest/actions.interfaces";

const ALERT_TEMPLATE_FOR_EVALUATE_QUERY_DX = "startree-threshold-query-dx";

export const SqlQueryView = (): JSX.Element => {
    const { t } = useTranslation();
    const {
        selectedDataset,
        selectedMetric,
        editedDatasourceFieldValue,
        aggregationFunction,
        granularity,
        queryFilters,
        setWorkingAlert,
        enumeratorQuery,
        setEnumeratorQuery,
        setAlertRecommendations,
        alertInsight,
        setWorkingAlertEvaluation,
        setViewColumnsListDrawer,
        apiState,
        setApiState,
    } = useCreateAlertStore();
    const componentStyles = multipleDimensionStyle();
    const { notify } = useNotificationProviderV1();
    const { getEvaluation, evaluation, status, errorMessages } =
        useGetEvaluation();
    useEffect(() => {
        setApiState({
            ...apiState,
            evaluationState: {
                ...apiState.evaluationState,
                status,
            },
        });
    }, [status]);

    useEffect(() => {
        notifyIfErrors(
            status,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.alert-evaluation"),
            })
        );
    }, [status]);

    useEffect(() => {
        evaluation && setWorkingAlertEvaluation(evaluation);
    }, [evaluation]);

    const handleRunEnumerations = (): void => {
        let isCustomMetrics = false;
        if (selectedMetric === t("label.custom-metric-aggregation")) {
            isCustomMetrics = true;
        }
        const workingAlertUpdated = getWorkingAlert({
            templateName: ALERT_TEMPLATE_FOR_EVALUATE_QUERY_DX,
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
                enumeratorQuery: enumeratorQuery,
            },
            isMultiDimensionAlert: true,
        });
        setWorkingAlert(workingAlertUpdated);
        setApiState({
            ...apiState,
            alertRecommedationState: {
                ...apiState.alertRecommedationState,
                status: ActionStatus.Working,
            },
        });
        getAlertRecommendation(workingAlertUpdated as EditableAlert)
            .then((recommendations) => {
                setAlertRecommendations(recommendations);
                setApiState({
                    ...apiState,
                    alertRecommedationState: {
                        ...apiState.alertRecommedationState,
                        status: ActionStatus.Done,
                    },
                });
            })
            .catch(() => {
                notify(
                    NotificationTypeV1.Error,
                    t("errors.could-not-compute-detection-recommendations")
                );
                setApiState({
                    ...apiState,
                    alertRecommedationState: {
                        ...apiState.alertRecommedationState,
                        status: ActionStatus.Error,
                    },
                });
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
        <Grid item xs={12}>
            <Grid item xs={12}>
                <Grid container>
                    <Grid
                        item
                        className={componentStyles.textAreaContainer}
                        xs={12}
                    >
                        <TextareaAutosize
                            aria-label="minimum height"
                            className={componentStyles.textArea}
                            minRows={3}
                            placeholder={t(
                                "label.select-distinct-dimension-from-dataset",
                                {
                                    dimension:
                                        selectedDataset?.dimensions?.[0] ??
                                        "someColumn",
                                    dataset: selectedDataset?.dataset?.name,
                                }
                            )}
                            value={enumeratorQuery}
                            onChange={(e) => setEnumeratorQuery(e.target.value)}
                        />
                        <Box
                            className={componentStyles.footer}
                            justifyContent="space-between"
                        >
                            <Button
                                disabled={
                                    apiState.evaluationState?.status ===
                                    ActionStatus.Working
                                }
                                size="small"
                                startIcon={
                                    apiState.evaluationState?.status ===
                                    ActionStatus.Working ? (
                                        <CircularProgress
                                            color="inherit"
                                            size={20}
                                        />
                                    ) : null
                                }
                                variant="contained"
                                onClick={() => handleRunEnumerations()}
                            >
                                {t("label.run-enumeration")}
                            </Button>
                            <Button
                                size="small"
                                variant="contained"
                                onClick={() => setViewColumnsListDrawer(true)}
                            >
                                {t("label.view-columns-list")}
                            </Button>
                        </Box>
                    </Grid>
                </Grid>
            </Grid>
        </Grid>
    );
};
