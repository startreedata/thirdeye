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
import React from "react";
import { Box, Divider, Grid, TextField, Typography } from "@material-ui/core";
import { useTranslation } from "react-i18next";
import { Autocomplete } from "@material-ui/lab";
import { toLower } from "lodash";

// app components
import { InputSectionV2 } from "../../../../components/form-basics/input-section-v2/input-section-v2.component";

// types
import { DatasetInfo } from "../../../../utils/datasources/datasources.util";

// utils
import { useGetDatasourcesTree } from "../../../../utils/datasources/use-get-datasources-tree.util";

// state
import { useCreateAlertStore } from "../../hooks/state";

export const SelectDatasetAndMetric = (): JSX.Element => {
    const { t } = useTranslation();
    const { datasetsInfo } = useGetDatasourcesTree();
    const {
        selectedDataset,
        selectedMetric,
        setSelectedDataset,
        setSelectedMetric,
    } = useCreateAlertStore();

    return (
        <Grid item xs={12}>
            <Grid container>
                <Grid item xs={4}>
                    <InputSectionV2
                        description={t(
                            "message.select-dataset-to-monitor-and-detect-anomalies"
                        )}
                        inputComponent={
                            <Autocomplete<DatasetInfo>
                                fullWidth
                                data-testId="datasource-select"
                                getOptionLabel={(option) =>
                                    option.dataset.name as string
                                }
                                noOptionsText={t(
                                    "message.no-options-available-entity",
                                    { entity: t("label.dataset") }
                                )}
                                options={datasetsInfo || []}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        InputProps={{ ...params.InputProps }}
                                        placeholder={t(
                                            "message.select-dataset"
                                        )}
                                        variant="outlined"
                                    />
                                )}
                                renderOption={(
                                    option: DatasetInfo
                                ): JSX.Element => {
                                    return (
                                        <Box
                                            data-testId={`${toLower(
                                                option.dataset.name
                                            )}-datasource-option`}
                                        >
                                            <Typography variant="h6">
                                                {option.dataset.name}
                                            </Typography>
                                            <Typography variant="caption">
                                                {t("message.num-metrics", {
                                                    num: option.metrics.length,
                                                })}
                                            </Typography>
                                        </Box>
                                    );
                                }}
                                value={selectedDataset}
                                onChange={(_, selectedTableInfo) => {
                                    if (!selectedTableInfo) {
                                        return;
                                    }
                                    setSelectedDataset(selectedTableInfo);
                                }}
                            />
                        }
                        label={t("label.dataset")}
                    />
                </Grid>
                <Grid item xs={4}>
                    <InputSectionV2
                        description={t(
                            "message.select-metric-to-identify-unusual-changes-when-it-occurs"
                        )}
                        inputComponent={
                            <Autocomplete<string>
                                fullWidth
                                data-testId="metric-select"
                                disabled={!selectedDataset} // !selectedTable}
                                groupBy={(option) =>
                                    option ===
                                    t("label.custom-metric-aggregation")
                                        ? "Y"
                                        : "N"
                                }
                                noOptionsText={t(
                                    "message.no-options-available-entity",
                                    { entity: t("label.metric") }
                                )}
                                options={
                                    selectedDataset
                                        ? (function () {
                                              const a =
                                                  selectedDataset.metrics.map(
                                                      (m) => m.name
                                                  );
                                              a.unshift(
                                                  t(
                                                      "label.custom-metric-aggregation"
                                                  )
                                              );

                                              return a;
                                          })()
                                        : []
                                }
                                renderGroup={(params) => (
                                    <li key={params.key}>
                                        {params.children}
                                        <Divider />
                                    </li>
                                )}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        InputProps={{ ...params.InputProps }}
                                        placeholder={
                                            !selectedDataset
                                                ? t(
                                                      "message.select-dataset-first"
                                                  )
                                                : t("message.select-metric")
                                        }
                                        variant="outlined"
                                    />
                                )}
                                value={selectedMetric}
                                onChange={(_, metric) => {
                                    metric && setSelectedMetric(metric);
                                }}
                            />
                        }
                        label={t("label.metric")}
                    />
                </Grid>
            </Grid>
        </Grid>
    );
};
