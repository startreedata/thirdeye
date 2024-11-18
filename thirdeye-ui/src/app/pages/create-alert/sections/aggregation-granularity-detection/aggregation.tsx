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
import { useTranslation } from "react-i18next";
import {
    Box,
    Button,
    Grid,
    TextareaAutosize,
    Typography,
} from "@material-ui/core";
import { KeyboardArrowDown, KeyboardArrowUp } from "@material-ui/icons";

// state
import { useCreateAlertStore } from "../../hooks/state";

// app components
import { RadioSection } from "../../../../components/form-basics/radio-section-v2/radio-section.component";

// utils
import { STAR_COLUMN } from "../../../../utils/datasources/datasources.util";

// types
import { RadioSectionOptions } from "../../../../components/form-basics/radio-section-v2/radio-section.interfaces";
import { MetricAggFunction } from "../../../../rest/dto/metric.interfaces";

// styles
import { aggregationStyles } from "./styles";

export const SelectAggregation = (): JSX.Element => {
    const { t } = useTranslation();
    const {
        selectedMetric,
        aggregationFunction,
        setAggregationFunction,
        queryFilters,
        setQueryFilters,
        setViewColumnsListDrawer,
        editedDatasourceFieldValue,
        setEditedDatasourceFieldValue,
    } = useCreateAlertStore();

    const [showSQLWhere, setShowSQLWhere] = useState(false);
    const componentStyles = aggregationStyles();

    const getAggregationOptions = (
        values: Array<string>
    ): RadioSectionOptions[] => {
        const options: RadioSectionOptions[] = [];
        values.map((item) =>
            options.push({
                value: item,
                label: item,
                onClick: () => setAggregationFunction(item),
                tooltipText: item,
            })
        );

        return options;
    };

    const renderAggregationFunctions = (): JSX.Element => {
        return (
            <Grid item xs={12}>
                <RadioSection
                    label={t("label.aggregation-function")}
                    options={
                        selectedMetric === STAR_COLUMN
                            ? getAggregationOptions([MetricAggFunction.COUNT])
                            : getAggregationOptions([
                                  MetricAggFunction.SUM,
                                  MetricAggFunction.AVG,
                                  MetricAggFunction.COUNT,
                                  MetricAggFunction.MIN,
                                  MetricAggFunction.MAX,
                              ])
                    }
                    subText={t(
                        "message.select-aggregation-function-to-combine-multiple-data-value-into-a-single-result"
                    )}
                    value={aggregationFunction || undefined}
                />
                <Button
                    className={componentStyles.sqlButton}
                    endIcon={
                        showSQLWhere ? (
                            <KeyboardArrowUp />
                        ) : (
                            <KeyboardArrowDown />
                        )
                    }
                    size="small"
                    variant="text"
                    onClick={() => setShowSQLWhere(!showSQLWhere)}
                >
                    {t("label.sql-where-filter")}
                </Button>
                {showSQLWhere && (
                    <Grid container>
                        <Grid
                            item
                            className={componentStyles.textAreaContainer}
                            xs={8}
                        >
                            <Typography
                                className={componentStyles.inputHeader}
                                variant="caption"
                            >
                                {t("label.sql-where-function")}{" "}
                                <Typography variant="caption">
                                    ({t("label.optional")})
                                </Typography>
                            </Typography>
                            <TextareaAutosize
                                aria-label="minimum height"
                                className={componentStyles.textArea}
                                minRows={3}
                                placeholder={t("label.placeholder-sql-where")}
                                value={queryFilters}
                                onChange={(e) =>
                                    setQueryFilters(e.target.value)
                                }
                            />
                            <Box
                                className={componentStyles.footer}
                                justifyContent="end"
                            >
                                <Button
                                    size="small"
                                    variant="contained"
                                    onClick={() =>
                                        setViewColumnsListDrawer(true)
                                    }
                                >
                                    {t("label.view-columns-list")}
                                </Button>
                            </Box>
                        </Grid>
                    </Grid>
                )}
            </Grid>
        );
    };

    const renderCustomMetricAggregation = (): JSX.Element => {
        return (
            <Grid item xs={12}>
                <Grid container>
                    <Grid
                        item
                        className={componentStyles.textAreaContainer}
                        xs={8}
                    >
                        <Typography
                            className={componentStyles.inputHeader}
                            variant="caption"
                        >
                            {t("label.custom-metric")}
                        </Typography>
                        <TextareaAutosize
                            aria-label="minimum height"
                            className={componentStyles.textArea}
                            minRows={3}
                            value={editedDatasourceFieldValue}
                            onChange={(e) =>
                                setEditedDatasourceFieldValue(e.target.value)
                            }
                        />
                        <Box
                            className={componentStyles.footer}
                            justifyContent="flex-end"
                        >
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
        );
    };

    return (
        <>
            {selectedMetric !== t("label.custom-metric-aggregation")
                ? renderAggregationFunctions()
                : renderCustomMetricAggregation()}
        </>
    );
};
