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
import React, { useState } from "react";
import { Box, Grid, TextField, Typography } from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import { useTranslation } from "react-i18next";

// app components
import { InputSectionV2 } from "../../../../components/form-basics/input-section-v2/input-section-v2.component";
import {
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../../../platform/components";

// styles
import { aggregationStyles } from "./styles";

// utils
import { GranularityValue } from "../../../../components/alert-wizard-v3/select-metric/select-metric.utils";
import { defaultStartingAlert, getWorkingAlert } from "../../utils";

// state
import { useCreateAlertStore } from "../../hooks/state";

// types
import { EditableAlert } from "../../../../rest/dto/alert.interfaces";

// apis
import { getAlertInsight } from "../../../../rest/alerts/alerts.rest";

export const SelectGranularity = (): JSX.Element => {
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const componentStyles = aggregationStyles();
    const {
        granularity,
        setGranularity,
        selectedDataset,
        selectedMetric,
        editedDatasourceFieldValue,
        aggregationFunction,
        setAlertInsight,
        setSelectedTimeRange,
        setWorkingAlert,
        queryFilters,
    } = useCreateAlertStore();

    const GRANULARITY_OPTIONS = [
        {
            label: t("label.daily"),
            value: GranularityValue.DAILY,
        },
        {
            label: t("label.hourly"),
            value: GranularityValue.HOURLY,
        },
        {
            label: t("label.15-minutes"),
            value: GranularityValue.FIFTEEN_MINUTES,
        },
        {
            label: t("label.10-minutes"),
            value: GranularityValue.TEN_MINUTES,
        },
        {
            label: t("label.5-minutes"),
            value: GranularityValue.FIVE_MINUTES,
        },
        {
            label: t("label.1-minute"),
            value: GranularityValue.ONE_MINUTE,
        },
    ];

    const [inputValue, setInputValue] = useState("");

    const handleGranularityChange = async (
        _: unknown,
        item: { label: string; value: GranularityValue } | null
    ): Promise<void> => {
        setGranularity(item?.value);
        setAlertInsight(null);
        if (
            item?.value &&
            (editedDatasourceFieldValue || selectedMetric) &&
            selectedDataset
        ) {
            try {
                let isCustomMetrics = false;
                if (selectedMetric === t("label.custom-metric-aggregation")) {
                    isCustomMetrics = true;
                }
                const workingAlertUpdated = getWorkingAlert({
                    templateName: defaultStartingAlert.template?.name,
                    metric: isCustomMetrics
                        ? editedDatasourceFieldValue
                        : (selectedMetric as string),
                    dataset: selectedDataset?.dataset,
                    aggregationFunction: aggregationFunction || "",
                    granularity: item.value,
                    isMultiDimensionAlert: false,
                    queryFilters,
                    min: 0,
                    max: 1,
                });

                const newAlertInsight = await getAlertInsight({
                    alert: {
                        template: workingAlertUpdated.template,
                        templateProperties:
                            workingAlertUpdated.templateProperties,
                    } as EditableAlert,
                });

                if (newAlertInsight) {
                    setAlertInsight(newAlertInsight);
                    setSelectedTimeRange({
                        startTime: newAlertInsight.datasetStartTime,
                        endTime: newAlertInsight.datasetEndTime,
                    });
                }
                setWorkingAlert(workingAlertUpdated);
            } catch (error) {
                notify(
                    NotificationTypeV1.Error,
                    t("message.error-while-fetching", {
                        entity: t("label.alert-insight"),
                    })
                );
            }
        }
    };

    return (
        <Grid item xs={12}>
            <Box marginBottom="10px">
                <Typography
                    className={componentStyles.inputHeader}
                    variant="h6"
                >
                    {t("label.granularity")}
                </Typography>
                <Typography variant="body2">
                    {t(
                        "label.select-the-time-increment-that-the-data-is-aggregated-to"
                    )}
                </Typography>
            </Box>
            <Grid item xs={4}>
                <InputSectionV2
                    inputComponent={
                        <>
                            <Autocomplete
                                fullWidth
                                getOptionLabel={(option) => option.label}
                                getOptionSelected={(option, value) =>
                                    option.value === value.value
                                }
                                inputValue={inputValue}
                                options={GRANULARITY_OPTIONS}
                                renderInput={(params) => {
                                    return (
                                        <TextField
                                            {...params}
                                            placeholder={t(
                                                "label.select-granularity"
                                            )}
                                            value={
                                                granularity
                                                    ? GRANULARITY_OPTIONS.find(
                                                          (g) =>
                                                              g.value ===
                                                              granularity
                                                      )
                                                    : undefined
                                            }
                                            variant="outlined"
                                        />
                                    );
                                }}
                                renderOption={({ label }) => (
                                    <Box
                                        alignItems="center"
                                        display="flex"
                                        justifyContent="space-between"
                                        width="100%"
                                    >
                                        {label}
                                    </Box>
                                )}
                                value={
                                    granularity
                                        ? GRANULARITY_OPTIONS.find(
                                              (g) => g.value === granularity
                                          )
                                        : undefined
                                }
                                onChange={handleGranularityChange}
                                onInputChange={(_event, newInputValue) => {
                                    setInputValue(newInputValue);
                                }}
                            />
                        </>
                    }
                />
            </Grid>
        </Grid>
    );
};
